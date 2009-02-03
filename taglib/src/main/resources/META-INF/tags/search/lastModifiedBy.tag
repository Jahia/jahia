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
<%@ tag body-content="empty" description="Renders the entry field for quering last editor of the content object." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for this field." %>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set target="${attributes}" property="name" value="src_lastEditor"/>
<c:set var="value" value="${h:default(param['src_lastEditor'], value)}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>