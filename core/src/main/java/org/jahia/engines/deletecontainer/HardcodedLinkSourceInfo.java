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
 package org.jahia.engines.deletecontainer;

import java.io.*;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia
 * Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class HardcodedLinkSourceInfo implements Serializable {
    private String objectType = "";
    private String objectSubType = "";
    private String name = "";
    private String title = "";
    private int pageID = -1;
    private String pageURL = "";
    private String ID = "";

    public HardcodedLinkSourceInfo () {
    }

    public String getObjectType () {
        return objectType;
    }

    public void setObjectType (String objectType) {
        this.objectType = objectType;
    }

    public String getObjectSubType () {
        return objectSubType;
    }

    public void setObjectSubType (String objectSubType) {
        this.objectSubType = objectSubType;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public int getPageID () {
        return pageID;
    }

    public void setPageID (int pageID) {
        this.pageID = pageID;
    }

    public String getPageURL () {
        return pageURL;
    }

    public void setPageURL (String pageURL) {
        this.pageURL = pageURL;
    }

    public String getID () {
        return ID;
    }

    public void setID (String ID) {
        this.ID = ID;
    }

    private void writeObject (ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject ();
    }

    private void readObject (ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject ();
    }

}