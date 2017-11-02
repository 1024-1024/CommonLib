package com.commonlib.dblib.qihoo.persistence.cache;

import com.commonlib.dblib.qihoo.persistence.entity.FieldProperty;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * 存储实体对应的属性信息
 * 
 */
public class PropertyCache {
    /**
     * 存储model的属性信息。
     * key为文件绑定对象的simpleName，value为文件绑定对象的属性信息
     */
    private final static HashMap<String, List<FieldProperty>> mPropertyMap;
    
    static {
        mPropertyMap = new HashMap<>();
    }


    /**
     * 从缓存池中，根据类名，查找对应的属性信息
     * 
     * @param className
     * @return
     */
    public static List<FieldProperty> getProperty(String className) {
        return mPropertyMap.get(className);
    }

    /**
     * 将实体的属性信息放入缓存池
     * 
     * @param className
     * @param fieldPropertyList
     */
    public static void putProperty(String className, List<FieldProperty> fieldPropertyList) {
        mPropertyMap.put(className, fieldPropertyList);
    }
}
