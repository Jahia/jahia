/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.taglibs.template.users;

import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a helper tag that will only display the body if the user property is public and the current user is not
 * the displayed user.
 *
 * @author loom
 *         Date: Sep 18, 2009
 *         Time: 8:31:43 AM
 */
public class IsPublicTag extends BodyTagSupport {
    private String userKey;
    private String property;
    private Boolean invert;

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setInvert(Boolean invert) {
        this.invert = invert;
    }

    @Override
    public int doStartTag() throws JspException {
        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        if (renderContext == null) {
            //final JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
            //renderContext = new RenderContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), jData.getProcessingContext().getUser());
            //renderContext.setSite(jData.getProcessingContext().getSite());
        }
        JahiaUser currentUser = renderContext.getUser();
        JahiaUser displayedUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userKey);
        if (currentUser.equals(displayedUser)) {
            // in the case of the current user we always return true.
            return super.doStartTag();
        }
        String displayPropertyPrivate = displayedUser.getProperty(property + "Public");
        boolean willExecuteBody = true;
        if (displayPropertyPrivate != null) {
            Boolean displayPropertyBoolean = new Boolean(displayPropertyPrivate);
            if (displayPropertyBoolean.booleanValue()) {
                willExecuteBody = true;
            } else {
                willExecuteBody = false;
            }
        }
        if (invert != null) {
            if (invert.booleanValue()) {
                willExecuteBody = !willExecuteBody;
            }
        }
        if (willExecuteBody) {
            return super.doStartTag();
        } else {
            return SKIP_BODY;
        }
    }

    @Override
    public int doAfterBody() throws JspException {
      try {
         BodyContent bodycontent = getBodyContent();
         String body = bodycontent.getString();
         JspWriter out = bodycontent.getEnclosingWriter();
         if(body != null) {
            out.print(body);
         }
      } catch(IOException ioe) {
         throw new JspException("Error:"+ioe.getMessage());
      }
      return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        userKey = null;
        property = null;
        return super.doEndTag();
    }

}
