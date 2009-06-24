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
package org.jahia.taglibs.template.container;

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRJahiaContentNode;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.data.beans.ContainerBean;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

/**
 * Allows to retrieve a specific container displayed on the current page, knowing its container id.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class GetContainerTag extends ValueJahiaTag implements ContainerSupport {

    private static transient final Logger logger = Logger.getLogger(GetContainerTag.class);

    private int containerID = -1;
    private String path = null;
    
    
    /**
     * Initializes an instance of this class.
     */
    public GetContainerTag() {
        super();
        setVar("container");
    }

    public int getContainerID() {
        return containerID;
    }

    public void setContainerID(int containerID) {
        this.containerID = containerID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int doStartTag() {
        pushTag();
        try {
            final ProcessingContext jParams = getProcessingContext();

            ContentContainer ctn = null;
            if (containerID != -1) {
                ctn = ContentContainer.getContainer(containerID);
            } else if (path != null) {
                try {
                    JCRNodeWrapper nodeWrapper = JCRStoreService.getInstance().getThreadSession(jParams.getUser()).getNode(getPath());
                    if (nodeWrapper.isValid() && nodeWrapper instanceof JCRJahiaContentNode) {
                        JCRJahiaContentNode jahiaContentNode = (JCRJahiaContentNode) nodeWrapper;
                        ctn = (ContentContainer) jahiaContentNode.getContentObject();
                    }
                } catch (Exception e) {
                    logger.error("Cannot use path",e);
                }
            }
            if (ctn == null) return SKIP_BODY;

            final ContainerBean ctnBean = new ContainerBean(ctn.getJahiaContainer(jParams, jParams.getEntryLoadRequest()), jParams);
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), ctnBean);
            }
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), ctnBean);
            }

        } catch (Exception e) {
            logger.error("Error in GetContainerTag", e);
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        resetState();
        popTag();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
        containerID = -1;
        path = null;
        setVar("container");
    }
}
