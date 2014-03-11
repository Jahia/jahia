/* REST API GENERAL FUNCTIONS */
/**
 * @Author : Jahia(rahmed)
 * This function serialize a form (or some form elements with a given css class) to an array to a JSON String,
 * then it send a Put Request to the JCR Rest API
 * @param formId : the id of the forms containing the elements to serialize
 * @param nodeIdentifier : the identifier of the node on witch do the PUT request
 * @param locale : the current locale for the PUT request
 * @param fieldsClass : the class of the form elements to serialize (Optional)
 * @param callback : the callback function for the PUT Request (Optional)
 * @param errorCallback : the error function for the PUT request (Optional)
 */
function formToJahiaAPICreateUpdateProperties(formId, nodeIdentifier, locale, fieldsClass, callback, errorCallback)
{
    //JSon Serialized String
    var serializedForm;
    var PostUrl = "/modules/api/default/"+locale+"/nodes/"+nodeIdentifier;
    //Creating the Json String to send with the PUT request
    serializedForm = JSON.stringify($(formId).serializeObject(fieldsClass));
    console.log("JSON STRING : "+serializedForm);
    console.log("URL : "+PostUrl);

    //Post the Serialized form to Jahia
    $.ajax({
        url: PostUrl,
        type: 'PUT',
        contentType: 'application/json',
        data: serializedForm,
        processData: false,
        dataType: 'json'
    }).done(function(result){
        //calling the callback
        callback(result,serializedForm);
    }).error(function(result){
        errorCallback(result);
    });
}

/**
 * @Author : Jahia(rahmed)
 * This function serialize a form (or some form elements with a given css class) to an array to a JSON String,
 * then it send a Put Request to the JCR Rest API
 * @param formId : the id of the forms containing the elements to serialize
 * @param nodeIdentifier : the identifier of the node on witch do the PUT request
 * @param locale : the current locale for the PUT request
 * @param fieldsClass : the class of the form elements to serialize (Optional)
 * @param callback : the callback function for the PUT Request (Optional)
 * @param errorCallback : the error function for the PUT request (Optional)
 */
function jahiaAPICreateUpdateProperties(propertieValue,propertieName, nodeIdentifier, locale, callback, errorCallback)
{
    //JSon Serialized String
    var jsonData;
    var PostUrl = "/modules/api/default/"+locale+"/nodes/"+nodeIdentifier;
    //Creating the Json String to send with the PUT request
    jsonData = "{\"properties\":{\""+propertieName+"\": { \"value\" : \""+propertieValue+"\"}}}";
    //Post the Serialized form to Jahia
    $.ajax({
        contentType: 'application/json',
        data: jsonData,
        dataType: 'json',
        processData: false,
        type: 'PUT',
        url: PostUrl
    }).done(function(result){
        //calling the callback
        if(!(callback === undefined))
        {
            callback(result,jsonData);
        }
    }).error(function(result){
        if(!(errorCallback === undefined))
        {
            errorCallback(result);
        }
    });
}

/**
 * @Author : Jahia(rahmed)
 * This function serialize a form (or some form elements with a given css class) to an array, then browse it and build a JSon Object with it
 * * @param fieldsClass : the class of the form elements to serialize (Optional)
 * @returns JSon Object containing all the properties to send to API
 */
$.fn.serializeObject = function(fieldsClass)
{
    var serializedArray;
    if(fieldsClass === undefined)
    {
        serializedArray = this.serializeArray();
    }
    else
    {
        serializedArray = $('.'+fieldsClass);
    }
    var serializedObject = {};
    $.each(serializedArray, function() {
        if(this.getAttribute("jcrtype") != undefined && this.getAttribute("jcrtype") == "Date")
        {
            this.value = new Date(this.value).toISOString();
        }
        if (serializedObject[this.name]) {
            if (!serializedObject[this.name].push) {
                serializedObject[this.name] = [serializedObject[this.name]];
            }
            serializedObject[this.name].push(this.value || '');
        } else {
            serializedObject[this.name] = {"value" : this.value || ''};
        }
    });
    return {"properties" : serializedObject};

};

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
    if (sent.contains('preferredLanguage'))
    {
        var windowToRefresh = window.parent;
        if(windowToRefresh == undefined)
            windowToRefresh = window;
        windowToRefresh.location.reload();
    } else
    {
        console.log('CallBack Called ...');
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
    console.log('error');
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
    //Calling the Jahia Restful API to Update the node
    $.ajax({
        url: urlPostChanges,
        type: 'PUT',
        contentType: 'application/json',
        data: jsonString,
        processData: false,
        dataType: 'json'
    }).done(function (){
        //hiding all the others images near the switches
        $('.switchIcons').hide();

        //showing the image near the switch
        $(doneImageId).fadeIn('slow').delay(1000).fadeOut('slow');
    });
}

