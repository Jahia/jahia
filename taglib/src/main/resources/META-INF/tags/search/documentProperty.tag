<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="empty" dynamic-attributes="attributes"
        description="Renders input control for the document property depending on its type (boolean, text, date, category)." %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${h:default(display, true)}"/>
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
                        <option value=""><fmt:message key="searchForm.any.any"/></option>
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
                <c:set target="${attributes}" property="id" value="${h:default(attributes.id, propName)}"/>
                <c:set var="value" value="${h:default(param[propName], value)}"/>
                <c:set var="categoryRoot"
                       value="${not empty descriptor.selectorOptions && not empty descriptor.selectorOptions.root ? descriptor.selectorOptions.root : 'root'}"/>
                <input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
                <ui:categorySelector fieldId="${attributes.id}"
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
