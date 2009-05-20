

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