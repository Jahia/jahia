<%@ tag body-content="empty" dynamic-attributes="attributes" description="Renders file type selection control with all file type groups configured in the applicationcontext-basejahiaconfig.xml file." %>
<%@ tag import="org.jahia.services.content.JCRContentUtils" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ attribute name="value" required="false" type="java.lang.String" 
              description="Represents a single file type to be used for search or a comma separated string of file types."%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="fileTypes" value="<%= JCRContentUtils.getInstance().getMimeTypes() %>"/>
<c:if test="${not empty value}">
    <% if (!JCRContentUtils.getInstance().getMimeTypes().containsKey(jspContext.getAttribute("value"))) {
        throw new IllegalArgumentException("Unsupported file type '" + jspContext.getAttribute("value") + "'. See applicationcontext-basejahiaconfig.xml file for configured file types.");
    } %>
</c:if>
<c:set var="value" value="${functions:default(param.src_fileType, value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="name" value="src_fileType"/>
    <select ${functions:attributes(attributes)}>
        <option value=""><fmt:message key="searchForm.any"/></option>
        <c:forEach items="${fileTypes}" var="type">
            <option value="${type.key}" ${value == type.key ? 'selected="selected"' : ''}><fmt:message key="searchForm.fileType.${type.key}"/></option>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_fileType" value="${fn:escapeXml(value)}"/></c:if>