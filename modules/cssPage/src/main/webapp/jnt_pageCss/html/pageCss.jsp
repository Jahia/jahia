<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="colorpicker.css"/>
<template:addResources type="javascript" resources="colorpicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.pack.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery-ui-1.7.3.custom.min.js"/>
<template:addResources type="css" resources="cssPage.css"/>
<c:set var="myDiese" value="#"/>  <%-- var for inligne Css color --%>
<jcr:nodeProperty node="${currentNode}" name="img1" var="imgResource1"/>
<jcr:nodeProperty node="${currentNode}" name="img2" var="imgResource2"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<script type="text/javascript">
    $(function() {
        $('#showChangeCss').click(function() {
            $('#changeCssForm').show();
            $.fancybox.resize();
            $.fancybox.center();
        });

        $("#showChangeCss").fancybox({
            'scrolling'          : 'no',
            'titleShow'          : false,
            'hideOnContentClick' : false,
            'showCloseButton'    : true,
            'overlayOpacity'     : 0.6,
            'transitionIn'        : 'none',
            'transitionOut'        : 'none',
            'centerOnScroll'     : true,
            'onClosed'           : function() {
                $('#changeCssForm').hide();
                $.fancybox.resize();
                $.fancybox.center();
            }
            
        });

        $('#backgroundColor, #bodyColor, #aLinkVisitedColor, #aHoverActiveColor, #colorResource1, #colorResource2, #h1Color, #h2Color, #h3Color, #h4Color, #h5Color').ColorPicker({
            onShow: function (colpkr) {
                $(colpkr).fadeIn(500);
                return false;
            },
            onHide: function (colpkr) {
                $(colpkr).fadeOut(500);
                return false;
            },
            onSubmit: function(hsb, hex, rgb, el) {
                $(el).val(hex);
                $(el).ColorPickerHide();
            },
            onChange: function(hsb, hex, rgb, el) {
                $(this).val(hex);
            },
            onBeforeShow: function() {
                $(this).ColorPickerSetColor(this.value);
            }
        }).bind('keyup', function() {
            $(this).ColorPickerSetColor(this.value);
        });

        $("#fileMyImg1").editable('${url.base}${currentNode.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit',
            callback : function (data, status, original) {
                var id = $(original).attr('jcr:id');
                $("#" + id).val(data.uuids[0]);
                $("#fileMyImg1").html($('<span>img1 uploaded</span>'));
            }
        });

        $("#fileMyImg2").editable('${url.base}${currentNode.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit',
            callback : function (data, status, original) {
                var id = $(original).attr('jcr:id');
                $("#" + id).val(data.uuids[0]);
                $("#fileMyImg2").html($('<span>img2 uploaded</span>'));
            }
        });
    });
</script>
<a href="#changeCssForm" id="showChangeCss"><img src="${url.currentModule}/img/cssIcone.png"/></a>

<div id="changeCssForm" style="display: none;">
<form class="formCssPage" id="myForm" action="${url.base}${currentNode.path}" method="post">
<input type="hidden" name="nodeType" value="jnt:pageCss"/>
<input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
<input type="hidden" name="newNodeOutputFormat" value="html"/>
<c:set var="props" value="${currentNode.properties}"/>
<jcr:nodeType name="jnt:pageCss" var="pageCsstype"/>
<c:set var="propDefs" value="${pageCsstype.declaredPropertyDefinitionsAsMap}"/>

