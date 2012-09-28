<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <td>
        <div class="jahia-template-gxt" jahiatype="module" id="module${currentNode.parent.parent.identifier}" type="existingNode"
             scriptInfo="" path="${currentNode.parent.parent.path}" template="hidden.system" dragdrop="false">
            ${currentNode.properties["j:url"].string}
    </div>
    </td>
    <td><a href="<c:url value='${url.base}${currentNode.parent.parent.path}.html'/>" >${currentNode.parent.parent.path}</a></td>
    <td>${currentNode.properties["jcr:language"].string}</td>
    <td>${currentNode.properties["j:active"].string}</td>
    <td>${currentNode.properties["j:default"].string}</td>
