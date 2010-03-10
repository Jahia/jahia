<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field">
    <label class="left" for="${currentNode.name}">${props.label}</label>
    <textarea type="text" name="${currentNode.name}" cols="${props.cols}" rows="${props.rows}"><c:if test="${not empty sessionScope.formError}">${sessionScope.formDatas[currentNode.name][0]}</c:if><c:if test="${empty sessionScope.formError}">${props.defaultValue}</c:if>
    </textarea>
</p>