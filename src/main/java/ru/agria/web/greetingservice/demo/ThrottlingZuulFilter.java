package ru.agria.web.greetingservice.demo;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletResponse;

@Profile("throttled")
@Component
class ThrottlingZuulFilter extends ZuulFilter {

    private final HttpStatus tooManyRequests = HttpStatus.TOO_MANY_REQUESTS;

    private final RateLimiter rateLimiter;

    @Autowired
    public ThrottlingZuulFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        try {
            RequestContext currentContext = RequestContext.getCurrentContext();
            HttpServletResponse response = currentContext.getResponse();

            if (!rateLimiter.tryAcquire()) {

                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setStatus(this.tooManyRequests.value());
                response.getWriter().append(this.tooManyRequests.getReasonPhrase());

                currentContext.setSendZuulResponse(false);

                throw new ZuulException(this.tooManyRequests.getReasonPhrase(),
                                        this.tooManyRequests.value(),
                                        this.tooManyRequests.getReasonPhrase());
            }
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;
    }
}