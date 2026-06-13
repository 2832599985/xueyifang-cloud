package com.xueyifang.cloud.common.core.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultUtilsTest {

    @Test
    void successUsesLegacyResponseContract() {
        BaseResponse<Integer> response = ResultUtils.success(1);

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo(1);
    }

    @Test
    void errorUsesErrorCodeAndCustomMessage() {
        BaseResponse<Object> response = ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数不能为空");

        assertThat(response.getCode()).isEqualTo(40001);
        assertThat(response.getMessage()).isEqualTo("参数不能为空");
        assertThat(response.getData()).isNull();
    }
}
