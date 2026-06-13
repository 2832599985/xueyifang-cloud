package com.xueyifang.cloud.common.web.exception;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> handleBusinessException(BusinessException exception) {
        log.warn("BusinessException: {}", exception.getMessage(), exception);
        return buildErrorResponse(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数验证失败";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtils.error(ErrorCode.PARAMS_ERROR, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<?>> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtils.error(ErrorCode.PARAMS_ERROR, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtils.error(ErrorCode.PARAMS_ERROR, message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> handleRuntimeException(RuntimeException exception) {
        log.error("RuntimeException", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception exception) {
        log.error("Exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"));
    }

    private ResponseEntity<BaseResponse<?>> buildErrorResponse(int code, String message) {
        HttpStatus status = mapHttpStatus(code);
        return ResponseEntity.status(status).body(ResultUtils.error(code, message));
    }

    private HttpStatus mapHttpStatus(int code) {
        if (code == ErrorCode.USER_NOT_LOGIN.getCode()
                || code == ErrorCode.TOKEN_INVALID.getCode()
                || code == ErrorCode.TOKEN_EXPIRED.getCode()
                || code == ErrorCode.USER_PASSWORD_ERROR.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (code == ErrorCode.NO_AUTH_ERROR.getCode()
                || code == ErrorCode.FORBIDDEN_ERROR.getCode()
                || code == ErrorCode.PERMISSION_DENIED.getCode()
                || code == ErrorCode.USER_NO_PUBLISH_PERMISSION.getCode()
                || code == ErrorCode.USER_ACCOUNT_DISABLED.getCode()) {
            return HttpStatus.FORBIDDEN;
        }

        if (code == ErrorCode.NOT_FOUND_ERROR.getCode()
                || code == ErrorCode.USER_NOT_EXIST.getCode()
                || code == ErrorCode.SERVICE_NOT_EXIST.getCode()
                || code == ErrorCode.ORDER_NOT_EXIST.getCode()
                || code == ErrorCode.DISPUTE_NOT_EXIST.getCode()
                || code == ErrorCode.CHAT_MESSAGE_NOT_EXIST.getCode()
                || code == ErrorCode.CHAT_RECEIVER_NOT_EXIST.getCode()) {
            return HttpStatus.NOT_FOUND;
        }

        if (code == ErrorCode.RATE_LIMIT_EXCEEDED.getCode()) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }

        if (code == ErrorCode.DUPLICATE_SUBMIT.getCode()
                || code == ErrorCode.USER_STUDENT_ID_EXIST.getCode()
                || code == ErrorCode.USER_USERNAME_EXIST.getCode()) {
            return HttpStatus.CONFLICT;
        }

        if (code >= ErrorCode.SYSTEM_ERROR.getCode()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
