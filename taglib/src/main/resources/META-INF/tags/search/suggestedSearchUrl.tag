<%@ tag body-content="empty" description="Renders the search URL built from current search request parameters, considering the specified suggestion search term. The suggested term can be specified explicitly by providing suggestion attribute or by nesting this tag inside &lt;s:suggestions/&gt; tag." import="org.jahia.taglibs.search.SuggestionsTag"
%><%@ attribute name="suggestion" required="false" type="java.lang.String"
              description="The search term suggestion to generate the URL for."
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" 
%><c:if test="${empty suggestion}"><%
SuggestionsTag parent = (SuggestionsTag) findAncestorWithClass(this, SuggestionsTag.class);
if (parent == null) {
     throw new JspTagException("Suggestion term must be provided either through the 'suggestion' attribute" + 
             " or by nesting this tag into <s:suggestions/> tag");
}
jspContext.setAttribute("suggestion", parent.getSuggestion().getSuggestedQuery());
%></c:if><c:set var="searchUrl"><search:searchUrl exclude='src_terms[0].term'/></c:set><c:url value="${searchUrl}" context="/">
	<c:param name="src_terms[0].term" value="${suggestion}"/>
</c:url>