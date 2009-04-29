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
//
//  JahiaFieldSubDefinition
//  EV      25.11.2000
//
//  getID
//  getJahiaID
//  getTemplateID
//  getName
//  getTitle
//  getType
//  getDefaultValue
//
//  setID
//  setTitle
//  setType
//  setDefaultValue
//

package org.jahia.data.fields;

import java.io.Serializable;



public class JahiaFieldSubDefinition implements Serializable {


    private int     ID;
    private int 	fieldDefID;
    private int     pageDefID;
    private int     type;

    private boolean isMetadata;

    /***
        * constructor
        * EV    25.11.2000
        *
        */
    public JahiaFieldSubDefinition(int ID,
                                   int fieldDefID,
                                   int pageDefID,
                                   boolean isMetadata)
    {
        this.ID             = ID;
        this.fieldDefID		= fieldDefID;
        this.pageDefID      = pageDefID;
        this.isMetadata     = isMetadata;
    } // end constructor


    /**
     * No arg constructor required for serialization support.
     */
    protected JahiaFieldSubDefinition() {

    }

    /***
        * accessor methods
        * EV    25.11.2000
        *
        */
    public  int     getID()                 {   return ID;              }
    public  int     getFieldDefID()         {   return fieldDefID;      }
    public  int     getPageDefID()          {   return pageDefID;       }
    public  int     getType()               {   return type;            }

    public void setID( int ID ) { this.ID = ID; }
    public void setFieldDefID(int id) { this.fieldDefID = id; }
    public void setPageDefID(int id) { this.pageDefID = id; }
    public void setType( int value ) { this.type = value; }
    public boolean isMetadata() {
        return isMetadata;
    }

    public void setMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    // end accessor methods



    /**
     * Return a string representation of the internal state.
     *
     * @return A string representation of this object.
     */
    public String toString ()
    {
        StringBuffer output = new StringBuffer ("Detail of fieldSubDef ["+getID()+"]\n");
        output.append ("  - id ["+Integer.toString (getID())+"]\n");
        output.append ("  - fieldDef ["+getFieldDefID()+"]\n");
        output.append ("  - pageDef ["+getPageDefID()+"]\n");
        output.append ("  - type ["+getType()+"]\n");
        return output.toString();
    }


} // end JahiaFieldSubDefinition
