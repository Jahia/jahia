<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div id="${currentNode.UUID}">

<script type="text/javascript">


function replace() {
    var http = false;
    if(navigator.appName == "Microsoft Internet Explorer") {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        http = new XMLHttpRequest();
    }
    http.open("GET", '${url.current}?ajaxcall=true&categorykey='+document.forms.filter.categorykey.value, true);
    http.onreadystatechange=function() {
        if(http.readyState == 4) {
            document.getElementById('${currentNode.UUID}').innerHTML = http.responseText;
        }
    }
    http.send(null);
}
</script>



<jcr:node var="category" path="/content/categories"/>
<form id="filter">
    Category : <select name="categorykey" onchange="javascript:replace()"/>
    <c:if test="${empty param.categorykey}"><option selected value="">All</option></c:if>
    <c:if test="${not empty param.categorykey}"><option value="">All</option></c:if>
    <c:forEach items="${category.children}" var="cat">
        <c:if test="${jcr:isNodeType(cat, 'jnt:category')}">
            <jcr:nodeProperty node="${cat}" name="jcr:title" var="catTitle" />
            <c:if test="${cat.name eq param.categorykey}"> <option selected value="${cat.name}">${catTitle.string}</option> </c:if>
            <c:if test="${cat.name ne param.categorykey}"> <option value="${cat.name}">${catTitle.string}</option> </c:if>
        </c:if>
    </c:forEach>
    </select>
</form>
<c:forEach items="${currentNode.children}" var="subchild">
    <p>
        <c:if test="${empty param.categorykey}">
            <template:module node="${subchild}" />
        </c:if>

        <c:if test="${not empty param.categorykey}">
            <jcr:nodeProperty node="${subchild}" name="j:defaultCategory" var="category" />

            <c:set var="contains" value="false" />
            <c:forEach items="${category}" var="val">
                <c:if test="${val.node.name == param.categorykey}">
                    <c:set var="contains" value="true" />
                </c:if>
            </c:forEach>
            <c:if test="${contains eq true}">
                <template:module node="${subchild}" />
            </c:if>
        </c:if>
    </p>
</c:forEach>
</div>