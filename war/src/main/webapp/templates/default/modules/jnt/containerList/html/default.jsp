<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

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


<c:forEach items="${currentNode.children}" var="subchild">
<c:if test="${jcr:isNodeType(subchild, 'jnt:container')}">
<p>
    ${currentNode.name} <a href="${pageContext.request.contextPath}/render/default${subchild.path}.jcr.html">link</a>
    <div id ="content${subchild.UUID}"></div>
    <script type="text/javascript">
        replace("${pageContext.request.contextPath}/render/default${subchild.path}.html","content${subchild.UUID}");
    </script>
</p>
</c:if>
</c:forEach>
