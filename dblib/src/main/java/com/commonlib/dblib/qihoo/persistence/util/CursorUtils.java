package com.commonlib.dblib.qihoo.persistence.util;

import android.database.Cursor;
import android.text.TextUtils;

import com.commonlib.dblib.qihoo.persistence.entity.FieldProperty;

import java.util.Map;

/**
 * 通过 model 解析游标信息
 * 
 */
public class CursorUtils {
    private static final String CLASS_NAME = "CursorUtils";

    /**
     * 将cursor信息按照传入的class信息进行解析，返回解析后的结果
     * 
     * @param cursor
     * @param clazz
     * @param propertyMap
     * @return
     */
    public static <T> T getEntity(Cursor cursor, Class<T> clazz, Map<String, FieldProperty> propertyMap) {
        MyhcLog.debug(CLASS_NAME, "getEntity", "begin.........");
        T entity = null;
        try {
            if (cursor != null && propertyMap != null && clazz != null) {

                int columnCount = cursor.getColumnCount();
                if (columnCount > 0) {
                    entity = (T) clazz.newInstance();
                    for (int i = 0; i < columnCount; i++) {
                        String columnName = cursor.getColumnName(i);
                        
                        if (!TextUtils.isEmpty(columnName)) {
//                            MyhcLog.debug(CLASS_NAME, "getEntity", "columnName = " + columnName);
                            FieldProperty property = propertyMap.get(columnName);
                            String value = cursor.getString(i);
                            if (property != null && value != null) {
                                property.setValueForObject(entity, value);
                            } else {
                                MyhcLog.warn(CLASS_NAME, "getEntity", "this column " + columnName + " not exiseted or value is null!");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyhcLog.error(CLASS_NAME, "getEntity", e);
        }

        MyhcLog.debug(CLASS_NAME, "getEntity", "end........");
        return entity;
    }

}
