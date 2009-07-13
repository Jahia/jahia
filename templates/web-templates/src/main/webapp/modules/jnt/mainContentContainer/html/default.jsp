<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<div class="maincontent"> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/> 
    <jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>
    <h3>${mainContentTitle.string}</h3>

        ${mainContentBody.string}
</div>
<br class="clear"/>
