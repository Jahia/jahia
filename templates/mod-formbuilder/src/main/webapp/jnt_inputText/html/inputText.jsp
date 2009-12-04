<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set value="${currentNode.propertiesAsString}" var="props"/>
<label>${props.label}</label><input type="text" name="${currentNode.name}" maxlength="${props.size}" value="${props.defaultValue}"/> 