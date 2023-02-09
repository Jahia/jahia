/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
        if (pageContext.getAttribute(getVar(), scope) == null) {
            pageContext.setAttribute(getVar(), getConstants(), scope);
        }
        return SKIP_BODY;
    }

    private Map<?, ?> getConstants() throws JspTagException {
        Map<?, ?> constants = null;
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
