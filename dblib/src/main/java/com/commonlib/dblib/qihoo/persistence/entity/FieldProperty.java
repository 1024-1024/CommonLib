package com.commonlib.dblib.qihoo.persistence.entity;

import com.commonlib.dblib.qihoo.persistence.FieldUtils;
import com.commonlib.dblib.qihoo.persistence.util.MyhcLog;

import java.lang.reflect.Field;

/**
 * Field属性信息
 */
public class FieldProperty {

    private static final String CLASS_NAME = "FieldProperty";

    /**
     * 域
     */
    private Field mField;

    /**
     * 属性名称
     */
    private String mColumnName;

    /**
     * 属性类型
     */
    private Class<?> mDataType;

    /**
     * 是否是主键
     */
    private boolean mIsPrimaryKey;

    public FieldProperty(Field field) {
        super();

        this.mField = field;
        this.mColumnName = field.getName();
        this.mDataType = field.getType();

        if (FieldUtils.isPrimaryKey(field)) {
            mIsPrimaryKey = true;
        }

    }

    public String getColumnName() {
        return mColumnName;
    }

    public boolean isPrimaryKey() {
        return mIsPrimaryKey;
    }

    public Class<?> getDataType() {
        return mDataType;
    }

    /**
     * 为传入对象的field属性赋值
     * 
     * @param object
     * @param value
     */
    public void setValueForObject(Object object, Object value) {
        if (object == null) {
            MyhcLog.error(CLASS_NAME, "setValueForObject", "object == null");
        }
        if (value == null) {
            MyhcLog.error(CLASS_NAME, "setValueForObject", "value == null");
        }
        try {
            mField.setAccessible(true);

            if (mDataType == String.class) {
                mField.set(object, value.toString());
            } else if (mDataType == int.class || mDataType == Integer.class) {
                mField.set(object, value == null ? null : Integer.parseInt(value.toString()));
            } else if (mDataType == float.class || mDataType == Float.class) {
                mField.set(object, value == null ? null : Float.parseFloat(value.toString()));
            } else if (mDataType == double.class || mDataType == Double.class) {
                mField.set(object, value == null ? null : Double.parseDouble(value.toString()));
            } else if (mDataType == long.class || mDataType == Long.class) {
                mField.set(object, value == null ? null : Long.parseLong(value.toString()));
            } else if (mDataType == boolean.class || mDataType == Boolean.class) {
                mField.set(object, value == null ? null : "true".equals(value.toString()));
            } else if (mDataType.getSuperclass() == Enum.class) {
                mField.set(object, Enum.valueOf((Class<Enum>) mDataType, value.toString()));
            } else {
                mField.set(object, value);
            }
        } catch (IllegalAccessException e) {
            MyhcLog.error(CLASS_NAME, "setValueForObject", e);
        } catch (IllegalArgumentException e) {
            MyhcLog.error(CLASS_NAME, "setValueForObject", e);
        }
    }

    /**
     * 获取当前field在传入对象中的值
     * 
     * @param object
     * @return
     */
    public Object getValueFromObject(Object object) {
        Object value = null;
        try {
            mField.setAccessible(true);
            value = mField.get(object);
        } catch (IllegalAccessException e) {
            MyhcLog.error(CLASS_NAME, "getValueFromObject", e);
        } catch (IllegalArgumentException e) {
            MyhcLog.error(CLASS_NAME, "getValueFromObject", e);
        }
        return value;
    }

}
