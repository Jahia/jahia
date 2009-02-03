/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
        this.type           = type;
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
