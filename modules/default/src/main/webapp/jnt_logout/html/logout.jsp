<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<a href="<c:url value='${url.logout}'/>">${currentNode.properties["jcr:title"].string}</a>