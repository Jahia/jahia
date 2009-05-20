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
package org.jahia.services.content.nodetypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.util.ISO8601;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 20, 2008
 * Time: 10:53:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ValueImpl implements Value {
    
    private static final Logger logger = Logger.getLogger(ValueImpl.class);
    
    protected String value;
    protected int type;

    public ValueImpl(String v, int type) {
        this(v, type, false);
    }

    public ValueImpl(Calendar c) {
        this(ISO8601.format(c), PropertyType.DATE);
    }

    public ValueImpl(boolean b) {
        this(Boolean.toString(b), PropertyType.BOOLEAN);
    }

    public ValueImpl(long b) {
        this(Long.toString(b), PropertyType.LONG);
    }

    public ValueImpl(double d) {
        this(Double.toString(d), PropertyType.DOUBLE);
    }

    public ValueImpl(String v, int type, boolean isConstraint) {
        this.value = v;
        this.type = type;

        if (value != null && !isConstraint) {
            switch (type) {
                case PropertyType.LONG :
                    this.value = Long.toString(new Double(value).longValue());
                    break;
            }
        }

        if (value != null && isConstraint) {
            switch (type) {
                // handle constant constraint
                case PropertyType.LONG :
                case PropertyType.DOUBLE :
                case PropertyType.DATE :
                case PropertyType.BINARY :
                    if (value.indexOf(',') == -1) value = "["+value+","+value+"]";
            }
        }

    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value;
    }

    public InputStream getStream() throws IllegalStateException, RepositoryException {
        String s = getString();
        try {
            return new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return new ByteArrayInputStream(s.getBytes());
        }
    }

    public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
        if (getType() == PropertyType.DATE) {
            return getDate().getTimeInMillis();
        } else {
            String s = getString();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ValueFormatException(s);
            }
        }
    }

    public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
        String s = getString();
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new ValueFormatException(s);
        }
    }

    public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
        return ISO8601.parse(getString());
    }

    public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
        String s = getString();
        try {
            return Boolean.parseBoolean(s);
        } catch (NumberFormatException e) {
            throw new ValueFormatException(s);
        }
    }

    public int getType() {
        return type;
    }

    public boolean checkConstraint(String value) throws ValueFormatException, IllegalStateException, RepositoryException {
        if (value == null || value.length() == 0) {
            return true;
        }
        try {
            switch (type) {
                case PropertyType.STRING:
                    Pattern p = Pattern.compile(getString());
                    return p.matcher(value).matches();
                case PropertyType.LONG:
                case PropertyType.DOUBLE:
                case PropertyType.DATE:
                    String s = getString();
                    boolean lowerBoundIncluded = s.charAt(0) == '[';
                    boolean upperBoundIncluded = s.charAt(s.length()-1) == ']';
                    String lowerBound = s.substring(1,s.indexOf(','));
                    String upperBound = s.substring(s.indexOf(',')+1, s.length()-1);
                    switch (type) {
                        case PropertyType.LONG:
                        case PropertyType.DOUBLE: {
                            double v = Double.parseDouble(value);
                            double l = Double.parseDouble(lowerBound);
                            double u = Double.parseDouble(upperBound);
                            return (lowerBoundIncluded?v>=l:v>l) && (upperBoundIncluded?v<=u:v<u);
                        }
                        case PropertyType.DATE: {
                            long v = Long.parseLong(value);
                            long l = ISO8601.parse(lowerBound).getTimeInMillis();
                            long u = ISO8601.parse(upperBound).getTimeInMillis();
                            return (lowerBoundIncluded?v>=l:v>l) && (upperBoundIncluded?v<=u:v<u);
                        }
                    }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return true;
        }
        return true;
    }
}