/**
 * @Author : Jahia(rahmed)
 * This function changes the user Password calling the action changePassword.do
 * The new password is picked directly from the password change form in this page.
 */
function changePassword(passwordMandatory, passwordNotMatching)
{
    //passwords checks
    if ($("#passwordField").val() == "") {
        /*alert("<fmt:message key='serverSettings.user.errors.password.mandatory'/>");*/

        alert(passwordMandatory);
        return false;
    }
    if ($("#passwordField").val() != $("#passwordconfirm").val()) {
        /*alert("<fmt:message key='serverSettings.user.errors.password.not.matching'/>");*/
        alert(passwordNotMatching);
        return false;
    }
    var params = {password: $("#passwordField").val()};
    $.post( changePasswordUrl, { password: $("#passwordField").val(), passwordconfirm:  $("#passwordconfirm").val()},
        function(result)
        {
            if(result['result'] != 'success')
            {
                $('#passwordError').html(result['errorMessage']);
                $('#passwordError').fadeIn('slow').delay(4000).fadeOut('slow');
                $('#passwordField').focus();
            }
            else
            {
                switchRow('password');
                $('#passwordSuccess').addClass("text-success");
                $('#passwordSuccess').html(result['errorMessage']);
                $('#passwordSuccess').fadeIn('slow').delay(4000).fadeOut('slow');
            }
        },
        'json');
}
/* Edit User Details User Picture */
/**
 * @Author : Jahia(rahmed)
 * This function Upload a picture the user picked and update his user picture with it
 * The picture to upload is directly picked from the form
 */
function updatePhoto(imageId, locale, nodePath, userId, callbackFunction, errorFunction)
{
    var uploadUrl = "/modules/api/default/"+locale+"/byPath"+nodePath+"/files/profile";
   //Upload the picture
   $.ajaxFileUpload({
        url: uploadUrl,
        secureuri:false,
        fileElementId: imageId,
        dataType: 'json',
        success: function(result)
        {
            var fileId = result["id"];
            //Set the uploaded Picture as the User Picture
            jahiaAPICreateUpdateProperties(fileId,"j:picture", userId, locale, function(){
                //Check If Jahia had the time to create the Avatar

                //building the avatar URL
                var filename = $("#"+imageId).val();
                var imageUrl = "/files/default/users/root/files/profile/"+filename+"?t=avatar_120";
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
            }, errorFunction);
        },
        error: function(result){
            errorFunction(result);
        }
    });
}

/**
 * @Author : Jahia(rahmed)
 * This function make a JSON Post of all the form entries (textInputs, select and ckeditors) contained in a Row
 * rowId: the Id of the from which post the form entries
 */
function saveCkEditorChanges(rowId,nodeIdentifier ,locale, callback, errorCallback )
{
    var reloadType='other';
    var urlPostChanges ="/modules/api/default/"+locale+"/nodes/"+nodeIdentifier;
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
    $.ajax({
    url: urlPostChanges,
    type: 'PUT',
    contentType: 'application/json',
    data: myJSONText,
    dataType: 'json'
    }).done(function(result){
        //calling the callback
        callback(result,reloadType);
    }).error(function(result){
        errorCallback(result);
    });
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

/* About me functions */
/**
 * @Author : Jahia(rahmed)
 * This function Hide the extra part of the about text when the user finish to read it
 */
/*function hideMoreText()
 {
 $('#aboutMeText').css({
 height: 'auto',
 overflow: 'hidden'
 });
 $('#aboutMeBlock').css({
 height: '115px',
 overflow:'hidden'
 });
 $('#btnLessAbout').hide();
 $('#btnMoreAbout').show();

 }*/

/**
 * @Author : Jahia(rahmed)
 * This function Show the extra part of the about text so the user can read it (scroll view)
 */
/*function showMoreText()
 {
 $('#aboutMeText').css({
 height: '115px',
 overflow: 'auto',
 paddingRight: '5px'

 });
 $('#about').css({
 height: '160px',
 overflow: 'auto'
 });
 $('#btnMoreAbout').hide();
 $('#btnLessAbout').show();
 }*/
