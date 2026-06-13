package com.xueyifang.cloud.common.web.exception;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionMapsUnauthorizedCodesToUnauthorizedResponse() {
        ResponseEntity<BaseResponse<?>> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("login required");
    }

    @Test
    void runtimeExceptionHidesInternalMessage() {
        ResponseEntity<BaseResponse<?>> response = handler.handleRuntimeException(
                new RuntimeException("internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("系统错误");
    }
}
