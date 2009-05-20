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
//  JahiaContainerSubDefinition
//  EV      25.11.2000
//
//  getID
//  getPageDefID
//  getTitle
//  getStructure
//
//  setID
//  setTitle
//  setStructure
//

package org.jahia.data.containers;

/**
 * The purpose of this object is to store what page uses a certain container
 * definition. At least that's what I understand from Eric's code. Title may
 * vary from original title in sub definition.
 *
 * @todo FIND WHAT THIS IS FOR ...
 */

import java.util.ArrayList;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import java.io.Serializable;


public class JahiaContainerSubDefinition implements Serializable {


    private int     ID;
    private int 	ctnDefID;
    private int     pageDefID;
    private int     jahiaID;
    private List<JahiaContainerStructure>  structure;

    /***
        * constructor
        * EV    13.02.2001
        *
        */
    public JahiaContainerSubDefinition(int ID,
                                       int pageDefID,
                                       int jahiaID,
                                       List<JahiaContainerStructure> structure)
    throws JahiaException
    {
        this.ID             = ID;
        this.ctnDefID		= 0;
        this.pageDefID      = pageDefID;
        this.structure      = structure;
        this.jahiaID        = jahiaID;
    } // end constructor

    /**
     * No arg constructor required for serialization support.
     */
    protected JahiaContainerSubDefinition () {

    }

    /***
        * accessor methods
        * EV    25.11.2000
        *
        */
    public int getID() {
        return ID;
    }

    public int getCtnDefID() {
        return ctnDefID;
    }

    public int getPageDefID() {
        return pageDefID;
    }

    public List<JahiaContainerStructure> getStructure() {
        return structure;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setStructure(List<JahiaContainerStructure> structure) {
        this.structure = structure;
    }

    public void composeStructure(List<String> struct) throws JahiaException {
        this.structure = new ArrayList<JahiaContainerStructure>();
        if (struct != null) {
            for (int i = 0; i < struct.size(); i++) {
                this.structure.add(new JahiaContainerStructure(struct.get(i),
                        this.getID(), i, jahiaID));

            }
        }
    }
    // end accessor methods


} // end JahiaContainerSubDefinition
