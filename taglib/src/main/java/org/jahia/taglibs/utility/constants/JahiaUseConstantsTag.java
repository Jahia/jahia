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
package org.jahia.taglibs.utility.constants;

import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.taglibs.unstandard.ClassUtils;
import org.apache.taglibs.unstandard.TagUtils;
import org.apache.taglibs.unstandard.UseConstantsTag;

/**
 * This is a simple wrapper to the UseConstantsTag taken from the jakarta unstandard taglibs
 * to avoid adding too much tld.
 * <a href="http://jakarta.apache.org/taglibs/sandbox/doc/unstandard-doc/index.html#useConstants">javadoc</a>
 * User: hollis
 * Date: 13 févr. 2008
 * Time: 10:38:16
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class JahiaUseConstantsTag extends UseConstantsTag {
    
    @Override
    public int doStartTag() throws JspException {
        int scope = TagUtils.getScope(getScope(), 1);
        Map constants = (Map) pageContext.getAttribute(getVar(), scope);
        if (constants == null) {
            pageContext.setAttribute(getVar(), getConstants(), scope);
        }
        return SKIP_BODY;
    }

    private Map getConstants() throws JspTagException {
        Map constants = null;
        try {
            constants = ClassUtils.getClassConstants(getClassName());
        } catch (ClassNotFoundException e) {
            throw new JspTagException("Class not found: " + getClassName());
        } catch (IllegalArgumentException e) {
            throw new JspTagException("Illegal argument: " + getClassName());
        } catch (IllegalAccessException e) {
            throw new JspTagException("Illegal access: " + getClassName());
        }

        // make the map keys case-insensitive and the map unmodifiable
        return UnmodifiableMap.decorate(new CaseInsensitiveMap(constants));
    }

}
