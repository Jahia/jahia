<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="pageformcreation.css"/>

<c:set var="formid" value="form" />
<c:set var="nodeType" value="jnt:page" />

<c:set var="writeable" value="${jcr:hasPermission(currentNode,'addChildNodes') and currentResource.workspace eq 'live'}" />
<c:if test='${writeable}'>
<form class="pageFormCreation" method="post" action="${renderContext.mainResource.node.name}/" name="${formid}">
    <input type="hidden" name="nodeType" value="jnt:page">
    <input type="hidden" name="normalizeNodeName" value="true"/>
    <input type="hidden" name="jcr:mixinTypes" value="jmix:hasTemplateNode"/>
    <input type="hidden" name="j:templateNode" value="${currentNode.properties['templateNode'].string}"/>
    <h3>${currentNode.properties['jcr:title'].string}</h3>
    <fieldset>
        <legend>${currentNode.properties['jcr:title'].string}</legend>

        <p><label for="title"><fmt:message key="label.title"/></label>
            <input type="text" name="jcr:title" id="title" class="field" value=""
                   tabindex="20"/></p>


        <p><label for="description"><fmt:message
                key="label.description"/></label>
            <textarea name="jcr:description" id="description" cols="45" rows="3"
                      tabindex="21" ></textarea></p>

        <div>
            <input type="submit" class="button"
                   value="${currentNode.properties['buttonLabel'].string}" tabindex="28"
                   onclick="if (document.${formid}.elements['jcr:title'].value == '') {
                               alert('you must fill the title ');
                               return false;
                           }
                           document.${formid}.action = '${renderContext.mainResource.node.name}/'+encodeURIComponent(document.${formid}.elements['jcr:title'].value);
                           document.${formid}.submit();
                       "
                     ${disabled} />
        </div>
    </fieldset>
</form>
</c:if>
<c:if test="${not writeable}">
    page creation form is only available in live
</c:if>