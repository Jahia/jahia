<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

 <jcr:nodeProperty node="${currentNode}" name="newsTitle" var="newsTitle"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDate" var="newsDate"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDesc" var="newsDesc"/>
 <jcr:nodeProperty node="${currentNode}" name="newsImage" var="newsImage"/>
<li class="summary">
    <!--start newsListItem -->
    <div class="summaryImg"><img src="${newsImage.node.url}" alt='${newsTitle.string}'/></div>
    <h4><a href="${url.current}">${newsTitle.string}</a></h4>
    <p class="summaryresume"> ${fn:substring(newsDesc.string,0,120)}</p>
    <div class="clear"> </div>
</li>