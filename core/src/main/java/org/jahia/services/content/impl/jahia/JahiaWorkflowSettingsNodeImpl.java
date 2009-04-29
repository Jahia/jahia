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
package org.jahia.services.content.impl.jahia;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jahia.content.ContentObject;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 3, 2008
 * Time: 11:27:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaWorkflowSettingsNodeImpl extends NodeImpl {
    private ContentObject object;
    private Node parent;

    public JahiaWorkflowSettingsNodeImpl(SessionImpl session, Node parent, ContentObject object) {
        super(session);
        this.object = object;
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();

            // todo type
        }
    }
}
