package org.jahia.portal.pluto.bridges.struts;

import org.apache.pluto.container.driver.PortletServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * This class is a remplacement for the default portlet servlet dispatcher because we need to make the servlet
 * request and response objects available to the Struts bridge. We do this using ThreadLocal's as we have no way to
 * pass the data another way.
 *
 * @author loom
 *         Date: Jun 19, 2009
 *         Time: 4:53:57 PM
 */
public class PlutoStrutsPortletServlet extends PortletServlet {

    private static ThreadLocal<HttpServletRequest> tlServletRequest = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> tlServletResponse = new ThreadLocal<HttpServletResponse>();
    private static ThreadLocal<ServletContext> tlServletContext = new ThreadLocal<ServletContext>();

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doGet(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doPost(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    @Override
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doPut(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    public static HttpServletRequest getHttpServletRequest() {
        return tlServletRequest.get();
    }

    public static HttpServletResponse getHttpServletResponse() {
        return tlServletResponse.get();
    }

    public static ServletContext getStaticServletContext() {
        return tlServletContext.get();
    }
}
