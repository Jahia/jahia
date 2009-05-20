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
        setValueID("container");
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
        setValueID("container");
    }
}
