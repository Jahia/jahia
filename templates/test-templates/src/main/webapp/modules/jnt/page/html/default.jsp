<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<template:template>
<head>
    <title>${currentNode.name}</title>
</head>
<body>
<h1>Page : ${currentNode.name}</h1>
<div id="nav">
    <h2>Navigation</h2>
    <h3>Path</h3>
    <c:set var="currentPath" value=""/>
    <c:forTokens items="${currentNode.path}" delims="/" var="itemPath">
        <c:set var="currentPath" value="${currentPath}/${itemPath}"/>
        <a href="<%= request.getContextPath() %>/render/default${currentPath}.html">${itemPath}</a> /
    </c:forTokens>
    <h3>Menu</h3>
    <ul>
    <c:forEach items="${currentNode.children}" var="child">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
        <li>
            <a href="<%= request.getContextPath() %>/render/default${child.path}.html">${child.name}</a>

        </li>
        </c:if>
    </c:forEach>
</ul>
</div>

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

<div id="content">
        <h2>Contenu</h2>
        <c:forEach items="${currentNode.children}" var="child">
            <c:forEach items="${child.children}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:container')}">
            <p>
                ${child.name} <a href="<%= request.getContextPath() %>/render/default${subchild.path}.jcr.html">link</a>
                <div id ="content${subchild.UUID}"></div>
                <script type="text/javascript">
                    replace("<%= request.getContextPath() %>/render/default${subchild.path}.jcr.html","content${subchild.UUID}");
                </script>
            </p>
            </c:if>
            </c:forEach>
        </c:forEach>
</div>
</body>
</template:template>