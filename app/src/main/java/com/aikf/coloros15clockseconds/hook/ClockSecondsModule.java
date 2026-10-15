package com.aikf.coloros15clockseconds.hook;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.libxposed.api.XposedInterface.ExceptionMode;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;

import com.aikf.coloros15clockseconds.ClockSecondsConfig;

/**
 * 状态栏时钟显秒模块主入口（新版 libxposed API 102）。
 *
 * <p>两条渲染路径：</p>
 * <ul>
 *   <li><b>原生路径（ColorOS 17+）</b>：系统自带显秒能力，但开启后 5 分钟自动关闭。
 *       模块 hook {@code ClockSecondsController} 去掉时限，让显秒保持开启。</li>
 *   <li><b>自渲染路径（ColorOS 15/16 及原生能力缺失时）</b>：系统没有显秒能力，
 *       模块在 SystemUI 内接管时钟文本格式，把秒加进去，并用 1 秒级定时器驱动刷新。</li>
 * </ul>
 *
 * <p>两条路径共用"点击/长按状态栏时钟切换显秒"的交互，状态存于 {@link ClockSecondsConfig}。</p>
 */
public final class ClockSecondsModule extends XposedModule {

    private static final String TAG = "ColorOSClockSeconds";

    private static final int MODE_ENABLED = ClockSecondsConfig.MODE_ENABLED;

    /* ---- 原生路径（ColorOS 17+）hook id ---- */
    private static final String HOOK_DEADLINE_CHECK_ID = "clock-seconds-deadline-check";
    private static final String HOOK_EXPIRY_ID = "clock-seconds-expiry";
    private static final String HOOK_MODE_CHANGE_DEADLINE_ID = "clock-seconds-mode-change-deadline";

    /* ---- 自渲染路径 hook id ---- */
    private static final String HOOK_CLOCK_SMALL_TIME_ID = "clock-small-time";
    private static final String HOOK_CLOCK_UPDATE_ID = "clock-update";

    /* ---- 设置页（ColorOS 17+ 原生开关）hook id ---- */
    private static final String HOOK_SETTINGS_STATE_ID = "clock-seconds-settings-state";
    private static final String HOOK_SETTINGS_HINT_ID = "clock-seconds-settings-hint";

    private static final String SETTINGS_CONTROLLER_CLASS =
            "com.oplus.settings.feature.notification.controller.ClockSecondsModePreferenceController";
    private static final String SETTINGS_HINT =
            "模块运行中：开启显秒后不会在 5 分钟后自动关闭；可点击状态栏时钟快速切换。";

    /** 时钟视图 -> 是否已挂切换监听（弱引用，避免泄漏）。 */
    private static final Set<Object> LISTENER_ATTACHED =
            Collections.newSetFromMap(new WeakHashMap<>());
    /** 时钟视图 -> 正在运行的 1 秒定时器（弱键，视图回收后条目自动消失）。 */
    private static final Map<Object, Runnable> TICKERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final long TICK_INTERVAL_MS = 1000L;

    /**
     * 进程内缓存的显秒开关状态。
     *
     * <p>关键：1 秒定时器每次 tick 都要判断"是否显秒"，若每次都去读 {@code Settings.Secure}
     * 就是每秒一次跨进程 IPC，纯属浪费电。因此这里缓存状态，只在
     * 「进程内首次遇到时钟」与「用户点击切换」两个时机刷新。</p>
     */
    private static volatile boolean sSecondsEnabled;
    private static volatile boolean sCacheReady;

    /** 缓存的格式化器（按 pattern 复用），避免每秒重建 SimpleDateFormat。 */
    private static volatile SimpleDateFormat sCachedFormat;
    private static volatile String sCachedPattern;

    /** 反射缓存的 updateClock 方法，供定时器驱动刷新。 */
    private static volatile Method sUpdateClockMethod;

