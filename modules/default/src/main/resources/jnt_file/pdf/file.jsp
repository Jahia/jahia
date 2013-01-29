<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><c:set var="url" value="${url.convert}${currentNode.path}.pdf"/><c:set target="${renderContext}" property="redirect" value="${url}"/>