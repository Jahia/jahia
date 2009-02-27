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
<%@ page import="java.util.*" %>

<%@include file="/views/engines/common/taglibs.jsp" %>

<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="confirmRestoreNav" class="java.lang.String" scope="request"/>

<%
  String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
  String engineView = (String)request.getAttribute("engineView");

  Map engineMap = (Map)request.getAttribute("jahia_session_engineMap");
  String theScreen = (String)engineMap.get("screen");

%>
<!-- versioning/containerlist/confirm_undelete.jsp (start) -->
<%@include file="../container/common-javascript.inc" %>
<script type="text/javascript" language="javascript">
<!--
function sendFormApply() {
  sendForm('restoreApply','');
}

function sendFormSave() {
  sendForm('restoreSave','');
}

//-->
</script>

<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Versioning.label"/>&nbsp;-&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.stepTwoOfTwo" defaultValue="Step 2 of 2 " /> : <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.confirmation" />
        </div>
      </div>
      <div class="content-body padded">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.clickOnOkOrApplyToUndelete" />.
      </div>
    </div>
  </div>
  <input type="hidden" name="lastscreen" value="<%=theScreen%>">
</div>
<!-- versioning/containerlist/confirm_undelete.jsp (end) -->