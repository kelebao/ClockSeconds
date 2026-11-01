package de.robv.android.xposed;

import java.lang.reflect.Member;

/** modern 版兼容层：模拟 legacy XC_MethodHook 的 before/after 语义。 */
public abstract class XC_MethodHook {

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    public static final class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;

        private Object result;
        private Throwable throwable;
        private boolean returnEarly;

        public Object getResult() {
            return result;
        }

        public void setResult(Object r) {
            result = r;
            returnEarly = true;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable t) {
            throwable = t;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }

        public Object getObjectExtra(String key) {
            return null;
        }

        public void setObjectExtra(String key, Object value) {
        }

        void setProceedResult(Object r) {
            result = r;
        }

        boolean isReturnEarly() {
            return returnEarly;
        }
    }

    /** 与 legacy 的返回值同形，业务代码不使用其内容。 */
    public static final class Unhook {
        private final Member hookedMethod;

        public Unhook(Member method) {
            this.hookedMethod = method;
        }

        public Member getHookedMethod() {
            return hookedMethod;
        }

        public void unhook() {
        }
    }
}
