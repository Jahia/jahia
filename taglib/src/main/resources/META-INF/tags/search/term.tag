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
<%@ tag body-content="empty" description="Renders search term input control." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value." %>
<%@ attribute name="match" required="false" type="java.lang.String" description="The match type for search terms." %>
<%@ attribute name="searchIn" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in." %>
<c:set var="formId" value="<%=this.getParent().toString() %>"/>
<c:set var="termIndex" value="${searchTermIndexes[formId]}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set var="key" value="src_terms[${termIndex}].term"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${h:default(param[key], value)}"/>
<c:set var="key" value="src_terms[${termIndex}].fields"/>
<c:set var="searchIn" value="${h:default(param[key], h:default(searchIn, 'content'))}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${not empty match && match != 'as_is'}">
    <c:set var="key" value="src_terms[${termIndex}].match"/>
    <input type="hidden" name="${key}" value="${h:default(param[key], match)}"/>
</c:if>
<c:forTokens items="${searchIn}" delims="," var="field">
    <input type="hidden" name="src_terms[${termIndex}].fields.${field}" value="true"/>
</c:forTokens>
<c:set target="${searchTermIndexes}" property="${formId}" value="${searchTermIndexes[formId] + 1}"/>