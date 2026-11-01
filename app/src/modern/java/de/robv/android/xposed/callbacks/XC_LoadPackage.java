package de.robv.android.xposed.callbacks;

/**
 * modern 版兼容层：把 libxposed 的 PackageLoadedParam 包成 legacy 的 XC_LoadPackage.LoadPackageParam，
 * 这样业务代码（ClockSecondsModule）一行都不用改。
 */
public final class XC_LoadPackage {

    private XC_LoadPackage() {
    }

    public static final class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public boolean isFirstApplication;

        public LoadPackageParam() {
        }
    }
}
