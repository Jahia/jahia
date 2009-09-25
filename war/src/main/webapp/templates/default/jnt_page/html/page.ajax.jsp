<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<script type="text/javascript">


function replace(url,divID) {
    var http = false;

    if(navigator.appName == "Microsoft Internet Explorer") {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        http = new XMLHttpRequest();
    }
    http.open("GET", url, true);
    http.onreadystatechange=function() {
        if(http.readyState == 4) {
            document.getElementById(divID).innerHTML = http.responseText;
        }
    }
    http.send(null);
}
</script>


<h1>Page : ${currentNode.name}</h1>
<div id="nav">
    <h2>Navigation</h2>
    <h3>Path</h3>
    <c:set var="currentPath" value=""/>
    <c:forTokens items="${currentNode.path}" delims="/" var="itemPath">
        <c:set var="currentPath" value="${currentPath}/${itemPath}"/>
        <a href="${url.base}${currentPath}.html">${itemPath}</a> /
    </c:forTokens>
    <h3>Menu</h3>
    <ul>
    <c:forEach items="${currentNode.children}" var="child">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
        <li>
            <a href="${url.base}${child.path}.html">${child.name}</a>

        </li>
        </c:if>
    </c:forEach>
</ul>
</div>


<div id="content">
        <h2>Content</h2>
        <c:forEach items="${currentNode.children}" var="child">
            <c:if test="${jcr:isNodeType(child, 'jnt:containerList')}">
                <div id ="content${child.UUID}"></div>
                <script type="text/javascript">
                    replace("${url.base}${child.path}.html","content${child.UUID}");
                </script>
            </c:if>
        </c:forEach>
</div>
