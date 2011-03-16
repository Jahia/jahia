<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${currentNode.propertiesAsString['j:type']=='plusminus'}">
    <template:option nodetype="jmix:rating" node="${currentNode}" view="hidden.plusone_minorone_form"/>
    <template:option nodetype="jmix:rating" node="${currentNode}" view="hidden.plusone_minorone"/>
</c:if>
<c:if test="${currentNode.propertiesAsString['j:type']=='average'}">
    <template:option nodetype="jmix:rating" node="${currentNode}" view="hidden.average"/>
</c:if>