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
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.bin.Jahia;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.registries.ServicesRegistry;

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
import java.util.*;
import java.net.MalformedURLException;

/**
 * This class uses the standard request dispatcher to execute a JSP script.
 *
 * It will try to resolve the JSP from the following schema :
 *
 * /templates/[currentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 * /templates/[parentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 * /templates/default/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].jsp
 *
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

    /**
     * Builds the script, tries to resolve the jsp template
     * @param resource resource to display
     * @param context
     * @throws IOException if template cannot be found, or something wrong happens
     */
    public RequestDispatcherScript(Resource resource, RenderContext context) throws IOException {
        try {
            String templatePath = getTemplatePath(resource);
            if (templatePath == null) {
                throw new IOException("Template not found for : "+resource);
            } else {
            	if (logger.isDebugEnabled()) {
            		logger.debug("Template '" + templatePath + "' resolved for resource: " + resource);
            	}
            }

            this.request = context.getRequest();
            this.response = context.getResponse();
            this.templatePath = templatePath;
            rd = request.getRequestDispatcher(templatePath);

        } catch (RepositoryException e) {            
            e.printStackTrace();
            throw new IOException();
        }
    }

    private String getTemplatePath(Resource resource) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();

        String templatePath;

        for (String template : resource.getTemplates()) {
            List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
            nodeTypeList.add(nt);
            Collections.reverse(nodeTypeList);
            for (ExtendedNodeType st : nodeTypeList) {
                List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackagesForModule(st.getName().replace(":","_"));
                SortedSet<JahiaTemplatesPackage> sortedPackages = new TreeSet<JahiaTemplatesPackage>(new PackageComparator());
                sortedPackages.addAll(packages);

                for (JahiaTemplatesPackage aPackage : sortedPackages) {
                    String currentTemplatePath = aPackage.getRootFolderPath();
                    templatePath = getTemplatePath(resource.getTemplateType(), template, st, currentTemplatePath);
                    if (templatePath != null) {
                        return templatePath;
                    }
                }
            }
        }

        return null;
    }

    private String getTemplatePath(String templateType, String template, ExtendedNodeType nt, String currentTemplatePath) {
        String templatePath = nt.getLocalName() + ( template.equals("default")?"":"." + template )  + ".jsp";
        String modulePath = currentTemplatePath + "/" + nt.getAlias().replace(':','_') + "/" + templateType +   "/" + templatePath;
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
     * @return the rendered resource
     * @throws IOException
     */
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
            logger.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        if(isWriter[0]) {
            return stringWriter.getBuffer().toString();
        } else {
            String s = outputStream.toString("UTF-8");
            return s;
        }

    }

    /**
     * Return printable information about the script : type, localization, file, .. in order to help
     * template developer to find the original source of the script
     *
     * @return
     */
    public String getInfo() {
        return "JSP dispatch : " + templatePath;
    }

    class PackageComparator implements Comparator<JahiaTemplatesPackage> {
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            if (o1.getRootFolder().endsWith("default")) return 99;
            if (o2.getRootFolder().endsWith("default")) return -99;
            return o1.getName().compareTo(o2.getName());
        }
    }
}
