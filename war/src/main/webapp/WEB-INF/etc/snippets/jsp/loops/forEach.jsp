<c:forEach items="${currentNode.properties['__value__'].values}" var="item" varStatus="status">
    <span>${item.string}</span><br/>
</c:forEach>