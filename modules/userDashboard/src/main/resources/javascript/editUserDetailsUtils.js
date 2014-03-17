/* REST API GENERAL FUNCTIONS */
/**
 * @Author : Jahia(rahmed)
 * This function make an ajax call to the Jahia API and return the result of this call
 * @param workspace : the workspace to use on this call (live, default)
 * @param locale : the locale to use in ISO 639-1
 * @param way : the way to find the JCR entity on which make the call (nodes, byPath, byType)
 * @param method : the METHOD to call (GET, POST, PUT, DELETE ...)
 * @param endOfURI : the information needed to complete the entity search (id if the way is nodes, path if byPath and type if byType) with the options (/propertie/<propertieName> for example)
 * @param json : the Json to pass with the call
 * @param callback : the callback function for the request (Optional)
 * @param errorCallback : the error function for the request (Optional)
 * @return callResult : the result of the Ajax call
 */
function jahiaAPIStandardCall(urlContext,workspace,locale, way, endOfURI,method, json , callback, errorCallback)
{
    var callResult;
    $.ajax({
        contentType: 'application/json',
        data: json,
        dataType: 'json',
        processData: false,
        type: method,
        url: urlContext+"/"+API_URL_START+"/"+workspace+"/"+locale+"/"+way+"/"+endOfURI,
        success:function(result) {
            //calling the callback
            if(!(callback === undefined))
            {
                callback(result,json);
            }
            callResult=result;
        },
        error : function(result){
            if(result.responseJSON != undefined)
            {
                return errorCallback(result, json);
            }
        }
    });
    return callResult;
}

/**
 * @Author : Jahia(rahmed)
 * This function serialize a form (or some form elements with a given css class) to a JSON String,
 * then it send a Put Request to the JCR Rest API
 * @param formId : the id of the forms containing the elements to serialize
 * @param nodeIdentifier : the identifier of the node on witch do the PUT request
 * @param locale : the current locale for the PUT request
 * @param fieldsClass : the class of the form elements to serialize
 * @param callback : the callback function for the PUT Request (Optional)
 * @param errorCallback : the error function for the PUT request (Optional)
 */
function formToJahiaCreateUpdateProperties(formId, nodeIdentifier, locale, fieldsClass, callback, errorCallback)
{
    var deleteList=new Array();
    //JSon Serialized String
    var serializedForm;
    var serializedObject;
    var result;
    //Creating the Json String to send with the PUT request
    serializedObject = $(formId).serializeObject(fieldsClass,deleteList);
    serializedForm = JSON.stringify(serializedObject);
    if(deleteList.length>0)
    {
        var deleteJsonString = "[";
        //Deleting the properties listed to be deleted
        for (var currentPropertie = 0; currentPropertie<deleteList.length;currentPropertie++)
        {
            if(deleteJsonString.length>1)
            {
                deleteJsonString+=",";
            }
            deleteJsonString+="\""+deleteList[currentPropertie]+"\"";
        }
        deleteJsonString+="]";
        //Delete the current propertie
        var endOfURI = nodeIdentifier+"/properties";
        result = jahiaAPIStandardCall(context,"default",locale, "nodes", endOfURI,"DELETE", deleteJsonString , undefined, errorCallback);
    }
    if (serializedForm != '{"properties":{"undefined":{"value":""}}}')
    {
        //Post the Serialized form to Jahia
        return jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", serializedForm , callback, errorCallback);
    }
    else
    {
      callback(result, serializedForm);
    }
}

/**
 * @Author : Jahia(rahmed)
 * This function serialize a form (or some form elements with a given css class) to an array, then browse it and build a JSon Object with it
 * All the form inputs with empty values are not serialized and put in the delete properties Table
 * @param fieldsClass : the class of the form elements to serialize
 * @param deleteList : Table of the properties to delete
 * @returns JSon Object containing all the properties to send to API
 */
$.fn.serializeObject = function(fieldsClass, deleteTable)
{
    var serializedArray;

    //index to browse the deleteTable
    var deleteIndex=0;

    //Serializing the form (or the by cssCLass) to an Array
    if(fieldsClass === undefined)
    {
        serializedArray = this.serializeArray();
    }
    else
    {
        serializedArray = $('.'+fieldsClass);
    }

    //Building the JSON Object from the array
    var serializedObject = {};

    //For each form element
    $.each(serializedArray, function() {
        var name = this.name;
        var value = this.value;

        //Adding to delete List all the form elements with empty values
        if(value=="")
        {
            deleteTable[deleteIndex] = this.name;
            deleteIndex++;
        }
        else
        {
            //formatting dates
            if(this.getAttribute("jcrtype") != undefined && this.getAttribute("jcrtype") == "Date")
            {
                value = new Date(value).toISOString();
            }

            //adding to object
            if (serializedObject[name]) {
                if (!serializedObject[name].push) {
                    serializedObject[name] = [serializedObject[name]];
                }
                serializedObject[name].push(value || '');
            } else {
                serializedObject[name] = {"value" : value || ''};
            }
        }
    });
    return {"properties" : serializedObject};
};

