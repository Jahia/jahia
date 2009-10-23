<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:if test="${currentNode.properties.firstname.boolean}">
    firstname </br>
</c:if>
<c:if test="${currentNode.properties.lastname.boolean}">
    lastname  </br>
</c:if>
<c:if test="${currentNode.properties.title.boolean}">
    title    </br>
</c:if>
<c:if test="${currentNode.properties.age.boolean}">
    title    </br>
</c:if>
<c:if test="${currentNode.properties.birthdate.boolean}">
    birthdate  </br>
</c:if>
<c:if test="${currentNode.properties.gender.boolean}">
    gender  </br>
</c:if>
<c:if test="${currentNode.properties.profession.boolean}">
    profession  </br>
</c:if>
<c:if test="${currentNode.properties.maritalStatus.boolean}">
    maritalStatus  </br>
</c:if>
<c:if test="${currentNode.properties.hobbies.boolean}">
    hobbies   </br>
</c:if>
<c:if test="${currentNode.properties.contact.boolean}">
    contact  </br>
</c:if>
<c:if test="${currentNode.properties.address.boolean}">
    address  </br>
</c:if>
<c:if test="${currentNode.properties.city.boolean}">
    city   </br>
</c:if>
<c:if test="${currentNode.properties.state.boolean}">
    state  </br>
</c:if>
<c:if test="${currentNode.properties.zip.boolean}">
    zip   </br>
</c:if>
<c:if test="${currentNode.properties.country.boolean}">
    country   </br>
</c:if>
<c:if test="${currentNode.properties.remarks.boolean}">
    remarks   </br>
</c:if>