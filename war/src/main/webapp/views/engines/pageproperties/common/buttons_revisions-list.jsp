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
<%@ page import="java.util.Collection" %>
<%@include file="/views/engines/common/taglibs.jsp" %>
<!-- actionBar (start) -->
<div id="actionBar">
  <%
  final Collection revisions = (Collection) request.getAttribute("revisions");
  if (revisions != null) {
    int nbRevisions = revisions.size();
    if (nbRevisions>0){ %>
      <span class="dex-PushButton">
        <span class="first-child">
          <a class="ico-back" href="javascript:setUseRevisionEntry();"><fmt:message key="org.jahia.engines.version.backToStep"/> 2, <fmt:message key="org.jahia.engines.version.useTheSelectedRevision"/></a>
        </span>
      </span>
      <span class="dex-PushButton">
        <span class="first-child">
          <a class="ico-back" href="javascript:sendForm('showSiteMap','')"><fmt:message key="org.jahia.engines.version.backToStep"/> 2, <fmt:message key="org.jahia.engines.version.ignoreRevisionDateSelection"/></a>
        </span>
      </span>

    <% } else { %>
      <span class="dex-PushButton">
        <span class="first-child">
          <a class="ico-back" href="javascript:sendForm('showSiteMap','')"><fmt:message key="org.jahia.engines.version.backToStep"/> 2</a>
        </span>
      </span>
    <% } %>
  <% } %>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:window.close();" class="ico-cancel" title="<fmt:message key="org.jahia.altCloseWithoutSave.label"/>">
        <fmt:message key="org.jahia.button.cancel"/></a>
    </span>
  </span>
</div>
<!-- actionBar (end) -->