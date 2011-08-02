<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="pageformcreation.css"/>

<c:set var="formid" value="form" />
<c:set var="nodeType" value="jnt:page" />

<c:if test="${not empty currentNode.properties['class']}">
    <div class="${currentNode.properties['class'].string}">
</c:if>

<template:tokenizedForm>
<form class="pageFormCreation" method="post" action="${renderContext.mainResource.node.name}/*" name="${formid}">
    <c:if test="${currentNode.properties.i18npages.boolean}">
        <input type="hidden" name="jcrNodeType" value="jnt:page">
    </c:if>
    <c:if test="${not currentNode.properties.i18npages.boolean}">
        <input type="hidden" name="jcrNodeType" value="jnt:noni18npage">
    </c:if>
    <input type="hidden" name="jcrNormalizeNodeName" value="true"/>
    <input type="hidden" name="jcrAutoAssignRole" value="owner"/>
    <input type="hidden" name="jcr:mixinTypes" value="jmix:hasTemplateNode"/>
    <input type="hidden" name="j:templateNode" value="${currentNode.properties['templateNode'].string}"/>
    <c:if test="${currentNode.properties.stayOnPage.boolean}">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
    </c:if>
    <h3>${fn:escapeXml(currentNode.displayableName)}</h3>
    <fieldset>
        <legend>${fn:escapeXml(currentNode.displayableName)}</legend>

        <p><label for="title"><fmt:message key="label.title"/></label>
            <input type="text" name="jcr:title" id="title" class="field" value=""
                   tabindex="20"/></p>


        <c:if test="${currentNode.properties['useDescription'].boolean}">
        <p><label for="description"><fmt:message
                key="label.description"/></label>
            <textarea name="jcr:description" id="description" cols="45" rows="3"
                      tabindex="21" ></textarea></p>
        </c:if>
        <div>
            <fmt:message key="label.noTitle" var="i18nNoTitle"/>
            <input type="submit" class="button"
                   value="${currentNode.properties['buttonLabel'].string}" tabindex="28"
                   onclick="if (document.${formid}.elements['jcr:title'].value == '') { alert('${functions:escapeJavaScript(i18nNoTitle)}'); return false; } else { return true; }" ${disabled}/>
        </div>
    </fieldset>
</form>
</template:tokenizedForm>

<c:if test="${not empty currentNode.properties['class']}">
    </div>
</c:if>
