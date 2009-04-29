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
//  JahiaContainerStructure
//
//  getctndefid
//  getObjectType
//  getObjectDefID
//  getObjectDef
//  getRank
//  setctndefid
//
//  equals
//


package org.jahia.data.containers;

import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import java.io.Serializable;

public class JahiaContainerStructure implements Serializable {

    private static final long serialVersionUID = 1371864326264863710L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaContainerStructure.class);

    public static final int JAHIA_FIELD = 1;
    public static final int JAHIA_CONTAINER = 2;
    public static final int ALL_TYPES = -1;

    private int subctndefid;
    private int objectType;
    private Object objectDef;
    private int objectDefID;
    private int rank;

    /***
     * constructor
     *
     */
    public JahiaContainerStructure (int subctndefid,
                                    int objectType,
                                    int objectDefID,
                                    int rank, int jahiaID)
        throws JahiaException {
        this.subctndefid = subctndefid;
        this.objectType = objectType;
        this.objectDefID = objectDefID;
        this.rank = rank;
        switch (objectType) {
            case (JAHIA_FIELD):
                this.objectDef = JahiaFieldDefinitionsRegistry.getInstance().
                                 getDefinition(objectDefID);
                break;
            case (JAHIA_CONTAINER):
                this.objectDef = JahiaContainerDefinitionsRegistry.getInstance().
                                 getDefinition(objectDefID);
                break;
        }
    } // end constructor

    /***
     * constructor - by objectName and ctndefid
     *
     */
    public JahiaContainerStructure(String objectName,
                                   int subctndefid,
                                   int rank,
                                   int jahiaID)
        throws JahiaException {
        this.subctndefid = subctndefid;
        this.rank = rank;

        boolean isField = objectName.startsWith("@f ");
        boolean isContainer = objectName.startsWith("@c ");
        if (isField || isContainer) {
            objectName = objectName.substring(3);
        }
        // determines structure object type (field or container ?) and definition id
        JahiaFieldDefinition fDef = null;
        if (!isContainer) {
            fDef= JahiaFieldDefinitionsRegistry.getInstance().getDefinition(jahiaID, objectName);
        }
        if ( fDef != null ) {
            this.objectType = JahiaContainerStructure.JAHIA_FIELD;
            this.objectDefID = fDef.getID();
            this.objectDef = fDef;
        } else {

            JahiaContainerDefinition cDef = null;
            if (!isField) {
                cDef = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(jahiaID, objectName);
            }
            if (cDef != null) {
                this.objectType = JahiaContainerStructure.JAHIA_CONTAINER;
                this.objectDefID = cDef.getID();
                this.objectDef = cDef;
            } else {
                String errorMsg = objectName +
                        " is neither a field or a container -> not declared !";
                logger.error(errorMsg + " -> BAILING OUT");
                throw new JahiaException(
                        "Cannot synchronize fields with database",
                        errorMsg, JahiaException.DATABASE_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }
        }
    } // end constructor


    /**
     * No arg constructor required for serialization support.
     */
    protected JahiaContainerStructure() {

    }

    /***
     * accessor methods
     *
     */
    public int getSubctndefid () {return subctndefid;
    }

    public int getObjectType () {return objectType;
    }

    public int getObjectDefID () {return objectDefID;
    }

    public Object getObjectDef () {return objectDef;
    }

    public int getRank () {return rank;
    }

    public void setSubctndefid (int defID) {this.subctndefid = defID;
    }

    // end accessor methods



    /***
     * equals
     *
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaContainerStructure theStruct = (JahiaContainerStructure)obj;
            if ((theStruct.getSubctndefid() == this.getSubctndefid()) &&
                    (theStruct.getObjectType() == this.getObjectType()) &&
                    (theStruct.getObjectDefID() == this.getObjectDefID()) &&
                    (theStruct.getRank() == this.getRank())) {
                return true;
            }
        }
        return false;
    } // end equals

} // end JahiaContainerStructure
