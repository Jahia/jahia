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

import org.apache.commons.vfs.FileObject;
import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:47:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSNodeIteratorImpl extends RangeIteratorImpl implements NodeIterator {
    private VFSSessionImpl session;

    public VFSNodeIteratorImpl(VFSSessionImpl session, Iterator iterator, long size) {
        super(iterator, size);
        this.session = session;
    }

    public Node nextNode() {
        FileObject object = (FileObject) next();
        return new VFSNodeImpl(object, session);
    }
}
