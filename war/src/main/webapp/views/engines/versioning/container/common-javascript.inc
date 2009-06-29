<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<script type="text/javascript" src="<%=URL%>../javascript/zimbra/versioning/ContainerVersionCompare.js"></script>
<script language="javascript" type="text/javascript">
<!--

document.onkeydown = keyDown;
function keyDown() {
    if (document.all) {
        var ieKey = event.keyCode;
        if (ieKey == 13) { document.retryForm.submit(); }
        if (ieKey == 87 && event.ctrlKey) { window.close(); }
    }
}

function handleLanguageChange(lang) {
    document.mainForm.method.value = "POST";
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engine_lang.value = lang;
    var engineView = '<%=engineView%>';
    var method = "";
    if ( engineView == "revisionsList" ){
      method = "showRevisionsList"; 
    } else if ( engineView == "confirmRestore" ){
      method = "showConfirmRestore"; 
    } else if ( engineView == "containersList" ){
      method = "showContainersList"; 
    }  
    sendForm(method);
}   


function sendFormSave(){
    window.close();
}

function sendFormClose(){
    sendForm('close', '');
}

function sendFormCancel(){
    sendForm('cancel', '');
}    

function sendForm(method, params) {
	if (typeof workInProgressOverlay != 'undefined') { workInProgressOverlay.launch(); }
    document.mainForm.method.value = method;
    document.mainForm.action = "<%=actionURL%>";
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engineview.value = "<%=engineView%>";
    if (params != null && params != "") {
        params += "&method=" + method;
    } else {
        params = "&method=" + method;
    }

    if (params != null && params != "") {
        if (document.mainForm.action.indexOf("?") > 0) {
            if (params.charAt(0) == "&") {
                document.mainForm.action += params;
            } else {
                document.mainForm.action += "&" + params;
            }
        } else {
            if (params.charAt(0) == "&") {
                document.mainForm.action += "?" + params.substring(1);
            } else {
                document.mainForm.action += "?" + params;
            }
        }
    }
    flagSubmit();
    document.mainForm.submit();
}

//-->
</script>