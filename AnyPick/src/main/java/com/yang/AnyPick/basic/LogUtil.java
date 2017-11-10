package com.yang.AnyPick.basic;

import android.util.Log;

/**
 * Created by YanGGGGG on 2017/5/2.
 */

public class LogUtil {
    private static boolean LOGV = true;
    private static boolean LOGD = true;
    private static boolean LOGI = true;
    private static boolean LOGW = true;
    private static boolean LOGE = true;

    public static void v(String mess) {
        if (LOGV) { Log.v(getTag()," "+ mess.toString()); }
    }
    public static void d(Object mess) {
        if (LOGD) {Log.d(getTag()," "+ mess.toString()); }
    }
    public static void i(Object mess) {
        if (LOGI) { Log.i(getTag()," "+ mess.toString()); }
    }
    public static void w(Object mess) {
        if (LOGW) { Log.w(getTag()," "+ mess.toString()); }
    }
    public static void e(Object mess) {
        if (LOGE) { Log.e(getTag(), " "+ mess.toString()); }
    }

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass;
    }
}
