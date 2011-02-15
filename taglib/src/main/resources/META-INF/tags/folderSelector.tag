<%@ tag body-content="empty" description="Renders the link to the folder path selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with." %>
<%@ attribute name="displayFieldId" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item display title with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do we need to show the include children checkbox? [true]" %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children checkbox field. [true]" %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The trigger link text." %>
<%@ attribute name="includeChildrenLabel" required="false" type="java.lang.String"
              description="The include children checkbox text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after an item is selectd. Three paramaters are passed as arguments: node identifier, node path and display name. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="onClose" required="false" type="java.lang.String"
              description="The JavaScript function to be called after window is closed." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. [nt:folder,jnt:virtualsite]" %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. [nt:folder]" %>
<%@ attribute name="root" required="false" type="java.lang.String"
              description="The path of the root node for the tree. [current site path]" %>
<%@ attribute name="valueType" required="false" type="java.lang.String"
              description="Either identifier, path or title of the selected item. This value will be stored into the target field. [path]" %>
<%@ attribute name="fancyboxOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery FancyBox plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ attribute name="treeviewOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery Treeview plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.folderSelector.selectFolder"/></c:set></c:if>
<c:if test="${empty includeChildrenLabel}"><c:set var="includeChildrenLabel"><fmt:message key="selectors.folderSelector.selectFolder.includeChildren"/></c:set></c:if>
<uiComponents:treeItemSelector fieldId="${fieldId}" displayFieldId="${displayFieldId}" fieldIdIncludeChildren="${fieldIdIncludeChildren}" displayIncludeChildren="${functions:default(displayIncludeChildren, 'true')}"
	includeChildren="${functions:default(includeChildren, 'true')}" label="${label}" includeChildrenLabel="${includeChildrenLabel}" onSelect="${onSelect}"  onClose="${onClose}"
	nodeTypes="${functions:default(nodeTypes, 'nt:folder,jnt:virtualsite')}" selectableNodeTypes="${functions:default(selectableNodeTypes, 'nt:folder')}"
	root="${root}" valueType="${valueType}" fancyboxOptions="${fancyboxOptions}" treeviewOptions="${treeviewOptions}"/>