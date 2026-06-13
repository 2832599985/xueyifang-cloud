package com.xueyifang.cloud.common.web.filter;

import com.xueyifang.cloud.common.core.context.TraceConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void generatesRequestIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                requestIdInChain.set(MDC.get(TraceConstants.MDC_REQUEST_ID_KEY)));

        assertThat(response.getHeader(TraceConstants.REQUEST_ID_HEADER)).isNotBlank();
        assertThat(requestIdInChain.get()).isEqualTo(response.getHeader(TraceConstants.REQUEST_ID_HEADER));
        assertThat(MDC.get(TraceConstants.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    void reusesIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader(TraceConstants.REQUEST_ID_HEADER, "  req-123  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                assertThat(MDC.get(TraceConstants.MDC_REQUEST_ID_KEY)).isEqualTo("req-123"));

        assertThat(response.getHeader(TraceConstants.REQUEST_ID_HEADER)).isEqualTo("req-123");
    }
}
