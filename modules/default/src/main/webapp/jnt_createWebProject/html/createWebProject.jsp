<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="css" resources="pageformcreation.css"/>
<template:addResources type="css" resources="create-web-project.css"/>
<template:addResources type="css" resources="loading.css"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<c:set var="nodeType" value="jnt:page" />

<c:if test="${not empty currentNode.properties['class']}">
    <div class="${currentNode.properties['class'].string}">
</c:if>

<template:tokenizedForm  allowsMultipleSubmits="true" >
<form class="webProjectCreation" id="webProjectCreationForm" method="post" action="<c:url value='${url.base}/sites.adminCreateSite.do'/>" >
    <c:if test="${currentNode.properties.stayOnPage.boolean}">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
    </c:if>
    <h3>${fn:escapeXml(currentNode.displayableName)}</h3>
    <fieldset>
        <legend>${fn:escapeXml(currentNode.displayableName)}</legend>

        <p><label for="siteTitle"><fmt:message key="label.title"/> (*)</label>
            <input type="text" name="siteTitle" id="siteTitle" class="inputsize2" value="" tabindex="20"/></p>

        <p><label for="siteKey"><fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/> (*)</label>
            <input type="text" name="siteKey" id="siteKey" class="inputsize2" value="" tabindex="20"/></p>

        <c:if test="${currentNode.properties['useServerName'].boolean}">
            <p><label for="siteServerName"><fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/> (*)</label>
                <input type="text" name="siteServerName" id="siteServerName" class="inputsize2" value="localhost" tabindex="20"/></p>
        </c:if>

        <c:if test="${currentNode.properties['useDescription'].boolean}">
            <p><label for="siteDescr"><fmt:message key="label.description"/></label>
                <textarea name="siteDescr" id="siteDescr" class="inputsize2" value="" tabindex="20"></textarea></p>
        </c:if>


        <c:if test="${currentNode.properties['useTemplatesSet'].boolean}">
            <p><label for="siteDescr"><fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/></label>
                <select name="templatesSet">
                    <jcr:node var="sets" path="/templateSets"/>
                    <c:forEach items="${sets.nodes}" var="set">
                        <c:if test="${set.properties['j:siteType'].string eq 'templatesSet'}">
                            <option value="${set.name}">${set.displayableName}</option>
                        </c:if>
                    </c:forEach>
                </select>
            </p>
        </c:if>
        <c:if test="${not currentNode.properties['useTemplatesSet'].boolean}">
            <input type="hidden" name="templatesSet" value="${currentNode.properties['defaultTemplatesSet'].node.name}">
        </c:if>

        <c:if test="${currentNode.properties['mixLanguage'].boolean}">
            <input type="hidden" name="mixLanguage" value="true">
        </c:if>

        <c:if test="${currentNode.properties['allowsUnlistedLanguages'].boolean}">
            <input type="hidden" name="allowsUnlistedLanguages" value="true">
        </c:if>

        <c:if test="${not empty currentNode.properties['forceLanguage'].string}">
            <input type="hidden" name="language" value="${currentNode.properties['forceLanguage'].string}">
        </c:if>

        <div>
            <input type="button" class="button" id="createSite_button"
                   value="${currentNode.properties['buttonLabel'].string}" tabindex="28"
                   onclick="createSite()" ${disabled}/>
        </div>
    </fieldset>
</form>
    <div style="display:none;" class="loading">
        <h1><fmt:message key="org.jahia.admin.workInProgressTitle"/></h1>
    </div>
</template:tokenizedForm>

<c:if test="${not empty currentNode.properties['class']}">
    </div>
</c:if>
