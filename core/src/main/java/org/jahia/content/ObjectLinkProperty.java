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
 package org.jahia.content;

/**
 * <p>Title: Represents a link property.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class ObjectLinkProperty {
    private int linkID = -1;
    private int position = 0;
    private String name;
    private String value;

    public static final int COMMON_OBJECT_LINK_PROPERTY = 0;
    public static final int LEFT_OBJECT_LINK_PROPERTY = 1;
    public static final int RIGTH_OBJECT_LINK_PROPERTY = 2;

    protected ObjectLinkProperty (int linkID,
                                  int position,
                                  String name,
                                  String value)
    {
        this.linkID = linkID;
        this.position = position;
        this.name = name;
        this.value = value;
    }

    public int getLinkID(){
        return this.linkID;
    }

    public void setLinkID(int linkID){
        this.linkID = linkID;
    }

    public int getPosition(){
        return this.position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }
}
