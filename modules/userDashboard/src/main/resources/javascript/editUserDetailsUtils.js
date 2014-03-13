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
    //Post the Serialized form to Jahia
    $.ajax({
        contentType: 'application/json',
        data: json,
        dataType: 'json',
        processData: false,
        type: method,
        url: urlContext+"/modules/api/"+workspace+"/"+locale+"/"+way+"/"+endOfURI
    }).done(function(result){
        //calling the callback
        if(!(callback === undefined))
        {
            callback(result,json);
        }
        callResult=result;
    }).error(function(result){
        if(!(errorCallback === undefined))
        {
            errorCallback(result, json);
        }
        callResult=result;
    });
    return callResult;
}

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
function jahiaAPIDELETE(urlContext,workspace,locale, endOfURI,method, callback, errorCallback)
{
    var callResult;
    //Post the Serialized form to Jahia
    $.ajax({
        contentType: 'application/json',
        data: json,
        dataType: 'json',
        processData: false,
        type: method,
        url: urlContext+"/modules/api/"+workspace+"/"+locale+"/nodes/"+endOfURI
    }).done(function(result){
        //calling the callback
        if(!(callback === undefined))
        {
            callback(result,json);
        }
        callResult=result;
    }).error(function(result){
        if(!(errorCallback === undefined))
        {
            errorCallback(result, json);
        }
        callResult=result;
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
 * @param fieldsClass : the class of the form elements to serialize (Optional)
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
    var PostUrl = "/modules/api/default/"+locale+"/nodes/"+nodeIdentifier;

    //Creating the Json String to send with the PUT request
    serializedObject = $(formId).serializeObject(fieldsClass,deleteList);
    serializedForm = JSON.stringify(serializedObject);

    //Deleting the properties listed to be deleted
    for (var currentPropertie = 0; currentPropertie<deleteList.length;currentPropertie++)
    {
        var endOfURI = nodeIdentifier+"/properties/"+deleteList[currentPropertie];
        //Delete the current propertie
        result = jahiaAPIStandardCall(context,"default",locale, "nodes", endOfURI,"DELETE", "" , undefined, errorCallback);
    }
    if (serializedForm != '{"properties":{}}')
    {
        //Post the Serialized form to Jahia
        result = jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", serializedForm , callback, errorCallback);
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
            deleteTable[deleteIndex] = this.name.replace(":","__");
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


function verifyAndSubmitAddress(cssClass, phoneErrorId, emailErrorId)
{
    $('#'+emailErrorId).hide();
    $('#'+phoneErrorId).hide();

    var phoneRegex = /^[0-9]{1,45}$/;
    var emailRegex = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;;
    var phoneSize = true;
    var phoneForm = true;
    var emailForm = true;

    //Phone fields verification
    $.each($("."+cssClass+".phone"), function(){
        if($(this).val().length>0 && $(this).val().length<5)
        {
            phoneSize=false;
        }
        if($(this).val().length>0 && !phoneRegex.test($(this).val()))
        {
            phoneForm=false;
        }
    });
    //Email fields verification
    $.each($("."+cssClass+".email"), function(){
        if(!emailRegex.test($(this).val()))
        {
            emailForm=false;
        }
    });

    //Submiting
    if(phoneSize && phoneForm && emailForm)
    {
        updateProperties(cssClass);
    }
    else{
        if(!phoneSize || !phoneForm)
        {
            $('#'+phoneErrorId).fadeIn('slow').delay(4000).fadeOut('slow');
        }
        if(!emailForm)
        {
            $('#'+emailErrorId).fadeIn('slow').delay(4000).fadeOut('slow');
        }
    }


}
/* Edit User Detail Global variables */


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


function urlExists(testUrl) {
    var http = jQuery.ajax({
        type:"HEAD",
        url: testUrl,
        async: false
    })
    return http.status;
    // this will return 200 on success, and 0 or negative value on error
}


/**
 * Edit User Details Callback Function
 * This function is called after the user properties Update in case the put request end with an error state
 */
var formError = function ()
{
    console.log('Error ...');
}

/**
 * @Author : Jahia(rahmed)
 * This function post the privacy properties in order to update JCR
 * The post is in string in order to allow multiple values on publicProperties as the Jahia API
 * Doesn't allow the JSON table attributes.
 * propertiesNumber: the number of properties in the loop
 * idNumber: The id of the switch triggering the update (for the check image near the switch)
 * propertiesNumber: The updated state by the switch (for the check image near the switch)
 */
function editVisibility(propertiesNumber,idNumber, value, nodeIdentifier, locale)
{
    var jsonString = "{\"properties\":{\"j:publicProperties\":{\"multiValued\":true,\"value\":[";
    var urlPostChanges ="/modules/api/default/"+locale+"/nodes/"+nodeIdentifier;
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
    jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", jsonString , function (){
        //hiding all the others images near the switches
        $('.switchIcons').hide();

        //showing the image near the switch
        $(doneImageId).fadeIn('slow').delay(1000).fadeOut('slow');
    }, undefined);
}

/**
 * @Author : Jahia(rahmed)
 * This function changes the user Password calling the action changePassword.do
 * The new password is picked directly from the password change form in this page.
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
 * This function Upload a picture the user picked and update his user picture with it
 * The picture to upload is directly picked from the form
 */
function updatePhoto(imageId, locale, nodePath, userId, callbackFunction, errorFunction)
{
    var uploadUrl = context+"/modules/api/default/"+locale+"/byPath"+nodePath+"/files/profile";
    //Upload the picture
    var validFilename = false;

    var filename = $("#"+imageId).val();
    var regex = /[:<>[\]*|"\\]/;

    if( filename == "")
    {
        $("#imageUploadEmptyError").fadeIn('slow').delay(4000).fadeOut('slow');
    }
    else
    {
        /*if(regex.test($("#"+imageId).val()))
        {
            $("#imageUploadNameError").fadeIn('slow').delay(4000).fadeOut('slow');
        }
        else
        {*/
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
                        /*var jsonResponse = JSON.parse(result.responseText);*/
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
    /*}*/
}

/**
 * @Author : Jahia(rahmed)
 * This function make a JSON Post of all the form entries (textInputs, select and ckeditors) contained in a Row
 * rowId: the Id of the from which post the form entries
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

    //Calling the Jahia Restful API to Update the node
    jahiaAPIStandardCall(context,"default",locale, "nodes", nodeIdentifier,"PUT", myJSONText, callback, errorCallback);
}


var currentElement = "";
var currentForm = "";
/**
 * @Author : Jahia(rahmed)
 * This function switch a row from the display view to the form view
 * elementId : id of the row to switch
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