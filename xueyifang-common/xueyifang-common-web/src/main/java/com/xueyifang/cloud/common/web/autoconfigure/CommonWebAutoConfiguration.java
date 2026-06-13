package com.xueyifang.cloud.common.web.autoconfigure;

import com.xueyifang.cloud.common.web.exception.GlobalExceptionHandler;
import com.xueyifang.cloud.common.web.filter.RequestIdFilter;
import com.xueyifang.cloud.common.web.filter.UserContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({GlobalExceptionHandler.class, RequestIdFilter.class, UserContextFilter.class})
public class CommonWebAutoConfiguration {
}
