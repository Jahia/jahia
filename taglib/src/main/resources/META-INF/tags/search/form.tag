<%-- 
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
--%>
<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the search controls." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="searchFor" required="false" type="java.lang.String"
              description="Specifies the search mode: pages or files [pages]." %>
<%@ attribute name="resultsPage" required="false" type="java.lang.String"
              description="You can set the target JSP template to be used after form submit. By default the JSP page is used, which is configured in the search-results element of the template deployment descriptor (templates.xml) for the current template set. The special keyword - this - can be used to identify the current page (the page will be preserved after form submit)." %>
<jsp:useBean id="searchTermIndexes" class="java.util.HashMap" scope="request"/>
<c:set var="formId" value="<%= this.toString() %>"/>
<c:set target="${searchTermIndexes}" property="${formId}" value="0"/>
<c:set target="${attributes}" property="action" value="${h:default(attributes.action, jahia.page.url)}"/>
<c:set target="${attributes}" property="name" value="${h:default(attributes.name, 'searchForm')}"/>
<c:set target="${attributes}" property="method" value="${h:default(attributes.method, 'get')}"/>
<c:set var="searchFor" value="${h:default(searchFor, 'autodetect')}"/>
<c:if test="${empty resultsPage}">
    <s:resultsPageUrl var="resultsPage"/>
</c:if>
<form ${h:attributes(attributes)}>
    <input type="hidden" name="src_mode" value="${searchFor}"/>
    <c:if test="${not empty resultsPage && resultsPage != 'this'}">
        <input type="hidden" name="template" value="${resultsPage}"/>
    </c:if>
    <jsp:doBody/>
</form>