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
//  JahiaContainerDefinition
//  EV      27.12.2000
//
//  getID
//  getJahiaID
//  getPageDefID
//  getName
//  getTitle
//  getStructure
//  setID
//
//  structureChanged( structure, jData )
//  setStructure( structure, jData )
//

package org.jahia.data.containers;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.content.ContainerDefinitionKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.Serializable;
import java.util.*;


/**
 * <p>Title: A container definition defines name, title and references to
 * structure and page references for an actual containerList. </p>
 * <p>Description: This class is used by Jahia to determine the name of the
 * containerList to create, the reference to the structure, its properties,
 * etc... </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Eric Vassalli
 * @version 1.0
 */

public class JahiaContainerDefinition extends ContentDefinition implements Serializable {

    private static final long serialVersionUID = 701533160597674738L;

    // standard container list type
    public static final int STANDARD_TYPE = 0;

    // the container list can hold only one container
    public static final int SINGLE_TYPE = 1;

    // mandatory container, if the container list is empty, a new one is automatically created
    public static final int MANDATORY_TYPE = 2;

    // single mandatory container
    public static final int SINGLE_MANDATORY_TYPE =
            SINGLE_TYPE | MANDATORY_TYPE;

    private static Logger logger = Logger.getLogger(JahiaContainerDefinition.class);

    static {
        ContentDefinition.registerType(ContainerDefinitionKey.CONTAINER_TYPE,
                JahiaContainerDefinition.class.getName());
    }

    private int ID;
    private int jahiaID;
    private String name;

    private ContainerEditView editView;  // the edit view definition

    private Properties ctnDefProperties = null;

    /**
     * @associates JahiaContainerSubDefinition
     */
    private Map<Integer, JahiaContainerSubDefinition> subDefs;

    private String containerType;

    private String parentCtnType;

    /**
     * constructor
     * EV    24.11.2000
     */
    public JahiaContainerDefinition(int anID,
                                    int aJahiaID,
                                    String aName,
                                    Map<Integer, JahiaContainerSubDefinition> subDefinitions, String containerType) {
        super(new ContainerDefinitionKey(anID));
        this.ID = anID;
        this.jahiaID = aJahiaID;
        this.name = aName;
        this.subDefs = subDefinitions;
        this.containerType = containerType;
    } // end constructor

    /**
     * No arg constructor required for serialization support.
     */
    protected JahiaContainerDefinition() {

    }

    /**
     * accessor methods
     * EV    24.11.2000
     */
    public int getID() {
        return ID;
    }

