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
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

    function ActionItem (theResourceID,theActionName, theActionUrl){
    	this.theResourceID 	= theResourceID;
    	this.theActionName 	= theActionName;
    	this.theActionUrl 	= theActionUrl;
    	return this;
    }
    
    function ActionLauncher(){
    	this.length = 0;
		this.actions 					= new Array();
		this.GetAction					= GetAction;
		this.AddAction					= AddAction;
		this.LaunchAction				= LaunchAction;
		this.LaunchActionForSelectBox	= LaunchActionForSelectBox;
		return this;
	}

	function GetAction(resourceID,actionName){
	
		for ( var i=0 ; i<this.length ; i++ )
		{
			var theAction = this[i];
	
			if ( (this[i].theResourceID == resourceID) && (this[i].theActionName == actionName) )
			{
				return theAction;
			}
		}
		return null;
	}

	function AddAction(theResourceID,theActionName,theActionUrl)
	{
		this[this.length] = new ActionItem(theResourceID,theActionName,theActionUrl);
		this.length++;
	}

	function LaunchAction(theResourceID,theActionName)
	{
		var theAction = this.GetAction(theResourceID,theActionName);
		if ( theAction != null ){
			eval(theAction.theActionUrl);
		}
	}

	function LaunchActionForSelectBox(selectBox,actionName,multiple)
	{
		if ( selectBox == null ){
			alert("Javascript element is not available");
		} else if ( selectBox.selectedIndex == -1 ){
			alert("At least one item must be selected");
		} else if (multiple == "true") {
			// look for the first action of this action name found for this resource
			// action of this type typically consist of a form submit with multiple values handling.
			this.LaunchAction(selectBox.options[selectBox.selectedIndex].value,actionName);
		} else {
			// check if several options are selected
			firstSelected = -1;
			nbSel = 0;
			for ( i=0 ;i<selectBox.options.length; i++ ){
				if ( selectBox.options[i].selected ){
					if ( firstSelected == -1 ){
						firstSelected = i;
					}
					nbSel += 1;
				}
				if ( nbSel > 1 ){
					break;
				}
			}	
			if ( nbSel > 1 ){
				alert("This function only applies to one item at a time.");
				// deselect all
				selectBox.selectedIndex = -1;
				// reselect only the first
				selectBox.selectedIndex = firstSelected;
			} else {
				this.LaunchAction(selectBox.options[selectBox.selectedIndex].value,actionName);
			}
		}
	}
	
	
	// End ActionLauncher CLass-----------------------------------------	
	//--------------------------------------------------------------------------