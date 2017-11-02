package com.commonlib.dblib.qihoo.persistence.listener;

public interface CallbackListener<T> {
    /**
     * 回调方法
     * 
     * @param data
     *            回调返回的结果数据，不同的调用返回不同类型的数据，对于无数据返回的调用，data值为null。
     */
    void callback(T data);
}
