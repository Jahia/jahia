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
//
//
//
// 28.07.2002 NK


package org.jahia.data.containers;

import java.io.Serializable;



/**
 * Holds information about a Field used to build the Container Edition Popup.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerEditViewField implements Serializable {

	private String name;
	private String descr = "";
	

	//--------------------------------------------------------------------------
	/**
	 * Constructor
	 *
	 * @param String name, the field name
	 */ 
	public ContainerEditViewField(String name)
	{
		if ( name != null && name.trim().length() > 0 ) {
			this.name = name;
		}
	}	
	
	//--------------------------------------------------------------------------
	/**
	 * Constructor
	 *
	 * @param String name, the field name
	 * @param String descr, the description
	 */ 
	public ContainerEditViewField(String name, String descr)
	{
		if ( name != null && name.trim().length() > 0 ){
			this.name = name;
		}
		if ( descr != null ){
			this.descr = descr;
		}
	}	

	//--------------------------------------------------------------------------
	/**
	 * Return the name, can be null
	 *
	 */ 
	public String getName(){
		return this.name;
	}

	//--------------------------------------------------------------------------
	/**
	 * Return the descr, can be null
	 *
	 */ 
	public String getDescr(){
		return this.descr;
	}
	
}
