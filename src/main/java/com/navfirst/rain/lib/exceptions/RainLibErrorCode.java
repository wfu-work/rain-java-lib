package com.navfirst.rain.lib.exceptions;

/** 与Go动态库error.code对应的错误码。 */
public enum RainLibErrorCode {
    INVALID_ARGUMENT,
    INVALID_HANDLE,
    CLOSED,
    ENVIRONMENT_ERROR,
    MODEL_ERROR,
    PYTHON_ERROR,
    PROTOCOL_ERROR,
    PREDICTION_ERROR,
    TIMEOUT,
    CANCELED,
    IO_ERROR,
    INTERNAL_ERROR,
    NOT_READY
}