<h3 class="boxCssPage-title">Change Css Form</h3>
<table class="changeCssFormTable">
<tr>
    <td valign="top">
        <fieldset>
            <legend><fmt:message key="jnt_pageCss.titles"/></legend>
            <div id="showTitles">
                <p>
                    <label class="left"
                           for="h1Color"><fmt:message key="jnt_pageCss.h1color"/></label>
                    <input class="myInput" id="h1Color" type="text" name="j:h1Color"
                           value="${props["j:h1Color"].string}" size="10">
                </p>
                <p>
                    <label class="left"
                           for="h1FontFamily"><fmt:message key="jnt_pageCss.h1FontFamily"/></label>
                    <select name="h1FontFamily" id="h1FontFamily">
                        <c:forEach items="${propDefs.h1FontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.h1FontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>

                <p>
                    <label class="left"
                           for="h2Color"><fmt:message key="jnt_pageCss.h2color"/></label>
                    <input class="myInput" id="h2Color" type="text" name="j:h2Color"
                           value="${props["j:h2Color"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="h2FontFamily"><fmt:message key="jnt_pageCss.h2FontFamily"/></label>
                    <select name="h2FontFamily" id="h2FontFamily">
                        <c:forEach items="${propDefs.h2FontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.h2FontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>

                <p>
                    <label class="left"
                           for="h3Color"><fmt:message key="jnt_pageCss.h3color"/></label>
                    <input class="myInput" id="h3Color" type="text" name="j:h3Color"
                           value="${props["j:h3Color"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="h3FontFamily"><fmt:message key="jnt_pageCss.h3FontFamily"/></label>
                    <select name="h3FontFamily" id="h3FontFamily">
                        <c:forEach items="${propDefs.h3FontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.h3FontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>

                <p>
                    <label class="left"
                           for="h4Color"><fmt:message key="jnt_pageCss.h4color"/></label>
                    <input class="myInput" id="h4Color" type="text" name="j:h4Color"
                           value="${props["j:h4Color"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="h4FontFamily"><fmt:message key="jnt_pageCss.h4FontFamily"/></label>
                    <select name="h4FontFamily" id="h4FontFamily">
                        <c:forEach items="${propDefs.h4FontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.h4FontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>

                <p>
                    <label class="left"
                           for="h5Color"><fmt:message key="jnt_pageCss.h5color"/></label>
                    <input class="myInput" id="h5Color" type="text" name="j:h5Color"
                           value="${props["j:h5Color"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="h5FontFamily"><fmt:message key="jnt_pageCss.h5FontFamily"/></label>
                    <select name="h5FontFamily" id="h5FontFamily">
                        <c:forEach items="${propDefs.h5FontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.h5FontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>
            </div>
        </fieldset>