/* Edit User Details Functions */
/**
 * @Author : Jahia(rahmed)
 * Edit User Details Callback Function
 * This function is called after the user properties Update
 * if the propertie preferredLanguage is updated the page is fully reloaded
 * in the other case an ajax load is enough to refresh the properties display
 * @param result : The PUT request result
 * @param sent : The sent Json with the PUT request (to check for the preferredLanguage Properties)
 */
var ajaxReloadCallback = function (result,sent)
{

    if (sent != undefined && sent.indexOf("preferredLanguage") != -1)
    {
        var windowToRefresh = window.parent;
        if(windowToRefresh == undefined)
            windowToRefresh = window;
        windowToRefresh.location.reload();
    } else
    {
        $('#editDetailspage').load(getUrl);
    }
}

/* Edit User Details Functions */
/**
 * @Author : Jahia(rahmed)
 * Edit User Details Callback Function
 * This function is called after the user properties Update in error cases
 * It formats and displays the Jahia API error messages
 * @param result : The PUT request result
 * @param sent : The sent Json with the PUT request (to check for the preferredLanguage Properties)
 */
var formError = function (result,sent)
{

    $("."+CurrentCssClass+".errorMessage").html("");
    var resultObject = {"message":""+result.responseJSON.message+"", "properties":[]};
    if(result.responseJSON != undefined)
    {
        //looking for JCR property name
        var propertiesArray=new Array();
        if(result.responseJSON.message.indexOf("j:")!=-1)
        {
            //split message on spaces
            propertiesArray=result.responseJSON.message.split(" ");

            for(var property=0;property<propertiesArray.length;property++)
            {
                if(propertiesArray[property].indexOf("j:")!=-1)
                {
                    if (resultObject["properties"]) {
                        if (!resultObject["properties"].push) {
                            resultObject["properties"] = [resultObject["properties"]];
                        }
                        resultObject["properties"].push(propertiesArray[property] || '');
                    } else {
                        resultObject["properties"] = {"keys" : propertiesArray[property] || ''};
                    }
                }
            }
        }
        var errorMessage = ""+resultObject["message"];
        var propertiesArray = resultObject["properties"];

        for(var propertyName = 0;propertyName<propertiesArray.length;propertyName++)
        {
            errorMessage=errorMessage.replace(propertiesArray[propertyName], propertiesNames[propertiesArray[propertyName]]);
        }
        $("."+CurrentCssClass+".errorMessage").hide();
        $("."+CurrentCssClass+".errorMessage").stop().fadeIn();
        $("."+CurrentCssClass+".errorMessage").stop().fadeOut();
        $("."+CurrentCssClass+".errorMessage").html($("."+CurrentCssClass+".errorMessage").html()+"<div>"+errorMessage+"</div>");
        $("."+CurrentCssClass+".errorMessage").fadeIn('slow').delay(1500).fadeOut('slow');

    }
    return resultObject;
}

/**
 * @Author : Jahia(rahmed)
 * This function verify the phone and email fields of an adress
 * the phone fields must have the 'phone' css class
 * The email fields must have the 'email' css class
 * @param cssClass : The class of the form adress fields
 * @param phoneErrorId : The css id of the div that will display the phone error message
 * @param emailErrorId : The css id of the div that will display the email error message
 * @return true if the address is valid and false in the other case
 */
function verifyAndSubmitAddress(cssClass, phoneErrorId, emailErrorId)
{
    var phoneValidation=true;
    var emailValidation=true;

    var phoneRegex = /^[0-9]{1,45}$/;
    var emailRegex = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;

    $.each($("."+cssClass+".phone"),function(){
        if($(this).val().length>0 && $(this).val().length<5)
        {
            phoneValidation=false;
        }
        if($(this).val().length>0 && !phoneRegex.test($(this).val()))
        {
            phoneValidation=false;
        }
    });

    $.each($("."+cssClass+".email"),function(){
        if($(this).val().length>0 && !emailRegex.test($(this).val()))
        {
            emailValidation=false;
        }
    });

    //displaying the error messages
    $("#"+phoneErrorId).hide();

    $("#"+emailErrorId).hide();
    if(!phoneValidation)
    {
        $('#'+phoneErrorId).fadeIn('slow').delay(1500).fadeOut('slow');
    }
    if(!emailValidation)
    {
        $('#'+emailErrorId).fadeIn('slow').delay(1500).fadeOut('slow');
    }
    return phoneValidation && emailValidation;
}

