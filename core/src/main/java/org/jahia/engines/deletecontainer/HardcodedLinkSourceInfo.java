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