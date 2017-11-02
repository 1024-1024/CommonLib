package com.commonlib.dblib.qihoo.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.commonlib.dblib.qihoo.persistence.cache.DatabaseCache;
import com.commonlib.dblib.qihoo.persistence.cache.PropertyCache;
import com.commonlib.dblib.qihoo.persistence.entity.FieldProperty;
import com.commonlib.dblib.qihoo.persistence.listener.CallbackListener;
import com.commonlib.dblib.qihoo.persistence.util.CursorUtils;
import com.commonlib.dblib.qihoo.persistence.util.DataBaseUtils;
import com.commonlib.dblib.qihoo.persistence.util.MyhcLog;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 抽象数据库表管理器，所有以数据库表方式存储的数据，均需要继承自本类
 * 
 */
public abstract class AbsTable<T> {
    /**
     * 日志标签
     */
    private static final String CLASS_NAME = "AbsTable";

    /**
     * 默认数据库存储位置
     */
    private static final String DEFAULT_DB_PATH = "default";

    // 用于保证数据的单线程操作
    private static Handler mHandler = null;
    private static HandlerThread mHandlerThread = null;

    /**
     * 数据库对象
     */
    private SQLiteDatabase mDatabase = null;

    /**
     * 该文件所管理/绑定的实体类
     */
    private Class<T> mBinderEntityClass = null;

    /**
     * 该文件所管理的Class的属性信息
     */
    private List<FieldProperty> mPropertyList = null;

    /**
     * 该文件所管理的Class的属性信息, key为属性名称
     */
    private Map<String, FieldProperty> mPropertyMap = null;
    /**
     * 对应的数据库表名
     */
    private String mTableName = "";

    /**
     * 用于标志主键信息
     * 
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface PrimaryKey {
    }

    /**
     * 用于标志临时数据信息
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TempKey {
    }


    /**
     * 获取数据库位置。
     * 子类可以根据指定的类型，进行指定位置的创建
     * 
     */
    protected String getDbPath() {
        return DEFAULT_DB_PATH;
    }

    /**
     * 初始化table，将数据库表检测升级等，仅为DBTableManager调用
     * 
     * @param context
     */
    synchronized final void initTable(Context context) {

        MyhcLog.debug(CLASS_NAME, "initFile", "begin .......");

        String dbPath = getDbPath();
        if (TextUtils.isEmpty(dbPath)) {
            dbPath = DEFAULT_DB_PATH;
        }

        // 从db缓存池中取出数据库对象
        mDatabase = DatabaseCache.getDb(dbPath);

        if (mDatabase == null) {
            // 创建数据库，数据库名等于程序包名
            String packageName = context.getPackageName();
            String dataBaseName = packageName + ".db";

            // 数据库文件不保存在默认位置（如自定义到sd 卡）
            if (!DEFAULT_DB_PATH.equals(dbPath)) {
                File dbFile = new File(dbPath);
                // 路径不存在则创建
                if (!dbFile.exists()) {
                    dbFile.mkdir();
                }
                dataBaseName = dbPath + File.separator + dataBaseName;
            }
            mDatabase = context.openOrCreateDatabase(dataBaseName, SQLiteDatabase.OPEN_READWRITE, null);

            DatabaseCache.putDb(dbPath, mDatabase);
        }

        Type type = getClass().getGenericSuperclass();
        Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];

        // 属性赋值
        this.mBinderEntityClass = (Class<T>) trueType;

        MyhcLog.debug(CLASS_NAME, "initFile", "mBinderEntityClass name = " + mBinderEntityClass.getName());

        // 检测并保存类的属性信息
        this.mPropertyList = PropertyCache.getProperty(mBinderEntityClass.getName());
        if (mPropertyList == null) {
            mPropertyMap = FieldUtils.getPropertyMap(mBinderEntityClass);
            mPropertyList = new ArrayList<>(mPropertyMap.values());
            PropertyCache.putProperty(mBinderEntityClass.getName(), mPropertyList);
        }