/**
 * @Author : Jahia(rahmed)
 * This function is called after the user picture Upload
 * to check if the avatar is created
 * @param testUrl: the file url to test
 * @return: 200 for success and 0 or negative value on error
 */
function urlExists(testUrl) {
    var http = jQuery.ajax({
        type:"HEAD",
        url: testUrl,
        async: false
    })
    return http.status;
}

/**
 * @Author : Jahia(rahmed)
 * This function post the privacy properties in order to update JCR
 * The post is in string in order to allow multiple values on publicProperties.
 * @param propertiesNumber: The number of properties in the loop
 * @param idNumber: The id of the switch triggering the update (for the check image near the switch)
 * @param value: State of the property switch
 * @param nodeIdentifier: The end of URI for the jahia API Standard Call is the user id
 * @param locale: The locale for the jahia API Standard Call
 */
function editVisibility(propertiesNumber,idNumber, value, nodeIdentifier, locale)
{
    var jsonString = "{\"properties\":{\"j:publicProperties\":{\"multiValued\":true,\"value\":[";
    var filled=false;
    //the image to put near the switch once the post is successful
    var doneImageId = '';
    if(value == true)
    {
        doneImageId = '#switchOn'+idNumber;
    }
    else
    {
        doneImageId = '#switchOff'+idNumber;
    }

    //Looping on properties and filling the data
    for(var currentPropertieIndex=0;currentPropertieIndex<propertiesNumber;currentPropertieIndex++)
    {
        //replacing the list of public properties by the list of all the switches in true state
        if($('#publicProperties'+currentPropertieIndex).bootstrapSwitch('state') == true)
        {
            if(filled==true)
            {
                jsonString += ",";
            }
            jsonString += "\""+$("#publicProperties"+currentPropertieIndex).val()+"\"";
            filled=true;
        }
    }
    jsonString += "]}}}";
    //posting the properties visibility to JCR
    CurrentCssClass = "privacyField"
    jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", jsonString , function (){
        //hiding all the others images near the switches
        $('.switchIcons').hide();

        //showing the image near the switch
        $(doneImageId).fadeIn('slow').delay(1000).fadeOut('slow');
    }, formError);
}

/**
 * @Author : Jahia(rahmed)
 * This function changes the user Password calling the action changePassword.do
 * The new password is picked directly from the password change form in this page.
 * The error messages are displayed in the '#passwordErrors' div
 * The success messages are displayed in the '#passwordSuccess' div
 * @param oldPasswordMandatory: The error message for the empty old password case
 * @param confirmationMandatory: The error message for the empty confirmation case
 * @param passwordMandatory: The error message for the empty password case
 * @param passwordNotMatching: The error message for the non matching passwords case
 */
function changePassword(oldPasswordMandatory,confirmationMandatory, passwordMandatory, passwordNotMatching)
{
    //passwords checks
    if ($("#oldPasswordField").val() == "") {
        $("#passwordErrors").hide();
        $('#passwordErrors').html(oldPasswordMandatory);
        $('#passwordErrors').fadeIn('slow').delay(2000).fadeOut('slow');
        $('#oldPasswordField').focus();
    }
    else if ($("#passwordField").val() == "") {
        $("#passwordErrors").hide();
        $('#passwordErrors').html(passwordMandatory);
        $('#passwordErrors').fadeIn('slow').delay(2000).fadeOut('slow');
        $('#passwordField').focus();
    }

    else if ($("#passwordconfirm").val() == "") {
        $("#passwordErrors").hide();
        $('#passwordErrors').html(confirmationMandatory);
        $('#passwordErrors').fadeIn('slow').delay(2000).fadeOut('slow');
        $('#passwordconfirm').focus();
    }

    else if ($("#passwordField").val() != $("#passwordconfirm").val())
    {
        $("#passwordField").val("");
        $("#passwordconfirm").val("");
        $("#passwordErrors").hide();
        $('#passwordErrors').html(passwordNotMatching);
        $('#passwordErrors').fadeIn('slow').delay(2000).fadeOut('slow');
        $('#passwordField').focus();
    }
    else{
        $.post( changePasswordUrl, { oldpassword: $("#oldPasswordField").val(), password: $("#passwordField").val(), passwordconfirm:  $("#passwordconfirm").val()},
            function(result)
            {
                if(result['result'] == 'success')
                {
                    switchRow('password');
                    $('#passwordSuccess').addClass("text-success");
                    $('#passwordSuccess').html(result['errorMessage']);
                    $('#passwordSuccess').fadeIn('slow').delay(2000).fadeOut('slow');
                }
                else
                {
                    $("#passwordField").val("");
                    $("#passwordconfirm").val("");
                    $("#oldPasswordField").val("");
                    $("#passwordErrors").hide();
                    $("#passwordErrors").html(result['errorMessage']);
                    $("#passwordErrors").fadeIn('slow').delay(2000).fadeOut('slow');
                    $("input[name="+result['focusField']+"]").focus();
                }
            },
            'json');
    }
}

