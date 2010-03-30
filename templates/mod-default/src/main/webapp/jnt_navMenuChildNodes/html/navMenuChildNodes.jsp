<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jsp:include page="../../jnt_navMenuMultilevel/html/navMenuMultilevel.jsp">
    <jsp:param name="startLevel" value="1"/>
    <jsp:param name="maxDepth" value="1"/>    
    <jsp:param name="useVisitedNodeAsBaseline" value="true"/>    
</jsp:include>