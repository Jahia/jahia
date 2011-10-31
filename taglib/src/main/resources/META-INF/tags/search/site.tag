<%@ tag body-content="empty" description="Renders site selection language selection control. By default all sites are used. This can be overridden by providing a list of sites to be disaplyed for selection." %>
<%@ tag import="org.jahia.services.sites.JahiaSite" %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false"
              description="Represents a single site key to be used for search or a comma separated string of site keys." %>
<%@ attribute name="valueOptions" required="false"
              description="Represents a comma separated string of site keys to be displayed in the selection list." %>
<%@ attribute name="allowAll" required="false" type="java.lang.Boolean"
              description="If set to true, we diaplys an option to search in all sites. [true]" %>
<%@ attribute name="includeReferencesFrom" required="false"
              description="Normally references from other sites than the selected ones are not found. In this parameter you can specify one or multiple comma separated site keys and then node references to these sites will also be displayed in the search results." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:if test="${display}">
    <c:set var="value" value="${not empty value ? fn:replace(value, ' ', '') : ''}"/>
    <c:set var="value" value="${functions:default(paramValues['src_sites.values'], not empty value ? fn:split(value, ',') : null)}"/>
    <c:set var="selectedValues" value=",${not empty value ? fn:join(value, ',') : renderContext.site.name},"/>
    <c:set var="allowAll" value="${not empty allowAll ? allowAll : true}"/>
    <c:set target="${attributes}" property="name" value="src_sites.values"/>
    <% jspContext.setAttribute("allSites", org.jahia.services.sites.JahiaSitesBaseService.getInstance().getSites()); %>
    <c:if test="${not empty valueOptions}">
    	<c:set var="valueOptions" value=",${fn:replace(valueOptions, ' ', '')},"/>
    </c:if>
    <select ${functions:attributes(attributes)}>
   		<c:if test="${allowAll}">
   			<option value="-all-" ${fn:contains(selectedValues, '-all-') ? 'selected="selected"' : ''}><fmt:message key="searchForm.any"/></option>
   		</c:if>
    	<c:forEach items="${allSites}" var="site">
    		<c:set var="siteKeyToCheck" value=",${site.siteKey},"/>
    		<c:if test="${site.siteKey != 'systemsite' && (empty valueOptions || fn:contains(valueOptions, siteKeyToCheck))}">
            	<option value="${site.siteKey}" ${fn:contains(selectedValues, site.siteKey) ? 'selected="selected"' : ''}>${fn:escapeXml(not empty site.title ? site.title : site.siteKey)}</option>
            </c:if>
    	</c:forEach>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_sites.values" value="${fn:escapeXml(value)}"/></c:if>
<c:if test="${not empty includeReferencesFrom}">
    <input type="hidden" name="src_sitesForReferences.values" value="${fn:escapeXml(includeReferencesFrom)}"/>
</c:if>