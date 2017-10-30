package com.commonlog.Q;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by zhangweilong on 2017/10/30.
 */

public class CLog {

    private static final String TAG = "CLog";
    private static CLog.LogType _logLevel;
    private static boolean enable;

    static {
        _logLevel = CLog.LogType.DEBUG;
        enable = true;
    }

    public CLog() {
    }

    public static void debug(String className, String methodName, String msg) {
        debug(className, methodName, msg, (Exception)null);
    }

    public static void debug(String className, String methodName, String msg, Exception e) {
        if(_logLevel.ordinal() >= CLog.LogType.DEBUG.ordinal()) {
            log(CLog.LogType.DEBUG, className, methodName, msg, e);
        }
    }

    public static void warn(String className, String methodName, String msg) {
        warn(className, methodName, msg, (Exception)null);
    }

    public static void warn(String className, String methodName, String msg, Exception e) {
        if(_logLevel.ordinal() >= CLog.LogType.WARN.ordinal()) {
            log(CLog.LogType.WARN, className, methodName, msg, e);
        }
    }

    public static void error(String className, String methodName, String msg) {
        if(_logLevel.ordinal() >= CLog.LogType.ERROR.ordinal()) {
            log(CLog.LogType.ERROR, className, methodName, msg, (Exception)null);
        }
    }

    public static void error(String className, String methodName, String msg, Exception e) {
        if(_logLevel.ordinal() >= CLog.LogType.ERROR.ordinal()) {
            log(CLog.LogType.ERROR, className, methodName, msg, e);
        }
    }

    public static void error(String className, String methodName, Exception e) {
        if(_logLevel.ordinal() >= CLog.LogType.ERROR.ordinal()) {
            log(CLog.LogType.ERROR, className, methodName, "", e);
        }
    }

    private static void log(CLog.LogType logType, String className, String methodName, String msg, Exception e) {
        if(e != null) {
            if(!TextUtils.isEmpty(msg)) {
                msg = msg + "\r\n" + getStackTrace(e);
            } else {
                msg = getStackTrace(e);
            }
        }

        if(className == null) {
            className = "";
        }

        if(methodName == null) {
            methodName = "";
        }

        String content = String.format("%s--%s--:%s", new Object[]{className, methodName, msg});
        sendSystemLog(logType, content);
    }

    private static void sendSystemLog(CLog.LogType logType, String msg) {
        if(enable) {
            switch(logType.ordinal()) {
                case 1:
                    errorLog(msg);
                    break;
                case 2:
                    warnLog(msg);
                    break;
                case 3:
                    debugLog(msg);
            }
        }

    }

    public static void debugLog(String str) {
        short maxSingleLength = 2000;
        if(str.length() > maxSingleLength) {
            int chunkCount = str.length() / maxSingleLength;

            for(int i = 0; i <= chunkCount; ++i) {
                int max = 2000 * (i + 1);
                if(max >= str.length()) {
                    Log.d(TAG, str.substring(maxSingleLength * i));
                } else {
                    Log.d(TAG, str.substring(maxSingleLength * i, max));
                }
            }
        } else {
            Log.d(TAG, str);
        }

    }

    public static void warnLog(String str) {
        short maxSingleLength = 2000;
        if(str.length() > maxSingleLength) {
            int chunkCount = str.length() / maxSingleLength;

            for(int i = 0; i <= chunkCount; ++i) {
                int max = 2000 * (i + 1);
                if(max >= str.length()) {
                    Log.w(TAG, str.substring(maxSingleLength * i));
                } else {
                    Log.w(TAG, str.substring(maxSingleLength * i, max));
                }
            }
        } else {
            Log.d(TAG, str);
        }

    }

    public static void errorLog(String str) {
        short maxSingleLength = 2000;
        if(str.length() > maxSingleLength) {
            int chunkCount = str.length() / maxSingleLength;

            for(int i = 0; i <= chunkCount; ++i) {
                int max = 2000 * (i + 1);
                if(max >= str.length()) {
                    Log.e(TAG, str.substring(maxSingleLength * i));
                } else {
                    Log.e(TAG, str.substring(maxSingleLength * i, max));
                }
            }
        } else {
            Log.e(TAG, str);
        }

    }

    private static String getStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return baos.toString();
    }

    public static void disable() {
        enable = false;
    }

    //枚举会生成序列号 1，2，3，但是不建议用这中方式，网上有构造方法的形式，可自行google (add_by_zwl)
    public static enum LogType {
        ERROR,
        WARN,
        DEBUG;

        private LogType() {
        }
    }

}
