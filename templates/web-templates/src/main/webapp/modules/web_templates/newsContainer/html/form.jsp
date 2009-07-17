<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

 <jcr:nodeProperty node="${currentNode}" name="newsTitle" var="newsTitle"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDate" var="newsDate"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDesc" var="newsDesc"/>
 <jcr:nodeProperty node="${currentNode}" name="newsImage" var="newsImage"/>
<form action="${baseUrl}${currentNode.path}" method="post">
    <input type="hidden" name="nodeType" value="web_templates:newsContainer"/>
    <div class="newsListItem"><!--start newsListItem -->

        <h4><input type="text" name="newsTitle" value="${newsTitle.string}"/></h4>

        <p class="newsInfo">
            <span class="newsLabelDate"><fmt:message key="news.date"/> :</span>
            <span class="newsDate">
                <fmt:formatDate value="${newsDate.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate value="${newsDate.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
            </span>
        </p>

        <div class="newsImg"><a href="${baseUrl}${currentNode.path}.html"><img src="${newsImage.node.url}"/></a></div>
        <p class="newsResume">
            <textarea rows="10" cols="80" name="newsDesc">${newsDesc.string}</textarea>
        </p>

        <div class="more">    <input type="submit"/>
       </div>
        <div class="clear"> </div>
    </div>




</form>