<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field"> ${props.label}</br>
          <select name="${currentNode.name}">
              <option>${props.option}</option>
          </select>
</p>