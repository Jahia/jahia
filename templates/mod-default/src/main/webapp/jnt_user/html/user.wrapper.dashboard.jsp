<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>



        <div class="idTabsContainer"><!--start idTabsContainer-->

            <ul class="idTabs">
                <c:if test="${currentResource.resolvedTemplate == 'page'}">
                    <li><a class="on selected" href="${url.base}${currentNode.path}.page.html"><span>Profile</span></a>
                    </li>
                </c:if>
                <c:if test="${currentResource.resolvedTemplate != 'page'}">
                    <li><a class="off" href="${url.base}${currentNode.path}.page.html"><span>Profile</span></a>
                    </li>
                </c:if>
                <template:profileExtensions var="profileExtensions" />
                <c:forEach items="${profileExtensions}" var="ext">
                    <c:if test="${currentResource.resolvedTemplate == ext.key}">
                        <li><a class="on selected" href="${url.base}${currentNode.path}.${ext.key}.html"><span>${ext.value}</span></a>
                        </li>
                    </c:if>
                    <c:if test="${currentResource.resolvedTemplate != ext.key}">
                        <li><a class="off" href="${url.base}${currentNode.path}.${ext.key}.html"><span>${ext.value}</span></a>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
        <div class="tabContainer"><!--start tabContainer-->
            ${wrappedContent}
        <div class="clear"></div></div>
        <!--stop tabContainer-->