    public int getJahiaID() {
        return jahiaID;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, JahiaContainerSubDefinition> getSubDefs() {
        return subDefs;
    }

    public void setID(int anID) {
        this.ID = anID;
        setObjectKey(new ContainerDefinitionKey(anID));
    }

    public void setName(String aName) {
        this.name = aName;
    }

    public static ContentDefinition getChildInstance(String IDInType) {

        try {
            return org.jahia.registries.JahiaContainerDefinitionsRegistry
                    .getInstance().getDefinition(Integer.parseInt(IDInType));

        } catch (JahiaException je) {
            logger.debug("Error retrieving ContentDefinition instance for id : " + IDInType, je);
        }
        return null;
    }

    /**
     * ContentDefinitionInterface implementation
     *
     * @param contentObject
     * @param entryState
     * @return
     */
    public String getTitle(ContentObject contentObject,
                           ContentObjectEntryState entryState) {
        try {
            return getTitle(LanguageCodeConverters.languageCodeToLocale(entryState.getLanguageCode()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }


    /* getTitle **************************************************************/
    public String getTitle(Locale locale) {
        return getContainerListNodeDefinition().getLabel(locale);
    }

    /* getStructure **********************************************************/
    public Iterator<JahiaContainerStructure> getStructure() {
        return getStructure(JahiaContainerStructure.ALL_TYPES);
    }


    /* getStructure (typeFlag) ***********************************************/
    public Iterator<JahiaContainerStructure> getStructure(int typeFlag) {
        List<JahiaContainerStructure> structure;
        JahiaContainerSubDefinition theSubDef = getSubDef();
        if (theSubDef != null) {
            structure = theSubDef.getStructure();
        } else {
            return (new ArrayList<JahiaContainerStructure>()).iterator();
        }
        List<JahiaContainerStructure> out = new ArrayList<JahiaContainerStructure>();
        for (JahiaContainerStructure theStructure : structure) {
            if (theStructure.getObjectType() == JahiaContainerStructure.JAHIA_FIELD) {
                // add a sanity check to make sure that the field definition
                // exists for the sub definition.
                JahiaFieldDefinition fieldDef = (JahiaFieldDefinition) theStructure.getObjectDef();
                if (fieldDef.getItemDefinition() == null || fieldDef.getType() == -1) {
                    logger.warn("Field definition " + fieldDef.getID() + " in structure of container definition " + getID() + " is missing, we will not include it in the structure of this template!");
                    continue;
                }
            }
            if ((theStructure.getObjectType() & typeFlag) != 0) {
                out.add(theStructure);
            }
        }
        return out.iterator();
    }

    /* setStructure **********************************************************/
    public void setStructure(List<JahiaContainerStructure> structure)
            throws JahiaException {
        JahiaContainerSubDefinition theSubDef = getSubDef();
        if (theSubDef == null) {
            theSubDef = createSubDef();
        }
        theSubDef.setStructure(structure);
    }

    /* composeStructure ******************************************************/
    public void composeStructure(List<String> structure)
            throws JahiaException {
        JahiaContainerSubDefinition theSubDef = getSubDef();
        if (theSubDef == null) {
            theSubDef = createSubDef();
        }
        theSubDef.composeStructure(structure);
    }


    /* getSubDef *************************************************************/
    public JahiaContainerSubDefinition getSubDef() {
        return (JahiaContainerSubDefinition) subDefs.get(0);
    } // end getSubDef


    /* createSubDef **********************************************************/
    private JahiaContainerSubDefinition createSubDef()
            throws JahiaException {
        JahiaContainerSubDefinition theSubDef = new JahiaContainerSubDefinition(getID(), 0, jahiaID, null);
        if (subDefs == null) {
            subDefs = new HashMap<Integer, JahiaContainerSubDefinition>();
        }
        subDefs.put(0, theSubDef);
        return theSubDef;
    } // end createSubDef


    /* findFieldInStructure *************************************************/
    public JahiaFieldDefinition findFieldInStructure(String fieldName) {
        Iterator<JahiaContainerStructure> structure = getStructure();
        if (fieldName != null) {
            while (structure.hasNext()) {
                JahiaContainerStructure theStructure = (JahiaContainerStructure) structure.next();
                if ((theStructure.getObjectType() & JahiaContainerStructure.JAHIA_FIELD) != 0) {
                    JahiaFieldDefinition theFieldDef = (JahiaFieldDefinition) theStructure.getObjectDef();
                    if (fieldName.equals(theFieldDef.getName())) {
                        return theFieldDef;
                    }
                }
            }
        }
        return null;
    }


    /* findContainerInStructure **********************************************/
    public JahiaContainerDefinition findContainerInStructure(String containerName) {
        Iterator<JahiaContainerStructure> structure = getStructure();
        if (containerName != null) {
            while (structure.hasNext()) {
                JahiaContainerStructure theStructure = (JahiaContainerStructure) structure.next();
                if ((theStructure.getObjectType() & JahiaContainerStructure.JAHIA_CONTAINER) != 0) {
                    JahiaContainerDefinition theContainerDef = (JahiaContainerDefinition) theStructure.getObjectDef();
                    if (containerName.equals(theContainerDef.getName())) {
                        return theContainerDef;
                    }
                }
            }
        }
        return null;
    }


    /* structureChanged ******************************************************/
    public synchronized boolean structureChanged(List<String> struct)
            throws JahiaException {
        // gets structure
        List<JahiaContainerStructure> structure;
        JahiaContainerSubDefinition theSubDef = getSubDef();
        if (theSubDef != null) {
            structure = theSubDef.getStructure();
        } else {
            structure = null;
        }

        if ((struct == null) || (structure == null)) {
            // compares null
            logger.debug(name + ":Structure non-existant in memory, must load from database");
            return true;
        } else if (struct.size() != structure.size()) {
            // compares sizes
            logger.debug(name + ":Size are not equal (cur=" +
                    Integer.toString(structure.size()) + ", new=" + Integer.toString(struct.size()) + ")");
            return true;
        } else {
            // compares fields
            for (int i = 0; i < structure.size(); i++) {
                JahiaContainerStructure theStructure = (JahiaContainerStructure) structure.get(i);
                JahiaContainerStructure aStructure = new JahiaContainerStructure(
                        struct.get(i), theSubDef.getID(), i, jahiaID);
                if (!theStructure.equals(aStructure)) {
                    logger.debug(name + ":Size equal, fields not equal.");
                    return true;
                }
            }
            // JahiaConsole.println("JahiaContainerDefinition.structureChanged", name + ":Size equal, fields equal.");
            return false;
        }
    } // end structureChanged

    public void setProperties(Properties newProperties) {
        this.ctnDefProperties = newProperties;
    }

    public Properties getProperties() {
        return this.ctnDefProperties;
    }

    public String getProperty(String propertyName) {
        if (this.ctnDefProperties != null) {
            return this.ctnDefProperties.getProperty(propertyName);
        } else {
            return null;
        }
    }

    public void setProperty(String propertyName, String propertyValue) {
        if (this.ctnDefProperties != null) {
            this.ctnDefProperties.setProperty(propertyName, propertyValue);
        } else {
            logger.error("ERROR: Properties object is not defined, ignoring property insertion.");
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Set the Container Edit View
     */
    public void setContainerEditView(ContainerEditView anEditView) {
        this.editView = anEditView;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the Container Edit View
     * Can be null
     */
    public ContainerEditView getContainerEditView() {
        return this.editView;
    }

    /**
     * Returns the alias names ( container definition names )
     *
     * @return
     */
    public String[] getAliasNames() {
        return new String[]{};
    }

    public int getContainerListType() {
        ExtendedNodeDefinition nodeDef = getContainerListNodeDefinition();
        if (nodeDef.getRequiredPrimaryTypes()[0].isNodeType(Constants.JAHIANT_CONTAINERLIST)) {
            nodeDef = nodeDef.getRequiredPrimaryTypes()[0].getChildNodeDefinitionsAsMap().get("*");
        }        

        int type = JahiaContainerDefinition.STANDARD_TYPE;
        if (!nodeDef.allowsSameNameSiblings()) type += JahiaContainerDefinition.SINGLE_TYPE;
        if (nodeDef.isMandatory()) type += JahiaContainerDefinition.MANDATORY_TYPE;
        return type;
    }

    public String getContainerType() {
        return containerType;
    }

    public String getContainerListNodeType() {
        String[] s = parentCtnType.split(" ");
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            ExtendedNodeDefinition nodeDef = nt.getChildNodeDefinitionsAsMap().get(s[1]);
            ExtendedNodeType listType = nodeDef.getRequiredPrimaryTypes()[0];
            return listType.getName();
        } catch (NoSuchNodeTypeException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getPrimaryType() {
        return getContainerType();
    }

    public String getParentCtnType() {
        return parentCtnType;
    }

    public void setParentCtnType(String parentCtnType) {
        this.parentCtnType = parentCtnType;
    }

    public ExtendedNodeDefinition getNodeDefinition() {
        try {
            String[] s = parentCtnType.split(" ");
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            ExtendedNodeDefinition nodeDef = nt.getChildNodeDefinitionsAsMap().get(s[1]);
            ExtendedNodeType listType = nodeDef.getRequiredPrimaryTypes()[0];
            ExtendedNodeDefinition[] defs = listType.getChildNodeDefinitions();
            for (ExtendedNodeDefinition def : defs) {
                if (def.getName().equals("*")) {
                    // must check the type here for boxes
                    return def;
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    public List<ExtendedNodeType> getMixinNodeTypes() {
        List<ExtendedNodeType> results = new ArrayList<ExtendedNodeType>();
        String s = getContainerListNodeDefinition().getSelectorOptions().get("addMixin");
        if (s != null ) {
            String[] types = s.split(",");
            for (String type : types) {
                try {
                    results.add(NodeTypeRegistry.getInstance().getNodeType(type));
                } catch (NoSuchNodeTypeException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return results;
    }

    public ExtendedNodeDefinition getContainerListNodeDefinition() {
        try {
            String[] s = parentCtnType.split(" ");
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s[0]);
            return nt.getChildNodeDefinitionsAsMap().get(s[1]);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    public List<ExtendedNodeType> getContainerListMixinNodeTypes() {
        List<ExtendedNodeType> results = new ArrayList<ExtendedNodeType>();
        String s = getContainerListNodeDefinition().getSelectorOptions().get("addListMixin");
        if (s != null ) {
            String[] types = s.split(",");
            for (String type : types) {
                try {
                    results.add(NodeTypeRegistry.getInstance().getNodeType(type.trim()));
                } catch (NoSuchNodeTypeException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return results;
    }


} // end JahiaContainerDefinition
