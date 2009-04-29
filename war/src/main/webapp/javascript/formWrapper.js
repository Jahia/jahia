

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