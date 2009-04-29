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
package org.jahia.taglibs.template.field;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.beans.ContainerBean;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class AbstractFieldTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(AbstractFieldTag.class);

    protected JahiaField loadField(final String fieldName, final String containerName) throws JahiaException {

        // checks if in declaration phase or if there are already container
        JahiaContainer container = null;
        // checks if we are inside a container
        ContainerTag parent = null;
        if (containerName == null || containerName.length() == 0) {
            parent = (ContainerTag) findAncestorWithClass(this, ContainerTag.class);
            if (parent != null) {
                container = parent.getContainer();
            }
        } else {
            ContainerBean containerBean = (ContainerBean) pageContext.findAttribute(containerName);
            if (containerBean != null) {
                container = containerBean.getJahiaContainer();
            }
        }

        JahiaField theField = null;
        if (parent == null && container == null) {
            logger.warn("Field not found since its parent container cannot be found. This tag has to be used inside a container tag");
        } else if (parent == null) {
            theField = container.getField(fieldName);
        } else {
            if (parent.displayBody()) {
                // must display the field of the current container
                if (container != null) {
                    theField = container.getField(fieldName);
                }
            }
        }
        return theField;
    }
}
