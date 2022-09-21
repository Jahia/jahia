/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
