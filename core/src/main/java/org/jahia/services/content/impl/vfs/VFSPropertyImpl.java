/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileObject;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.Name;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * 
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:28 AM
 * 
 */
public class VFSPropertyImpl extends VFSItemImpl implements Property {

    private Node node;
    private VFSSessionImpl session;
    private Name name;
    private Value[] values;
    private Value value;

    public VFSPropertyImpl(Name name, Node node, VFSSessionImpl session, Value value) {
        this.name = name;
        this.node = node;
        this.session = session;
        this.value = value;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.value = value;
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.values = values;
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        return value;
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return values;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getString();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getStream();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getLong();
        }
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getDouble();
        }
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getDate();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        if (value != null) {
            return value.getBoolean();
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        return node;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType(node.getPrimaryNodeType().getName()).getPropertyDefinition(name.toString());
    }

    public int getType() throws RepositoryException {
        return value.getType();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return value.getBinary();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return value.getDecimal();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isMultiple() throws RepositoryException {
        if (values != null) {
            return true;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
