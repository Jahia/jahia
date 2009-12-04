<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field">
          <select name="${currentNode.name}">
              <option>${props.option}</option>
          </select>
</p>