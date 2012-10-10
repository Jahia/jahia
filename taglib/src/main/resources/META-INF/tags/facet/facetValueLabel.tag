<%@ tag body-content="empty" description="Renders the label of a facet value." %>
<%@ tag import="java.text.SimpleDateFormat"%>
<%@ tag import="java.util.Date"%>
<%@ tag import="java.util.Locale"%>
<%@ tag import="org.apache.solr.schema.DateField"%>
<%@ tag import="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"%>
<%@ tag import="org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService"%>
<%@ tag import="org.jahia.services.render.RenderContext"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean" description="Should we display the label or just return it in the parameter set by attribute var."%>
<%@ attribute name="currentFacetField" required="false" type="org.apache.solr.client.solrj.response.FacetField" description="The FacetField for the current facet." %>
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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="facetValueName" value=""/>
<c:set var="display" value="${functions:default(display, true)}"/>

<c:choose>
    <c:when test="${currentFacetField != null && facetValueCount != null}">
        <c:set var="currentFacetName" value="${currentFacetField.name}"/>
        <c:set var="facetValueName" value="${facetValueCount.name}"/>        
    </c:when>
    <c:otherwise>
        <c:set var="currentFacetName" value="${currentActiveFacet != null ? currentActiveFacet.key : ''}"/>
        <c:set var="facetValueName" value="${currentActiveFacetValue.key}"/>
    </c:otherwise>
</c:choose>
<c:if test="${functions:matches('[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}', facetValueName)}">
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
<c:if test="${not empty facetValueFormats[currentFacetName]}">
    <c:set var="dateFieldFormat" value="${facetValueFormats[currentFacetName]}"/>
    <jsp:useBean id="dateFieldForFormatting" class="org.apache.solr.schema.DateField" scope="application"/>
    <% 
    Date date = null;
    SimpleDateFormat df = null;
    try {
        DateField dateField = (DateField)application.getAttribute("dateFieldForFormatting");
        date = dateField.toObject((String)jspContext.findAttribute("facetValueName"));
        RenderContext renderContext = (RenderContext)jspContext.findAttribute("renderContext");
        df = new SimpleDateFormat((String)jspContext.findAttribute("dateFieldFormat"), renderContext != null ? renderContext.getMainResource().getLocale() : Locale.ENGLISH);
    } catch (Exception e) {
    %>  <utility:logger value="<%=e.toString()%>" level="WARN"/> <%
    }
    %>
    <c:set var="mappedLabel" value="<%=df != null && date != null ? df.format(date) : null%>"/>
</c:if>
<c:if test="${not empty facetValueRenderers[currentFacetName]}">
  <c:set var="fieldRenderer" value="${facetValueRenderers[currentFacetName]}"/>
  <c:set var="fieldNodeType" value="${facetValueNodeTypes[currentFacetName]}"/>
  <c:if test="${not empty fieldNodeType}">
      <c:set var="fieldPropertyType" value="${fieldNodeType.propertyDefinitionsAsMap[currentFacetName]}"/>
  </c:if>
  <c:set var="mappedLabel" value='<%=ChoiceListRendererService.getInstance().getRenderers().get((String)jspContext.findAttribute("fieldRenderer")).getStringRendering((RenderContext) jspContext.findAttribute("renderContext"), (ExtendedPropertyDefinition) jspContext.findAttribute("fieldPropertyType"), jspContext.findAttribute("facetValueName"))%>'/>
</c:if>
<c:set var="mappedLabel" value="${empty mappedLabel ? facetValueName : mappedLabel}"/>
<c:if test="${display}">
    ${mappedLabel}
</c:if>
<c:set var="facetValueLabel" value="${mappedLabel}"/>
