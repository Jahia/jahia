<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" %>
<%@include file="/views/engines/common/taglibs.jsp" %>

<script type="text/javascript">
<!--

function check(){
    // override this function if needed in subengine to perform form data check
    // before submit !!!
    return true;
}

function saveContent(){
	// used by Html editors
	// override this for preprocessing before for submission
	if (typeof workInProgressOverlay != 'undefined') workInProgressOverlay.launch();
}

function teleportCaptainFlam(what) {
	document.mainForm.submit();
}


function handleLanguageChange(action)
{
    document.mainForm.method.value = "POST";
    document.mainForm.action = action;
    if ( check() ){
		saveContent();
        teleportCaptainFlam(document.mainForm);
        //document.mainForm.submit();
    }
}

function handleActionChange(what)
{
	saveContent();
    document.forms["mainForm"].method = "POST";
    document.forms["mainForm"].action = "<bean:write filter="false" name="jahiaEngineCommonData" property="engineURL" />&screen=" + what;
    document.forms["mainForm"].submit();
}

function sendFormSave()
{
    if ( check() ){
        document.mainForm.method.value = "POST";
        document.mainForm.action = "<bean:write filter="false" name="jahiaEngineCommonData" property="engineURL" />&screen=save";
		saveContent();
        teleportCaptainFlam(document.mainForm);
        //document.mainForm.submit();
    }
}

function sendFormSaveAndAddNew()
{
    document.mainForm.method.value = "POST";
    document.mainForm.action = "<bean:write filter="false" name="jahiaEngineCommonData" property="engineURL" />&screen=save&addnew=true";
    if ( check() ){
		saveContent();
        teleportCaptainFlam(document.mainForm);
        //document.mainForm.submit();
    }
}

function sendFormApply()
{
    document.mainForm.method.value = "POST";
    document.mainForm.action = "<bean:write filter="false" name="jahiaEngineCommonData" property="engineURL" />&screen=apply";
    if ( check() ){
		saveContent();
        teleportCaptainFlam(document.mainForm);
        //document.mainForm.submit();
    }
}

function sendFormCancel()
{
    document.mainForm.method.value = "POST";
    document.mainForm.action = "<bean:write filter="false" name="jahiaEngineCommonData" property="engineURL" />&screen=cancel";
    // FIXME: this option is desactivated for now. Why a check on a cancel button?
    //if ( check() ){
    if ( true ){
		saveContent();
        teleportCaptainFlam(document.mainForm);
        //document.mainForm.submit();
    }
}

function sendFormClose()
{
    if ( true ){
        window.close();
    }
}

document.onkeydown = keyDown;
function keyDown() {
    if (document.all) {
        var ieKey = event.keyCode;
        if (ieKey == 13 && event.ctrlKey) { sendFormSave(); }
        if (ieKey == 87 && event.ctrlKey) { sendFormCancel(); }
    }
}

    function setWaitingCursor() {
        document.body.style.cursor = "wait";
    }

window.onload = function() {
    scroll(0, 0);
}

//window.onunload = closeEngineWin;

//-->
</script>