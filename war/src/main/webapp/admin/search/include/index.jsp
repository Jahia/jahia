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
<%@page import   = "java.util.*" %>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="url"     		class="java.lang.String"        scope="request"/>     <% // http files path. %>
<jsp:useBean id="input"   		class="java.lang.String"        scope="request"/>     <% // inputs size. %>
<jsp:useBean id="values" 		class="java.util.HashMap" 	scope="request"/>     <% // Map containing values. %>
<jsp:useBean id="subAction"		class="java.lang.String"    scope="request"/>     <% // the default screen %>

<p class="error">
  <fmt:message key="org.jahia.admin.search.ManageSearch.theIndexCanTake.label"/>&nbsp;!
</p>

<h3>
  <fmt:message key="org.jahia.admin.search.ManageSearch.whatIsIndexing.label"/>
</h3>

<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.indexingIsProcess.label"/>.
</p>
<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.indexingSite.label"/>.
</p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.noteWhenReIndex.label"/>.
<p>
  
<h3>
  <fmt:message key="org.jahia.admin.search.ManageSearch.whenToIndex.label"/>
</h3>

<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.becauseProcess.label"/>&nbsp;:
</p>    
<ul>
  <li><fmt:message key="org.jahia.admin.search.ManageSearch.afterFirst.label"/>.</li>
  <li><fmt:message key="org.jahia.admin.search.ManageSearch.afterRestoring.label"/>.</li>
  <li><fmt:message key="org.jahia.admin.search.ManageSearch.anyTime.label"/>.</li>
</ul>
<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.noteWhenContent.label"/>.
</p>


  
