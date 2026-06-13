package com.xueyifang.cloud.common.web.filter;

import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextFilterTest {

    private final UserContextFilter filter = new UserContextFilter();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void setsUserContextFromTrustedHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader(AuthConstants.USER_ID_HEADER, " 42 ");
        request.addHeader(AuthConstants.USER_ROLE_HEADER, "2");
        request.addHeader(AuthConstants.USER_PUBLISH_PERMISSION_HEADER, "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Optional<LoginUserContext>> contextInChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                contextInChain.set(UserContextHolder.get()));

        assertThat(contextInChain.get()).hasValue(new LoginUserContext(42L, 2, 1));
        assertThat(UserContextHolder.get()).isEmpty();
    }

    @Test
    void leavesUserContextEmptyWhenUserIdHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Optional<LoginUserContext>> contextInChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                contextInChain.set(UserContextHolder.get()));

        assertThat(contextInChain.get()).isEmpty();
        assertThat(UserContextHolder.get()).isEmpty();
    }

    @Test
    void clearsExistingContextWhenHeaderIsInvalid() throws Exception {
        UserContextHolder.set(new LoginUserContext(7L, 1, 0));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader(AuthConstants.USER_ID_HEADER, "not-a-number");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Optional<LoginUserContext>> contextInChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                contextInChain.set(UserContextHolder.get()));

        assertThat(contextInChain.get()).isEmpty();
        assertThat(UserContextHolder.get()).isEmpty();
    }
}
