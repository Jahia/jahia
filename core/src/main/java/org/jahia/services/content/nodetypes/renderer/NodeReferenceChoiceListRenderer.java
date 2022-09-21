/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
