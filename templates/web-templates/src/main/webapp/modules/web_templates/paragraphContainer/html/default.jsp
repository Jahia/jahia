<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/>
<jcr:nodeProperty node="${currentNode}" name="insertWidth" var="insertWidth"/>
<jcr:nodeProperty node="${currentNode}" name="insertType" var="insertType"/>
<jcr:nodeProperty node="${currentNode}" name="insertPosition" var="insertPosition"/>
<jcr:nodeProperty node="${currentNode}" name="insertText" var="insertText"/>
<jcr:nodeProperty node="${currentNode}" name="mainContentAlign" var="mainContentAlign"/>
<jcr:nodeProperty node="${currentNode}" name="mainContentImage" var="mainContentImage"/>
<jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>


        <h3>${mainContentTitle.string}</h3>
        <div class='${insertType.resource.defaultValue}-top float${insertPosition.resource.defaultValue}'
             style='width:${insertWidth}px'>

            <div class="${insertType.resource.defaultValue}-bottom">
                ${insertText}
            </div>
        </div>
        <div class="float${mainContentAlign.resource.defaultValue}"><img src="${mainContentImage.file.image}"></div>
        <div>
            ${mainContentBody}
        </div>
        <div class="clear"> </div>
