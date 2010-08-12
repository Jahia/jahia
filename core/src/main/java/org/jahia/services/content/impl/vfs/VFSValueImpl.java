package org.jahia.services.content.impl.vfs;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:04:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSValueImpl implements Value {

    Object value;
    int type;

    public VFSValueImpl(String value) {
        this(value, PropertyType.STRING);
    }

    public VFSValueImpl(Binary value) {
        this(value, PropertyType.BINARY);
    }

    public VFSValueImpl(long value) {
        this(new Long(value), PropertyType.LONG);
    }

    public VFSValueImpl(double value) {
        this(new Double(value), PropertyType.DOUBLE);
    }

    public VFSValueImpl(BigDecimal value) {
        this(value, PropertyType.DECIMAL);
    }

    public VFSValueImpl(Calendar value) {
        this(value, PropertyType.DATE);
    }

    public VFSValueImpl(boolean value) {
        this(new Boolean(value), PropertyType.BOOLEAN);
    }

    public VFSValueImpl(Node value, boolean weakReference) throws RepositoryException {
        if (weakReference) {
            this.value = value.getIdentifier();
            this.type = PropertyType.WEAKREFERENCE;
        } else {
            this.value = value.getIdentifier();
            this.type = PropertyType.REFERENCE;
        }
    }

    public VFSValueImpl(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.toString();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getStream() throws RepositoryException {
        return (InputStream) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Binary getBinary() throws RepositoryException {
        return (Binary) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return ((Long) value).longValue();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return ((Double) value).doubleValue();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return (BigDecimal) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return (Calendar) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return ((Boolean) value).booleanValue();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getType() {
        return type;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
