package com.commonlib.dblib.qihoo.persistence.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.commonlib.dblib.qihoo.persistence.entity.FieldProperty;

/**
 * 
 * 数据库操作工具类
 */
public class DataBaseUtils {

    private static final String CLASS_NAME = "DataBaseUtil";

    // 数据类型常量
    private static final String INTEGER = "INTEGER";
    private static final String REAL = "REAL";
    private static final String TEXT = "TEXT";
    private static final String NUMERIC = "NUMERIC";


    /**
     * 检查该table是否已经存在
     * 
     * @param tableName
     * @param db
     * @return
     */
    public static boolean isTableExisted(String tableName, SQLiteDatabase db) {
        MyhcLog.debug(CLASS_NAME, "isTableExisted", "begin......");
        String sql = "select count(*) as c from Sqlite_master where type ='table' and name = ? ";
        Cursor resultCursor = null;
        boolean existed = false;
        try {
            resultCursor = db.rawQuery(sql, new String[] { String.valueOf(tableName) });

            while (resultCursor.moveToNext()) {
                int tableCount = resultCursor.getInt(resultCursor.getColumnIndex("c"));
                if (tableCount == 1) {
                    existed = true;
                }
            }
        } catch (Exception e) {
            MyhcLog.error(CLASS_NAME, "isTableExisted", e);
        } finally {
            if (resultCursor != null) {
                resultCursor.close();
                resultCursor = null;
            }
        }
        MyhcLog.debug(CLASS_NAME, "isTableExisted", "existed == "+ existed);
        MyhcLog.debug(CLASS_NAME, "isTableExisted", "end......");
        return existed;
    }

    
    /**
     * 检测当前数据库是否可以使用
     * 
     * @return
     */
    public static boolean isValidDb(SQLiteDatabase db) {
        MyhcLog.debug(CLASS_NAME, "isValidDb", "begin......");
        boolean valided = false;

        if (db == null) {
            MyhcLog.warn(CLASS_NAME, "isValidDb", "db == null");
            return valided;
        }

        if (!db.isOpen()) {
            MyhcLog.warn(CLASS_NAME, "isValidDb", "db is closed");
            return valided;
        }

        // 有效
        valided = true;
        MyhcLog.debug(CLASS_NAME, "isValidDb", "end......");
        return valided;
    }

