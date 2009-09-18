<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="startDate" var="startDate"/>
<jcr:nodeProperty node="${currentNode}" name="endDate" var="endDate"/>
<jcr:nodeProperty node="${currentNode}" name="location" var="location"/>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="eventsType" var="eventsType"/>
<jcr:nodeProperty node="${currentNode}" name="body" var="body"/>



        <div class="eventsListItem"><!--start eventsListItem -->
            <div class="eventsInfoDate">
                <div class="eventsDate">
                    <span class="day"><fmt:formatDate pattern="dd" value="${startDate.date}"/></span>
                    <span class="month"><fmt:formatDate pattern="MM" value="${startDate.date}"/></span>
                    <span class="year"><fmt:formatDate pattern="yyyy" value="${startDate.date}"/></span>
                </div>
                <!-- <c:if test="${not empty endDate}">  -->
                    <div class="eventsTxtDate">
                        <span><fmt:message key='to'/></span>
                    </div>
                    <div class="eventsDate">
                        <span class="day"><fmt:formatDate pattern="dd" value="${endDate.date}"/></span>
                        <span class="month"><fmt:formatDate pattern="MM" value="${endDate.date}"/></span>
                        <span class="year"><fmt:formatDate pattern="yyyy" value="${endDate.date}"/></span>
                    </div>
               <!--  </c:if>    -->
            </div>
            <div class="eventsBody"><!--start eventsBody -->
                <p class="eventsLocation"><span>${location.string}</span></p>
                <p class="eventsLocation"><span>${eventsType.string}</span></p>
                <h4>${location.string}</h4>

                <p class="eventsResume">
                    ${body.string}></p>
              <!--  <template:getContentObjectCategories var="eventsContainerCatKeys"
                                                     objectKey="contentContainer_${pageScope.eventContainer.ID}"/> -->
              <!--  <c:if test="${!empty eventsContainerCatKeys }">     -->
                    <div class="eventsMeta">
        	    <span class="categoryLabel"><fmt:message key='category'/>  :</span>
                     <!--   <ui:displayCategoryTitle categoryKeys="${eventsContainerCatKeys}"/>       -->
                    </div>
               <!--  </c:if>   -->
            </div>
            <!--start eventsBody -->
            <div class="clear"> </div>
        </div>

