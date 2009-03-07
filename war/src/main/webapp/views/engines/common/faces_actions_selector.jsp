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

<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@page language = "java"%>
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.params.*"%>
<%@page import = "org.jahia.views.engines.*"%>

<%@include file="/views/engines/common/taglibs.jsp" %>

<script language="javascript">

// With Netscape 4.7, the engine popup window resizing cause a cache error
// expiration due to the document POSTing. This function prevent this error by
// automatically reloading the content.
// This function can be removed when Jahia will no more support Netscape 4.7.

window.onresize = function() {
    var browser = navigator.appName + navigator.appVersion;
    if (browser.indexOf("Netscape4.7") != -1) {
        window.location.reload();
    }
}
</script>
<br>
<table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
<form name="selector" action="">
<tr>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
    <td width="100%" align="right" valign="bottom" class="text">
    	<logic:iterate id="button" name="jahiaEngineButtonsHelper" property="buttons" type="java.lang.String">
	        <logic:equal name="button" value="OK_BUTTON">
		        <internal:jahiaButton img="ok"
		            href="javascript:sendFormSave();"
		            altBundle="JahiaInternalResources" altKey="org.jahia.altApplyAndClose.label" />
			</logic:equal>
			<logic:equal name="button" value="SAVE_ADD_NEW_BUTTON">
		        <internal:jahiaButton img="saveAddNew"
		            href="javascript:sendFormSaveAndAddNew();"
		            altBundle="JahiaInternalResources" altKey="org.jahia.altApplyAndAddContainer.label" />
			</logic:equal>
			<logic:equal name="button" value="APPLY_BUTTON">
		        <internal:jahiaButton img="apply"
		            href="javascript:sendFormApply();"
		            altBundle="JahiaInternalResources" altKey="org.jahia.altApplyWithoutClose.label" />
	        </logic:equal>
	        <logic:equal name="button" value="CANCEL_BUTTON">
		        <internal:jahiaButton img="cancel"
		            href="javascript:sendFormCancel();"
		            altBundle="JahiaInternalResources" altKey="org.jahia.altCloseWithoutSave.label" />
	        </logic:equal>
	        <logic:equal name="button" value="CLOSE_BUTTON">
		        <internal:jahiaButton img="close"
		            href="javascript:sendFormClose();"
		            altBundle="JahiaInternalResources" altKey="org.jahia.altClose.label" />
	        </logic:equal>
	    </logic:iterate>
    </td>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
</tr>
<!-- tab buttons -->
<logic:present name="tab-buttons">
<tr>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
    <td width="100%" class="text" valign="bottom">
	    	<tiles:insert beanName="tab-buttons"/>
    </td>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
</tr>
</logic:present>
</form>
</table>