<c:forTokens items="${currentNode.properties['__value__'].string}" delims="," var="item" varStatus="status">
    <span>${item}</span><br/>
</c:forTokens>