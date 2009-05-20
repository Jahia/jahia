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
package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileContent;
import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 24, 2008
 * Time: 4:03:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSContentNodeIteratorImpl extends RangeIteratorImpl implements NodeIterator {
    private VFSSessionImpl session;

    public VFSContentNodeIteratorImpl(VFSSessionImpl session, FileContent c) {
        super(Arrays.asList(c).iterator(), 1);
        this.session = session;
    }

    public Node nextNode() {
        return new VFSContentNodeImpl(session, (FileContent) next());
    }
}
