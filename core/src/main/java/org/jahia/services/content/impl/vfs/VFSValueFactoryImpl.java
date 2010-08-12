package org.jahia.services.content.impl.vfs;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:03:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSValueFactoryImpl implements ValueFactory {
    public Value createValue(String value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(String value, int type) throws ValueFormatException {
        switch (type) {
            case PropertyType.BINARY :
                throw new ValueFormatException("Not allowed to convert string ["+value+"] to binary value");
            case PropertyType.BOOLEAN :
                return createValue(Boolean.parseBoolean(value));
            case PropertyType.DATE :
                Date date = null;
                try {
                    date = DateFormat.getInstance().parse(value);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    return createValue(calendar);
                } catch (ParseException e) {
                    throw new ValueFormatException("Error converting value [" + value +"] to calendar value");
                }                
            case PropertyType.DECIMAL :
                return createValue(new BigDecimal(value));
            case PropertyType.DOUBLE :
                return createValue(Double.parseDouble(value));
            case PropertyType.LONG :
                return createValue(Long.parseLong(value));
            case PropertyType.REFERENCE :
                return new VFSValueImpl(value, PropertyType.REFERENCE);
            case PropertyType.STRING :
                return createValue(value);
            case PropertyType.WEAKREFERENCE :
                return new VFSValueImpl(value, PropertyType.WEAKREFERENCE);
        }
        throw new ValueFormatException("Unsupported value type " + type);
    }

    public Value createValue(long value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(double value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(BigDecimal value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(boolean value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(Calendar value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(InputStream value) {
        return new VFSValueImpl(new VFSBinaryImpl(value));
    }

    public Value createValue(InputStream value, long size) {
        return new VFSValueImpl(new VFSBinaryImpl(value));
    }

    public Value createValue(Binary value) {
        return new VFSValueImpl(value);
    }

    public Value createValue(Node value) throws RepositoryException {
        return new VFSValueImpl(value, false);
    }

    public Value createValue(Node value, boolean weak) throws RepositoryException {
        return new VFSValueImpl(value, weak);
    }

    public Binary createBinary(InputStream stream) throws RepositoryException {
        return new VFSBinaryImpl(stream);  //To change body of implemented methods use File | Settings | File Templates.
    }

}
