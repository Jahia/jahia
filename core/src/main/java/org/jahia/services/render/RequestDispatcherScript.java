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

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

/**
 * This class uses the standard request dispatcher to execute a JSP script.
 * <p/>
 * It will try to resolve the JSP from the following schema :
 * <p/>
 * /templates/[currentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 * /templates/[parentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 * /templates/default/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 * <p/>
 * And then iterates on the supertype of the resource, until nt:base
 *
 * @author toto
 */
public class RequestDispatcherScript implements Script {

    private static final Logger logger = Logger.getLogger(RequestDispatcherScript.class);
    

    private RequestDispatcher rd;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String templatePath;
    private String templateName;
    private Template template;
    private JahiaTemplatesPackage module;

    /**
     * Builds the script, tries to resolve the jsp template
     *
     * @param resource resource to display
     * @param context
     * @throws IOException if template cannot be found, or something wrong happens
     */
    public RequestDispatcherScript(Resource resource, RenderContext context) throws IOException {
        try {
            resolveTemplatePath(resource, context);
            if (templatePath == null) {
                throw new IOException("Template not found for : " + resource);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Template '" + templatePath + "' resolved for resource: " + resource);
                }
            }

            template = new RequestDispatcherTemplate(templatePath, templateName, module, templateName);

            this.request = context.getRequest();
            this.response = context.getResponse();
            rd = request.getRequestDispatcher(templatePath);

        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    private void resolveTemplatePath(Resource resource, final RenderContext context) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();

        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(
                nt.getSupertypes()));
        nodeTypeList.add(nt);
        Collections.reverse(nodeTypeList);
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        nodeTypeList.remove(base);
        nodeTypeList.add(base);
        if (resource.getWrappedMixinType() == null) {
            for (String template : resource.getTemplates()) {
                for (ExtendedNodeType st : nodeTypeList) {
                    SortedSet<JahiaTemplatesPackage> sortedPackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getSortedAvailableTemplatePackagesForModule(
                            st.getAlias().replace(":", "_"), context);
                    for (JahiaTemplatesPackage aPackage : sortedPackages) {
                        String currentTemplatePath = aPackage.getRootFolderPath();
                        templatePath = getTemplatePath(resource.getTemplateType(), template, st, currentTemplatePath);
                        if (templatePath != null) {
                            module = aPackage;
                            templateName = template;
                            return;
                        }
                    }
                }
            }
        } else {
            for (String template : resource.getTemplates()) {
                SortedSet<JahiaTemplatesPackage> sortedPackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getSortedAvailableTemplatePackagesForModule(
                        resource.getWrappedMixinType().getAlias().replace(":", "_"), context);
                for (JahiaTemplatesPackage aPackage : sortedPackages) {
                    String currentTemplatePath = aPackage.getRootFolderPath();
                    templatePath = getTemplatePath(resource.getTemplateType(), template, resource.getWrappedMixinType(),
                                                   currentTemplatePath);
                    if (templatePath != null) {
                        module = aPackage;
                        return;
                    }
                }

            }
        }
    }

    private String getTemplatePath(String templateType, String template, ExtendedNodeType nt, String currentTemplatePath) {
        String n = nt.getAlias();
        if (nt.getPrefix().length() > 0) {
            n = n.substring(nt.getPrefix().length() + 1);
        }

        String templatePath = n + (template.equals("default") ? "" : "." + template) + ".jsp";
        String modulePath = currentTemplatePath + "/" + nt.getAlias().replace(':', '_') + "/" + templateType + "/" + templatePath;
        try {
            if (Jahia.getStaticServletConfig().getServletContext().getResource(modulePath) != null) {
                return modulePath;
            }
        } catch (MalformedURLException e) {
        }
        return null;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @return the rendered resource
     * @throws RenderException
     */
    public String execute() throws RenderException {
        final boolean[] isWriter = new boolean[1];
        final StringWriter stringWriter = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        Object oldModule = request.getAttribute("currentModule");
        request.setAttribute("currentModule",getTemplate().getModule());       
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
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule",oldModule);
        }
        if (isWriter[0]) {
            return stringWriter.getBuffer().toString();
        } else {
            try {
                String s = outputStream.toString("UTF-8");
                return s;
            } catch (IOException e) {
                throw new RenderException(e);
            }
        }

    }

    /**
     * Return template information associated to this script
     *
     * @return
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Return the module where the script comes from
     *
     * @return the module name
     */
    public JahiaTemplatesPackage getModule() {
        return module;
    }
}
