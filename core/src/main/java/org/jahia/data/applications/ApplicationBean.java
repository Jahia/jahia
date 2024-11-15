/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.data.applications;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.pluto.container.om.portlet.UserAttribute;
import org.jahia.registries.ServicesRegistry;

/**
 * This object contains all the data relative to an application, notably the
 * context in which it should run, it's type (servlet or JSP) and additionally
 * information such as rights, etc...
 *
 * @author Serge Huber
 * @version 1.0
 */
public class ApplicationBean implements Serializable, Comparator<ApplicationBean> {

    private static final long serialVersionUID = -5886294839254670413L;

    private String ID;
    private String name;
    private String context;
    private boolean visible = false;
    private String description = "";
    private String type;
    private List<EntryPointInstance> entryPointInstances;
    private List<UserAttribute> userAttributes;

    /**
     * Basic constructor
     * @param ID
     * @param name
     * @param context
     * @param visible
     * @param desc
     * @param type
     */
    public ApplicationBean(String ID, String name,String context,boolean visible,String desc,String type) {
        setID(ID);
        this.name = name;
        this.context = context;
        this.visible = visible;
        this.description = desc;
        this.type = type;
    } // end constructor

    /**
     * accessor methods
     * {
     */
    public String getID() {
        return ID;
    }

    /**
     * Get name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get context
     *
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Get visible status
     *
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Get description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set id
     *
     * @param ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Set name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set visibility
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Set description
     *
     * @param descr
     */
    public void setDescription(String descr) {
        this.description = descr;
    }

    // end accessor methods

    /**
     * Compare methode
     *
     * @param c1
     * @param c2
     * @return
     * @throws ClassCastException
     */
    public int compare(ApplicationBean c1, ApplicationBean c2)
            throws ClassCastException {

        return (c1.getName().compareToIgnoreCase(c2.getName()));

    }

    /**
     * Get type
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Get all entrypoint definition
     *
     * @return
     */
    public List<EntryPointDefinition> getEntryPointDefinitions() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getAppEntryPointDefinitions(this);
    }

    /**
     * find EntryPointInstance by definition name
     *
     * @param definitionName
     * @return
     */
    public EntryPointDefinition getEntryPointDefinitionByName(String definitionName) {
        EntryPointDefinition entryPointDefinition = null;
        Iterator<EntryPointDefinition> entryPointDefinitions = getEntryPointDefinitions().iterator();
        while (entryPointDefinitions.hasNext()) {
            EntryPointDefinition curEntryPointDefinition =  entryPointDefinitions.next();
            if (curEntryPointDefinition.getName().equals(definitionName)) {
                entryPointDefinition = curEntryPointDefinition;
                break;
            }
        }
        return entryPointDefinition;
    }

    /**
     * Get entryPointInstance
     *
     * @return
     */
    public List<EntryPointInstance> getEntryPointInstances() {
        return entryPointInstances;
    }

    /**
     * Set entrypoint
     *
     * @param entryPointInstances
     */
    public void setEntryPointInstances(List<EntryPointInstance> entryPointInstances) {
        this.entryPointInstances = entryPointInstances;
    }

    /**
     * eqiasl method
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplicationBean that = (ApplicationBean) o;

        return ID.equals(that.ID);

    }

    /**
     * return hashcode
     *
     * @return
     */
    public int hashCode() {
        return ID.hashCode();
    }

    /**
     * Get user attributes
     *
     * @return
     */
    public List<UserAttribute> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Set user attributes
     *
     * @param userAttributes
     */
    public void setUserAttributes(List<UserAttribute> userAttributes) {
        this.userAttributes = userAttributes;
    }
}
