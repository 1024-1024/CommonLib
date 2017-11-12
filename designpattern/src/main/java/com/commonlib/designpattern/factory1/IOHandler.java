package com.commonlib.designpattern.factory1;

/**
 * Created by zhangweilong on 2017/11/12.
 */

public abstract class IOHandler<T> {
    abstract void add(T t);
    abstract void remove(T t);
    abstract void update(T t);
    abstract T query();

}
