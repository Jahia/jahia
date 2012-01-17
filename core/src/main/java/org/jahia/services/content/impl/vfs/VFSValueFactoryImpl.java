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

package org.jahia.services.content.impl.vfs;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:03:58 PM
 * 
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
