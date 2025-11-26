package org.jahia.bin.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Http servlet filter that handles multiple response.sendError calls following the same request.
 * This prevents IllegalStateException when the response is already committed.
 *
 */
public class ChainedErrorHandlerFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ChainedErrorHandlerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Intentionally empty - no initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        chain.doFilter(request, new ChainedErrorHandlerResponseWrapper(request, httpServletResponse));
    }

    @Override
    public void destroy() {
        // Intentionally empty - no cleanup needed
    }

    /**
     * Response wrapper that intercepts sendError calls and prevents IllegalStateException
     * when the response is already committed.
     *
     * <p>When a sendError call is attempted on a committed response, this wrapper logs
     * a warning with the error details and any original exception that caused the error,
     * instead of throwing an exception.</p>
     */
    private static class ChainedErrorHandlerResponseWrapper extends HttpServletResponseWrapper {
        private final ServletRequest request;

        ChainedErrorHandlerResponseWrapper(ServletRequest request, HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
            this.request = request;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            if (isCommitted()) {
                logCommittedWarning(msg);
                return;
            }
            super.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            if (isCommitted()) {
                logCommittedError();
                return;
            }
            super.sendError(sc);
        }

        private void logCommittedWarning(String msg) {
            log.warn("Response has already been committed. Cannot send error with message: {}", msg);
            logOriginalException();
        }

        private void logCommittedError() {
            log.warn("Response has already been committed. Cannot send error.");
            logOriginalException();
        }

        private void logOriginalException() {
            if (log.isDebugEnabled()) {
                Object exception = request.getAttribute("javax.servlet.error.exception");
                if (exception instanceof Throwable) {
                    log.debug("Original exception that caused the error:", (Throwable) exception);
                }
            }
        }
    }
}
