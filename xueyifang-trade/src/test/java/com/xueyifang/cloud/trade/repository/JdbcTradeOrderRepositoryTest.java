package com.xueyifang.cloud.trade.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTradeOrderRepositoryTest {

    @Test
    void normalizesDatabaseRoleValues() {
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("1")).isEqualTo(1);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("2")).isEqualTo(2);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("STUDENT")).isEqualTo(1);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("ADMIN")).isEqualTo(2);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode(" student ")).isEqualTo(1);
    }

    @Test
    void defaultsUnknownRoleToStudentCode() {
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode(null)).isEqualTo(1);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("")).isEqualTo(1);
        assertThat(JdbcTradeOrderRepository.normalizeRoleCode("UNKNOWN")).isEqualTo(1);
    }
}