        // 保证数据库操做位于单线程中，解决数据同步问题
        if (mHandler == null) {
            mHandlerThread = new HandlerThread("dbHandlerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }

        try {
            // 创建或更新数据库表
            createOrUpdateTable();
        } catch (SQLException e) {
            MyhcLog.error(CLASS_NAME, "initFile", e);
            throw e;
        }

        MyhcLog.debug(CLASS_NAME, "initFile", "end .......");
    }



    /**
     * 创建或更新数据库表
     */
    private void createOrUpdateTable() throws SQLException {
        MyhcLog.debug(CLASS_NAME, "createOrUpdateTable", "begin .......");
        // 设置文件名称
        mTableName = mBinderEntityClass.getSimpleName();

        boolean isExited = DataBaseUtils.isTableExisted(mTableName, mDatabase);
        if (!isExited) {
            DataBaseUtils.createTable(mDatabase, mTableName, mPropertyList);
        } else {
            DataBaseUtils.upgradeTable(mDatabase, mTableName, mPropertyList);
        }
        MyhcLog.debug(CLASS_NAME, "createOrUpdateTable", "end .......");
    }

    /**
     * 获取数据存储所对应的数据库表名
     * 
     * @return
     */
    protected final String getTableName() {
        return mBinderEntityClass.getSimpleName();
    }


    // 数据库的CRUD操作
    // ///////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// 插入 ///////////////////////////////////////////////////
    /**
     * 直接将传入的实体对象插入到数据库表
     * 
     * @param entity
     */
    public synchronized void insert(final T entity) {
        MyhcLog.debug(CLASS_NAME, "insert", "begin .......");
        // 数据校验
        if (null == entity) {
            MyhcLog.warn(CLASS_NAME, "insert", "entity is empty");
            return;
        }
        // 构造集合
        List<T> entitys = new ArrayList<T>(1);
        entitys.add(entity);
        // 调用集合方法
        insert(entitys);
        MyhcLog.debug(CLASS_NAME, "insert", "end .......");
    }

    /**
     * 直接将传入的实体对象集合插入到数据库表
     * 
     * @param entitys
     */
    public synchronized void insert(final List<T> entitys) {
        MyhcLog.debug(CLASS_NAME, "insert<list>", "begin .......");
        // 数据校验
        if (null == entitys || entitys.size() == 0) {
            MyhcLog.warn(CLASS_NAME, "insert<list>", "entitys is empty");
            return;
        }
//
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                MyhcLog.debug(CLASS_NAME, "insert", "handler run begin ...");
                boolean valided = DataBaseUtils.isValidDb(mDatabase);
                if (valided) {
                    mDatabase.beginTransaction();
                    try {
                        for (T entity : entitys) {
                            if (entity instanceof AbsTableEntity) {
                                MyhcLog.debug(CLASS_NAME, "insert<list>", "will call generateId...");
                                ((AbsTableEntity) entity).generateId();
//                                MyhcLog.debug(CLASS_NAME, "insert<list>", "toString "+entity.toString());  
                            } else {
                                MyhcLog.warn(CLASS_NAME, "insert<list>", "entity type Illegal");
                            }

                            ContentValues values = DataBaseUtils.getContentValuesForInsert(entity, mPropertyList);
                            mDatabase.insertOrThrow(mTableName, "", values);
                        }
                        mDatabase.setTransactionSuccessful();
                    } catch (SQLException e) {
                        MyhcLog.error(CLASS_NAME, "insert<list>", e);
                    } finally {
                        mDatabase.endTransaction();
                    }
                }
//                MyhcLog.debug(CLASS_NAME, "insert", "handler run end ...");
//            }
//        });
        MyhcLog.debug(CLASS_NAME, "insert<list>", "end .......");
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// 查询 ///////////////////////////////////////////////////

    /**
     * 异步返回数据库中所有记录信息
     * 
     * @return
     */
    public synchronized void query(CallbackListener<List<T>> listener) {
        MyhcLog.debug(CLASS_NAME, "query", "begin .......");
        if (listener == null) {
            MyhcLog.warn(CLASS_NAME, "query", "listener is empty");
            return;
        }
        mHandler.post(new QueryRunnable(listener));
        MyhcLog.debug(CLASS_NAME, "query", "end .......");
    }

    /**
     * 
     * 查询Runnable
     * 
     * <br>==========================
     * <br> 公司：奇虎360
     * <br> 开发：chenbaolong@360.cn
     * <br> 创建时间：2014-7-11下午6:21:13
     * <br>==========================
     */
    private class QueryRunnable implements Runnable {
        CallbackListener<List<T>> listener = null;

        public QueryRunnable(CallbackListener<List<T>> listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void run() {
            MyhcLog.debug(CLASS_NAME, "QueryRunnable", "handler run begin ...");
            final List<T> list = new ArrayList<T>();
            Cursor cursor = null;
            try {
                boolean valided = DataBaseUtils.isValidDb(mDatabase);
                if (!valided) {
                    if (listener != null) {
                        listener.callback(null);
                    }
                }

                String querySql = "select * from " + mTableName;
                cursor = mDatabase.rawQuery(querySql, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        T t = (T) CursorUtils.getEntity(cursor, mBinderEntityClass, mPropertyMap);
                        list.add(t);
                    }

                    if (listener != null) {
                        listener.callback(list);
                    }
                }
            } catch (SQLException e) {
                MyhcLog.error(CLASS_NAME, "QueryRunnable", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                cursor = null;
            }

            MyhcLog.debug(CLASS_NAME, "QueryRunnable", "handler run end ...");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// 修改 ///////////////////////////////////////////////////

    /**
     * 更新数据库中存储的该实体信息
     * 
     * @param entity
     * @return
     */
    public synchronized void update(final T entity) {
        // 数据校验
        if (entity == null) {
            MyhcLog.warn(CLASS_NAME, "update", "entity is empty");
            return;
        }

        List<T> entitys = new ArrayList<T>(1);
        entitys.add(entity);
        // 调用集合方法
        update(entitys);
    }

    /**
     * 更新数据库中存储的该实体信息
     * 
     * @param entitys
     * @return
     */
    public synchronized void update(final List<T> entitys) {
        MyhcLog.debug(CLASS_NAME, "update", "begin......");
        // 数据校验
        if (null == entitys || entitys.size() == 0) {
            MyhcLog.warn(CLASS_NAME, "update<list>", " entitys is empty");
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                MyhcLog.debug(CLASS_NAME, "update", "handler run begin ...");
                boolean valided = DataBaseUtils.isValidDb(mDatabase);
                if (valided) {
                    mDatabase.beginTransaction();
                    try {
                        for (T entity : entitys) {
                            ContentValues values = DataBaseUtils.getContentValues(entity, mPropertyList);
                            // 获取主键where条件
                            String where = DataBaseUtils.getWhere(entity, mPropertyList);
                            mDatabase.update(mTableName, values, where, null);
                        }
                        mDatabase.setTransactionSuccessful();
                    } catch (Exception e) {
                        MyhcLog.error(CLASS_NAME, "update", e);
                    } finally {
                        mDatabase.endTransaction();
                    }
                }

                MyhcLog.debug(CLASS_NAME, "update", "handler run end ...");
            }
        });
        MyhcLog.debug(CLASS_NAME, "update", "end......");
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// 删除 ///////////////////////////////////////////////////
    /**
     * 删除传入的实体
     * 
     * @param entity
     * @return
     */
    public synchronized void delete(T entity) {
        MyhcLog.debug(CLASS_NAME, "delete", "begin......");
        // 数据校验
        if (null == entity) {
            MyhcLog.warn(CLASS_NAME, "delete", "entity is null");
            return;
        }

        // 构造集合
        List<T> entitys = new ArrayList<T>(1);
        entitys.add(entity);
        // 调用集合方法
        delete(entitys);
        MyhcLog.debug(CLASS_NAME, "delete", "end......");
    }

    /**
     * 删除传入的实体集合
     * 
     * @param entitys
     * @return
     */
    public synchronized void delete(List<T> entitys) {
        MyhcLog.debug(CLASS_NAME, "delete<list>", "begin......");
        // 数据校验
        if (null == entitys || entitys.size() == 0) {
            MyhcLog.warn(CLASS_NAME, "delete<list>", "entitys is empty");
            return;
        }
        
        //将数据拷贝一分
        final List<T> entityList = new ArrayList(entitys);
        
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                MyhcLog.debug(CLASS_NAME, "delete<list>", "handler run begin ...");
                boolean valided = DataBaseUtils.isValidDb(mDatabase);
                if (valided) {
                    mDatabase.beginTransaction();
                    try {
                        for (T entity : entityList) {
                            String where = DataBaseUtils.getWhere(entity, mPropertyList);
                            MyhcLog.debug(CLASS_NAME, "delete<list>", "delete where = "+ where);
                            mDatabase.delete(mTableName, where, null);
                        }
                        mDatabase.setTransactionSuccessful();
                    } catch (Exception e) {
                        MyhcLog.error(CLASS_NAME, "delete<list>", e);
                    } finally {
                        mDatabase.endTransaction();
                    }
                }
                MyhcLog.debug(CLASS_NAME, "delete<list>", "handler run end ...");
            }
        });

        MyhcLog.debug(CLASS_NAME, "delete<list>", "end......");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    /**
     * 调用数据库标准方法，为反射扩展功能使用
     * 
     * @param sql
     */
    private synchronized void execSql(final String sql) {
        MyhcLog.debug(CLASS_NAME, "execSql", "begin......");
        if (!TextUtils.isEmpty(sql)) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    MyhcLog.debug(CLASS_NAME, "execSql", "handler run begin ...");
                    try {
                        boolean valided = DataBaseUtils.isValidDb(mDatabase);
                        if (valided) {
                            mDatabase.execSQL(sql);
                        }
                    } catch (Exception e) {
                        MyhcLog.error(CLASS_NAME, "execSql", e);
                    }

                    MyhcLog.debug(CLASS_NAME, "execSql", "handler run end ...");
                }
            });
        }
        MyhcLog.debug(CLASS_NAME, "execSql", "end......");
    }


    /**
     * 调用数据库标准查询方法，为反射扩展功能使用
     * 
     * @param sql
     * @param listener
     */
    private synchronized void rawQuery(final String sql, final CallbackListener<Cursor> listener) {
        MyhcLog.debug(CLASS_NAME, "rawQuery", "begin......");
        if (!TextUtils.isEmpty(sql) && listener != null) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    MyhcLog.debug(CLASS_NAME, "rawQuery", "handler run begin ...");
                    try {
                        boolean valided = DataBaseUtils.isValidDb(mDatabase);
                        if (valided) {
                            Cursor cursor = mDatabase.rawQuery(sql, null);
                            if (listener != null) {
                                listener.callback(cursor);
                            }
                        }
                    } catch (Exception e) {
                        MyhcLog.error(CLASS_NAME, "rawQuery", e);
                    }

                    MyhcLog.debug(CLASS_NAME, "rawQuery", "handler run end ...");
                }
            });

            MyhcLog.debug(CLASS_NAME, "rawQuery", "end......");
        }
    }

}
