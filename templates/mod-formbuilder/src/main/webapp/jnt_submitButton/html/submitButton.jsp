<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set value="${currentNode.propertiesAsString}" var="props"/>
<input type="submit" value="${props.label}"/> 