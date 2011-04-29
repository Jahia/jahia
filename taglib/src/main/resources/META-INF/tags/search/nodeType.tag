<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ tag body-content="empty" description="Renders node type selection control with all node types available." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of node types to search in that are available for user selection. [any,nt:file,nt:folder]" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided." %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>
<c:set var="selectionOptions" value="${fn:replace(selectionOptions, ' ', '')}"/>
<c:set var="selectionOptions" value="${functions:default(selectionOptions, 'any,nt:file,nt:folder')}"/>
<c:set var="value" value="${functions:default(param.src_nodeType, value)}"/>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:if test="${display}">
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