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
package org.jahia.taglibs.template.cache;

import org.jahia.content.ContentObjectKey;
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
@SuppressWarnings("serial")
public class AddDependencyTag extends TagSupport {
    private ContentBean bean = null;
    private Set<ContentObjectKey> set = null;

    public void setBean(ContentBean bean) {
        this.bean = bean;
    }

    public void setSet(Set<ContentObjectKey> set) {
        this.set = set;
    }

    @Override
    public int doStartTag() throws JspException {
        if (set == null) {
            final CacheTag tag = (CacheTag) findAncestorWithClass(this, CacheTag.class);
            if (tag != null)
                set = (Set<ContentObjectKey>)tag.getDependencies();
        }
        if (set != null) {
            set.add((ContentObjectKey)bean.getContentObject().getObjectKey());
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
