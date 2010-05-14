package org.jahia.params.valves;

import org.jahia.services.content.JCRSessionFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 14, 2010
 * Time: 11:18:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthValveContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JCRSessionFactory sessionFactory;

    public AuthValveContext(HttpServletRequest request, HttpServletResponse response, JCRSessionFactory sessionFactory) {
        this.request = request;
        this.response = response;
        this.sessionFactory = sessionFactory;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
