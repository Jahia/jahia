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
package org.jahia.services.content.nodetypes.renderer;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderContext;

/**
 * Renderer that is used to display a title or name of the referenced node, e.g. a category or a tag.
 * 
 * @author Sergiy Shyrkov
 */
public class NodeReferenceChoiceListRenderer extends AbstractChoiceListRenderer {

    /* (non-Javadoc)
     * @see org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer#getStringRendering(org.jahia.services.render.RenderContext, org.jahia.services.content.JCRPropertyWrapper)
     */
    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        Value[] values = propertyWrapper.isMultiple() ? propertyWrapper.getValues() : new Value[] { propertyWrapper
                .getValue() };
        List<String> displayValues = new LinkedList<String>();
        if (values != null && values.length > 0) {
            for (Value value : values) {
                if (value != null) {
                    JCRNodeWrapper node = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (node != null) {
                        String title = null;
                        if (node.hasProperty(Constants.JCR_TITLE)) {
                            title = node.getProperty(Constants.JCR_TITLE).getString();
                        }
                        displayValues.add(StringUtils.isNotEmpty(title) ? title : node.getName());
                    }
                }
            }
        }
        return StringUtils.join(displayValues, ", ");
    }

    public String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef,
            Object propertyValue) throws RepositoryException {
        String displayValue = "";
        JCRNodeWrapper node = (JCRNodeWrapper) propertyValue;
        if (node != null) {
            String title = null;
            if (node.hasProperty(Constants.JCR_TITLE)) {
                title = node.getProperty(Constants.JCR_TITLE).getString();
            }
            displayValue = StringUtils.isNotEmpty(title) ? title : node.getName();
        }
        return displayValue;
    }
}
