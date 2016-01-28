package org.apache.felix.http.base.internal.commands;

import org.apache.felix.http.base.internal.handler.HandlerRegistry;
import org.apache.felix.http.base.internal.handler.ServletHandler;
import org.apache.felix.http.base.internal.service.HttpServiceImpl;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.service.http.HttpService;

import java.lang.reflect.Field;

/**
 * Created by loom on 06.01.16.
 */
@Command(scope = "jahia", name = "servlets", description="List registered servlets in OSGi Http service")
@Service
public class ServletListCommand implements Action {

    @Reference
    private HttpService httpService;

    @Override
    public Object execute() throws Exception {
        ShellTable table = new ShellTable();
        table.column(new Col("Id"));
        table.column(new Col("Alias"));
        table.column(new Col("Servlet"));
        table.column(new Col("Servlet-Name"));
        table.column(new Col("Servlet-Info"));
        table.column(new Col("Servlet-Context-Path"));

        if (httpService instanceof HttpServiceImpl) {
            HttpServiceImpl httpServiceImpl = (HttpServiceImpl) httpService;
            Field handlerRegistryField = httpServiceImpl.getClass().getDeclaredField("handlerRegistry");
            handlerRegistryField.setAccessible(true);
            HandlerRegistry handlerRegistry = (HandlerRegistry) handlerRegistryField.get(httpServiceImpl);
            for (ServletHandler servletHandler : handlerRegistry.getServlets()) {
                table.addRow().addContent(servletHandler.getId(),
                        servletHandler.getAlias(),
                        servletHandler.getServlet().getClass(),
                        servletHandler.getServlet().getServletConfig().getServletName(),
                        servletHandler.getServlet().getServletInfo(),
                        servletHandler.getServlet().getServletConfig().getServletContext().getContextPath());
            }
            table.print(System.out, true);
        } else {
            // unknown http service implementation
        }

        return null;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }
}
