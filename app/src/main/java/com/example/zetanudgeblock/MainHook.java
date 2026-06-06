package com.example.zetanudgeblock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "ZetaNudgeBlock";
    private static final String TARGET_PKG = "com.scatterlab.messenger";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PKG)) return;
        XposedBridge.log(TAG + ": Loaded into " + TARGET_PKG);

        try {
            hookOkHttp(lpparam.classLoader);
        } catch (Throwable e) {
            XposedBridge.log(TAG + ": OkHttp hook failed: " + e.getMessage());
        }

        try {
            hookURLConnection(lpparam.classLoader);
        } catch (Throwable e) {
            XposedBridge.log(TAG + ": URLConnection hook failed: " + e.getMessage());
        }
    }

    private void hookOkHttp(ClassLoader cl) throws Throwable {
        Class<?> responseClass = XposedHelpers.findClass("okhttp3.Response", cl);

        XposedHelpers.findAndHookMethod(responseClass, "header", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String name = (String) param.args[0];
                        if ("interstitial-ad".equalsIgnoreCase(name)) {
                            XposedBridge.log(TAG + ": Blocked interstitial-ad header");
                            param.setResult(null);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(responseClass, "header", String.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String name = (String) param.args[0];
                        if ("interstitial-ad".equalsIgnoreCase(name)) {
                            XposedBridge.log(TAG + ": Blocked interstitial-ad header (default)");
                            param.setResult(null);
                        }
                    }
                });

        XposedBridge.log(TAG + ": OkHttp hook installed");
    }

    private void hookURLConnection(ClassLoader cl) throws Throwable {
        XposedHelpers.findAndHookMethod(
                "java.net.URLConnection", cl,
                "getHeaderField", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String name = (String) param.args[0];
                        if ("interstitial-ad".equalsIgnoreCase(name)) {
                            XposedBridge.log(TAG + ": Blocked interstitial-ad header (URLConnection)");
                            param.setResult(null);
                        }
                    }
                });

        XposedBridge.log(TAG + ": URLConnection hook installed");
    }
}
