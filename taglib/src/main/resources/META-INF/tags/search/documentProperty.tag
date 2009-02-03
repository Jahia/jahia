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
        description="Renders input control for the document property depending on its type (boolean, text, date, category)." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="documentType" required="true" type="java.lang.String"
              description="The node type of this property." %>
<%@ attribute name="name" required="true" type="java.lang.String" description="The name of the property." %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value for the property" %>
<%@ attribute name="from" required="false" type="java.lang.String"
              description="For date properties. Initial value for date from in case of the range date type." %>
<%@ attribute name="to" required="false" type="java.lang.String"
              description="For date properties. Initial value for date to in case of the range date type." %>
<%@ attribute name="match" required="false" type="java.lang.String"
              description="For text properties. The match type for search term." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="For category properties. The include children initial value." %>
<c:set var="propName" value="src_properties(${documentType}).${name}.value"/>
<c:set var="value" value="${h:default(param[propName], value)}"/>
<s:documentPropertyDescriptor documentType="${documentType}" name="${name}">
    <c:if test="${display}">
        <c:set target="${attributes}" property="name" value="${propName}"/>
        <c:choose>
            <c:when test="${descriptor.type == 'BOOLEAN'}">
                <c:set target="${attributes}" property="type" value="checkbox"/>
                <c:set target="${attributes}" property="value" value="true"/>
                <c:if test="${value == 'true'}">
                    <c:set target="${attributes}" property="checked" value="checked"/>
                </c:if>
                <input ${h:attributes(attributes)}/>
            </c:when>
            <c:when test="${descriptor.type == 'TEXT'}">
                <c:if test="${descriptor.constrained}">
                    <select ${h:attributes(attributes)}>
                        <option value=""><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.search.any"
                                                                        defaultValue="any"/></option>
                        <c:forEach items="${descriptor.allowedValues}" var="allowedValue">
                            <option value="${fn:escapeXml(allowedValue)}" ${value == allowedValue ? 'selected="selected"' : ''}>${fn:escapeXml(allowedValue)}</option>
                        </c:forEach>
                    </select>
                </c:if>
                <c:if test="${!descriptor.constrained}">
                    <c:set var="propName" value="src_properties(${documentType}).${name}.match"/>
                    <c:set var="match" value="${h:default(param[propName], match)}"/>
                    <select name="src_properties(${documentType}).${name}.match">
                        <option value="any_word" ${'any_word' == match ? 'selected="selected"' : ''}>!!!contains any
                            word
                        </option>
                        <option value="all_words" ${'all_words' == match ? 'selected="selected"' : ''}>!!!contains all
                            words
                        </option>
                        <option value="exact_phrase" ${'exact_phrase' == match ? 'selected="selected"' : ''}>!!!contains
                            exact phrase
                        </option>
                        <option value="without_words" ${'without_words' == match ? 'selected="selected"' : ''}>!!!does
                            not contain
                        </option>
                    </select>
                    <input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
                </c:if>
            </c:when>
            <c:when test="${descriptor.type == 'CATEGORY'}">
                <c:set var="propName" value="src_properties(${documentType}).${name}.categoryValue.value"/>
                <c:set target="${attributes}" property="name" value="${propName}"/>
                <c:set target="${attributes}" property="id" value="${propName}"/>
                <c:set var="value" value="${h:default(param[propName], value)}"/>
                <c:set var="categoryRoot"
                       value="${not empty descriptor.selectorOptions && not empty descriptor.selectorOptions.root ? descriptor.selectorOptions.root : 'root'}"/>
                <input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
                <ui:categorySelector fieldId="${propName}"
                                     fieldIdIncludeChildren="src_properties(${documentType}).${name}.categoryValue.includeChildren"
                                     root="${categoryRoot}"/>
            </c:when>
            <c:when test="${descriptor.type == 'DATE'}">
                <s:date name="src_properties(${documentType}).${name}.dateValue" value="${value}" from="${from}"
                        to="${to}"/>
            </c:when>
        </c:choose>
    </c:if>
    <c:if test="${!display}">
        <c:choose>
            <c:when test="${descriptor.type == 'BOOLEAN' && value == 'true'}">
                <input type="hidden" name="${propName}" value="${value}"/>
            </c:when>
            <c:when test="${descriptor.type == 'TEXT'}">
                <input type="hidden" name="${propName}" value="${value}"/>
                <c:if test="${not descriptor.constrained && not empty match}">
                    <input type="hidden" name="src_properties(${documentType}).${name}.match" value="${match}"/>
                </c:if>
            </c:when>
            <c:when test="${descriptor.type == 'CATEGORY'}">
                <input type="hidden" name="src_properties(${documentType}).${name}.categoryValue.value"
                       value="${value}"/>
                <c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
                <input type="hidden" name="src_properties(${documentType}).${name}.categoryValue.includeChildren"
                       value="${includeChildren}"/>
            </c:when>
            <c:when test="${descriptor.type == 'DATE'}">
                <input type="hidden" name="src_properties(${documentType}).${name}.dateValue.type" value="${value}"/>
                <c:if test="${value == 'range'}">
                    <c:if test="${not empty from}">
                        <input type="hidden" name="src_properties(${documentType}).${name}.dateValue.from"
                               value="${from}"/>
                    </c:if>
                    <c:if test="${not empty to}">
                        <input type="hidden" name="src_properties(${documentType}).${name}.dateValue.to" value="${to}"/>
                    </c:if>
                </c:if>
            </c:when>
        </c:choose>
    </c:if>
</s:documentPropertyDescriptor>
