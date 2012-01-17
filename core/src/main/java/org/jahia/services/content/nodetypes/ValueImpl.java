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

package org.jahia.services.content.nodetypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.math.BigDecimal;

import javax.jcr.*;

import org.apache.jackrabbit.util.ISO8601;
import org.slf4j.Logger;

/**
 * Implementation of the JCR's {@link Value} interface for holding property
 * values of different types.
 * 
 * @author Thomas Draier
 */
public class ValueImpl implements Value {
    
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ValueImpl.class);
    
    protected String value;
    protected int type;

    public ValueImpl(String v) {
        this(v, PropertyType.STRING);
    }

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

    public Binary getBinary() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
