<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:if test="${jcr:hasPermission(currentNode, 'write')}">
    <c:set var="aclNode" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
    <c:if test="${not empty aclNode}">
        <template:addResources type="css" resources="jquery.autocomplete.css"/>
        <template:addResources type="css" resources="thickbox.css"/>
        <template:addResources type="css" resources="searchusers.css"/>
        <template:addResources type="javascript" resources="jquery.min.js"/>
        <template:addResources type="javascript" resources="jquery.ajaxQueue.js"/>
        <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
        <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
        <template:addResources type="javascript" resources="thickbox-compressed.js"/>
        <template:addResources type="javascript" resources="jquery.form.js"/>
        <script type="text/javascript">
            $(document).ready(function() {

                /**
                 * As any property can match the query, we try to intelligently display properties that either matched or make
                 * sense to display.
                 * @param node
                 */
                function getUserNameText(node) {
                    if ((node["j:firstName"] || node["j:lastName"]) && (node["j:firstName"] != '' || node["j:lastName"] != '')) {
                        return node["j:firstName"] + ' ' + node["j:lastName"];
                    } else if (node["j:nodename"] != null) {
                        return node["j:nodename"];
                    }
                    return "node is not a user";
                }

                $("#searchUser").autocomplete("${url.find}", {
                    dataType: "json",
                    cacheLength: 1,
                    parse: function parse(data) {
                        return $.map(data, function(row) {
                            return {
                                data: row,
                                value: getUserNameText(row),
                                result: getUserNameText(row)
                            }
                        });
                    },
                    formatItem: function(item) {
                        return getUserNameText(item);
                    },
                    extraParams: {
                        query : "SELECT * FROM [jnt:user] AS user WHERE user.[j:nodename] LIKE '%{$q}%' OR user.[j:lastName] LIKE '%{$q}%' OR user.[j:firstName] LIKE '%{$q}%'",
                        language : "JCR-SQL2",
                        removeDuplicatePropValues : "true"
                    }
                });

                $("#setAclForm").ajaxForm({dataType: "json",resetForm : true,success: function() {window.location.reload()}});
            });
        </script>
        <form method="post" action="${url.base}${aclNode.path}.setAcl.do" class="userssearchform" id="setAclForm">

            <jcr:nodeProperty name="jcr:title" node="${aclNode}" var="title"/>
            <c:if test="${not empty title.string}">
                <label class="addUsers" for="searchUser">${fn:escapeXml(title.string)}:&nbsp;</label>
            </c:if>
            <fmt:message key='search.users.defaultText' var="startSearching"/>
            <input type="text" name="user" id="searchUser" value="${startSearching}"
                   onfocus="if(this.value==this.defaultValue)this.value='';"
                   onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
            <input class="addusersubmit" type="submit" title="<fmt:message key='search.submit'/>"/>
            <input type="hidden" name="acl" value="rew--"/>
        </form>
        <br class="clear"/>
    </c:if>
</c:if>
<template:linker property="j:bindedComponent" />