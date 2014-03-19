/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
