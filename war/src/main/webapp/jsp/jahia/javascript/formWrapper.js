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

    function InputValueItem (theValue,isSelected){
    	this.theValue 	= theValue;
    	this.isSelected	= isSelected;
    	return this;
    }

    /**
     * theName			: the input name
     * theType			: SelectBox, Hidden, ... 
     */
    function InputItem (theName,theType){
    	this.length = 0;
    	this.theName 		= theName;
    	this.theType 		= theType;
    	this.AddValueItem	= AddValueItem;
    	return this;
    }
    
	function AddValueItem(valueItem){
		this[this.length] = valueItem;
		this.length++;
	}		

    function FormWrapper(){
    	this.length = 0;
		this.theName					= "";
		this.theAction					= "";
		this.theEncoding				= "";
		this.AddInput					= AddInput;
		this.WrapForm					= WrapForm;
		this.GetSimpleInput				= GetSimpleInput;
		this.GetSelectInput				= GetSelectInput;
		this.GetRadioOrCheckBoxInput	= GetRadioOrCheckBoxInput;
		return this;
	}


	function AddInput(theInput)
	{
		if ( theInput != null ){
			this[this.length] = theInput;
			this.length++;
		}
	}

	/**
	 * Parse an entire html form and build the corresponding Input bean objects.
	 */
	function WrapForm(theForm)
	{
		if ( theForm == null ){
			alert("FormWrapper.WrapForm : theForm object is null ??");
			return;
		}
		//alert("FormWrapper.WrapForm : Started with form : " + theForm.name);

		this.theName = theForm.name;
		this.theAction = theForm.action;
		this.theEncoding = theForm.encoding;
		
		var nbEl = theForm.elements.length;
		for ( var i=0 ; i<nbEl ; i++ )
		{
			//alert("FormWrapper.WrapForm : found element : " + theForm.elements[i].name);
			switch (theForm.elements[i].type){
				case "select-one"	:
					this.AddInput(GetSelectInput(theForm.elements[i]));			
					break;
				case "select-multiple"	:
					this.AddInput(GetSelectInput(theForm.elements[i]));			
					break;
				case "radio"	:
					this.AddInput(GetRadioOrCheckBoxInput(theForm.elements[i]));			
					break;
				case "checkbox"	:
					this.AddInput(GetRadioOrCheckBoxInput(theForm.elements[i]));			
					break;
				default	:
					this.AddInput(GetSimpleInput(theForm.elements[i]));
			}	
		}
	}

	//--------------------------------------------------------------------------
	function GetSimpleInput(element){
		
		if ( element.type == "file"
			 || element.type == "hidden"
			 || element.type == "password"
			 || element.type == "text"
			 || element.type == "textarea" )
		{

			var theInput = new InputItem(element.name,element.type);
			var theInputValueItem = new InputValueItem (element.value,true)
			theInput.AddValueItem(theInputValueItem);

		}
			
		return theInput;
	}	

	//--------------------------------------------------------------------------
	function GetSelectInput(element){

		var theInput = new InputItem(element.name,element.type);

		for ( i=0 ;i<element.options.length; i++ ){
			var theInputValueItem = new InputValueItem (element.options[i].value,element.options[i].selected);
			theInput.AddValueItem(theInputValueItem);
		}	
		return theInput;
	}	
	
	//--------------------------------------------------------------------------
	function GetRadioOrCheckBoxInput(element){

		var theInput = new InputItem(element.name,element.type);
		var theInputValueItem = new InputValueItem (element.value,element.checked)
		theInput.AddValueItem(theInputValueItem);
	
		return theInput;
	}	

	//--------------------------------------------------------------------------
	// for performance issue, avoid this processing
	function EscapeValue(val){
		
		if ( val == null || val == "" ){
			return val;
		}	
		
		var result = "";
		for ( var i=0 ; i<val.length ; i++ )
		{
			switch (val.charAt(i)){
				case '&'	:
					result += "&amp;";
					break;
				case '<'	:
					result += "&lt;";
					break;
				case '>'	:
					result += "&gt;";
					break;
				case '\\'	:
					result += "&#092;";
					break;
				case "'"	:
					result += '\'';
					break;
				case '"'	:
					result += '&quot;';
					break;
				default		:
					result += val.charAt(i);
			}
		}
		result = result.replace(/\s/g,"&#032;");
		//alert(result);
		return result;		
	}
	
	// End formWrapper CLass-----------------------------------------	
	//--------------------------------------------------------------------------