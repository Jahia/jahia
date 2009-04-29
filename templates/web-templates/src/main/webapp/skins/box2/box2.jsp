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
<%@ include file="../../common/declarations.jspf" %>
<div class="box2 ">
    <div class="box2-topright"></div><div class="box2-topleft"></div>
<c:if test="${not empty boxTitle}">
    <h3 class="box2-header"><span>${boxTitle}</span></h3>
</c:if>
  <div class="box2-text">
      <template:includeContent/>
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"> </div></div>

<%--remove boxTitle for reuse in boxes--%>
<c:remove var="boxTitle" scope="request"/>