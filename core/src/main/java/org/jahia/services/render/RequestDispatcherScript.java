/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render;

import org.jahia.data.beans.TemplatePathResolverFactory;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.bin.Jahia;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class RequestDispatcherScript implements Script {

    private RequestDispatcher rd;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public RequestDispatcherScript(Resource resource, HttpServletRequest request, HttpServletResponse response) throws IOException {
        TemplatePathResolverFactory factory = (TemplatePathResolverFactory) SpringContextSingleton.getInstance().getContext().getBean("TemplatePathResolverFactory");
        ProcessingContext threadParamBean = Jahia.getThreadParamBean();
        TemplatePathResolverBean templatePathResolver = factory.getTemplatePathResolver(threadParamBean);

        try {
            ExtendedNodeType nt = (ExtendedNodeType) resource.getNode().getPrimaryNodeType();
            String templatePath = getTemplatePath(resource, templatePathResolver, nt);

            if (templatePath == null) {
                List<ExtendedNodeType> nodeTypeList = Arrays.asList(nt.getSupertypes());
                Collections.reverse(nodeTypeList);
                for (ExtendedNodeType st : nodeTypeList) {
                    templatePath = getTemplatePath(resource, templatePathResolver, st);
                    if (templatePath != null) {
                        break;
                    }
                }
            }
            if (templatePath == null) {
                throw new IOException("template not found");
            }

            this.request = request;
            this.response = response;

            rd = request.getRequestDispatcher(templatePath);

        } catch (RepositoryException e) {            
            throw new IOException();
        }
    }

    private String getTemplatePath(Resource resource, TemplatePathResolverBean templatePathResolver, ExtendedNodeType nt) {
        return templatePathResolver.lookup("modules/" +
                nt.getAlias().replace(':','/') +
                "/" + resource.getExtension() +
                "/" + resource.getTemplate() + ".jsp");
    }

    public String execute() throws IOException {
        final boolean[] isWriter = new boolean[1];
        final StringWriter stringWriter = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        try {
            rd.include(request, new HttpServletResponseWrapper(response) {
                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return new ServletOutputStream() {
                        @Override
                        public void write(int i) throws IOException {
                            outputStream.write(i);
                        }
                    };
                }

                public PrintWriter getWriter() throws IOException {
                    isWriter[0] = true;
                    return new PrintWriter(stringWriter);
                }
            });
        } catch (ServletException e) {
            e.printStackTrace();
            throw new IOException();
        }
        if(isWriter[0]) {
            return stringWriter.getBuffer().toString();
        } else {
            String s = outputStream.toString("UTF-8");
            return s;
        }

    }
}
