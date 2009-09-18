<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="file" var="file"/>
<jcr:nodeProperty node="${currentNode}" name="fileDesc" var="fileDesc"/>
<jcr:nodeProperty node="${currentNode}" name="fileDisplayDetails" var="fileDisplayDetails"/>


            <li class="document">
                <c:if test="${!empty title.string}">
                    <a href="${file.node.url}">${title.string}</a>
                </c:if>
                <c:if test="${empty title.string}">${file.node.url}</c:if>

<!-- <span class="docsize">taille formatée --> <!-- mettres les proprietes metadata date et createur --></span>

                    <span class="resume">${fileDesc.string}
                    </span>
            </li>

