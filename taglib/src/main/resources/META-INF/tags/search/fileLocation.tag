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
<%@ tag body-content="empty" description="Renders file path selection control." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value of the file path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="The initial value of the include children field." %>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set target="${attributes}" property="name" value="src_fileLocation.value"/>
<c:set target="${attributes}" property="id" value="src_fileLocation.value"/>
<c:set var="value" value="${h:default(param['src_fileLocation.value'], value)}"/>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${h:default(param['src_fileLocation.includeChildren'], empty paramValues['src_fileLocation.value'] ? includeChildren : 'false')}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${display}">
    <ui:folderSelector fieldId="${attributes.id}" fieldIdIncludeChildren="src_fileLocation.includeChildren"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_fileLocation.includeChildren" value="true"/>
</c:if>
