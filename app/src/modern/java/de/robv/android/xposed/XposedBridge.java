package de.robv.android.xposed;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;

/**
 * modern 版兼容层：把 legacy 的 XposedBridge API 翻译成 libxposed（API 102）的 Hooker 链。
 *
 * <p>业务代码只依赖 {@link #log(String)} / {@link #hookAllMethods(Class, String, XC_MethodHook)}，
 * 所以 ClockSecondsModule 在 legacy / modern 两个 flavor 下源码完全一致。</p>
 */
public final class XposedBridge {

    private static final String TAG = "ClockSeconds";

    private static volatile XposedInterface sApi;

    private XposedBridge() {
    }

    /** 由 ModernEntry 在 onModuleLoaded 里调用，注入框架接口。 */
    public static void attach(XposedInterface api) {
        sApi = api;
    }

    public static void log(String text) {
        XposedInterface api = sApi;
        if (api != null) {
            api.log(Log.INFO, TAG, text == null ? "null" : text);
        }
    }

    public static void log(Throwable t) {
        log(t == null ? "null" : Log.getStackTraceString(t));
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> clazz, String methodName,
                                                           XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        XposedInterface api = sApi;
        if (api == null || clazz == null || callback == null) {
            return unhooks;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.isBridge() || method.isSynthetic()) {
                continue;
            }
            try {
                method.setAccessible(true);
                api.hook(method).intercept(chain -> dispatch(callback, method, chain));
                unhooks.add(new XC_MethodHook.Unhook(method));
            } catch (Throwable t) {
                log("hookAllMethods failed: " + clazz.getName() + "#" + methodName + " -> " + t);
            }
        }
        return unhooks;
    }

    private static Object dispatch(XC_MethodHook callback, Method method,
                                   XposedInterface.Chain chain) throws Throwable {
        XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
        param.method = method;
        param.thisObject = chain.getThisObject();
        param.args = chain.getArgs().toArray();
        callback.beforeHookedMethod(param);
        if (!param.isReturnEarly()) {
            try {
                param.setProceedResult(chain.proceed(param.args));
            } catch (Throwable t) {
                param.setThrowable(t);
            }
        }
        callback.afterHookedMethod(param);
        if (param.getThrowable() != null) {
            throw param.getThrowable();
        }
        return param.getResult();
    }
}
