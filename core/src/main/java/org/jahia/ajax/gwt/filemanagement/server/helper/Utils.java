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
package org.jahia.ajax.gwt.filemanagement.server.helper;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
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
            case PropertyType.REFERENCE:
                type = GWTJahiaNodePropertyType.REFERENCE ;
                theValue = val.getString() ;
                break ;
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
                value = PathValue.valueOf(val.getString()) ;
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