</td>
<td valign="top">
        
		<fieldset>
            <legend>
                <fmt:message key="jnt_pageCss.pageColors"/>
            </legend>
            <div id="showPageColors">
                <p>
                    <label class="left"
                           for="colorResource1"><fmt:message key="jnt_pageCss.colorResource1"/></label>
                    <input class="myInput" id="colorResource1" type="text" name="j:colorResource1"
                           value="${props["j:colorResource1"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="colorResource2"><fmt:message key="jnt_pageCss.colorResource2"/></label>
                    <input class="myInput" id="colorResource2" type="text" name="j:colorResource2"
                           value="${props["j:colorResource2"].string}" size="10">
                </p>
            </div>
        </fieldset>
        
        <fieldset>
            <legend><fmt:message key="jnt_pageCss.body"/></legend>
            <div id="showBody">
                <p>
                    <label class="left"
                           for="backgroundColor"><fmt:message key="jnt_pageCss.backgroundColor"/></label>
                    <input id="backgroundColor" type="text"
                           value="${currentNode.properties["j:backgroundColor"].string}" name="j:backgroundColor"
                           size="10">
                </p>

                <p>
                    <label class="left"
                           for="bodyColor"><fmt:message key="jnt_pageCss.color"/> </label>
                    <input id="bodyColor" type="text" name="j:bodyColor"
                           value="${currentNode.properties["j:bodyColor"].string}" size="10">
                </p>


                <p>
                    <label class="left"
                           for="fontFamily"><fmt:message key="jnt_pageCss.fontfamily"/> </label>
                    <select name="fontFamily" id="fontFamily">
                        <c:forEach items="${propDefs.fontFamily.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.fontFamily.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>
                <p>
                    <label class="left"
                           for="lineHeight"><fmt:message key="jnt_pageCss.lineheight"/> </label>
                    <select name="lineHeight" id="lineHeight">
                        <c:forEach items="${propDefs.lineHeight.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.lineHeight.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>
                <p>
                    <label class="left"
                           for="fontSize"><fmt:message key="jnt_pageCss.fontsize"/> </label>
                    <select name="fontSize" id="fontSize">
                        <c:forEach items="${propDefs.fontSize.valueConstraints}" var="valueOption">
                            <option value="${valueOption}" <c:if test='${valueOption eq props.fontSize.string}'>selected</c:if>>${valueOption}</option>
                        </c:forEach>
                    </select>
                </p>
            </div>

        </fieldset>

        <fieldset>
            <legend><fmt:message key="jnt_pageCss.links"/></legend>
            <div id="showLinks">
                <p>
                    <label class="left"
                           for="aLinkVisitedColor"><fmt:message key="jnt_pageCss.linkVisited"/></label>
                    <input class="myInput" id="aLinkVisitedColor" type="text" name="j:aLinkVisitedColor"
                           value="${props["j:aLinkVisitedColor"].string}" size="10">
                </p>

                <p>
                    <label class="left"
                           for="aHoverActiveColor"><fmt:message key="jnt_pageCss.hoverActive"/></label>
                    <input class="myInput" id="aHoverActiveColor" type="text" name="j:aHoverActiveColor"
                           value="${props["j:aHoverActiveColor"].string}" size="10">
                </p>
            </div>
        </fieldset>

        <fieldset>
            <legend>
                <fmt:message key="jnt_pageCss.backgroundImages"/>
            </legend>
            <div id="showBackGroundImages">
                <input type="hidden" name="img1" id="myImg1"/>

                <div id="fileMyImg1" jcr:id="myImg1">
                    <c:choose>
                        <c:when test="${not empty imgResource1}"><span><fmt:message key="jnt_pageCss.changeImg1"/></span></c:when>
                        <c:when test="${empty imgResource1}"><span><fmt:message key="jnt_pageCss.addImg1"/></span></c:when>
                    </c:choose>
                </div>

                <input type="hidden" name="img2" id="myImg2"/>

                <div id="fileMyImg2" jcr:id="myImg2">
                    <c:choose>
                        <c:when test="${not empty imgResource2}"><span><fmt:message key="jnt_pageCss.changeImg2"/>2</span></c:when>
                        <c:when test="${empty imgResource2}"><span><fmt:message key="jnt_pageCss.addImg2"/></span></c:when>
                    </c:choose>
                </div>
               <%-- <form action="${url.base}${currentNode.path}" method="post">
                    <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                    <input type="hidden" name="newNodeOutputFormat" value="html"/>
                    <input type="hidden" name="img2" value=""/>
                    <input type="submit" value="remove Img2"/>
                </form>  --%>
            </div>
        </fieldset>
    </td>
</tr>
<tr>
    <td colspan="2" align="center">
        <input type="reset" value="Reset" class="button" tabindex="3"/>
        <input type="submit" value="Submit" class="button" tabindex="4"/>
    </td>
</tr>
</table>
</form>
</div>

<template:addResources>
	<style type="text/css">
	
<c:if test="${!empty currentNode.properties['j:backgroundColor'].string || !empty currentNode.properties['j:bodyColor'].string || !empty currentNode.properties['fontFamily'].string || !empty currentNode.properties['lineHeight'].string || !empty currentNode.properties['fontSize'].string  }">
            /******************************************************************************
            *  General
            ******************************************************************************/ 
	body {
		<c:if test="${!empty currentNode.properties['j:backgroundColor'].string}">			
					background-color: ${myDiese}${currentNode.properties["j:backgroundColor"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['j:bodyColor'].string}">
				color: ${myDiese}${currentNode.properties["j:bodyColor"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['fontFamily'].string}">
				font-family: ${currentNode.properties.fontFamily.string};
		</c:if>
		<c:if test="${!empty currentNode.properties['lineHeight'].string}">
				line-height: ${currentNode.properties.lineHeight.string};
		</c:if>
		<c:if test="${!empty currentNode.properties['fontSize'].string}">
				font-size: ${currentNode.properties.fontSize.string};
		</c:if>
	}
</c:if>    
            /******************************************************************************
            *  General
            ******************************************************************************/  

<c:if test="${!empty currentNode.properties['j:aLinkVisitedColor'].string}">
			#bodywrapper a,
			#bodywrapper a:link,
			#bodywrapper a:visited {
				 color: ${myDiese}${currentNode.properties["j:aLinkVisitedColor"].string};   
			}
</c:if>

<c:if test="${!empty currentNode.properties['j:aHoveActiveColor'].string}">
        #bodywrapper a:hover,
        #bodywrapper a:active {
            color: ${myDiese}${currentNode.properties["j:aHoveActiveColor"].string};
        }
