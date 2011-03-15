<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@tag import="org.jahia.params.valves.LoginEngineAuthValveImpl"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
  <c:set var="formId" value="<%= this.toString() %>"/>
  <c:set target="${attributes}" property="action" value="${functions:default(attributes.action, jahia.page.url)}"/>
  <c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'loginForm')}"/>
  <c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
  <form ${functions:attributes(attributes)}>
      <c:choose>
          <c:when test="${not empty requestScope['javax.servlet.error.request_uri']}">
              <input type="hidden" name="redirect" value="${requestScope['javax.servlet.error.request_uri']}"/>
          </c:when>
          <c:otherwise>
              <input type="hidden" name="redirect" value="<c:url value='${url.base}${renderContext.mainResource.node.path}.html'/>"/>
          </c:otherwise>
      </c:choose>
    <input type="hidden" name="<%=LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER%>" value="true"/>
    <c:if test="${doRedirect}">
        <input type="hidden" name="<%=LoginEngineAuthValveImpl.DO_REDIRECT%>" value="true"/>
    </c:if>
    <jsp:doBody/>
  </form>
</c:if>