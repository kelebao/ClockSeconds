package io.github.kelebao.colorosclockseconds;

import android.app.Application;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.kelebao.colorosclockseconds.hook.ClockSecondsModule;

/**
 * libxposed（API 102）入口。只负责把 libxposed 的回调翻译成 legacy 形状，
 * 真正的 hook 逻辑仍然在 ClockSecondsModule 里（与 legacy flavor 共用同一份源码）。
 */
public class ModernEntry extends XposedModule {

    private static final String SYSTEM_UI = "com.android.systemui";

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        // 注意：LSPosed 2.2.1 会丢弃 onModuleLoaded 里的 log()，这里的附注仅作兜底。
        XposedBridge.attach(this);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        try {
            if (!SYSTEM_UI.equals(param.getPackageName())) {
                return;
            }
            XC_LoadPackage.LoadPackageParam lp = new XC_LoadPackage.LoadPackageParam();
            lp.packageName = param.getPackageName();
            lp.classLoader = param.getDefaultClassLoader();
            lp.processName = currentProcessName();
            lp.isFirstApplication = param.isFirstPackage();
            new ClockSecondsModule().handleLoadPackage(lp);
        } catch (Throwable t) {
            XposedBridge.log("modern entry failed: " + Log.getStackTraceString(t));
        }
    }

    private static String currentProcessName() {
        try {
            return Application.getProcessName();
        } catch (Throwable ignored) {
            // 取不到就按包名兜底
        }
        return SYSTEM_UI;
    }
}
