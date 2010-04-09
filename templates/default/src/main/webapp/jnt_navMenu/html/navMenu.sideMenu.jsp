<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jsp:include page="navMenu.jsp">
    <jsp:param name="navMenuWrapper" value="${empty jcr:getParentsOfType(currentNode, 'jnt:navMenu') ? 'wrapper.sideMenu.default' : 'wrapper.none'}"/>
</jsp:include>