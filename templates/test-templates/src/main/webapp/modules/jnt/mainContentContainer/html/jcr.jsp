<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/>
<div class="mainContent">
    <h3>${mainContentTitle.string}</h3>
<p>
    <jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>
    ${mainContentBody.string}
</p>
</div>