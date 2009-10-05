<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="fileContentSource" var="fileContentSource"/>
 <template:fileImport path="${fileContentSource.file.realName}"/>