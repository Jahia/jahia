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



weakreference test: <jcr:nodePropertyRenderer node="${currentNode}" name="weakreferenceTest" renderer="nodeReference"/><br/>
weakreference File: ${currentNode.properties.weakReferenceFile.node.url} <br/>
weakreference Folder: ${currentNode.properties.weakReferenceFolder.node.url} <br/>
weakreference File Image: <img src="${currentNode.properties.weakReferenceFileMimeImage.node.url}"><br/>
weakreference File Text: ${currentNode.properties.weakReferenceFileMimeText.node.fileContent.contentType} <br/>

