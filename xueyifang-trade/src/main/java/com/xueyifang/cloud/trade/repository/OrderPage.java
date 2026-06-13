package com.xueyifang.cloud.trade.repository;

import java.util.List;

public record OrderPage(
        List<TradeOrder> records,
        long total) {
}
