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
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="name" required="true" rtexprvalue="true" %>
<%@ attribute name="inputName" required="true" rtexprvalue="true" %>
<%@ attribute name="cssClassName" required="false" rtexprvalue="true" %>
<%@ attribute name="readOnlyInput" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="displayInput" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="thePath" value="<%=request.getParameter(inputName)%>" scope="request"/>
<c:set var="readOnlyInput" value="${not empty readOnlyInput ? readOnlyInput : true}"/>
<c:set var="displayInput" value="${not empty displayInput ? displayInput : true}"/>

<div class="${cssClassName}">
    <template:jahiaPageForm name="${name}" method="get">
        <c:choose>
            <c:when test="${displayInput}">
                <input type="text" name="${inputName}" id="${inputName}" value="${thePath}"
                       <c:if test="${readOnlyInput}">readonly="readonly"</c:if> />
            </c:when>
            <c:otherwise>
                <input type="hidden" name="${inputName}" id="${inputName}" value="${thePath}"
                       <c:if test="${readOnlyInput}">readonly="readonly"</c:if> />
            </c:otherwise>
        </c:choose>
    </template:jahiaPageForm>
    <c:if test="${requestScope.currentRequest.editMode}">
        <ui:folderSelector fieldId="${inputName}" displayIncludeChildren="false"
                           onSelect="function (path) { document.${name}.${inputName}.value=path; document.${name}.submit(); return false; }"/>
        <fmt:message key="mediagallery.imagefolder"/>
    </c:if>
    <ui:thumbView path="${thePath}" cssClassName="thumbView"/>
</div>
