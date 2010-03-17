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
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class AreaTag extends ModuleTag implements ParamParent {

    private static Logger logger = Logger.getLogger(AreaTag.class);

    private String areaType = "jnt:contentList";
    private boolean forceCreation;

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public void setForceCreation(boolean forceCreation) {
        this.forceCreation = forceCreation;
    }

    @Override
    protected String getModuleType() throws RepositoryException {
        return "area";
    }

    @Override
    protected void missingResource(RenderContext renderContext, Resource currentResource) throws IOException {
        try {
            if (renderContext.isEditMode() || forceCreation) {
                JCRSessionWrapper session = currentResource.getNode().getSession();
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if(!nodeWrapper.isCheckedOut())
                        nodeWrapper.checkout();
                    node = nodeWrapper.addNode(path, areaType);
                    session.save();
                } else {

                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    if(!parent.isCheckedOut())
                        parent.checkout();
                    node = parent.addNode(StringUtils.substringAfterLast(path, "/"), areaType);
                    session.save();
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot create area",e);
        }
    }

}