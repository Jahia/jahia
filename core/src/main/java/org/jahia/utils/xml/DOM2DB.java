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
//  DOM2DB
//
//  NK      25.07.2001
//

package org.jahia.utils.xml;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.data.xml.JahiaXmlDocument;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.DBRowDataFilter;
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.JahiaTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Tools used to load a DOM Document created using DB2DOM.
 *
 * @author Khue Nguyen
 * @see org.jahia.tools.xml.DB2DOM
 * @version 1.0
 */
public class DOM2DB extends JahiaXmlDocument {
	
	private static final String CLASS_NAME		= "DOM2DB";
	
	private static final String DATABASE_TAG 	= "database";
	private static final String TABLE_TAG 		= "table";
	private static final String ROW_TAG 		= "row";
	private static final String COLUMN_TAG 		= "column";

	private static final String NAME_ATTRIB		= "name";
	private static final String TYPE_ATTRIB		= "type";
	
	
	List mTables = null;
	
	
   	//--------------------------------------------------------------------------
	/**
     * Constructor
     *
     * @param (String) path, the full path to the application.xml file
     */
    public DOM2DB (String docPath) throws JahiaException
    {
      super(docPath);
      extractDocumentData();
    }


   	//--------------------------------------------------------------------------
   	/**
     * Constructor using a gived parser
     * 
     * @param (String) path, the full path to a xml file
     * @param (Parser) parser, the parser to use
     */
	public DOM2DB (String docPath, org.xml.sax.helpers.ParserAdapter parser) 
	throws JahiaException {
		super(docPath,parser);
		extractDocumentData();
	}


   	//--------------------------------------------------------------------------
	/**
     * Extracts data from the application.xml file. Build the JahiaWebAppsWarPackage object
     * to store extracted data
     */
   	public void extractDocumentData() throws JahiaException {

    	//JahiaConsole.println(CLASS_NAME+".extractDocumentData","started");

       	if (m_XMLDocument == null) {

        	throw new JahiaException( CLASS_NAME,
                                    "The Document is null !",
                                       JahiaException.ERROR_SEVERITY,
                                       JahiaException.ERROR_SEVERITY);
       	}


		if (!m_XMLDocument.hasChildNodes()) {

	    	throw new JahiaException(	CLASS_NAME,
                                    "Main document node has no children",
                                    JahiaException.ERROR_SEVERITY,
                                    JahiaException.ERROR_SEVERITY);
      	}


		Element docElNode = (Element) m_XMLDocument.getDocumentElement();

       	if (!docElNode.getNodeName().equalsIgnoreCase(DATABASE_TAG)) {

        	throw new JahiaException(  "Invalid Document format",
                                        "Tag <"+DATABASE_TAG+"> is not present as starting tag in file",
                                        JahiaException.ERROR_SEVERITY,
                                        JahiaException.ERROR_SEVERITY);
       	}


       	mTables = XMLParser.getChildNodes(docElNode,TABLE_TAG);
	
      	//JahiaConsole.println(CLASS_NAME+".extractDocumentData","done");
   }

   	//--------------------------------------------------------------------------
   	/**
     * Returns an Iterator of all table names
     * 
     * @return List the list of table names
     */
	public List getTableNames() 
	throws JahiaException {
		List v = new ArrayList();
		
		int size = mTables.size();
		for ( int i=0 ; i<size ; i++ ){
			Element el = (Element)mTables.get(i);
			v.add(XMLParser.getAttributeValue(el,NAME_ATTRIB));
		}
		return v;
	}

   	//--------------------------------------------------------------------------
   	/**
     * Returns an Iterator of the tables
     * 
     * @return Iterator the list of tables
     */
	public Iterator getTables() 
	throws JahiaException {
		return mTables.iterator();
	}

