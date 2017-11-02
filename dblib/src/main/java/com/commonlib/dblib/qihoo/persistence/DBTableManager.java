package com.commonlib.dblib.qihoo.persistence;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.commonlib.dblib.qihoo.persistence.config.Config;
import com.commonlib.dblib.qihoo.persistence.util.MyhcLog;

/**
 * 数据库表管理者
 * 
 */
public class DBTableManager {

    private static final String CLASS_NAME = "DBTableManager";

    /**
     * 存储表class和对象的映射。<br>
     * 
     * key为文件绑定对象的AbsTable的class，value为AbsTable
     */
    private final static Map<Class<?>, AbsTable<?>> mTabCache;

    static {
        mTabCache = new HashMap<>();
    }

    /**
     * 返回SQLite持久化库的版本
     * 
     * @return
     */
    public static String getVersion() {
        return Config.VERSION;
    }

    /**
     * 禁用日志
     */
    public static synchronized void disableLog() {
        MyhcLog.disable();
    } 
    
    /**
     * 注册table
     * 
     * @param context
     * @param clazz
     * @throws IllegalArgumentException
     */
    public static synchronized void register(Context context, Class<?> clazz) throws IllegalArgumentException {
        DBTableManager.register(context, new Class[] {clazz});
    }

    /**
     * 注册table
     * 
     * @param context
     * @param clazzs
     * @throws IllegalArgumentException
     */
    public static synchronized void register(Context context, Class<?>[] clazzs) throws IllegalArgumentException {

        MyhcLog.debug(CLASS_NAME, "register", "begin..............");

        // 数据校验
        if (context == null || clazzs == null) {
            MyhcLog.error(CLASS_NAME, "register", "context or clazzs is null");
            throw new IllegalArgumentException("context or clazzs Illegal !");
        }
        
        // 编译需要管理的文件信息
        for (Class<?> clazz : clazzs) {
            try {
                if (!mTabCache.containsKey(clazz)) {
                    // 构造file对象
                    AbsTable<?> table = (AbsTable<?>) clazz.newInstance();

                    // 初始化文件
                    table.initTable(context);

                    mTabCache.put(clazz, table);
                } else {
                    MyhcLog.debug(CLASS_NAME, "register", "this " + clazz.getName() + " already register.");
                }
            } catch (InstantiationException e) {
                MyhcLog.error(CLASS_NAME, "register", e);
            } catch (IllegalAccessException e) {
                MyhcLog.error(CLASS_NAME, "register", e);
            } catch (Exception e) {
                MyhcLog.error(CLASS_NAME, "register", e);
            }
        }

        MyhcLog.debug(CLASS_NAME, "register", "end..............");
    }

    /**
     * 通过TableManagerClass 获取 TableManager对象。<br>
     * 如果已经注册，则返回注册过的TableManager对象，未注册则返回null。
     * 
     * @param clazz
     * @return
     */
    public static synchronized <T> T getTable(Class<T> clazz) {
        MyhcLog.debug(CLASS_NAME, "getTable", "begin..............");
        T table = null;
        if (mTabCache != null && mTabCache.containsKey(clazz)) {
            table = (T) mTabCache.get(clazz);
        } else {
            MyhcLog.warn(CLASS_NAME, "getTable", clazz.getName() + " not regist");
        }
        MyhcLog.debug(CLASS_NAME, "getTable", "end..............");
        return table;
    }

}
