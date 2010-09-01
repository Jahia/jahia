package org.jahia.services.content;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Special case of a REFERENCE or WEAKREFERENCE value that can store a String identifier that's not necessarily a
 * UUID.
 *
 * @author loom
 *         Date: Sep 1, 2010
 *         Time: 3:26:42 PM
 */
public class ExternalReferenceValue implements Value {

    private final int type;
    private final String identifier;

    public ExternalReferenceValue(String identifier, int type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return identifier;
    }

    public InputStream getStream() throws RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to stream !");
    }

    public Binary getBinary() throws RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to binary !");
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to long !");
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to double !");
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to decimal !");
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to date !");
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to boolean !");
    }

    public int getType() {
        return type;
    }
}