    private boolean nativeSupported;

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        String packageName = param.getPackageName();
        if (ClockSecondsConfig.SYSTEM_UI_PACKAGE.equals(packageName)) {
            installSystemUiHooks(param.getDefaultClassLoader());
        } else if (ClockSecondsConfig.SETTINGS_PACKAGE.equals(packageName)) {
            installSettingsHooks(param.getDefaultClassLoader());
        }
    }

    // ==================================================================
    // SystemUI
    // ==================================================================

    private void installSystemUiHooks(ClassLoader classLoader) {
        nativeSupported = ClockSecondsConfig.isNativeSupported(classLoader);
        log(Log.INFO, TAG, "ColorOS native clock-seconds capability: " + nativeSupported);
        if (nativeSupported) {
            installNativeLimitRemovalHooks(classLoader);
        }
        installClockViewHooks(classLoader);
    }

    /** 原生路径：去掉"5 分钟后自动关闭显秒"的限制（等价于原项目核心逻辑）。 */
    private void installNativeLimitRemovalHooks(ClassLoader classLoader) {
        Class<?> controllerClass;
        try {
            controllerClass =
                    Class.forName(ClockSecondsConfig.NATIVE_CONTROLLER_CLASS, false, classLoader);
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Native controller class not found", error);
            return;
        }

        // 三个 hook 各自独立安装：任一失败不影响其余，避免"一处反射异常导致去时限整体失效"。
        try {
            Method isDeadlineActive =
                    controllerClass.getDeclaredMethod("isOplusFiveMinutesActive", long.class);
            // ColorOS 仅在持久化开关开启时才会查询该 helper，这里直接返回 true。
            hook(isDeadlineActive)
                    .setId(HOOK_DEADLINE_CHECK_ID)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> true);
            log(Log.INFO, TAG, "Hooked isOplusFiveMinutesActive");
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Failed to hook isOplusFiveMinutesActive", error);
        }

        try {
            Method scheduleExpiry =
                    controllerClass.getDeclaredMethod("scheduleExpiryLocked", int.class, long.class);
            Field lastAppliedMode = findField(controllerClass, "lastAppliedMode");
            if (lastAppliedMode == null) {
                throw new NoSuchFieldException("lastAppliedMode");
            }
            // 开启态下不排期失效。
            hook(scheduleExpiry)
                    .setId(HOOK_EXPIRY_ID)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        if (lastAppliedMode.getInt(chain.getThisObject()) == MODE_ENABLED) {
                            return null;
                        }
                        return chain.proceed();
                    });
            log(Log.INFO, TAG, "Hooked scheduleExpiryLocked");
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Failed to hook scheduleExpiryLocked", error);
        }

        try {
            Method onUserOrModeChanged = controllerClass.getDeclaredMethod(
                    "onUserOrModeChanged", int.class, int.class, long.class);
            // 开启态下把 deadline 参数改写为 0（立即/永久）。
            hook(onUserOrModeChanged)
                    .setId(HOOK_MODE_CHANGE_DEADLINE_ID)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        int mode = ((Number) chain.getArg(1)).intValue();
                        if (mode != MODE_ENABLED) {
                            return chain.proceed();
                        }
                        return chain.proceed(new Object[]{
                                chain.getArg(0),
                                chain.getArg(1),
                                0L
                        });
                    });
            log(Log.INFO, TAG, "Hooked onUserOrModeChanged");
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Failed to hook onUserOrModeChanged", error);
        }
    }

    /**
     * 时钟视图 hook：定位状态栏时钟，挂切换监听；在自渲染路径下接管时间格式并驱动 1 秒刷新。
     */
    private void installClockViewHooks(ClassLoader classLoader) {
        Class<?> clockClass = null;
        for (String candidate : ClockSecondsConfig.CLOCK_CLASS_CANDIDATES) {
            try {
                clockClass = Class.forName(candidate, false, classLoader);
                log(Log.INFO, TAG, "Located status bar clock class: " + candidate);
                break;
            } catch (Throwable ignored) {
                // 试下一个候选
            }
        }
        if (clockClass == null) {
            log(Log.ERROR, TAG, "No status bar clock class matched candidates");
            return;
        }

        Method smallTime = findMethod(clockClass, "getSmallTime", CharSequence.class);
        Method updateClock = findMethod(clockClass, "updateClock", void.class);

        if (smallTime != null) {
            sUpdateClockMethod = updateClock;
            try {
                hook(smallTime)
                        .setId(HOOK_CLOCK_SMALL_TIME_ID)
                        .setExceptionMode(ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object self = chain.getThisObject();
                            attachToggleListenerIfNeeded(self);
                            if (shouldRenderSeconds(self)) {
                                ensureTicker(self);
                                return formatNowWithSeconds(contextOf(self));
                            }
                            return chain.proceed();
                        });
                log(Log.INFO, TAG, "Hooked getSmallTime for seconds rendering");
            } catch (Throwable error) {
                log(Log.ERROR, TAG, "Failed to hook getSmallTime", error);
            }
        } else if (updateClock != null) {
            sUpdateClockMethod = updateClock;
            try {
                hook(updateClock)
                        .setId(HOOK_CLOCK_UPDATE_ID)
                        .setExceptionMode(ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object result = chain.proceed();
                            Object self = chain.getThisObject();
                            attachToggleListenerIfNeeded(self);
                            if (shouldRenderSeconds(self)) {
                                ensureTicker(self);
                                applyTextDirectly(self);
                            }
                            return result;
                        });
                log(Log.INFO, TAG, "Hooked updateClock for seconds rendering");
            } catch (Throwable error) {
                log(Log.ERROR, TAG, "Failed to hook updateClock", error);
            }
        } else {
            log(Log.ERROR, TAG, "Clock class has neither getSmallTime nor updateClock");
        }
    }

    /** 自渲染路径下是否需要把秒渲染出来。原生路径由系统负责渲染，这里返回 false。 */
    private boolean shouldRenderSeconds(Object clockView) {
        if (nativeSupported) {
            return false;
        }
        ensureCacheReady(clockView);
        return sSecondsEnabled;
    }

    /** 进程内首次需要判断显秒状态时，读一次 {@code Settings.Secure} 并缓存（之后不再每秒读）。 */
    private void ensureCacheReady(Object clockView) {
        if (sCacheReady) {
            return;
        }
        Context context = contextOf(clockView);
        if (context == null) {
            return;
        }
        sSecondsEnabled = ClockSecondsConfig.isEnabled(context.getContentResolver(), false);
        sCacheReady = true;
    }

    /** 给时钟视图挂上"点击/长按切换"监听（每个实例只挂一次）。 */
    private void attachToggleListenerIfNeeded(Object clockView) {
        if (!(clockView instanceof View)) {
            return;
        }
        synchronized (LISTENER_ATTACHED) {
            if (LISTENER_ATTACHED.contains(clockView)) {
                return;
            }
        }
        final View view = (View) clockView;
        // 视图操作必须在主线程；格式化回调可能不在主线程。
        Runnable attachTask = () -> {
            Context context = view.getContext();
            if (context == null) {
                return;
            }
            try {
                int gesture = ClockSecondsConfig.getGesture(context.getContentResolver());
                if (gesture == ClockSecondsConfig.GESTURE_LONG_PRESS) {
                    view.setLongClickable(true);
                    view.setOnLongClickListener(v -> {
                        toggleFromView(v);
                        return true;
                    });
                } else {
                    view.setClickable(true);
                    view.setOnClickListener(this::toggleFromView);
                }
                // 仅在挂载成功后才登记，失败时下次仍会重试。
                synchronized (LISTENER_ATTACHED) {
                    LISTENER_ATTACHED.add(view);
                }
                log(Log.INFO, TAG, "Attached clock toggle listener, gesture=" + gesture);
            } catch (Throwable error) {
                log(Log.ERROR, TAG, "Failed to attach clock toggle listener", error);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            attachTask.run();
        } else {
            MAIN_HANDLER.post(attachTask);
        }
    }

    /** 点击/长按状态栏时钟：翻转显秒开关并立即刷新。 */
    private void toggleFromView(View view) {
        Context context = view.getContext();
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean enabled = !ClockSecondsConfig.isEnabled(resolver, nativeSupported);
        ClockSecondsConfig.setEnabled(resolver, enabled, nativeSupported);
        sSecondsEnabled = enabled;
        sCacheReady = true;
        log(Log.INFO, TAG, "Clock seconds toggled -> " + enabled);
        if (enabled) {
            // 开启：起 1 秒定时器，开始每秒刷新
            ensureTicker(view);
        } else {
            // 关闭：立刻停掉定时器，不再每秒刷新（省电）
            stopTicker(view);
        }
        refreshClock(view);
    }

    /** 立即刷新一次时钟显示。 */
    private void refreshClock(Object clockView) {
        Method updateClock = sUpdateClockMethod;
        if (updateClock != null) {
            try {
                updateClock.setAccessible(true);
                updateClock.invoke(clockView);
                return;
            } catch (Throwable ignored) {
                // 回退到直接改文本
            }
        }
        applyTextDirectly(clockView);
    }

    /** 直接把带秒的时间文本写到时钟 TextView 上（兜底路径）。 */
    private void applyTextDirectly(Object clockView) {
        if (!(clockView instanceof TextView)) {
            return;
        }
        TextView textView = (TextView) clockView;
        Context context = textView.getContext();
        if (context == null) {
            return;
        }
        try {
            textView.setText(formatNowWithSeconds(context));
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Failed to apply clock text", error);
        }
    }

    /**
     * 保证时钟视图上有一个 1 秒级定时器（每个实例只起一个）。
     *
     * <p>关闭显秒时定时器会自行停止（见 run 内判断），不再空转，因此"隐藏秒"时不会持续耗电。</p>
     */
    private void ensureTicker(Object clockView) {
        if (!(clockView instanceof View)) {
            return;
        }
        synchronized (TICKERS) {
            if (TICKERS.containsKey(clockView)) {
                return;
            }
        }
        final WeakReference<Object> ref = new WeakReference<>(clockView);
        final Runnable[] holder = new Runnable[1];
        holder[0] = () -> {
            Object view = ref.get();
            if (!(view instanceof View)
                    || !((View) view).isAttachedToWindow()
                    || !sSecondsEnabled) {
                // 视图已销毁 / 显秒已关闭：停止定时器，避免每秒空转
                if (view != null) {
                    stopTicker(view);
                }
                return;
            }
            refreshClock(view);
            MAIN_HANDLER.postDelayed(holder[0], TICK_INTERVAL_MS);
        };
        synchronized (TICKERS) {
            TICKERS.put(clockView, holder[0]);
        }
        MAIN_HANDLER.postDelayed(holder[0], TICK_INTERVAL_MS);
    }

    /** 停止并移除某个时钟视图上的 1 秒定时器。 */
    private void stopTicker(Object clockView) {
        Runnable tick;
        synchronized (TICKERS) {
            tick = TICKERS.remove(clockView);
        }
        if (tick != null) {
            MAIN_HANDLER.removeCallbacks(tick);
        }
    }

    /** 按系统 12/24 小时制设置，生成带秒的时间文本。格式化器按 pattern 缓存复用，避免每秒 new 一个。 */
    private static CharSequence formatNowWithSeconds(Context context) {
        boolean is24Hour = context != null && DateFormat.is24HourFormat(context);
        String pattern = is24Hour ? "HH:mm:ss" : "h:mm:ss a";
        SimpleDateFormat format = sCachedFormat;
        if (format == null || !pattern.equals(sCachedPattern)) {
            format = new SimpleDateFormat(pattern, Locale.getDefault());
            sCachedFormat = format;
            sCachedPattern = pattern;
        }
        return format.format(System.currentTimeMillis());
    }

    private static Context contextOf(Object view) {
        return view instanceof View ? ((View) view).getContext() : null;
    }

    /** 沿继承链查找无参方法（ColorOS 定制类常把实现放在父类）。 */
    private static Method findMethod(Class<?> clazz, String name, Class<?> returnType) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.getName().equals(name)
                        && method.getParameterCount() == 0
                        && returnType.isAssignableFrom(method.getReturnType())) {
                    return method;
                }
            }
        }
        return null;
    }

    /** 沿继承链查找字段并强制可访问（ColorOS 的字段多为包级/私有）。 */
    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // 继续向上找
            }
        }
        return null;
    }

    // ==================================================================
    // Settings（ColorOS 17+ 原生开关的显示与状态）
    // ==================================================================

    private void installSettingsHooks(ClassLoader classLoader) {
        // 注意：Settings 进程用自身 ClassLoader 探测 SystemUI 的 ClockSecondsController 必然失败，
        // 因此这里不做能力门禁，直接尝试安装；类缺失时由 try/catch 兜底。
        try {
            Class<?> settingsControllerClass =
                    Class.forName(SETTINGS_CONTROLLER_CLASS, false, classLoader);
            Method getCheckedItem = settingsControllerClass.getDeclaredMethod("getCheckedItem");
            Class<?> preferenceScreenClass =
                    Class.forName("androidx.preference.PreferenceScreen", false, classLoader);
            Method displayPreference = settingsControllerClass.getDeclaredMethod(
                    "displayPreference", preferenceScreenClass);
            Field contentResolver = settingsControllerClass.getDeclaredField("mContentResolver");
            Field preferenceField = settingsControllerClass.getDeclaredField("mPreference");
            contentResolver.setAccessible(true);
            preferenceField.setAccessible(true);

            hook(getCheckedItem)
                    .setId(HOOK_SETTINGS_STATE_ID)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        ContentResolver resolver =
                                (ContentResolver) contentResolver.get(chain.getThisObject());
                        int mode = android.provider.Settings.Secure.getInt(
                                resolver, ClockSecondsConfig.NATIVE_SECONDS_KEY, 0);
                        return mode == MODE_ENABLED ? 1 : chain.proceed();
                    });

            hook(displayPreference)
                    .setId(HOOK_SETTINGS_HINT_ID)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        Object menuPreference = preferenceField.get(chain.getThisObject());
                        if (menuPreference != null) {
                            try {
                                menuPreference.getClass()
                                        .getMethod("setSummary", CharSequence.class)
                                        .invoke(menuPreference, SETTINGS_HINT);
                            } catch (Throwable ignored) {
                                // 某些版本 Preference 无 setSummary，忽略
                            }
                        }
                        return result;
                    });

            log(Log.INFO, TAG, "Installed clock-seconds Settings hooks");
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Failed to install clock-seconds Settings hooks", error);
        }
    }
}
