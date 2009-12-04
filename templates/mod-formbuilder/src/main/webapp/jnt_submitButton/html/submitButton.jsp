<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set value="${currentNode.propertiesAsString}" var="props"/>

<span class="divButton"><input type="submit" value="${props.label}"/></span> 