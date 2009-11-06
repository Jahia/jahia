<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set value="true" var="editable" scope="request" />
<c:set value="${currentNode.editableChildren}" var="currentList" scope="request"/>