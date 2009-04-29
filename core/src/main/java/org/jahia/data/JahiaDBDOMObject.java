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
//
//  JahiaDBDOMObject
//
//  NK      25.07.2001
//

package org.jahia.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.DBRowDataFilter;
import org.jahia.utils.xml.DB2DOM;


/**
 * A DB DOM Object
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaDBDOMObject extends JahiaDOMObject {

    private DB2DOM dbDOM = null;

    //--------------------------------------------------------------------------
    /**
     * JahiaDBDOMObject
     *
     *
     * @auhtor NK
     */
    public JahiaDBDOMObject()
    throws JahiaException {

        dbDOM = new DB2DOM();
        xmlDoc = dbDOM.getDocument();
    }

    //--------------------------------------------------------------------------
    /**
     * Append a table in the document
     *
     * @param String tableName used to generate the <table name="tabName"> tag.
     * @param ResultSet rs
     * @see org.jahia.utils.xml.DB2DOM.java
     *
     * @auhtor NK
     */
    public void addTable(	String tableName, ResultSet rs, DBRowDataFilter filter )
    throws JahiaException,SQLException {

        dbDOM.addTable(tableName,rs, filter);
    }

    //--------------------------------------------------------------------------
    /**
     * Append a table in the document
     *
     * @param String tableName used to generate the <table name="tabName"> tag.
     * @param ResultSet rs
     * @see org.jahia.utils.xml.DB2DOM.java
     *
     * @auhtor NK
     */
    public void addTable(	String tableName, ResultSet rs)
    throws JahiaException,SQLException {

        dbDOM.addTable(tableName,rs, null);
    }

}
