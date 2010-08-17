<%@ tag body-content="empty" description="Renders the link to the category selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted category value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include children checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children input field." %>
<%@ attribute name="root" type="java.lang.String" description="The root category to start with." %>
<%@ attribute name="autoSelectParent" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="Allows to control if we have to auto check the parent of a category when selected or not. [false]" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="displayIncludeChildren" value="${functions:default(displayIncludeChildren, true)}"/>
<c:set var="autoSelectParent" value="${functions:default(autoSelectParent, false)}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in sub-categories --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:set var="root" value="${not empty root ? root : 'root'}"/>
&nbsp;<a href="#select"
onclick="javascript:{var categoriesSelector = window.open('${pageContext.request.contextPath}/engines/categories/launcher.jsp?autoSelectParent=${autoSelectParent}&pid=${jahia.page.ID}&contextId=${fieldId}@${root}&selectedCategories=' + document.getElementById('${fieldId}').value, '<%="categoriesSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable=yes,scrollbars=yes,height=800,width=600'); categoriesSelector.focus(); return false;}"
title='<fmt:message key="selectors.categorySelector.selectCategories"/>'><fmt:message key="selectors.select"/></a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><fmt:message key="selectors.categorySelector.selectCategories.includeChildren"/></label>
</c:if>
