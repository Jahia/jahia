<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" %>
<%@include file="/views/engines/common/taglibs.jsp" %>
<internal:i18n />

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
	if (workInProgressOverlay) workInProgressOverlay.launch();
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