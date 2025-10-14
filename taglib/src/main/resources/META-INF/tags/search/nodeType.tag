<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ tag body-content="empty" description="Renders node type selection control with all node types available." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="value" required="false" type="java.lang.String"
              description="Filter by a single node type (filtering by multiple node types is not supported). Default value is 'jmix:searchable'. Ensure the 'display' attribute is set to 'false'. 'value' and 'selectionOptions' attributes must not be used together." %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of node types generating a dropdown allowing visitors to chose from. Ensure the 'display' attribute is set to 'true'. Must not be used to filter the results by default, use 'value' instead. 'value' and 'selectionOptions' attributes must not be used together." %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="When enabled, display a dropdown allowing visitors to chose from, using the 'selectionOptions' attribute values. When disabled, use the 'value' attribute value." %>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:if test="${display}">
    <c:set var="selectionOptions" value="${fn:replace(selectionOptions, ' ', '')}"/>
    <c:set var="selectionOptions" value="${functions:default(selectionOptions, 'any,nt:file,nt:folder')}"/>
    <c:set var="value" value="${functions:default(param.src_nodeType, value)}"/>

    <select ${functions:attributes(attributes)} name="src_nodeType">
    	<c:forTokens items="${selectionOptions}" delims="," var="option">
    		<c:if test="${option == 'any'}">
    			<option value=""><fmt:message key="searchForm.any"/></option>
    		</c:if>
    		<c:if test="${option != 'any'}">
    			<jcr:nodeType name="${option}" var="type"/>
            	<option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>${jcr:label(type,currentResource.locale)}</option>
    		</c:if>
    	</c:forTokens>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_nodeType" value="${fn:escapeXml(value)}"/></c:if>