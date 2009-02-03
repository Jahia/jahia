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

package org.jahia.taglibs.template.cache;

import org.jahia.data.beans.ContentBean;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 5 nov. 2008
 * Time: 15:08:10
 * To change this template use File | Settings | File Templates.
 */
public class AddDependencyTag extends TagSupport {
    private ContentBean bean = null;
    private Set set = null;

    public void setBean(ContentBean bean) {
        this.bean = bean;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    @Override
    public int doStartTag() throws JspException {
        if (set == null) {
            final CacheTag tag = (CacheTag) findAncestorWithClass(this, CacheTag.class);
            if (tag != null)
                set = tag.getDependencies();
        }
        if (set != null) {
            set.add(bean.getContentObject().getObjectKey());
        }
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        bean = null;
        set = null;
        return super.doEndTag();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