/* Edit User Details User Picture */
/**
 * @Author : Jahia(rahmed)
 * This function Upload a picture the user picked and update his user picture property with it
 * The picture to upload is directly picked from the form
 * @param imageId : The id of the input file form field
 * @param locale: The locale for the API call URL build
 * @param nodepath : The path of the user node for the API call URL build
 * @param userId : The user Id for the picture propertie Update
 * @param callbackFunction : the callback function
 * @param errorFunction : The error callback function
 */
function updatePhoto(imageId, locale, nodePath, userId, callbackFunction, errorFunction)
{
    var uploadUrl = context+"/"+API_URL_START+"/default/"+locale+"/byPath"+nodePath+"/files/profile";

    //checking if the file input has been filled
    if( $("#"+imageId).val() == "")
    {
        $("#imageUploadEmptyError").fadeIn('slow').delay(4000).fadeOut('slow');
    }
    else
    {
        //Upload the picture
        $.ajaxFileUpload({
                url: uploadUrl,
                secureuri:false,
                fileElementId: imageId,
                dataType: 'json',
                success: function(result)
                {
                    if(result["localizedMessage"]===undefined)
                    {
                        var fileId = result["id"];
                        var fileName = result["name"];
                        //JSon Serialized String
                        var jsonData;
                        var endOfURI = userId+"/properties/j__picture";

                        //Creating the Json String to send with the PUT request
                        jsonData = "{\"value\":\""+fileId+"\"}";

                        //Requesting the Jahia API to update the user picture
                        jahiaAPIStandardCall(context,"default",locale, "nodes", endOfURI,"PUT", jsonData, function(){
                            //Check If Jahia had the time to create the Avatar
                            //building the avatar URL
                            var imageUrl = context+"/files/default/users/root/files/profile/"+fileName+"?t=avatar_120";
                            var avatarExists=false;
                            var ExistsCode=-1;
                            //Check 5 times if the avatar exists
                            for(var testNumber = 0 ; !avatarExists &&  testNumber<5; testNumber++)
                            {
                                ExistsCode =urlExists(imageUrl);
                                if(ExistsCode!=404)
                                {
                                    //Avatar has been found refreshing the profile display ...
                                    avatarExists=true;
                                    callbackFunction(result, "picture");
                                }
                            }

                        }, errorFunction);}
                    else
                    {
                        $("#imageUploadError").fadeIn('slow').delay(4000).fadeOut('slow');
                    }
                },
                error: function(result){
                    errorFunction(result);
                }
            });
        }
}
/**
 * @Author : Jahia(rahmed)
 * This function make a JSON Post of ckeditor contained in a Row
 * It also defines the error message div css class for the error callback
 * The div has to be defined following the pattern : <ID>Field
 * @param rowId: the Id of the from which post the form entries
 * @param nodeIdentifier : the endofURI for the Jahia API Standard Call
 * @param locale : the locale for the Jahia API Standard Call
 * @param callback : the callback function for the Jahia API Standard Call
 * @param errorCallback : the error callback function for the Jahia API Standard Call
 */
function saveCkEditorChanges(rowId,nodeIdentifier ,locale, callback, errorCallback )
{

    //Opening the JSON
    var jsonPropTable = {};
    var jsonTable = {};

    //getting the ckeditors
    var editorId = rowId+"_editor";
    var editor = CKEDITOR.instances[editorId];
    var editorValue=editor.getData().trim();

    jsonPropTable["j:"+rowId] = {"value" : editorValue};

    jsonTable["properties"]=jsonPropTable;

    //calling ajax POST
    var myJSONText = JSON.stringify(jsonTable);
    CurrentCssClass=rowId+"Field";

    //Calling the Jahia Restful API to Update the node
    jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", myJSONText, callback, errorCallback);
}


var currentElement = "";
var currentForm = "";
/**
 * @Author : Jahia(rahmed)
 * This function switches a row from the display view to the form view hiding other form view already active
 * @elementId : id of the row to switch
 */
function switchRow(elementId)
{
    //building css element id
    elementId="#"+elementId;

    //building css form id
    var elementFormId = elementId+"_form";
    //Checking which element to show and which element to hide
    if( $(elementId).is(":visible"))
    {
        if(currentForm!='')
        {
            $(currentForm).hide();
            $(currentElement).show();
        }
        //Hide the display row
        $(elementId).hide();
        //Show the form
        $(elementFormId).show();
    }
    else
    {
        //Hide the Form
        $(elementFormId).hide();
        //Show the display Row
        $(elementId).show();
    }
    currentElement = elementId;
    currentForm = elementFormId;
}