<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<div class="maincontent"> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>
    <jcr:nodeProperty node="${currentNode}" name="mainContentImage" var="mainContentImage"/>
    <h3>${mainContentTitle.string}</h3>
        <img src="${mainContentImage.node.url}" alt="${mainContentImage.node.url}"/>
        ${mainContentBody.string}
</div>
<br class="clear"/>