   	//--------------------------------------------------------------------------
   	/**
     * Write the contents of a table in DB
     * 
	 * @param (Element) the Element table
	 * @param (Statement) the db statement to use to insert data in table
     */
	public static void sqlInsert(Element table, Statement stmt) 
	throws JahiaException,SQLException {
		sqlInsert(table, stmt, null);
	}
	
   	//--------------------------------------------------------------------------
   	/**
     * Write the contents of a table in DB
     * 
	 * @param (Element) the Element table
	 * @param (Statement) the db statement to use to insert data in table
	 * @param (DBRowDataFilter) an optional data filter. Give a null instance if you want no filtering
     */
	public static void sqlInsert(Element table, Statement stmt, DBRowDataFilter filter ) 
	throws JahiaException,SQLException {

	
		String tableName = XMLParser.getAttributeValue(table,NAME_ATTRIB);
		String insertQuery = "INSERT INTO " + tableName + " (";		

		JahiaConsole.println(CLASS_NAME+".sqlInsert"," table name=" + tableName );
		
		String colName = null;
		String colVal = null;
		int colType = 0;
		
		List rows = XMLParser.getChildNodes(table,ROW_TAG);
		List cols = null;
		Element r = null;
		Element c = null;
		int nbRows = rows.size();
		int nbCols = 0;
		boolean isText = false;
		
		StringBuffer buff = null;
		StringBuffer valBuff = null;
		StringBuffer colBuff = null;
		
		for ( int i=0 ; i<nbRows ; i++ ){

			Map vals = new HashMap();

			r = (Element)rows.get(i);
			buff = new StringBuffer(insertQuery);
			valBuff = new StringBuffer();
			colBuff = new StringBuffer();
			cols = XMLParser.getChildNodes(r,COLUMN_TAG);
			nbCols = cols.size();
			for ( int j=0 ; j<nbCols ; j++ ){
				isText = false;
				c = (Element)cols.get(j);
				colName = XMLParser.getAttributeValue(c,NAME_ATTRIB);								
				colType = Integer.parseInt(XMLParser.getAttributeValue(c,TYPE_ATTRIB));

				//JahiaConsole.println(CLASS_NAME+".sqlInsert"," column node : name=" + colName );
				
                Node textNode = c.getFirstChild();
				
				if ( textNode == null ){
					colVal = "";
				} else if (textNode.getNodeType() == Node.TEXT_NODE) {
                	colVal = textNode.getNodeValue();
                } else {
	                throw new JahiaException(CLASS_NAME,
	                     "Value of column is not in correct format, should only be text",
	                     JahiaException.ERROR_SEVERITY,
	                     JahiaException.ERROR_SEVERITY);
				}                	

				vals.put(colName.toLowerCase(),colVal);
								
                if( (colType == Types.VARCHAR) 
                	|| (colType == Types.CHAR) 
                	|| (colType == Types.BLOB) 
                	|| (colType == Types.LONGVARCHAR) ) {
                    isText = true;
                
                } else if ( colType == 8 ){
                	Float f = new Float(colVal);
                	colVal = (new Long(f.longValue())).toString();
                } else {
                    try {
                        Integer.parseInt(colVal);
                    } catch (NumberFormatException nfe) {
                        isText = true;
                    }
                }
					
				// build columns list and values	
                colBuff.append(colName);
                if(isText) {
                    valBuff.append("'");
                }
                valBuff.append(JahiaTools.quote(JahiaTools.text2XMLEntityRef(colVal,1)));
                if(isText) {
                    valBuff.append("'");
                }

                if(j<(nbCols-1)) {
                    valBuff.append(", ");
                    colBuff.append(", ");
                }

			}			

			buff.append(colBuff.toString().toLowerCase());
			buff.append(") VALUES(");
			buff.append(valBuff.toString());
			buff.append(")");
			
			if ( filter == null || filter.inValue(vals) ){
				if ( valBuff.length()>0 ){		
					stmt.executeUpdate(buff.toString());			
				}
			}
			//JahiaConsole.println(CLASS_NAME+".sqlInsert",buff.toString());
		}
	}

}
