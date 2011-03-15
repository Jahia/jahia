<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="jquery.autocomplete.css" />
<template:addResources type="css" resources="thickbox.css" />
<template:addResources type="css" resources="simplesearchform.css" />

<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />
<template:addResources>
<script type="text/javascript">
    $(document).ready(function() {

        /**
         * As any property can match the query, we try to intelligently display properties that either matched or make
         * sense to display.
         * @param node
         */
        function getText(node) {
            if (node.matchingProperties.length > 0) {
                var firstMatchingProperty = node.matchingProperties[0];
                return node[firstMatchingProperty];
            }
            if (node["jcr:title"] != null) {
                return node["jcr:title"];
            } else if (node["text"] != null) {
                return node["text"];
            } else if (node["j:nodename"] != null) {
                return node["j:nodename"];
            }
        }

        function format(result) {
            return getText(result["node"]);
        }

        $("#searchTerm").autocomplete("${url.find}", {
            dataType: "json",
            selectFirst: false,
            cacheLength: 1,
            parse: function parse(data) {
                return $.map(data, function(row) {
				    return {
					    data: row,
					    value: getText(row["node"]),
					    result: getText(row["node"])
				    }
			    });
            },
            formatItem: function(item) {
			    return format(item);
		    },
            extraParams: {
                query : "/jcr:root${renderContext.site.path}//element(*, nt:base)[jcr:contains(.,'{$q}*')]",
                language : "xpath",
                propertyMatchRegexp : "{$q}.*",
                removeDuplicatePropValues : "true"
            }
        });
    });
</script>
</template:addResources>
<template:addCacheDependency uuid="${currentNode.properties.result.string}"/>
<c:if test="${not empty currentNode.properties.result.node}">
<s:form method="post" class="simplesearchform" action="<c:url value='${url.base}${currentNode.properties.result.node.path}.html'/>">
		<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
		<c:if test="${not empty title.string}">
		<label for="searchTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
		</c:if>
		<fmt:message key='search.startSearching' var="startSearching"/>
       	<s:term id="searchTerm" value="${startSearching}" searchIn="siteContent,tags" onfocus="if(this.value==this.defaultValue)this.value='';" onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
       	<s:site value="${renderContext.site.name}" display="false"/>
       	<s:language value="${renderContext.mainResource.locale}" display="false" />
    	<input class="searchsubmit" type="submit"  title="<fmt:message key='search.submit'/>" value=""/>

</s:form><br class="clear"/>
</c:if>