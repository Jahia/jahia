<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>



<div class="peopleListItem">

    <div class="peopleBody">

<table>
    <thead><td> </td><td>First Name</td><td>Last Name</td><td>Function (BU)</td><td>Email</td></thead>

    <tr>
<c:forEach items="${currentNode.editableChildren}" var="subchild">
<p>
    <template:module node="${subchild}" template="${subNodesTemplate}" />
</p>
</c:forEach>

</table>


                <div class="clear"></div>
    </div>
    <!--stop peopleBody -->
    <div class="clear"></div>
</div>
