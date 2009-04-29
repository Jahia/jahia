/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
