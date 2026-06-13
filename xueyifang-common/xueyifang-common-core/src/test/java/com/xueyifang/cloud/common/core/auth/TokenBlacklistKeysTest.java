package com.xueyifang.cloud.common.core.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBlacklistKeysTest {

    @Test
    void buildsStableSha256KeyFromTrimmedToken() {
        assertThat(TokenBlacklistKeys.fromToken("  token-value  "))
                .isEqualTo(TokenBlacklistKeys.fromToken("token-value"))
                .startsWith(TokenBlacklistKeys.PREFIX)
                .hasSize(TokenBlacklistKeys.PREFIX.length() + 64);
    }

    @Test
    void rejectsBlankToken() {
        assertThatThrownBy(() -> TokenBlacklistKeys.fromToken(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
