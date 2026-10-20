package com.navfirst.rain.lib.exceptions;

import lombok.Getter;

import java.io.Serial;

/**
 * 创建：馥溪凝
 * 日期：2022/04/11 21:31
 * 描述：com.navfirst.dmonitor.lib.exceptions
 */
@Getter
public class RainLibException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;

    private final String msg;

    public RainLibException() {
        this(RainLibErrorCode.INTERNAL_ERROR, "internal error");
    }

    public RainLibException(String code, String msg) {
        this(code, msg, null);
    }

    public RainLibException(String code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public RainLibException(RainLibErrorCode code, String msg) {
        this(code.name(), msg);
    }

    public RainLibException(RainLibErrorCode code, String msg, Throwable cause) {
        this(code.name(), msg, cause);
    }

    public RainLibException(String msg) {
        this(RainLibErrorCode.INTERNAL_ERROR, msg);
    }

    public RainLibException(Exception e) {
        this(RainLibErrorCode.INTERNAL_ERROR, e.getMessage(), e);
    }

}
