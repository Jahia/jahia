/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.content.decorator;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.LazyPropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for the JCR nodes of type <code>jnt:passwordHistoryEntry</code>.
 * 
 * @author Sergiy Shyrkov
 */
public class JCRPasswordHistoryEntryNode extends JCRNodeDecorator {

    class FilteredIterator extends FilteredPropertyIterator {

        public FilteredIterator(PropertyIterator propertyIterator) {
            super(propertyIterator);
        }

        @Override
        boolean isFiltered(String s) throws RepositoryException {
            return !canGetProperty(s);
        }
    }

    private transient static Logger logger = LoggerFactory.getLogger(JCRPasswordHistoryEntryNode.class);

    private static final String PROTECTED_PROPERTY = "j:password";

    public JCRPasswordHistoryEntryNode(JCRNodeWrapper node) {
        super(node);
    }

    private boolean canGetProperty(String propertyName) throws RepositoryException {
        return getSession().isSystem() || !PROTECTED_PROPERTY.equals(propertyName);
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        if (getSession().isSystem()) {
            // no filtering for system sessions
            return super.getProperties();
        }
        return new LazyPropertyIterator(this, getSession().getLocale()) {
            @Override
            protected PropertyIterator getI18NPropertyIterator() {
                if (i18nPropertyIterator == null) {
                    return new FilteredIterator(super.getI18NPropertyIterator());
                }
                return i18nPropertyIterator;
            }

            @Override
            protected PropertyIterator getPropertiesIterator() {
                if (propertyIterator == null) {
                    propertyIterator = new FilteredIterator(super.getPropertiesIterator());
                }
                return propertyIterator;
            }
        };
    }

    @Override
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        Map<String, String> props = super.getPropertiesAsString();
        if (getSession().isSystem() || !props.containsKey(PROTECTED_PROPERTY)) {
            return props;
        }
        Map<String, String> filtered = new HashMap<String, String>(props);
        filtered.remove(PROTECTED_PROPERTY);

        return filtered;
    }

    @Override
    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        if (!canGetProperty(s)) {
            throw new PathNotFoundException(s);
        }

        return super.getProperty(s);
    }

    @Override
    public String getPropertyAsString(String name) {
        try {
            return canGetProperty(name) ? super.getPropertyAsString(name) : null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean hasProperty(String s) throws RepositoryException {
        return super.hasProperty(s) && canGetProperty(s);
    }
}
