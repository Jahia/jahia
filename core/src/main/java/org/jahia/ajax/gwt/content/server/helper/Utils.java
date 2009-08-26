/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.content.server.helper;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.apache.jackrabbit.value.*;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.File;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 4 avr. 2008 - 12:28:42
 */
public class Utils {

    /**
     * Delete a folder no matter what is contained inside.
     * @param folder the folder to delete
     * @return true if deletion completed well
     */
    public static boolean recDel(File folder) {
        if (true) {
            return false ;
        }
        if (folder.isDirectory()) {
            File[] files = folder.listFiles() ;
            boolean ret = true ;
            for (File file : files) {
                ret &= recDel(file);
            }
            return ret && folder.delete() ;
        } else {
            return folder.delete() ;
        }
    }

    public static GWTJahiaNodePropertyValue convertValue(Value val) throws RepositoryException {
        String theValue ;
        int type ;

        switch (val.getType()) {
            case PropertyType.BINARY:
                type = GWTJahiaNodePropertyType.BINARY ;
                theValue = val.getString() ;
                break ;
            case PropertyType.BOOLEAN:
                type = GWTJahiaNodePropertyType.BOOLEAN ;
                theValue = String.valueOf(val.getBoolean()) ;
                break ;
            case PropertyType.DATE:
                type = GWTJahiaNodePropertyType.DATE ;
                theValue = String.valueOf(val.getDate().getTimeInMillis()) ;
                break ;
            case PropertyType.DOUBLE:
                type = GWTJahiaNodePropertyType.DOUBLE ;
                theValue = String.valueOf(val.getDouble()) ;
                break ;
            case PropertyType.LONG:
                type = GWTJahiaNodePropertyType.LONG ;
                theValue = String.valueOf(val.getLong()) ;
                break ;
            case PropertyType.NAME:
                type = GWTJahiaNodePropertyType.NAME ;
                theValue = val.getString() ;
                break ;
            case PropertyType.PATH:
                type = GWTJahiaNodePropertyType.PATH ;
                theValue = val.getString() ;
                break ;
            case ExtendedPropertyType.WEAKREFERENCE:
            case PropertyType.REFERENCE:
                return new GWTJahiaNodePropertyValue(ContentManagerHelper.getGWTJahiaNode((JCRNodeWrapper) ((JCRValueWrapper)val).getNode(), false));
            case PropertyType.STRING:
                type = GWTJahiaNodePropertyType.STRING ;
                theValue = val.getString() ;
                break ;
            case PropertyType.UNDEFINED:
                type = GWTJahiaNodePropertyType.UNDEFINED ;
                theValue = val.getString() ;
                break ;
            default:
                type = GWTJahiaNodePropertyType.UNDEFINED ;
                theValue = val.getString() ;
        }

        return new GWTJahiaNodePropertyValue(theValue, type) ;
    }

    public static Value convertValue(GWTJahiaNodePropertyValue val) throws RepositoryException {
        Value value ;

        switch (val.getType()) {
            case GWTJahiaNodePropertyType.BINARY:
                value = new BinaryValue(val.getBinary()) ;
                break ;
            case GWTJahiaNodePropertyType.BOOLEAN:
                value = new BooleanValue(val.getBoolean()) ;
                break ;
            case GWTJahiaNodePropertyType.DATE:
                Calendar cal = Calendar.getInstance() ;
                cal.setTime(val.getDate());
                value = new DateValue(cal) ;
                break ;
            case GWTJahiaNodePropertyType.DOUBLE:
                value = new DoubleValue(val.getDouble()) ;
                break ;
            case GWTJahiaNodePropertyType.LONG:
                value = new LongValue(val.getLong()) ;
                break ;
            case GWTJahiaNodePropertyType.NAME:
                value = NameValue.valueOf(val.getString()) ;
                break ;
            case GWTJahiaNodePropertyType.PATH:
                value = PathValue.valueOf(val.getString());
                break ;
            case GWTJahiaNodePropertyType.REFERENCE:
                value = ReferenceValue.valueOf(val.getString()) ;
                break ;
            case GWTJahiaNodePropertyType.STRING:
                value = new StringValue(val.getString()) ;
                break ;
            case GWTJahiaNodePropertyType.UNDEFINED:
                value = new StringValue(val.getString()) ;
                break ;
            default:
                value = new StringValue(val.getString()) ;
        }

        return value ;
    }

}
