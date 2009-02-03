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
<%@ tag body-content="empty" description="Renders items per page drop down box." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.Integer" description="The initial value." %>
<%@ attribute name="options" required="false" type="java.lang.String"
              description="Allowed options as a comma separated list of value." %>
<c:set var="value" value="${h:default(param['src_itemsPerPage'], h:default(value, '10'))}"/>
<c:set var="options" value="${h:default(options, '5,10,20,30,50,100')}"/>
<c:set target="${attributes}" property="name" value="src_itemsPerPage"/>
<c:if test="${display}">
    <select name="src_itemsPerPage">
        <c:forTokens items="${options}" delims="," var="opt">
            <option value="${opt}" ${opt == value ? 'selected="selected"' : ''}>${opt}</option>
        </c:forTokens>
    </select>
</c:if>
<c:if test="${!display}">
    <input type="hidden" ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
</c:if>