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
 package org.jahia.data.fields;

import java.util.Map;
import java.util.Set;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class ContainerFieldsEditHelper extends FieldsEditHelperAbstract {

    private JahiaContainer container; // current container
    private Map ctnListFieldAcls;
    private Set visibleFields;

    public ContainerFieldsEditHelper(JahiaContainer container){
        this.container = container;
    }

    public void setContainer(JahiaContainer container){
        this.container = container;
    }

    public JahiaContainer getContainer(){
        return this.container;
    }

    public void setCtnListFieldAcls(Map ctnListFieldAcls){
        this.ctnListFieldAcls = ctnListFieldAcls;
    }

    public Map getCtnListFieldAcls(){
        return this.ctnListFieldAcls;
    }

    public void setVisibleFields(Set visibleFields){
        this.visibleFields = visibleFields;
    }

    public Set getVisibleFields(){
        return this.visibleFields;
    }

    public JahiaField getField(int fieldId){
        JahiaField field = null;
        try {
            field = this.getContainer().getField(fieldId);
        } catch ( JahiaException je ){

        }
        return field;
    }

    public JahiaField getField(String fieldName){
        JahiaField field = null;
        try {
            field = this.getContainer().getField(fieldName);
        } catch ( JahiaException je ){

        }
        return field;
    }
}
