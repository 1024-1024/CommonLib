package com.commonlib.dblib.qihoo.persistence.cache;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库缓存池
 */
public class DatabaseCache {
    /**
     * 数据库缓存池
     */    
    private static Map<String, SQLiteDatabase> dbMap = new HashMap<String, SQLiteDatabase>();
    
    /**
     * 从缓存池中，根据数据库地址，获取数据库对象
     * 
     * @param dbPath
     * @return
     */
    public static synchronized SQLiteDatabase getDb(String dbPath){
       return dbMap.get(dbPath);
    }
    
    /**
     * 将实体的属性信息放入缓存池
     * 
     * @param dbPath
     * @param db
     */
    public static synchronized void putDb(String dbPath, SQLiteDatabase db){
        dbMap.put(dbPath, db);
    }
    
}
