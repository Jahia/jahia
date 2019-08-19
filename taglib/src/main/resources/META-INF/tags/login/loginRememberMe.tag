<%@ tag import="org.jahia.settings.SettingsBean" %>
<%@ tag body-content="empty" description="Renders the checkbox for quering to allow enabling cookie usage for login." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%
  jspContext.setAttribute("readOnlyEnabled", SettingsBean.getInstance().isFullReadOnlyMode());
%>
<c:if test="${!currentRequest.logged}">
  <c:set target="${attributes}" property="type" value="checkbox"/>
  <c:set target="${attributes}" property="name" value="useCookie"/>
  <input ${readOnlyEnabled ? 'disabled' : ''} ${functions:attributes(attributes)}/>
</c:if>  