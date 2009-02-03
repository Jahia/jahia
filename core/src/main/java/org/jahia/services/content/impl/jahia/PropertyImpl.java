/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 16:54:47
 * To change this template use File | Settings | File Templates.
 */
public class PropertyImpl extends ItemImpl implements Property {

    protected Node node;
    protected Value[] values;
    protected ExtendedPropertyDefinition def;

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Value value) {
        super(session);
        this.node = node;
        this.values = new Value[] {value};
        this.def = def;
    }

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Value[] values) {
        super(session);
        this.node = node;
        this.values = values;
        this.def = def;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(String[] strings) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(long l) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(double v) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(Node node) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0];
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return values;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getString();
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getStream();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getLong();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getDouble();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getDate();
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0].getBoolean();
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return def;
    }

    public int getType() throws RepositoryException {
        if (def != null) {
            return def.getRequiredType();
        }
        return 0;
    }

    public String getName() throws RepositoryException {
        if (def != null) {
            return def.getName();
        }
        return null;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

}
