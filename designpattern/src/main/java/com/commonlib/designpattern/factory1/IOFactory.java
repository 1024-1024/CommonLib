package com.commonlib.designpattern.factory1;

/**
 * Created by zhangweilong on 2017/11/12.
 *
 * <P>工厂类，获取handler实例</P>
 */

public class IOFactory {


    public static <T extends IOHandler> T getHandler(Class<T> tClass) {
        IOHandler ioHandler = null;
        try {
            ioHandler = (IOHandler) Class.forName(tClass.getName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (T) ioHandler;
    }

}
