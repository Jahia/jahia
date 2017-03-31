/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.jackrabbit.value.AbstractValueFactory;
import org.jahia.api.Constants;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Jahia's extension of the value factory, which takes care that references are always created as weakrerences, unless one is using the method createValue(Node, false)
 *
 * @author Benjamin Papez
 */
public class JCRValueFactoryImpl extends AbstractValueFactory {

    private static final ValueFactory valueFactory = new JCRValueFactoryImpl();

    /**
     * Constructs a <code>ValueFactory</code> object.
     */
    protected JCRValueFactoryImpl() {
    }

    /**
     *
     */
    public static ValueFactory getInstance() {
        return valueFactory;
    }

    /**
     * @see AbstractValueFactory#checkPathFormat(String)
     */
    protected void checkPathFormat(String pathValue) throws ValueFormatException {
        // ignore
    }

    /**
     * @see AbstractValueFactory#checkNameFormat(String)
     */
    protected void checkNameFormat(String nameValue) throws ValueFormatException {
        // ignore
    }

    @Override
    public Value createValue(Node value) throws RepositoryException {
        // always generate weakreferences on default
        final Value valueToBeWrapped = super.createValue(value, !value.isNodeType(Constants.NT_VERSION));
        return new EqualsFriendlierValue(valueToBeWrapped);
    }

    @Override
    public Value createValue(String value, int type) throws ValueFormatException {
        final Value valueToBeWrapped = super.createValue(value, type != PropertyType.REFERENCE ? type : PropertyType.WEAKREFERENCE);
        return new EqualsFriendlierValue(valueToBeWrapped);
    }

    private static class EqualsFriendlierValue implements Value {
        private final Value value;

        private EqualsFriendlierValue(Value value) {
            this.value = value;
        }

        @Override
        public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
            return value.getString();
        }

        @Override
        public InputStream getStream() throws RepositoryException {
            return value.getStream();
        }

        @Override
        public Binary getBinary() throws RepositoryException {
            return value.getBinary();
        }

        @Override
        public long getLong() throws ValueFormatException, RepositoryException {
            return value.getLong();
        }

        @Override
        public double getDouble() throws ValueFormatException, RepositoryException {
            return value.getDouble();
        }

        @Override
        public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
            return value.getDecimal();
        }

        @Override
        public Calendar getDate() throws ValueFormatException, RepositoryException {
            return value.getDate();
        }

        @Override
        public boolean getBoolean() throws ValueFormatException, RepositoryException {
            return value.getBoolean();
        }

        @Override
        public int getType() {
            return value.getType();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                Value val = (Value) obj;

                // otherwise look deeper
                final int type = value.getType();
                if (type != val.getType()) {
                    return false;
                } else {
                    try {
                        switch (type) {
                            case PropertyType.BOOLEAN:
                                return value.getBoolean() == val.getBoolean();
                            case PropertyType.DATE:
                                return value.getDate().equals(val.getDate());
                            case PropertyType.DOUBLE:
                                Double doubleValue = value.getDouble();
                                return doubleValue.equals(val.getDouble());
                            case PropertyType.LONG:
                                return value.getLong() == val.getLong();
                            default:
                                return value.getString().equals(val.getString());
                        }
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            try {
                return value != null ? value.getString().hashCode() : 0;
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