    /**
     * 通过model创建数据库表
     * 
     * @param model
     * @param db
     */
    public static void createTable(SQLiteDatabase db, String tableName, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "createTable", "begin......");
        String createSql = getCreateSql(tableName, propertyList);
        if (!TextUtils.isEmpty(createSql)) {
            boolean valided = isValidDb(db);
            if (valided) {
                MyhcLog.debug(CLASS_NAME, "createTable", "will exec create sql.");
                db.execSQL(createSql);
                MyhcLog.debug(CLASS_NAME, "createTable", "will exec create sql over.");
            }
        }
        MyhcLog.debug(CLASS_NAME, "createTable", "begin......");
    }


    /**
     * 待创建表明及属性集合，构造出创建表的语句
     * 
     * @param tableName
     * @param propertyList
     * @return
     */
    private static String getCreateSql(String tableName, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "getCreateSql", "begin ........");
        if (propertyList == null) {
            return null;
        }

        String createSql = "CREATE TABLE ";

        String createCols = "( ";

        // 迭代属性信息
        for (FieldProperty property : propertyList) {
            if (property.isPrimaryKey()) {
                createCols += property.getColumnName() + " " + getSQLDataType(property.getDataType()) + " PRIMARY KEY, ";
            } else {
                createCols += property.getColumnName() + " " + getSQLDataType(property.getDataType()) + ", ";
            }
        }

        createCols = createCols.substring(0, createCols.length() - 2);
        createCols += ")";

        createSql += tableName + createCols;

        MyhcLog.debug(CLASS_NAME, "getCreateSql", "createSql = " + createSql);

        MyhcLog.debug(CLASS_NAME, "getCreateSql", "end ........");
        return createSql;
    }

    /**
     * 根据Java中的数据类型，映射出数据库中对应的是数据类型
     * 
     * @param dataType
     * @return
     */
    private static String getSQLDataType(Class<?> dataType) {
        MyhcLog.debug(CLASS_NAME, "getSQLDataType", "begin ........");
        String typeStr = TEXT;

        if (dataType == null) {
            MyhcLog.warn(CLASS_NAME, "getSQLDataType ", "dataType ==  null");
            return typeStr;
        }

        if (dataType == int.class || dataType == Integer.class || dataType == long.class || dataType == Long.class) {
            typeStr = INTEGER;
            MyhcLog.debug(CLASS_NAME, "getSQLDataType", "will return  INTEGER ");
            return typeStr;
        } else if (dataType == float.class || dataType == Float.class || dataType == double.class || dataType == Double.class) {
            typeStr = REAL;
            MyhcLog.debug(CLASS_NAME, "getSQLDataType", "will return  REAL ");
            return typeStr;
        } else if (dataType == boolean.class || dataType == Boolean.class) {
            typeStr = NUMERIC;
            MyhcLog.debug(CLASS_NAME, "getSQLDataType", "will return  NUMERIC ");
            return typeStr;
        } else if (dataType == String.class) {
            typeStr = TEXT;
            MyhcLog.debug(CLASS_NAME, "getSQLDataType", "will return  TEXT ");
            return typeStr;
        }

        if (dataType.getSuperclass() == Enum.class) {
            typeStr = TEXT;
            MyhcLog.debug(CLASS_NAME, "getSQLDataType", "datatype is enmu, will return  TEXT ");
            return typeStr;
        }

        MyhcLog.warn(CLASS_NAME, "getSQLDataType ", dataType.getName() + " 不是基本的数据类型");
        return typeStr;
    }


    /**
     * 升级表
     * 
     * @param db
     * @param tableName
     * @param propertyList
     */
    public static void upgradeTable(SQLiteDatabase db, String tableName, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "upgradeTable", "begin ........");
        String currentCreateSql = getCreateSql(tableName, propertyList);
        String originalCreateSql = getOriginalCreateSql(tableName, db);
        MyhcLog.debug(CLASS_NAME, "upgradeTable", "currentCreateSql = "+currentCreateSql);
        MyhcLog.debug(CLASS_NAME, "upgradeTable", "originalCreateSql = "+originalCreateSql);
        // 是否需要更新判断
        if (!TextUtils.isEmpty(currentCreateSql) && !TextUtils.isEmpty(originalCreateSql) && !currentCreateSql.equals(originalCreateSql)) {
            MyhcLog.debug(CLASS_NAME, "upgradeTable", "must be updata table.");
            List<String> updateCols = getUpdateCol(currentCreateSql, originalCreateSql);
            MyhcLog.debug(CLASS_NAME, "upgradeTable", "will updata table.");
            for (String updateCol : updateCols) {
                String sql = "ALTER TABLE  " + tableName + " ADD COLUMN " + updateCol;
                db.execSQL(sql);
            }
            MyhcLog.debug(CLASS_NAME, "upgradeTable", "will updata table over.");
        }
        
        MyhcLog.debug(CLASS_NAME, "upgradeTable", "end ........");
    }

    /**
     * 获取当前数据库表的创建SQL
     * 
     * @param db
     * @param tableName
     * @return
     */
    private static String getOriginalCreateSql(String tableName, SQLiteDatabase db) {
        MyhcLog.debug(CLASS_NAME, "getOriginalCreateSql", "begin ........");
        String createTableSql = "";
        String sql = "select sql from Sqlite_master  where type ='table' and name = ? ";
        Cursor resultCursor = null;
        try {
            boolean valided = isValidDb(db);
            if (valided) {
                resultCursor = db.rawQuery(sql, new String[] { String.valueOf(tableName) });
                if (resultCursor != null) {
                    while (resultCursor.moveToNext()) {
                        createTableSql = resultCursor.getString(resultCursor.getColumnIndex("sql"));// 获取建表语句
                    }
                }
            }
        } catch (Exception e) {
            MyhcLog.error(CLASS_NAME, "getOriginalCreateSql", e);
        } finally {
            if (resultCursor != null) {
                resultCursor.close();
                resultCursor = null;
            }
        }
        MyhcLog.debug(CLASS_NAME, "getOriginalCreateSql", "end ........");
        return createTableSql;
    }

    /**
     * 返回修改字段及数据类型，用于更新数据库表
     * 
     * @param currentCreateSql
     * @param originalCreateSql
     * @return
     */
    private static List<String> getUpdateCol(String currentCreateSql, String originalCreateSql) {
        MyhcLog.debug(CLASS_NAME, "getUpdateCol", "begin ........");
        List<String> updateList = new ArrayList<String>();

        if (!TextUtils.isEmpty(currentCreateSql) && !TextUtils.isEmpty(originalCreateSql)) {
            if (currentCreateSql.equals(originalCreateSql)) {
                // 不需要更新
                return updateList;
            }

            int pos1 = currentCreateSql.indexOf("(");
            int pos2 = currentCreateSql.indexOf(")");

            String currentSub = currentCreateSql.substring(pos1 + 1, pos2);

            String[] currentCols = currentSub.split(",");

            for (String string : currentCols) {
                updateList.add(string);
            }

            pos1 = originalCreateSql.indexOf("(");
            pos2 = originalCreateSql.indexOf(")");
            String originalSub = originalCreateSql.substring(pos1 + 1, pos2);
            String[] originalCols = originalSub.split(",");

            for (String string : originalCols) {
                boolean exist = updateList.contains(string);
                if (exist) {
                    updateList.remove(string);
                }
            }
        }
        
        MyhcLog.debug(CLASS_NAME,"getUpdateCol","updateList = "+updateList);
        MyhcLog.debug(CLASS_NAME, "getUpdateCol", "end ........");
        return updateList;
    }


    /**
     * 获取传入对象的属性信息
     * 
     * @param entity
     * @param propertyList
     * @return
     */
    public static ContentValues getContentValues(Object entity, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "getContentValues", "begin ........");
        ContentValues propertyInfo = new ContentValues();

        for (FieldProperty property : propertyList) {
            if (property.isPrimaryKey()) {
                continue;
            }
            // 将非主键的信息写入到contentValues中
            String pcolumn = property.getColumnName();
            Object value = property.getValueFromObject(entity);
            if (value != null) {
                propertyInfo.put(pcolumn, value.toString());
            }
        }
        MyhcLog.debug(CLASS_NAME, "getContentValues", "end ........");
        return propertyInfo;
    }

    /**
     * 获取传入对象的属性信息
     * 
     * @param entity
     * @param propertyList
     * @return
     */
    public static ContentValues getContentValuesForInsert(Object entity, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "getContentValuesForInsert", "begin ........");
        ContentValues propertyInfo = new ContentValues();

        for (FieldProperty property : propertyList) {
            String pcolumn = property.getColumnName();
            Object value = property.getValueFromObject(entity);
            if (value != null) {
                propertyInfo.put(pcolumn, value.toString());
//                MyhcLog.debug(CLASS_NAME, "getContentValuesForInsert", "pcolumn = "+pcolumn +", value = "+ value.toString());
            }
        }
        MyhcLog.debug(CLASS_NAME, "getContentValuesForInsert", "end ........");
        return propertyInfo;
    }

    /**
     * 根据主键获取where信息
     * 
     * @param entity
     * @param propertyList
     * @return
     */
    public static String getWhere(Object entity, List<FieldProperty> propertyList) {
        MyhcLog.debug(CLASS_NAME, "getWhere", "begin ........");
        StringBuffer where = new StringBuffer();
        if (propertyList != null) {
            for (FieldProperty property : propertyList) {
                if (property.isPrimaryKey()) {
                    String column = property.getColumnName();
                    where.append(column);
                    where.append("='");
                    Object value = property.getValueFromObject(entity);
                    where.append(value.toString());
                    where.append("'");
                }
            }
        }
        MyhcLog.debug(CLASS_NAME, "getWhere", "end ........");
        return where.toString();
    }

}
