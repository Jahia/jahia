<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<c:forEach items="${widgets.children}" var="node" varStatus="status">
    <li class="widget color-red">
        <div class="widget-head">
            <h3><jcr:nodeProperty node="${node}" name="jcr:title"/></h3>
        </div>
        <div class="widget-content" id="widget${status.index}">
            <script type="text/javascript">
                $(document).ready(function() {
                    $.get('${url.base}${node.path}.html', {ajaxcall:true}, function(data) {
                        $("#widget${status.index}").html(data);
                    });
                });
            </script>
        </div>
    </li>
</c:forEach>