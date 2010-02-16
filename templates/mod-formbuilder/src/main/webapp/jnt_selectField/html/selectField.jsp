<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field"> ${props.label}</br>
          <select name="${currentNode.name}">
              <c:forTokens items="${props.option}" delims="," var="option">
                <option>${option}</option>
              </c:forTokens>
          </select>
</p>