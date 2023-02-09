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
package org.jahia.taglibs.query;

import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.servlet.jsp.JspTagException;

import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.jcr.query.JQOMTag;

/**
 * Superclass for tags, which are nested inside {@link QueryDefinitionTag} or {@link JQOMTag},
 * used for building queries using QOM.
 *
 * @author Sergiy Shyrkov
 */
public abstract class QOMBuildingTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 1651349341403605347L;

    private String selectorName;

    private QOMBuilder qomBuilder;

    /**
     * Returns an instance of the current {@link QOMBuilder} or throws an
     * exception if the parent {@link QueryDefinitionTag} or {@link JQOMTag} is not found.
     *
     * @return an instance of the current {@link QOMBuilder} or throws an
     * exception if the parent {@link QueryDefinitionTag} or {@link JQOMTag} is not found
     * @throws JspTagException in case the parent {@link QueryDefinitionTag} or {@link JQOMTag}
     *             cannot be found
     */
    protected final QOMBuilder getQOMBuilder() throws JspTagException {
        if (qomBuilder == null) {
            QueryDefinitionTag tag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
            if (tag == null) {
                throw new JspTagException("The tag " + this.getClass().getName() + " must be nested inside "
                        + QueryDefinitionTag.class.getName() + " tag.");
            }
            qomBuilder = tag.getQOMBuilder();
        }

        return qomBuilder;
    }

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     */
    protected final QueryObjectModelFactory getQOMFactory() throws JspTagException {
        return getQOMBuilder().getQOMFactory();
    }

    protected String getSelectorName() {
        if (selectorName == null) {
            Source source;
            try {
                source = getQOMBuilder().getSource();
                if (source != null) {
                    if (source instanceof Selector) {
                        selectorName = ((Selector) source).getSelectorName();
                    } else {
                        throw new IllegalAccessError(
                                "Need to specify the selector name because the query contains more than one selector.");
                    }
                }
            } catch (JspTagException e) {
                throw new IllegalArgumentException("Unable to detect the selectorName value, as no parent "
                        + QueryDefinitionTag.class.getName() + " was found");
            }
        }

        return selectorName;
    }

    @Override
    protected void resetState() {
        selectorName = null;
        qomBuilder = null;
        super.resetState();
    }

    /**
     * Sets the node selector name.
     *
     * @param selectorName the node selector name
     */
    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }
}
