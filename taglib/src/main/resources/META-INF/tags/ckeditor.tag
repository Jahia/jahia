<%@ tag body-content="scriptless" description="Initializes the CKEditor configuration and replaces all elements, matching the specified selector (jQuery selector syntax) with CKEditor instances. The body of the tag contains the CKEditor configuration options to override the defaults in the form {option1: value, option2: value}. By default the CKEditor will use a simplified toolbar and no Jahia-specific content (file, image, link, etc.) pickers. The tag is supposed to be used in the content of Jahia rendering templates as it expects the renderContext bean being available." %>
<%@ attribute name="selector" required="true" type="java.lang.String"
              description="The jQuery syntax based selector to find HTML elements that will be replaced with instances of the CKEditor when the page is loaded." %>
<%@ attribute name="includeJQuery" required="false" type="java.lang.Boolean"
              description="The functionality of this tag depends on the jQuery. If this attribute is set to false the jquery.min.js won't be automatically included by this tag and it is up to the template developer to include a proper jQuery version for this tag to work properly. This attribute is mainly provided to override the jQuery version included if needed. [true]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="${empty includeJQuery || includeJQuery ? 'jquery.min.js,' : ''}ckeditor/ckeditor.js,ckeditor/adapters/jquery.js"/>
<jsp:doBody var="config"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready( function()
        {
            CKEDITOR.replace( '${selector}',
            $.extend({ toolbar: 'Mini',
                filebrowserBrowseUrl: null,
                filebrowserFlashBrowseUrl: null,
                filebrowserImageBrowseUrl: null,
                filebrowserLinkBrowseUrl: null
            }, ${not empty config ? config : '{}'
			}) );
        });
    </script>
</template:addResources>