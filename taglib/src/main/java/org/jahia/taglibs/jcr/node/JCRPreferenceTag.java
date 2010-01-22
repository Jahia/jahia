/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.node;

import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 déc. 2009
 */
public class JCRPreferenceTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 9045514505202851314L;
    private String var;
    private String name;
    private String defaultValue;

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            final JahiaPreferencesService service = ServicesRegistry.getInstance().getJahiaPreferencesService();
            JCRNodeWrapper value = service.getGenericPreferenceNode(name,
                                                                    Jahia.getThreadParamBean().getUser());
            if (value == null && defaultValue !=null) {
                service.setGenericPreferenceValue(name, defaultValue,
                                                  Jahia.getThreadParamBean());
                value = service.getGenericPreferenceNode(name, Jahia.getThreadParamBean().getUser());
            }
            pageContext.setAttribute(var, value);
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        defaultValue = null;
        name = null;
        var = null;
        super.resetState();
    }
}
