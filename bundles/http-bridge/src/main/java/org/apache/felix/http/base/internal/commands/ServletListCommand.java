/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