</c:if>    
            /******************************************************************************
            *  Titles
            ******************************************************************************/
<c:if test="${!empty currentNode.properties['j:h1Color'].string || !empty currentNode.properties['h1FontFamily'].string}">
	#bodywrapper h1 {
		<c:if test="${!empty currentNode.properties['j:h1Color'].string}">	
				color: ${myDiese}${currentNode.properties["j:h1Color"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['h1FontFamily'].string}">
				font-family: ${currentNode.properties.h1FontFamily.string};
			
		</c:if>
	}
</c:if>  

<c:if test="${!empty currentNode.properties['j:h2Color'].string || !empty currentNode.properties['h2FontFamily'].string}">
	#bodywrapper h2 {
		<c:if test="${!empty currentNode.properties['j:h2Color'].string}">	
				color: ${myDiese}${currentNode.properties["j:h2Color"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['h2FontFamily'].string}">
				font-family: ${currentNode.properties.h2FontFamily.string};
			
		</c:if>
	}
</c:if> 
<c:if test="${!empty currentNode.properties['j:h3Color'].string || !empty currentNode.properties['h3FontFamily'].string}">
	#bodywrapper h3 {
		<c:if test="${!empty currentNode.properties['j:h3Color'].string}">	
				color: ${myDiese}${currentNode.properties["j:h3Color"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['h3FontFamily'].string}">
				font-family: ${currentNode.properties.h3FontFamily.string};
			
		</c:if>
	}
</c:if>
<c:if test="${!empty currentNode.properties['j:h4Color'].string || !empty currentNode.properties['h4FontFamily'].string}">
	#bodywrapper h4 {
		<c:if test="${!empty currentNode.properties['j:h4Color'].string}">	
				color: ${myDiese}${currentNode.properties["j:h4Color"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['h4FontFamily'].string}">
				font-family: ${currentNode.properties.h4FontFamily.string};
			
		</c:if>
	}
</c:if>
<c:if test="${!empty currentNode.properties['j:h5Color'].string || !empty currentNode.properties['h5FontFamily'].string}">
	#bodywrapper h5 {
		<c:if test="${!empty currentNode.properties['j:h5Color'].string}">	
				color: ${myDiese}${currentNode.properties["j:h5Color"].string};
		</c:if>
		<c:if test="${!empty currentNode.properties['h5FontFamily'].string}">
				font-family: ${currentNode.properties.h5FontFamily.string};
			
		</c:if>
	}
</c:if> 
            /******************************************************************************
            *  Page colors
            ******************************************************************************/
<c:if test="${!empty currentNode.properties['j:colorResource1'].string}">			
        #bodywrapper .colorResource1 {
            background-color: ${myDiese}${currentNode.properties["j:colorResource1"].string} !important;
        }
</c:if>
<c:if test="${!empty currentNode.properties['j:colorResource2'].string}">
        #bodywrapper .colorResource2 {
            background-color: ${myDiese}${currentNode.properties["j:colorResource2"].string} !important;
        }
</c:if>    
            /******************************************************************************
            *  Background images
            ******************************************************************************/
<c:if test="${!empty imgResource1.node.url}">			
        #bodywrapper .imgResource1 {
            background-image: url('${imgResource1.node.url}');
        }
</c:if> 
<c:if test="${!empty imgResource2.node.url}">					
        #bodywrapper .imgResource2 {
            background-image: url('${imgResource2.node.url}');
        }
</c:if> 
    </style>
</template:addResources>

