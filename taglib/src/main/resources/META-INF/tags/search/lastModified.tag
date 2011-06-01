<%@ tag body-content="empty" description="Renders a date control to query the last modification date of a content object." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String"
              description="Initial date value. Supported values are: anytime, today, last_week, last_month, last_three_months, last_six_months, range. In case of range value, the from and to attributes can be provided." %>
<%@ attribute name="from" required="false" type="java.lang.String"
              description="Initial value for date from in case of the range date type." %>
<%@ attribute name="to" required="false" type="java.lang.String"
              description="Initial value for date to in case of the range date type." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib"%>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib"%>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:set target="${attributes}" property="name" value="src_lastModified"/>
<c:set var="valueParamName" value="${attributes.name}.type"/>
<c:set var="value" value="${functions:default(param[valueParamName], value)}"/>
<c:if test="${empty requestScope['org.apache.jsp.tag.web.search.dateTag.included']}">
    <c:set var="org.apache.jsp.tag.web.search.dateTag.included" value="true" scope="request"/>
    <template:addResources>
        <script type="text/javascript">
            function searchDateTypeToggle(dateType, range) {
                if (dateType.value == 'range' && range.style.display != 'none' || dateType.value != 'range' && range.style.display == 'none') {
                    return;
                }
                range.style.display = dateType.value == 'range' ? '' : 'none';
                for (var i = 0; i < range.childNodes.length; i++) {
                    if (range.childNodes[i].nodeName.toLowerCase() == 'input') {
                        range.childNodes[i].disabled = dateType.value != 'range';
                    }
                }
            }
        </script>
    </template:addResources>
</c:if>
<c:if test="${display}">
    <c:set target="${attributes}" property="name" value="src_lastModified.type"/>
    <c:set var="rangeId" value="${functions:nextLongIdentifier()}"/>
    <select ${functions:attributes(attributes)} onchange="searchDateTypeToggle(this, document.getElementById('range-${rangeId}'));">
        <c:set target="${attributes}" property="name" value="src_lastModified"/>
        <option value="anytime" ${value == 'anytime' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.anytime"/></option>
        <option value="today" ${value == 'today' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.today"/></option>
        <option value="last_week" ${value == 'last_week' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.lastWeek"/></option>
        <option value="last_month" ${value == 'last_month' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.lastMonth"/></option>
        <option value="last_three_months" ${value == 'last_three_months' ? 'selected="selected"' : ''}>
            <fmt:message key="searchForm.date.lastThreeMonths"/></option>
        <option value="last_six_months" ${value == 'last_six_months' ? 'selected="selected"' : ''}>
            <fmt:message key="searchForm.date.lastSixMonths"/></option>
        <option value="range" ${value == 'range' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.range"/></option>
    </select><div ${value != 'range' ? 'style="display:none"' : ''} class="dateRange" id="range-${rangeId}">
    <c:set var="valueParamName" value="${attributes.name}.from"/>
    <fmt:message key="searchForm.date.from"/>:&nbsp;
    <input type="text" name="${valueParamName}" id="${fn:replace(valueParamName, '.', '_')}" value="${functions:default(param[valueParamName], from)}"/>
    <uiComponents:dateSelector fieldId="${fn:replace(valueParamName, '.', '_')}">
        {dateFormat: 'dd.mm.yy', showButtonPanel: true, showOn:'focus'}
    </uiComponents:dateSelector>
    <c:set var="valueParamName" value="${attributes.name}.to"/>
    <fmt:message key="searchForm.date.to"/>:&nbsp;
    <input type="text" name="${valueParamName}" id="${fn:replace(valueParamName, '.', '_')}" value="${functions:default(param[valueParamName], to)}"/>
    <uiComponents:dateSelector fieldId="${fn:replace(valueParamName, '.', '_')}">
        {dateFormat: 'dd.mm.yy', showButtonPanel: true, showOn:'focus'}
    </uiComponents:dateSelector>
</div>
</c:if>
<c:if test="${!display}"><input type="hidden" name="${valueParamName}" value="${fn:escapeXml(value)}"/></c:if>