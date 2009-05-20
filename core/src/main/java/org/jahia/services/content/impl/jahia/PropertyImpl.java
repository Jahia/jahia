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

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

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
    protected String name;

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Value value) {
        super(session);
        this.node = node;
        this.values = new Value[] {value};
        this.def = def;
    }

    public PropertyImpl(SessionImpl session, Node node, String name, ExtendedPropertyDefinition def, Value value) {
        super(session);
        this.node = node;
        this.values = new Value[] {value};
        this.name = name;
        this.def = def;
    }

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Value[] values) {
        super(session);
        this.node = node;
        this.values = values;
        this.def = def;
    }

    public PropertyImpl(SessionImpl session, Node node, String name, ExtendedPropertyDefinition def, Value[] values) {
        super(session);
        this.node = node;
        this.values = values;
        this.name = name;
        this.def = def;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(s, PropertyType.STRING));
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
        if (name != null) {
            return name;
        }
        if (def != null) {
            return def.getName();
        }
        return null;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

}
