/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.content;

import org.jahia.exceptions.JahiaException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: Represents a link between two objects.</p>
 * <p>Description: A link between two objects represents a semantic link that
 * could create any association of sense, ownership, categorization, reference,
 * and so on between two objects. This is a very general link definition that
 * allows us to create dynamic links that can then be used to do data integrity
 * checks, link data analysis, etc..</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class ObjectLink implements Serializable {

    private static final long serialVersionUID = -300945884777460149L;

    private int ID = -1;
    private ObjectKey leftObjectKey;
    private ObjectKey rightObjectKey;
    private String type;
    private Map<String, String> commonMetadata;

    public ObjectLink(int ID, ObjectKey leftObjectKey,
                      ObjectKey rightObjectKey,
                      String type,
                      Map<String, String> commonMetadata) {
        this.ID = ID;
        this.leftObjectKey = leftObjectKey;
        this.rightObjectKey = rightObjectKey;
        this.type = type;
        this.commonMetadata = commonMetadata;
    }

    static public ObjectLink createLink (ObjectKey leftObjectKey,
                                         ObjectKey rightObjectKey,
                                         String type, int status,
                                         java.util.Date creationDate,
                                         String creationUserKey,
                                         java.util.Date lastModificationDate,
                                         String lastModificationUserKey,
                                         Map<String, String> leftObjectMetadata,
                                         Map<String, String> rightObjectMetadata,
                                         Map<String, String> commonMetadata)
        throws JahiaException {
           return createLink(leftObjectKey, rightObjectKey, type, commonMetadata);
    }

    static public ObjectLink createLink(ObjectKey leftObjectKey,
                                        ObjectKey rightObjectKey,
                                        String type,
                                        Map<String, String> commonMetadata)
        throws JahiaException {
return null;
    }

    static public ObjectLink getLink (int linkID)
        throws JahiaException {
        return null;
    }

    static public List<ObjectLink> findByLeftAndRightObjectKeys (ObjectKey leftObjectKey,
        ObjectKey rightObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByLeftObjectKey (ObjectKey leftObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByRightObjectKey (ObjectKey rightObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByTypeAndLeftAndRightObjectKeys (String type,
        ObjectKey leftObjectKey,
        ObjectKey rightObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByTypeAndLeftAndLikeRightObjectKeys (String type,
            ObjectKey leftObjectKey,
            String rightObjectKey)
            throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByTypeAndLeftObjectKey (String type,
        ObjectKey leftObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByTypeAndRightObjectKey (String type,
        ObjectKey rightObjectKey)
        throws JahiaException {
        return Collections.emptyList();
    }

    static public List<ObjectLink> findByTypeAndRightAndLikeLeftObjectKey(String type,
			ObjectKey rightObjectKey, String leftObjectKey)
			throws JahiaException {
        return Collections.emptyList();
	}

    public void save()
        throws JahiaException {
    }

    public void remove()
        throws JahiaException {
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID () {
        return ID;
    }

    public ObjectKey getLeftObjectKey () {
        return leftObjectKey;
    }

    public void setLeftObjectKey (ObjectKey leftObjectKey) {
        this.leftObjectKey = leftObjectKey;
    }

    public ObjectKey getRightObjectKey () {
        return rightObjectKey;
    }

    public void setRightObjectKey (ObjectKey rightObjectKey) {
        this.rightObjectKey = rightObjectKey;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public Map<String, String> getCommonMetadata () {
        return commonMetadata;
    }

    public void setCommonObjectMetadata (Map<String, String> commonObjectMetadata) {
        if ( commonObjectMetadata != null ){
            this.commonMetadata = commonObjectMetadata;
        }
    }

}
