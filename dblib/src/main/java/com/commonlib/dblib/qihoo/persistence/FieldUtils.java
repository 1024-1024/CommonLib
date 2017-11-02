package com.commonlib.dblib.qihoo.persistence;

import com.commonlib.dblib.qihoo.persistence.entity.FieldProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 反射中的 Field 工具
 * 
 * <br>==========================
 * <br> 公司：奇虎360
 * <br> 开发：chenbaolong@360.cn
 * <br> 创建时间：2015-3-2下午3:45:30
 * <br>==========================
 */
public class FieldUtils {
 
    /**
     * 获取传入的类的所有属性信息，知道该类的父类为传入的类为止
     * 
     * @param clazz
     * @return
     */
    public static Map<String, FieldProperty> getPropertyMap(Class<?> clazz) {
        Map <String, FieldProperty> propertyMap = new HashMap<String, FieldProperty>();
        try {
            Class<?>  tempSuperClass = clazz;
            do {
                Field[] fields = tempSuperClass.getDeclaredFields();
                for (Field field : fields) {
                    // 临时数据、常量和自动生成的字段不需要数据库存储
                    if (FieldUtils.isTempKey(field) ||  FieldUtils.isConstantKey(field) || field.isSynthetic()) {
                        continue;
                    }
                    
                    FieldProperty property = new FieldProperty(field);
                    propertyMap.put(property.getColumnName(), property);
                }

                // 迭代获取超类
                tempSuperClass = tempSuperClass.getSuperclass();
                
            } while (!Object.class.getName().equals(tempSuperClass.getName()));

            return propertyMap;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 检测输入的内容是否是临时字段
     * 临时字段将不会持久化保存
     * 
     * @param field
     * @return
     */
    public static boolean isTempKey(Field field) {
        boolean tempKey = false;
        if (field != null) {
            if (field.getAnnotation(AbsTable.TempKey.class) != null) {
                tempKey = true;
            }
        }
        return tempKey;
    }
    
    /**
     * 检测输入的内容是否是 static final 字段
     * 常量字段将不会持久化保存
     * 
     * @param field
     * @return
     */
    private static boolean isConstantKey(Field field) {
        boolean isConstantKey = false;
        if (field != null) {
           boolean isFinal =  Modifier.isFinal(field.getModifiers());
           boolean isStatic =  Modifier.isStatic(field.getModifiers());
           if(isFinal && isStatic) {
               isConstantKey = true;
           }
        }
        return isConstantKey;
    }
    
    /**
     * 检测输入的内容是否是主键
     * 
     * @param field
     * @return
     */
    public static boolean isPrimaryKey(Field field) {
        boolean primaryKey = false;
        if (field != null) {
            if (field.getAnnotation(AbsTable.PrimaryKey.class) != null) {
                primaryKey = true;
            }
        }
        return primaryKey;
    }
}
