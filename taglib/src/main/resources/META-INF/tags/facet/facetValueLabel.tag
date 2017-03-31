<%@tag import="org.jahia.services.content.nodetypes.renderer.NodeReferenceChoiceListRenderer"%>
<%@tag import="org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer"%>
<%@ tag body-content="empty" description="Renders the label of a facet value." %>
<%@ tag import="java.text.SimpleDateFormat"%>
<%@ tag import="java.util.Date"%>
<%@ tag import="java.util.Locale"%>
<%@ tag import="org.apache.solr.client.solrj.response.FacetField"%>
<%@ tag import="org.apache.solr.client.solrj.response.RangeFacet"%>
<%@ tag import="org.apache.solr.schema.DateField"%>
<%@ tag import="org.apache.solr.util.DateMathParser"%>
<%@ tag import="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"%>
<%@ tag import="org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService"%>
<%@ tag import="org.jahia.services.render.RenderContext"%>
<%@ tag import="java.text.MessageFormat"%>
<%@ tag import="java.util.TimeZone" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean" description="Should we display the label or just return it in the parameter set by attribute var."%>
<%@ attribute name="currentFacetField" required="false" type="org.apache.solr.client.solrj.response.FacetField" description="The FacetField for the current facet." %>
<%@ attribute name="currentFacetFieldName" required="false" type="java.lang.String" description="The field name for the current facet." %>
<%@ attribute name="facetValueCount" required="false" type="java.lang.Object" description="The FacetField.Count for the current facet value." %>
<%@ attribute name="currentActiveFacet" required="false" type="java.lang.Object" description="Alternatively the Map.Entry with KeyValue from the active facet filters variable." %>
<%@ attribute name="currentActiveFacetValue" required="false" type="java.lang.Object" description="The current Key/Value entry from the active facet filters variable." %>
<%@ attribute name="facetValueLabels" required="false" type="java.util.Map" description="Mapping between facet values and label." %>
<%@ attribute name="facetValueFormats" required="false" type="java.util.Map" description="Mapping between facet names and formats." %>
<%@ attribute name="facetValueRenderers" required="false" type="java.util.Map" description="Mapping between facet names and renderers." %>
<%@ attribute name="facetValueNodeTypes" required="false" type="java.util.Map" description="Mapping between facet names and node types." %>
<%@ variable name-given="facetValueLabel" scope="AT_END"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="facetValueName" value=""/>
<c:set var="display" value="${functions:default(display, true)}"/>

<c:choose>
    <c:when test="${currentFacetFieldName != null && facetValueCount != null}">
        <c:set var="currentFacetName" value="${currentFacetFieldName}"/>
        <% 
        boolean dateRange = false;
        boolean range = false;
        Object gap = null;
        Number facetBeginRangeValue = null;
        Object facetValueCount = jspContext.findAttribute("facetValueCount");
        String facetValueName = "unknown";
        if (facetValueCount instanceof FacetField.Count) {
            FacetField.Count count = (FacetField.Count)facetValueCount;
            facetValueName = count.getName();
            if (count.getFacetField().getEnd() != null) {
                dateRange = true;
                gap = count.getFacetField().getGap();
            }
        } else if (facetValueCount instanceof RangeFacet.Count) {
            range = true;
            RangeFacet.Count count = (RangeFacet.Count)facetValueCount;
            facetValueName = count.getValue();
            gap = count.getRangeFacet().getGap();
            if (count.getRangeFacet().getGap() instanceof String) {
                dateRange = true;
            } else if (count.getRangeFacet().getGap() instanceof Double && Character.isDigit(facetValueName.charAt(0))) {
                facetBeginRangeValue = Double.parseDouble(facetValueName);
            } else if (Character.isDigit(facetValueName.charAt(0))) {
                facetBeginRangeValue = Long.parseLong(facetValueName);
            }
        }
        %>
        <c:set var="facetBeginRangeValue" value="<%=facetBeginRangeValue%>"/> 
        <c:set var="facetValueName" value="<%=facetValueName%>"/>        
        <c:set var="dateRange" value="<%=dateRange%>"/>
        <c:set var="range" value="<%=range%>"/>
        <c:set var="gap" value="<%=gap%>"/>
    </c:when>
    <c:when test="${currentFacetField != null && facetValueCount != null}">
        <c:set var="currentFacetName" value="${currentFacetField.name}"/>
        <c:set var="facetValueName" value="${facetValueCount.name}"/>
        <% 
        boolean dateRange2 = false;
        Object gap2 = null;
        Object facetValueCount = jspContext.findAttribute("facetValueCount");
        String facetValueName = "unknown";
        if (facetValueCount instanceof FacetField.Count) {
            FacetField.Count count = (FacetField.Count)facetValueCount;
            if (count.getFacetField().getEnd() != null) {
                dateRange2 = true;
                gap2 = count.getFacetField().getGap();
            }
        }
        %>
        <c:set var="dateRange" value="<%=dateRange2%>"/>
        <c:set var="gap" value="<%=gap2%>"/>
    </c:when>
    <c:otherwise>
        <c:set var="currentFacetName" value="${currentActiveFacet != null ? currentActiveFacet.key : ''}"/>
        <c:set var="facetValueName" value="${currentActiveFacetValue.key}"/>
    </c:otherwise>
</c:choose>
<c:set var="uuidPattern" value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}" />
<c:if test="${functions:matches(uuidPattern, facetValueName)}">
    <jcr:node var="refNode" uuid="${facetValueName}"/>
