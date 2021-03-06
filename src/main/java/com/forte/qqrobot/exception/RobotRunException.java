package com.forte.qqrobot.exception;

/**
 *
 * 一般发生在启动期间的异常
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class RobotRunException extends RobotRuntimeException {

    private static final String EXCEPTION_TAG = "run";

    @Override
    public String getExceptionTag(){
        return EXCEPTION_TAG;
    }

    public RobotRunException() {
    }

    public RobotRunException(String message, Object... format) {
        super(message, format);
    }

    public RobotRunException(String message) {
        super(message);
    }

    public RobotRunException(String message, Throwable cause, Object... format) {
        super(message, cause, format);
    }

    public RobotRunException(String message, Throwable cause) {
        super(message, cause);
    }

    public RobotRunException(Throwable cause) {
        super(cause);
    }

    public RobotRunException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RobotRunException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... format) {
        super(message, cause, enableSuppression, writableStackTrace, format);
    }

    /**
     * 不进行语言国际化转化的构造方法
     *
     * @param pointless 无意义参数，填任意值 pointless param
     * @param message   信息正文
     */
    public RobotRunException(int pointless, String message) {
        super(pointless, message);
    }

    /**
     * 不进行语言国际化转化的构造方法
     *
     * @param pointless 无意义参数，填任意值 pointless param
     * @param message   信息正文
     * @param cause     异常
     */
    public RobotRunException(int pointless, String message, Throwable cause) {
        super(pointless, message, cause);
    }

    /**
     * 不进行语言国际化转化的构造方法
     *
     * @param pointless          无意义参数，填任意值 pointless param
     * @param message            信息正文
     * @param cause              异常
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     */
    public RobotRunException(int pointless, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(pointless, message, cause, enableSuppression, writableStackTrace);
    }
}
