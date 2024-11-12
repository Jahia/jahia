/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

    public static ValueFactory getInstance() {
        return valueFactory;
    }

    @Override
    protected void checkPathFormat(String pathValue) throws ValueFormatException {
        // ignore
    }

    @Override
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
    public Value createValue(Node value, boolean weak) throws RepositoryException {
        final Value valueToBeWrapped = super.createValue(value, weak);
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
        public String getString() throws RepositoryException {
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
        public long getLong() throws RepositoryException {
            return value.getLong();
        }

        @Override
        public double getDouble() throws RepositoryException {
            return value.getDouble();
        }

        @Override
        public BigDecimal getDecimal() throws RepositoryException {
            return value.getDecimal();
        }

        @Override
        public Calendar getDate() throws RepositoryException {
            return value.getDate();
        }

        @Override
        public boolean getBoolean() throws RepositoryException {
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
                                BigDecimal doubleValue = BigDecimal.valueOf(value.getDouble());
                                BigDecimal doubleVal = BigDecimal.valueOf(val.getDouble());
                                return doubleValue.compareTo(doubleVal) == 0;
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
