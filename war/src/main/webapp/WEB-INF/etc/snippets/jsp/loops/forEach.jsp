<c:forEach items="${currentNode.properties['__value__']}" var="item" varStatus="status">
    <span>${item.string}</span><br/>
</c:forEach>