package com.xueyifang.cloud.common.core.context;

import java.io.Serializable;

public record LoginUserContext(Long userId, Integer role, Integer publishPermission) implements Serializable {
}
