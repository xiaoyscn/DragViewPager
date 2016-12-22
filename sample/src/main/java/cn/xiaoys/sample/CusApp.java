package cn.xiaoys.sample;

import android.app.Application;
import android.os.Process;
import android.util.Log;

/**
 * Created by xiaoys on 2016/12/21.
 */

public class CusApp extends Application {

    private static final String TAG = "CusApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    Log.e(TAG, e.toString());
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for (StackTraceElement aStackTrace : stackTrace) {
                        Log.e(TAG, aStackTrace.toString());
                    }
                } finally {
                    Process.killProcess(Process.myPid());
                }
            }
        });
    }
}
