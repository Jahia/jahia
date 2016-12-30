/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.content;

import org.jahia.data.beans.CategoryBean;

import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.Date;

/**
 * This is a wrapper for interface {@link javax.jcr.Value} to allow more types of properties.
 *
 * @author Cedric Mailleux
 * @since 6.1
 */
public interface JCRValueWrapper extends Value {
    /**
     * Returns a <code>CategoryBean</code> instance referred by this value. 
     * 
     * If the value does not match a valid category either <code>ValueFormatException</code> or <code>ItemNotFoundException</code> is thrown. 
     *
     * @return A <code>CategoryBean</code> referred by the value of this
     *         property.
     * @throws ValueFormatException  if conversion to a <code>CategoryBean</code> is
     *                               not possible.
     * @throws RepositoryException   if another error occurs.
     */   
    CategoryBean getCategory() throws ValueFormatException, RepositoryException;
    
    /**
     * Returns the property definition for this value. 
     *
     * @return The <code>PropertyDefinition</code> for the current value.
     * @throws RepositoryException   if another error occurs.
     */       
    PropertyDefinition getDefinition();
    
    /**
     * Returns a <code>Date</code> representation of the <code>Calendar</code>'s time of this value.
     * <p>
     * The object returned is a copy of the stored value, so changes to it are
     * not reflected in internal storage.
     *
     * @return A <code>Date</code> representation of this value.
     * @throws ValueFormatException if conversion to a <code>Date</code> is
     *                              not possible.
     * @throws RepositoryException  if another error occurs.
     */    
    Date getTime() throws ValueFormatException, RepositoryException;
    
    /**
     * Returns a <code>JCRNodeWrapper</code> instance referred by this value. The property has to be one of the following 
     * types <li><code>PropertyType.STRING</code></li> <li><code>PropertyType.REFERENCE</code></li> 
     * <li><code>PropertyType.WEAKREFERENCE</code></li> 
     * 
     * For other types a ValueFormatException is thrown. If the reference in the value cannot be resolved to an existing node, 
     * the method returns null. 
     *
     * @return A <code>JCRNodeWrapper</code> referred by the value of this
     *         property.
     * @throws ValueFormatException  if conversion to a <code>JCRNodeWrapper</code> is
     *                               not possible.
     * @throws IllegalStateException if <code>getStream</code> has previously
     *                               been called on this <code>Value</code> instance. In this case a new
     *                               <code>Value</code> instance must be acquired in order to successfully
     *                               call this method.
     * @throws RepositoryException   if another error occurs.
     */    
    JCRNodeWrapper getNode() throws ValueFormatException, IllegalStateException, RepositoryException;
}
