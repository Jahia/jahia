<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field">
     <input type="radio" name="${currentNode.name}" value="${props.value}" /> <label for="${currentNode.name}">${props.label}</label><br />
</p>