package com.commonlib.dblib.qihoo.persistence.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * 日志工具类
 * 
 * <br>==========================
 * <br> 公司：奇虎360
 * <br> 开发：chenbaolong@360.cn
 * <br> 创建时间：2014-4-9上午11:59:43
 * <br>==========================
 */
public class MyhcLog {

    /**
     * 该标记在需要调用Android系统Log接口在控制台打印日志信息时使用
     */
    private final static String TAG = "DataBase";

    // 设置日志级别
    private static LogType _logLevel = LogType.DEBUG;
    
    private static boolean enable = true;

    public enum LogType {
        /**
         * 错误信息级别，记录错误日志
         */
        ERROR,
        /**
         * 警告信息级别，记录错误和警告日志
         */
        WARN,
        /**
         * 调试信息级别，记录错误、警告和调试信息，为最详尽的日志级别
         */
        DEBUG
    }


    // --------- 调试日志接口 -------------
    public static void debug(String className, String methodName, String msg) {
        debug(className, methodName, msg, null);
    }

    public static void debug(String className, String methodName, String msg, Exception e) {
        if (_logLevel.ordinal() < LogType.DEBUG.ordinal())
            return;

        log(LogType.DEBUG, className, methodName, msg, e);
    }


    // --------- 警告日志 ---------
    public static void warn(String className, String methodName, String msg) {
        warn(className, methodName, msg, null);
    }

    public static void warn(String className, String methodName, String msg, Exception e) {
        if (_logLevel.ordinal() < LogType.WARN.ordinal())
            return;

        log(LogType.WARN, className, methodName, msg, e);
    }

    // --------- 错误日志 ---------

    public static void error(String className, String methodName, String msg) {
        if (_logLevel.ordinal() < LogType.ERROR.ordinal())
            return;

        log(LogType.ERROR, className, methodName, msg, null);
    }

    public static void error(String className, String methodName, String msg, Exception e) {
        if (_logLevel.ordinal() < LogType.ERROR.ordinal())
            return;

        log(LogType.ERROR, className, methodName, msg, e);
    }

    public static void error(String className, String methodName, Exception e) {
        if (_logLevel.ordinal() < LogType.ERROR.ordinal())
            return;

        log(LogType.ERROR, className, methodName, "", e);
    }

    /**
     * 记录日志消息到日志文件
     * 
     * @param logType
     *            日志种类
     * @param className
     *            产生日志的类名
     * @param methodName
     *            产生日志的方法名
     * @param msg
     *            日志消息
     * @param e
     *            异常对象，无异常为null。
     */
    private static void log(LogType logType, String className, String methodName, String msg, Exception e) {
        if (e != null) {
            if (!TextUtils.isEmpty(msg)) {
                msg = msg + "\r\n" + getStackTrace(e);
            } else {
                msg = getStackTrace(e);
            }
        }

        if (className == null) {
            className = "";
        }
        if (methodName == null) {
            methodName = "";
        }

        String content = String.format("%s`%s`%s", className, methodName, msg);
        sendSystemLog(logType, content);
    }

    /**
     * 往 Android 系统日志中写入日志信息
     * 
     * @param logType
     * @param msg
     */
    private static void sendSystemLog(LogType logType, String msg) {
        if (enable) {
            switch (logType) {
            case DEBUG:
                Log.d(TAG, msg);
                break;
            case WARN:
                Log.w(TAG, msg);
                break;
            case ERROR:
                Log.e(TAG, msg);
                break;
            }
        }

    }

    /**
     * 获取堆栈信息
     * 
     * @param e
     * @return
     */
    private static String getStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return baos.toString();
    }

    public static void disable() {
        enable = false;
    }
}
