<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="maincontent">
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/>
    <jcr:nodeProperty node="${currentNode}" name="mainContentBody" var="mainContentBody"/>
    <h4>${mainContentTitle.string}</h4>
    ${fn:substring(mainContentBody.string,0,20)}
    ${fn:substring(currentPage.url,0 ,fn:indexOf(currentPage.url,'.html') + 5)}
</div>
<br class="clear"/>
