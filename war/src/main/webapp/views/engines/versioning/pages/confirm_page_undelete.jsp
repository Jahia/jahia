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
<%@ page import="java.util.*" %>

<%@include file="/views/engines/common/taglibs.jsp" %>

<%
  String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
  String engineView = (String)request.getAttribute("engineView");

  Map engineMap = (Map)request.getAttribute("jahia_session_engineMap");
  String theScreen = (String)engineMap.get("screen");

%>
<!-- versioning/pages/confirm_page_undelete.jsp (start) -->
<%@include file="common-javascript.inc" %>
<script language="javascript">
<!--
function sendFormApply()
{
  sendForm('restoreApply','');
}

function sendFormSave()
{
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
          <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>&nbsp;-&nbsp;<fmt:message key="org.jahia.engines.version.stepThreeOfThree" /> : <fmt:message key="org.jahia.engines.version.confirmation" />
        </div>
      </div>
      <div class="content-body padded">
        <fmt:message key="org.jahia.engines.version.clickOnOkOrApplyToUndelete" />.
      </div>
    </div>
  </div>
</div>
<input type="hidden" name="lastscreen" value="<%=theScreen%>">
<!-- versioning/pages/confirm_page_undelete.jsp (end) -->