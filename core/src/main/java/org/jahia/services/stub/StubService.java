/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.stub;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * Service to provide Stubs for source edition in studio mode
 */
public class StubService {

    private static transient Logger logger = LoggerFactory.getLogger(StubService.class);

    /**
     * @param fileType type of the file
     * @param snippetType type of the snippet
     * @return Map of snippets fpr file type
     */

    private Map<String,String> nodeTypeView;

    public Map<String,String> getCodeSnippets(String fileType, String snippetType) {
        Map<String,String> stub = new LinkedHashMap<String, String>();
        InputStream is = null;
        try {
            ServletContext servletContext = JahiaContextLoaderListener.getServletContext();
            @SuppressWarnings("unchecked")
            Set<String> resources = servletContext.getResourcePaths("/WEB-INF/etc/snippets/"+fileType+"/"+snippetType+"/");
            if (resources != null) {
                for (String resource : resources) {
                    String r = StringUtils.substringAfterLast(resource,"/");
                    String v = StringUtils.substringBeforeLast(StringUtils.substring(r,StringUtils.indexOf(r,".") + 1),".");
                    String view = nodeTypeView.get(v) != null ? "/" + nodeTypeView.get(v) : "";
                    is = servletContext.getResourceAsStream(resource);
                    try {
                        stub.put(r + view, StringUtils.join(IOUtils.readLines(is), "\n"));
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read code snippets from " + fileType + "/" + snippetType, e);
        }
        return stub;
    }

    public void setNodeTypeView(Map<String, String> nodeTypeView) {
        this.nodeTypeView = nodeTypeView;
    }
}
