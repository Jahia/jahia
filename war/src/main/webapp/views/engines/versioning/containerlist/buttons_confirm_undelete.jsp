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
<!-- actionBar (start) -->
<div id="actionBar">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-back" href="javascript:sendForm('showContainersList','')">
        <fmt:message key="org.jahia.engines.version.backToStep"/>&nbsp;1&nbsp;:&nbsp;<fmt:message key="org.jahia.engines.version.showContainersList"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:sendForm('restoreSave','');" class="ico-ok" title="<fmt:message key="org.jahia.altApplyAndClose.label"/>" onclick="setWaitingCursor(1);">
              <fmt:message key="org.jahia.button.ok"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:sendForm('restoreApply','');" class="ico-apply" title="<fmt:message key="org.jahia.altApplyWithoutClose.label"/>" onclick="setWaitingCursor(1);">
        <fmt:message key="org.jahia.button.apply"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:window.close();" class="ico-cancel" title="<fmt:message key="org.jahia.altCloseWithoutSave.label"/>">
        <fmt:message key="org.jahia.button.cancel"/></a>
    </span>
  </span>
</div>
<!-- actionBar (end) -->