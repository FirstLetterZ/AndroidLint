package com.zpf.app2;

import android.util.Log;

public class TestLint {

    public static void check() {
        Log.i("TEST", "log test");
        Log.w("TEST", "log test");
        Log.i("TEST", "log test");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    Log.w("TEST","test create Thread");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
