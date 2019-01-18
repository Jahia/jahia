<%@ tag body-content="empty" description="Provides ability to exclude file references from search when searching in site content." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="value" required="false" type="java.lang.Boolean" 
              description="true, if file references should be skipped when searching for site content; false, if they need to be included [false]" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display a checkbox input control for this element or create a hidden one? [false]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="fieldName" value="src_excludeFileReferences"/>
<c:set var="value" value="${functions:default(param[fieldName], not empty value ? value : false)}"/>
<c:set var="display" value="${functions:default(display, false)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="type" value="checkbox"/>
    <c:set target="${attributes}" property="value" value="true"/>
    <c:set target="${attributes}" property="name" value="${fieldName}"/>
    <input ${functions:attributes(attributes)} ${value ? 'checked="checked"' : ''}/>
</c:if>
<c:if test="${!display}"><input type="hidden" name="${fieldName}" value="${fn:escapeXml(value)}"/></c:if>
