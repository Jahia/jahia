package org.jahia.bundles.servlet.bridge.jahia;

import org.jahia.bin.Action;
import org.jahia.bin.DefaultPostAction;
import org.jahia.bin.Render;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Bridge between an OSGi servlet and Jahia's render servlet.
 * User: loom
 * Date: Oct 21, 2010
 * Time: 9:09:31 AM
 * @todo For the moment we have hardcoded dependency injection, but maybe we could improve this by using Spring OSGi ?
 */
public class RenderServlet extends HttpServlet {

    public Render render = new Render();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            render.handleRequest(req, resp);
        } catch (Exception e) {
            log("Error in render servlet", e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance().getContext();
        MetricsLoggingService loggingService = (MetricsLoggingService) applicationContext.getBean("loggingService");
        JahiaTemplateManagerService templatesService = (JahiaTemplateManagerService) applicationContext.getBean("JahiaTemplateManagerService");
        JCRSessionFactory jcrSessionFactory = (JCRSessionFactory) applicationContext.getBean("jcrSessionFactory");
        RenderService renderService = (RenderService) applicationContext.getBean("RenderService");
        SettingsBean settingsBean = (SettingsBean) applicationContext.getBean("settingsBean");
        DefaultPostAction postAction = new DefaultPostAction();
        postAction.setLoggingService(loggingService);
        render.setDefaultPostAction(postAction);
        render.setJcrSessionFactory(jcrSessionFactory);
        render.setLoggingService(loggingService);
        render.setRenderService(renderService);
        render.setSettingsBean(settingsBean);
        render.setTemplateService(templatesService);
        render.init(config);
    }
}
