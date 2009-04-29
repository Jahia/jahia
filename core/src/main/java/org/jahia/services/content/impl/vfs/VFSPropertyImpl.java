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

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSPropertyImpl extends VFSItemImpl implements Property {
    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return new Value[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getType() throws RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
