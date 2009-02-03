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
