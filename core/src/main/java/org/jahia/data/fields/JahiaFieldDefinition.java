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
//
//  JahiaFieldDefinition
//  EV      25.11.2000
//
//  getID
//  getJahiaID
//  getName
//  getSubDefs
//  getTemplateID
//  getTitle
//  getType
//  getDefaultValue
//
//  setID
//  setTitle
//  setType
//

package org.jahia.data.fields;

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.FieldDefinitionKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.content.nodetypes.*;
import org.jahia.utils.DateUtils;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

public class JahiaFieldDefinition extends ContentDefinition implements Serializable {

    private static final long serialVersionUID = 9001976189590277991L;

    public static final String ALIAS_PROP_NAME = "ALIAS_NAME";

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaFieldDefinition.class);

    static {
        ContentDefinition.registerType(FieldDefinitionKey.FIELD_TYPE,
                                   JahiaFieldDefinition.class.getName());
    }

    public static final String IS_METADATA = "JahiaFieldDefinition.isMetadata";
    public static final String GLOBAL_FIELD = "global.field.";

    private int     ID;
    private int     jahiaID;
    private String  name;
    private boolean isMetadata;
    private String ctnType = null;
    /**
     * @associates JahiaFieldSubDefinition
     */
    private Map<Integer, JahiaFieldSubDefinition> subDefs;


    private Map<Object, Object> props = new ConcurrentHashMap<Object, Object>(11);

    /***
        * constructor
        * EV    25.11.2000
        *
        */
    public JahiaFieldDefinition(int     ID,
                                int     jahiaID,
                                String  name,
                                Map<Integer, JahiaFieldSubDefinition> subDefs )
    {
        super(new FieldDefinitionKey(ID));
        this.ID             = ID;
        this.jahiaID        = jahiaID;
        this.name           = name;
        this.subDefs        = subDefs;

        if ( this.getName().toLowerCase().startsWith(GLOBAL_FIELD) ){
            this.jahiaID = 0;

            for (final Map.Entry<Integer, JahiaFieldSubDefinition> entry : this.subDefs.entrySet()){
                JahiaFieldSubDefinition subDef = entry.getValue();
                subDef.setPageDefID(0);
                subDefs.remove(entry.getKey());
                subDefs.put(new Integer(0),subDef);
            }
        }


    } // end constructor

    /**
     * No arg constructor required for serialization support.
     */
    protected JahiaFieldDefinition() {

    }

    /***
        * accessor methods
        * EV    25.11.2000
        *
        */
    public  int     getID()                 {   return ID;              }
    public  int     getJahiaID()            {   return jahiaID;         }
    public  String  getName()               {   return name;            }
    public Map<Object, Object> getProperties()       {   return this.props;      }

    public void setID( int value ) {
        this.ID = value;
        setObjectKey(new FieldDefinitionKey(ID));
    }

    public void setProperties( Map<Object, Object> props ){
        if ( props != null ){
            this.props = props;
        }
    }

    public String getProperty(String name){
        return (String) this.props.get(name);
    }

    public void setProperty(String name, String value){
        if ( name != null && value != null ){
            this.props.put(name,value);
        }
    }

    /**
     *
     * @param IDInType
     * @return
     */
    public static ContentDefinition getChildInstance(String IDInType) {

        try {
            return org.jahia.registries.JahiaFieldDefinitionsRegistry
                .getInstance().getDefinition(Integer.parseInt(IDInType));
        } catch (JahiaException je) {
            logger.debug("Error retrieving ContentDefinition instance for id : " + IDInType, je);
        }
        return null;
    }

    /**
     *
     * @param contentObject
     * @param entryState
     * @return
     */
    public String getTitle( ContentObject contentObject,
                            ContentObjectEntryState entryState ) {
        String title = "";
        try {
            return this.getTitle(
                    LanguageCodeConverters
                                 .languageCodeToLocale(entryState.getLanguageCode()));
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
        }
        return title;
    }

    public String getTitle() {
        return getItemDefinition().getResourceBundleKey();
    }

    public String getTitle(Locale locale) {
        return getItemDefinition().getLabel(locale);
    }

    public String getDefaultValue() {
        String jahiaDefaultValue = "";
        try {
            int type = getType();
            if (type == FieldTypes.PAGE) {
                //
            } else {
                ExtendedPropertyDefinition propDef = getPropertyDefinition();
                if (propDef != null) {
                    Map<String,String> opts = propDef.getSelectorOptions();
                    Value[] defaultValues = propDef.getDefaultValues();
                    switch (propDef.getRequiredType()) {
                        case PropertyType.STRING :
                        case PropertyType.LONG :
                        case PropertyType.DOUBLE :
                        case PropertyType.BOOLEAN :
                            if (defaultValues.length > 0) {
                                jahiaDefaultValue += defaultValues[0].getString();
                                for (int i = 1; i < defaultValues.length; i++) {
                                    jahiaDefaultValue += JahiaField.MULTIPLE_VALUES_SEP+defaultValues[i].getString();
                                }
                            }
                            break;
                        case PropertyType.DATE :
                            String format = DateUtils.DEFAULT_DATETIME_FORMAT;
                            if (opts != null && opts.get("format") != null) {
                                format = opts.get("format");
                            }

                            jahiaDefaultValue  += "<jahia_calendar["+format+"]>";

                            if (defaultValues.length > 0) {
                                jahiaDefaultValue += new SimpleDateFormat(format).format(defaultValues[0].getDate().getTime());
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getting default values for: " + getName() + " pageDefID", e);
        }
        return jahiaDefaultValue;
    }

    public String getDefaultValueAsNull() {
        return getDefaultValue();
    }

    public int getType() {
        ExtendedItemDefinition itemDef = getItemDefinition();
        if (itemDef == null) {
            return -1;
        }
        return -1;
    }

    public boolean getIsMetadata() {
        return isMetadata;
    }

    public void setIsMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    public String getCtnType() {
        return ctnType;
    }

    public void setCtnType(String ctnType) {
        this.ctnType = ctnType;
    }

    /***
        * gets the field sub definition, from its page def id
        *
        */

//    private JahiaFieldSubDefinition getSubDef( int pageDefID )
//    {
//
//        //JahiaConsole.println("JahiaFieldDefinition.getSubDef","field def " + name + "[" + ID + "], request subdef for pageDefID=" + pageDefID);
//
//        // Introduced for metadata
//        // Cannot rely on this check, should be removed
//        if ( this.getName().toLowerCase().startsWith(GLOBAL_FIELD) ){
//            logger.debug("Global field detected, using pageDefID=0");
//            pageDefID = 0;
//        }
//
//        // Introduced for metadata
//        if ( this.getJahiaID() == 0 ){
//            logger.debug("SiteID == 0, using pageDefID = 0");
//            pageDefID = 0;
//        }
//
//        JahiaFieldSubDefinition sd = (JahiaFieldSubDefinition)subDefs.get( new Integer(pageDefID) );
//        /*
//        if ( sd != null ){
//            JahiaConsole.println("JahiaFieldDefinition.getSubDef",sd.toString());
//        } else {
//            JahiaConsole.println("JahiaFieldDefinition.getSubDef","sub def not found");
//        }
//        */

//        return sd;
//    } // end getSubDef



    /***
        * creates a sub definition
        *
        */

//    private JahiaFieldSubDefinition createSubDef( int pageDefID )
//    {
//
//        if ( this.getName().toLowerCase().startsWith(JahiaFieldDefinition.GLOBAL_FIELD) ){
//            pageDefID = 0;
//        }
//
//        JahiaFieldSubDefinition theSubDef = new JahiaFieldSubDefinition( 0, 0, pageDefID, this.getIsMetadata() );
//        if (subDefs == null) {
//            subDefs = new HashMap();
//        }
//        subDefs.put( new Integer(theSubDef.getPageDefID()), theSubDef );
//        return theSubDef;
//    } // end createSubDef


    /**
     * Returns the alias names ( container definition names )
     * @return
     */
    public String[] getAliasNames
    (){
        if ( props == null ){
            return new String[]{};
        }
        String strVal = (String) props.get(ALIAS_PROP_NAME);
        if ( strVal == null || "".equals(strVal.trim()) ){
            return new String[]{};
        }
        return JahiaTools.getTokens(strVal, " *+, *+");
    }


    /**
     * Returns true if the field can be indexed or not
     * @return
     */
    public boolean isIndexableField(){
        return getPropertyDefinition() == null
                || getPropertyDefinition().getIndex() != ExtendedPropertyDefinition.INDEXED_NO;
    }

    public ExtendedItemDefinition getItemDefinition() {
        try {
            String[] s = ctnType.split(" ");
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            ExtendedItemDefinition res = nt.getPropertyDefinitionsAsMap().get(s[1]);
            if (res == null) {
                res = nt.getChildNodeDefinitionsAsMap().get(s[1]);
            }
            return res;
        } catch (Exception e) {
            logger.error("Error getting node defintion for " + ctnType, e);
        }
        return null;
    }

    public ExtendedNodeDefinition getNodeDefinition() {
        try {
            String[] s = ctnType.split(" ");
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            return nt.getChildNodeDefinitionsAsMap().get(s[1]);
        } catch (Exception e) {
            logger.error("Error getting node defintion for " + ctnType, e);
        }
        return null;
    }

    public ExtendedPropertyDefinition getPropertyDefinition() {
        try {
            String[] s = ctnType.split(" ");
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            return nt.getPropertyDefinitionsAsMap().get(s[1]);
        } catch (Exception e) {
            logger.error("Error getting property defintion for " + ctnType, e);
        }
        return null;
    }

} // end JahiaFieldDefinition
