package org.jahia.bin.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/11/12
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSSChannelFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
