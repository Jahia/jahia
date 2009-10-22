<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="startDate" var="startDate"/>
<jcr:nodeProperty node="${currentNode}" name="endDate" var="endDate"/>
<jcr:nodeProperty node="${currentNode}" name="location" var="location"/>
<jcr:nodeProperty node="${currentNode}" name="eventsType" var="eventsType"/>
<jcr:nodeProperty node="${currentNode}" name="body" var="body"/>

        <div class="eventsListItem"><!--start eventsListItem -->
            <div class="eventsInfoDate">
                <div class="eventsDate">
                    <span class="day"><fmt:formatDate pattern="dd" value="${startDate.time}"/></span>
                    <span class="month"><fmt:formatDate pattern="MM" value="${startDate.time}"/></span>
                    <span class="year"><fmt:formatDate pattern="yyyy" value="${startDate.time}"/></span>
                </div>
                <c:if test="${not empty endDate}">
                    <div class="eventsTxtDate">
                        <span><fmt:message key='to'/></span>
                    </div>
                    <div class="eventsDate">
                        <span class="day"><fmt:formatDate pattern="dd" value="${endDate.time}"/></span>
                        <span class="month"><fmt:formatDate pattern="MM" value="${endDate.time}"/></span>
                        <span class="year"><fmt:formatDate pattern="yyyy" value="${endDate.time}"/></span>
                    </div>
                </c:if>
            </div>
            <div class="eventsBody"><!--start eventsBody -->
                <p class="eventsLocation"><span>${location.string}</span></p>
                <p class="eventsLocation"><span>${eventsType.string}</span></p>
                <h4>${title.string}</h4>

                <p class="eventsResume">
                    ${body.string}</p>
                    <div class="eventsMeta">
        	    <span class="categoryLabel"><fmt:message key='category'/>:</span>
                        <jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="cat"/>
                        <c:if test="${cat != null}">
                                    <c:forEach items="${cat}" var="category">
                                        ${category.category.title}
                                    </c:forEach>

                        </c:if>
	                    <div>
	        	    		<fmt:message key="tags"/>:&nbsp;<template:module node="${currentNode}" template="tags"/>
	                        <template:module node="${currentNode}" template="addTag"/>
	                    </div>
                    </div>
            </div>
            <!--start eventsBody -->
            <div class="clear"> </div>
        </div>