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

 package org.jahia.services.metadata;

import java.util.*;
import org.jahia.content.ContentDefinition;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class FieldDefinition {

    public static final String CONTENTDEFINITION_MAPPINGS = "contentDefinitionMappings";
    public static final String DEFINITION = "definition";
    public static final String READ_ONLY = "readOnly";
    public static final String EDITABLE_DEFINITION = "editableDefinition";
    public static final String HIDDEN_FROM_EDITING_ENGINE = "hiddenFromEditingEngine";
    public static final String REQUIRED = "required";
    public static final String ORDER = "fieldOrder";
    public static final String SCORE_BOOST = "scoreBoost";
    public static final String INDEXABLE_FIELD = "indexableField";
    public static final String ANALYZER = "indexAnalyzer";
    public static final String INDEX_STORE = "indexStore";

    /**
     * If true, the field value smust not be used in search highlighting.
     */
    public static final String EXCLUDE_FROM_HIGHLIGHTING = "excludeFromHighlighting";        

    private int id;
    private String name;
    private String title;
    private int type;
    private boolean readOnly;
    private boolean required;
    private boolean editableDefinition;
    private boolean hiddenFromEditingEngine;
    private String defaultValue;
    private String handlerClass;
    private int aclId;
    private int order;
    private float scoreBoost;
    private boolean indexableField = true;

    private List<String> contentDefinitionMappings = new ArrayList<String>();

    public FieldDefinition() {
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getType(){
        return this.type;
    }

    public void setType(int type){
        this.type = type;
    }

    public String getDefaultValue(){
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue){
        this.defaultValue = defaultValue;
    }

    public String getHandlerClass(){
        return this.handlerClass;
    }

    public void setHandlerClass(String handlerClass){
        this.handlerClass = handlerClass;
    }

    public boolean getRequired(){
        return this.required;
    }

    public void setRequired(boolean required){
        this.required = required;
    }

    public int getOrder(){
        return this.order;
    }

    public void setOrder(int order){
        this.order = order;
    }

    public float getScoreBoost() {
        return scoreBoost;
    }

    public void setScoreBoost(float scoreBoost) {
        this.scoreBoost = scoreBoost;
    }

    public boolean isIndexableField() {
        return indexableField;
    }

    public void setIndexableField(boolean indexableField) {
        this.indexableField = indexableField;
    }

    public boolean getReadOnly(){
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly){
        this.readOnly = readOnly;
    }

    public boolean getEditableDefinition(){
        return this.editableDefinition;
    }

    public void setEditableDefinition(boolean editableDefinition){
        this.editableDefinition = editableDefinition;
    }

    public boolean getHiddenFromEditingEngine(){
        return this.hiddenFromEditingEngine;
    }

    public void setHiddenFromEditingEngine(boolean hiddenFromEditingEngine){
        this.hiddenFromEditingEngine = hiddenFromEditingEngine;
    }

    public void setContentDefinitionMappings(List<String> contentDefinitionMappings){
        this.contentDefinitionMappings = contentDefinitionMappings;
    }

    public List<String> getContentDefinitionMappings(){
        return this.contentDefinitionMappings;
    }

    public String getContentDefinitionMappingsAsString(){
        StringBuffer buff = new StringBuffer("");
        List<String> vals = this.getContentDefinitionMappings();
        for ( int i=0; i<vals.size(); i++ ){
            buff.append(vals.get(i).toString());
            if ( i<vals.size()-1 ){
                buff.append(",");
            }
        }
        return buff.toString();
    }

    public int getAclId(){
        return this.aclId;
    }

    public void setAclId(int aclId){
        this.aclId = aclId;
    }

    static public boolean matchContentDefinitionMappings(
        ContentDefinition contentDefinition,
        String pattern){
        //@todo complete full expression match comparison
        if ( contentDefinition==null || pattern==null ){
            return false;
        }
        if ( "*".equals(pattern) ){
            return true;
        }
        return false;
    }
}
