<%@ tag body-content="empty" description="Renders the entry field for allowing to enter the user password." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:if test="${!currentRequest.logged}">
  <c:set target="${attributes}" property="type" value="password"/>
  <c:set target="${attributes}" property="name" value="password"/>
  <c:set var="value" value="${functions:default(param['username'], 'voyeur')}"/>
  <input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}" onfocus="this.value=''"/>
</c:if>  