</c:if>
<c:if test="${functions:matches('[0-9]+(/[a-zA-Z0-9_\\\\-]+)+', facetValueName)}">
    <jcr:node var="refNode" path="/${fn:substringAfter(facetValueName, '/')}"/>
</c:if>
<c:choose>
    <c:when test="${not empty refNode}">        
        <c:set var="mappedLabel" value="${refNode.displayableName}"/>
    </c:when>
    <c:when test="${not empty facetValueLabels}">
        <c:forEach items="${facetValueLabels}" var="currentFacetValueLabel">
            <c:if test="${empty mappedLabel and fn:endsWith(facetValueName, currentFacetValueLabel.key)}">
                <c:set var="mappedLabel" value="${currentFacetValueLabel.value}"/>
            </c:if>
        </c:forEach>
    </c:when>
</c:choose>
<c:if test="${not empty facetValueRenderers[currentFacetName]}">
  <c:set var="fieldRenderer" value="${facetValueRenderers[currentFacetName]}"/>
  <c:set var="fieldNodeType" value="${facetValueNodeTypes[currentFacetName]}"/>
  <c:if test="${not empty fieldNodeType}">
      <c:set var="fieldPropertyType" value="${fieldNodeType.propertyDefinitionsAsMap[currentFacetName]}"/>
  </c:if>
  <%
      ChoiceListRenderer renderer = ChoiceListRendererService.getInstance().getRenderers().get((String)jspContext.findAttribute("fieldRenderer"));
      String mappedLabel = renderer.getStringRendering((RenderContext) jspContext.findAttribute("renderContext"),
              (ExtendedPropertyDefinition) jspContext.findAttribute("fieldPropertyType"),
              renderer instanceof NodeReferenceChoiceListRenderer ? jspContext.findAttribute("refNode") : jspContext.findAttribute("facetValueName"));
  %>
  <c:set var="mappedLabel" value='<%=mappedLabel%>'/>
</c:if>

<c:set var="facetValueFormat" value="${facetValueFormats[currentFacetName]}"/>
<c:if test="${empty mappedLabel and dateRange}">
    <jsp:useBean id="dateFieldForFormatting" class="org.apache.solr.schema.DateField" scope="application"/>
    <% 
    try {
        DateField dateField = (DateField)application.getAttribute("dateFieldForFormatting");
        String gap = (String)jspContext.findAttribute("gap");
        Date dateBegin = dateField.toObject((String)jspContext.findAttribute("facetValueName"));
        Date dateEnd = null;
        if (gap != null && !gap.isEmpty()) {
            DateMathParser dmp = new DateMathParser(DateField.UTC, Locale.US);
            dmp.setNow(dateBegin);
            dateEnd = dmp.parseMath(gap);
        }
        RenderContext renderContext = (RenderContext)jspContext.findAttribute("renderContext");
        String dateFormat = (String)jspContext.findAttribute("facetValueFormat");
        if (dateFormat == null || !dateFormat.contains("{")) {
            dateFormat = "{0, date" + (dateFormat != null && !dateFormat.isEmpty() ? "," + dateFormat : "") + "}";
        }
        MessageFormat mf = new MessageFormat(dateFormat, renderContext != null ? renderContext.getMainResource().getLocale() : Locale.ENGLISH);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Object [] formats = mf.getFormats();
        for (int i = 0; i < formats.length; i++) {
            if (formats[i] instanceof SimpleDateFormat) {
                ((SimpleDateFormat)formats[i]).setTimeZone(tz);
            }
        }
        Object [] args = {dateBegin, dateEnd};
        %>
        <c:set var="mappedLabel" value="<%=mf.format(args)%>"/>
        <%
    } catch (Exception e) {
    %>  <utility:logger value="<%=e.toString()%>" level="WARN"/> <%
    }
    %>
</c:if>
<c:if test="${empty mappedLabel and range and not dateRange}">
    <c:set var="messageKey" value="${not empty facetValueFormat ? facetValueFormat : 'jnt_rangeFacet.value'}"/>
    <c:choose>
    <c:when test="${facetValueName == 'before' or facetValueName == 'after' or facetValueName == 'between'}">
        <c:set var="messageKey" value="${messageKey}.${facetValueName}"/>
        <fmt:message var="mappedLabel" key="${messageKey}">
            <fmt:param value="${facetValueName}"/>
        </fmt:message>        
    </c:when>
    <c:otherwise>
        <fmt:message var="mappedLabel" key="${messageKey}">
            <fmt:param value="${facetBeginRangeValue}"/>
            <fmt:param value="${facetBeginRangeValue + gap}"/>
        </fmt:message>
    </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${empty mappedLabel and not empty facetValueFormat}">
    <c:set var="facetIsADate" value="${functions:formatISODate(facetValueName, facetValueFormat, currentLocale)}"/>
    <c:choose>
        <c:when test="${not empty facetIsADate}">
            <c:set var="mappedLabel" value="${facetIsADate}"/>
        </c:when>
        <c:otherwise>
            <fmt:message var="mappedLabel" key="${facetValueFormat}">
                <fmt:param value="${facetValueName}"/>
            </fmt:message>
        </c:otherwise>
    </c:choose>
</c:if>
<c:set var="mappedLabel" value="${empty mappedLabel ? facetValueName : mappedLabel}"/>
<c:if test="${display}">
    ${mappedLabel}
</c:if>
<c:set var="facetValueLabel" value="${mappedLabel}"/>
