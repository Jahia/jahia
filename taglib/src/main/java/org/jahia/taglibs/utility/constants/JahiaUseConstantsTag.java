/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * 
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
