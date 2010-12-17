<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="${currentNode.properties['j:classname'].string}" <c:if test="${! empty currentNode.properties['j:id']}">id="${currentNode.properties['j:id'].string}"</c:if>>
     ${wrappedContent}
</div>