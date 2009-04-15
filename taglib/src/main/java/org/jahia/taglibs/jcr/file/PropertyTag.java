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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.jcr.file;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.services.content.nodetypes.NodeTypeRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 déc. 2007
 * Time: 18:19:55
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PropertyTag extends TagSupport {

    private String ntname;
    private String propname;

    private String id = "propertyDefinition";

    public String getNtname() {
        return ntname;
    }

    public void setNtname(String ntname) {
        this.ntname = ntname;
    }

    public String getPropname() {
        return propname;
    }

    public void setPropname(String propname) {
        this.propname = propname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int doStartTag() throws JspException {
        if (ntname != null && propname != null) {
            try {
                NodeType type = NodeTypeRegistry.getInstance().getNodeType(ntname);
                PropertyDefinition[] pds = type.getDeclaredPropertyDefinitions();
                for (int i = 0; i < pds.length; i++) {
                    PropertyDefinition pd = pds[i];
                    if (pd.getName().equals(propname)) {
                        pageContext.setAttribute(id, pd);
                        return EVAL_BODY_INCLUDE;
                    }
                }
                NodeDefinition[] nds = type.getDeclaredChildNodeDefinitions();
                for (int i = 0; i < nds.length; i++) {
                    NodeDefinition nd = nds[i];
                    if (nd.getName().equals(propname)) {
                        pageContext.setAttribute(id, nd);
                        return EVAL_BODY_INCLUDE;
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                return SKIP_BODY;
            }
        }

        return EVAL_BODY_INCLUDE;
    }

}
