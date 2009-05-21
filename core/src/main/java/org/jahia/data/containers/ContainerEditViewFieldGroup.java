/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//
//
//
// 28.07.2002 NK


package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.utils.i18n.ResourceBundleMarker;


/**
 * Holds information about a group of Fields used to build the Container Edition Popup.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerEditViewFieldGroup implements Serializable, Comparator<ContainerEditViewFieldGroup> {

    private static final long serialVersionUID = -8309759158817569039L;
    private int pos;
	private String name;
	private String title;
	private String descr;
	private Map<String, ContainerEditViewField> fields = new HashMap<String, ContainerEditViewField>();
	private List<String> fieldNames = new ArrayList<String>();


	//--------------------------------------------------------------------------
	/**
	 * Constructor
	 *
	 * @param String name
	 * @param String title
	 * @param String descr
	 */
	public ContainerEditViewFieldGroup(String name,String title,String descr)
	{
		setName(name);
		setTitle(title);
		setDescr(descr);
		if ( this.title == null || this.title.equals("") ){
			this.title = getName();
		}
	}

	//--------------------------------------------------------------------------
	/**
	 * Return the group name
	 *
	 */
	public String getName(){
		return this.name;
	}

	//--------------------------------------------------------------------------
	/**
	 * Set the Group Name
	 *
	 * @param String name
	 */
	public void setName(String name){
		if ( name == null || name.trim().equals("") ){
			return;
		}
		this.name = name;
		if ( this.title == null || this.title.equals("") ){
			this.title = getName();
		}
	}

	//--------------------------------------------------------------------------
	/**
	 * Return the title
	 *
	 */
	public String getTitle(){
		return this.title;
	}

    //--------------------------------------------------------------------------
    /**
     * Return the title
     *
     */
    public String getTitle(Locale locale){
        if ( locale == null ){
            return this.title;
        }
        String title = this.title;
        if ( title == null ){
            title = "";
        }
        boolean multipleFields = this.title.endsWith(";...");
        if ( multipleFields ){
            int pos = this.title.lastIndexOf(";...");
            title = title.substring(0,pos);
        }
        ResourceBundleMarker resMarker = ResourceBundleMarker.parseMarkerValue(title);
        if ( resMarker == null ){
            return this.title;
        }
        try {
            title = resMarker.getValue(locale);
            if ( multipleFields ){
                title += ";...";
            }
        } catch ( Exception t ){
            title = this.title;
        }
        return title;
    }

	//--------------------------------------------------------------------------
	/**
	 * Set the Group Title used to display the group
	 *
	 * @param String title
	 */
	public void setTitle(String title){
		if ( title == null || title.trim().equals("") ){
			return;
		}
		this.title = title;
	}

	//--------------------------------------------------------------------------
	/**
	 * Return the group descr
	 *
	 */
	public String getDescr(){
		return this.descr;
	}

	//--------------------------------------------------------------------------
	/**
	 * Set the Group Descr
	 *
	 * @param String Descr
	 */
	public void setDescr(String descr){
		if ( descr == null ){
			return;
		}
		this.descr = descr;
	}

	//--------------------------------------------------------------------------
	/**
	 * Add a field
	 *
	 * @param String name, the field name
	 * @param String descr, the descr
	 */
	public void addField(String name,String descr){
		if ( name == null || name.trim().equals("") ){
			return;
		}

		ContainerEditViewField field = new ContainerEditViewField(name,descr);
		if ( fields.get(name) == null ){
			fieldNames.add(name);
		}
		fields.put(name,field);
	}

	//--------------------------------------------------------------------------
	/**
	 * Add a field
	 *
	 * @param String name, the field name
	 * @param String descr, the descr
	 */
	public void addField(ContainerEditViewField editViewField){
		if ( editViewField==null || editViewField.getName() == null || editViewField.getName().trim().equals("") ){
			return;
		}

		if ( fields.get(editViewField.getName()) == null ){
			fieldNames.add(editViewField.getName());
		}
		fields.put(editViewField.getName(),editViewField);
	}

	//--------------------------------------------------------------------------
	/**
	 * field exists
	 *
	 * @param String fieldName
	 */
	public boolean fieldExists(String fieldName){
		int size = fieldNames.size();
		for ( int i=0 ; i<size ; i++ ){
			if ( ((String)fieldNames.get(i)).equals(fieldName) ){
				return true;
			}
		}
		return false;
	}

	//--------------------------------------------------------------------------
	/**
	 * Returns the List field names in this group
	 *
	 */
	public List<String> getFieldNames(){
		return fieldNames;
	}

	//--------------------------------------------------------------------------
	/**
	 * Returns the Map of fields in this group
	 *
	 */
	public Map<String, ContainerEditViewField> getFields(){
		return fields;
	}

	//--------------------------------------------------------------------------
	/**
	 * Returns a given ContainerEditViewField looking at it name
	 *
	 * @param name , the field name
	 * @param ContainerEditViewField
	 */
	public ContainerEditViewField getField(String name){
		return (ContainerEditViewField)fields.get(name);
	}

	//--------------------------------------------------------------------------
	/**
	 * Set the position used  to order this group
	 *
	 */
	public void setPos(int pos){
		this.pos = pos;
	}

	//--------------------------------------------------------------------------
	/**
	 * Get the position used  to order this group
	 *
	 */
	public int getPos(){
		return this.pos;
	}

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their pos
	 *
     * @param Object
     * @param Object
     */
	public int compare(ContainerEditViewFieldGroup c1, ContainerEditViewFieldGroup c2) throws ClassCastException {

		Integer I = new Integer(c1.getPos());
		Integer J = new Integer(c2.getPos());

		return ( I.compareTo(J) );

	}


}
