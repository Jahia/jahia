<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set value="${currentNode.propertiesAsString}" var="props"/>

<input class="button" type="submit" value="${props.label}"/>