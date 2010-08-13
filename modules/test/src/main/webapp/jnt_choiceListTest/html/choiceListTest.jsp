<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

Contract: ${currentNode.properties.contract.string}<br/>

Mandatory Contract: ${currentNode.properties.contractMandatory.string}<br/>

Contract with bundle(choicelist[resourceBundle]):
<jcr:nodePropertyRenderer node="${currentNode}" name="contractResourceBundle" renderer="resourceBundle"/><br/>

Contract with Default:
<jcr:nodePropertyRenderer node="${currentNode}" name="contractWhitDefault" renderer="resourceBundle"/><br/>

Country(choicelist[country]):
<jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="country"/><br/>

Country with flag(choicelist[country,flag]):
<jcr:nodePropertyRenderer node="${currentNode}" name="countryWithFlag" renderer="flagcountry"/><br/>

Sortable Fields Name(choicelist[sortableFieldnames]):
${currentNode.properties.sortableFieldsName.string}<br/>

Category(choicelist[nodes='/categories;jnt:category']):
<jcr:nodePropertyRenderer node="${currentNode}" name="category" renderer="nodeReference"/><br/>

Multiple categories:
<jcr:nodePropertyRenderer node="${currentNode}" name="multipleCategories" renderer="nodeReference"/><br/>

Multiple Subnode Types(choicelist[subnodetypes = 'jmix:droppableContent,jnt:file,jnt:folder']):
<jcr:nodeProperty node="${currentNode}" name= "multipleSubNodeTypes" var="SubNodeTypes"/>
<c:forEach items="${SubNodeTypes}" var="SubNodeType"><br/>
	-${SubNodeType.string}
</c:forEach><br/>




