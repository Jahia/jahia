/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

    protected JahiaField loadField(final String fieldName, String containerName) throws JahiaException {

        // checks if in declaration phase or if there are already container
        JahiaContainer container = null;
        // checks if we are inside a container
        ContainerTag parent = null;
        if (containerName == null || containerName.length() == 0) {
            parent = (ContainerTag) findAncestorWithClass(this, ContainerTag.class);
            if (parent != null) {
                container = parent.getContainer();
            }
        }
        if (parent == null && container == null) {
            if (containerName == null || containerName.length() == 0) {
                containerName = "container";
            }
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
