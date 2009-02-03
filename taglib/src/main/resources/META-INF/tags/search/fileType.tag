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
<%@ tag body-content="empty"
        description="Renders file type selection control with all file type groups configured in the applicationcontext-basejahiaconfig.xml file." %>
<%@ tag import="org.jahia.services.content.JCRContentUtils" %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<c:set var="fileTypes" value="<%= JCRContentUtils.getInstance().getMimeTypes() %>"/>
<c:if test="${not empty value}">
    <% if (!JCRContentUtils.getInstance().getMimeTypes().containsKey(jspContext.getAttribute("value"))) {
        throw new IllegalArgumentException("Unsupported file type '" + jspContext.getAttribute("value") + "'. See applicationcontext-basejahiaconfig.xml file for configured file types.");
    } %>
</c:if>
<c:set var="value" value="${h:default(param.src_fileType, value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="name" value="src_fileType"/>
    <select ${h:attributes(attributes)}>
        <option value=""><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.any"
                                                        defaultValue="any"/></option>
        <c:forEach items="${fileTypes}" var="type">
            <option value="${type.key}" ${value == type.key ? 'selected="selected"' : ''}><utility:resourceBundle resourceBundle="JahiaEnginesResources"
                    resourceName="org.jahia.engines.search.fileType.${type.key}" defaultValue="${type.key}"/></option>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_fileType" value="${value}"/></c:if>