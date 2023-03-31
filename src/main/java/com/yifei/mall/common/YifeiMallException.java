
package com.yifei.mall.common;

public class YifeiMallException extends RuntimeException {

    public YifeiMallException() {
    }

    public YifeiMallException(String message) {
        super(message);
    }

    /**
     * 丢出一个异常
     *
     * @param message
     */
    public static void fail(String message) {
        throw new YifeiMallException(message);
    }

}
