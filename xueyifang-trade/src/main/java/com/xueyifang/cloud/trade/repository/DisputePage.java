package com.xueyifang.cloud.trade.repository;

import java.util.List;

public record DisputePage(
        List<TradeDispute> records,
        long total) {
}
