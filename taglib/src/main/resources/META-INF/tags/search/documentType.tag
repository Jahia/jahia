<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
<%@ tag body-content="empty" description="Renders document type selection control with all node types available." %>
<%@include file="declaration.tagf" %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<c:set var="value" value="${h:default(param.src_documentType, value)}"/>
<c:if test="${display}">
    <select name="src_documentType">
        <option value=""><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.any"
                                                        defaultValue="any"/></option>
        <jcr:nodeType ntname="${jcr.nt_file}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeType ntname="${jcr.nt_folder}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeTypes baseType="${jcr.jahiamix_extension}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeTypes>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_documentType" value="${value}"/></c:if>