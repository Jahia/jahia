<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="java.util.HashSet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="function" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%!
    final String PUBLICPROPERTIES_PROPERTY = "j:publicProperties";

    HashSet<String> getPublicProperties(PageContext pageContext) throws RepositoryException {
        HashSet<String> publicProperties = (HashSet<String>) pageContext.getAttribute("editUserDetailsPublicProperties");
        if (publicProperties != null) {
            return new HashSet<String>(publicProperties);
        } else {
            publicProperties = new HashSet<String>();
            JCRNodeWrapper user = (JCRNodeWrapper) pageContext.getAttribute("user");
            Value[] values = null;
            if (user.hasProperty(PUBLICPROPERTIES_PROPERTY)) {
                values = user.getProperty(PUBLICPROPERTIES_PROPERTY).getValues();
            }
            if (values != null) {
                for (Value value : values) {
                    publicProperties.add("&quot;" + value.getString() + "&quot;");
                }
            }
            pageContext.setAttribute("editUserDetailsPublicProperties", publicProperties);
            return publicProperties;
        }
    }

    String getPublicPropertiesData(PageContext pageContext, String propertyName) throws RepositoryException {
        HashSet<String> publicProperties = getPublicProperties(pageContext);
        publicProperties.add("&quot;" + propertyName + "&quot;");
        return "{&quot;" + PUBLICPROPERTIES_PROPERTY + "&quot;:[" + StringUtils.join(publicProperties, ",") + "]}";
    }
%>
<%@ include file="../../getUser.jspf"%>

<%-- CSS inclusions --%>
<template:addResources type="css" resources="bootstrap-switch.css"/>
<template:addResources type="css" resources="dashboardUserProfile.css"/>

<%-- Javascripts inclusions --%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>

<template:addCacheDependency node="${user}"/>

<jsp:useBean id="now" class="java.util.Date"/>

<jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
<c:set var="publicPropertiesAsString" value=""/>
<c:forEach items="${publicProperties}" var="value">
    <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
</c:forEach>

<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function(){

            $(".btnMoreAbout").click(function(){
                $(".aboutMeText").css( { height:"100%",maxHeight: "500px", overflow: "auto", paddingRight: "5px" }, { queue:false, duration:500 });
                $(".btnMoreAbout").hide();
                $(".btnLessAbout").show();
            });

            $(".btnLessAbout").click(function(){
                $(".aboutMeText").css( { height:"100px", overflow: "hidden" }, { queue:false, duration:500 });
                $(".btnLessAbout").hide();
                $(".btnMoreAbout").show();
            });

            $('#tabView a').click(function (e) {
                e.preventDefault();
                $(this).tab('show');
            })
        });
    </script>
</template:addResources>

<ul class="nav nav-tabs" id="tabView">
    <li class="active"><a href="#private">Private view</a></li>
    <li><a href="#public">Public view</a></li>
</ul>

<div class="tab-content">
    <div class="tab-pane active" id="private">
        <div class="alert alert-info">
            <div class="row-fluid">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <div class="span2">
                    <c:choose>
                        <c:when test="${empty picture}">
                            <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                alt="" border="0"/>
                        </c:when>
                        <c:otherwise>
                            <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                alt="${fn:escapeXml(person)}"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="span10">
                    <h1>
                        <fmt:message key='jnt_user.j_about'/>
                    </h1>
                     <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                         ${user.properties['j:about'].string}
                     </div>
                    <br />
                    <button class="btn btn-small btn-primary btnMoreAbout">
                        <fmt:message key='mySettings.readMore'/>
                    </button>
                    <button class="btn btn-small btn-primary hide btnLessAbout">
                        <fmt:message key='mySettings.readLess'/>
                    </button>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span2"></div>
            <div class="span8">
                <h2><i class="icon-user"></i>&nbsp;<fmt:message key='mySettings.name'/></h2>
                <div class="box-1">
                    <c:if test="${(!empty user.properties['j:title'].string)}">
                        <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                    </c:if>
                    <c:if test="${(!empty user.properties['j:firstName'].string)}">
                        ${user.properties['j:firstName'].string}&nbsp;
                    </c:if>
                    <c:if test="${(!empty user.properties['j:lastName'].string)}">
                        ${user.properties['j:lastName'].string}&nbsp;
                    </c:if>
                </div>
                <h2><i class="icon-briefcase"></i>&nbsp;<fmt:message key='mySettings.profession'/></h2>
                <div class="box-1">
                    <c:if test="${(!empty user.properties['j:function'].string)}">
                        ${user.properties['j:function'].string}
                    </c:if>
                    <c:if test="${(!empty user.properties['j:organization'].string)}">
                        &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                        ${user.properties['j:organization'].string}
                    </c:if>
                </div>
                <h2><i class="icon-globe"></i>&nbsp;<fmt:message key='mySettings.social'/></h2>
                <div class="box-1">
                    <c:choose>
                        <c:when test="${!empty user.properties['j:facebookID'].string}">
                            <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!empty user.properties['j:skypeID'].string}">
                            <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!empty user.properties['j:twitterID'].string}">
                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!empty user.properties['j:linkedinID'].string}">
                            <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                </div>
                <h2><i class="icon-envelope"></i>&nbsp;<fmt:message key='mySettings.address'/></h2>
                <div class="box-1">
                    <div class="row-fluid">
                        <c:if test="${(!empty user.properties['j:phoneNumber'].string) or (!empty user.properties['j:mobileNumber'].string) or (!empty user.properties['j:email'].string)}">
                            <div class="pull-left">
                                <div>
                                    <strong><fmt:message key='jnt_user.j_email'/>&nbsp;:</strong>
                                    <c:if test="${!empty user.properties['j:email'].string}">
                                        &nbsp;${user.properties['j:email'].string}
                                    </c:if>
                                </div>
                                <div>
                                    <strong><fmt:message key='jnt_user.j_phoneNumber'/>&nbsp;:</strong>
                                    <c:if test="${!empty user.properties['j:phoneNumber'].string}">
                                        &nbsp;${user.properties['j:phoneNumber'].string}
                                    </c:if>
                                </div>
                                <div>
                                    <strong><fmt:message key='jnt_user.j_mobileNumber'/>&nbsp;:</strong>
                                    <c:if test="${!empty user.properties['j:mobileNumber'].string}">
                                        &nbsp;${user.properties['j:mobileNumber'].string}
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${(!empty user.properties['j:address']) or (!empty user.properties['j:zipCode']) or (!empty user.properties['j:city']) or (!empty user.properties['j:country'])}">
                            <div class="pull-right">
                                <div class="pull-left">
                                    <strong><fmt:message key='jnt_user.j_address'/>&nbsp;:</strong>&nbsp;
                                    <div class="pull-right">
                                        <c:if test="${!empty user.properties['j:address'].string}">
                                            ${user.properties['j:address'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:zipCode'].string}">
                                            ${user.properties['j:zipCode'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:city'].string}">
                                            ${user.properties['j:city'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:country'].string}">
                                            ${user.properties['j:country'].string}
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
                <h2><i class="icon-info-sign"></i>&nbsp;<fmt:message key="jnt_user.j_gender.other"/></h2>
                <div class="box-1">
                    <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                    <div>
                        <strong><fmt:message key="jnt_user.age"/>&nbsp;:</strong>
                        <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/>
                    </div>
                    <div>
                        <c:if test="${not empty birthDate}">
                            <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                        </c:if>
                        <strong><fmt:message key="jnt_user.j_birthDate"/>&nbsp;:</strong>
                        &nbsp;${displayBirthDate}
                    </div>
                    <div>
                        <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                        <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                        <strong><fmt:message key="jnt_user.preferredLanguage"/>&nbsp;:</strong>
                        &nbsp;${functions:displayLocaleNameWith(prefLang, prefLang)}
                    </div>
                </div>
            </div>
            <div class="span2"></div>
        </div>
    </div>
    <div class="tab-pane" id="public">
        <c:if test="${fn:contains(publicPropertiesAsString, 'j:picture') and fn:contains(publicPropertiesAsString, 'j:about')}">
            <div class="alert alert-info">
                <div class="row-fluid">
                    <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                    <div class="span2">
                        <c:choose>
                            <c:when test="${empty picture}">
                                <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/>
                            </c:when>
                            <c:otherwise>
                                <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                     alt="${fn:escapeXml(person)}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="span10">
                        <h1>
                            <fmt:message key='jnt_user.j_about'/>
                        </h1>
                        <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                            ${user.properties['j:about'].string}
                        </div>
                        <br />
                        <button class="btn btn-small btn-primary btnMoreAbout">
                            <fmt:message key='mySettings.readMore'/>
                        </button>
                        <button class="btn btn-small btn-primary hide btnLessAbout">
                            <fmt:message key='mySettings.readLess'/>
                        </button>
                    </div>
                </div>
            </div>
        </c:if>
        <div class="row-fluid">
            <div class="span2">
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:picture') and !fn:contains(publicPropertiesAsString, 'j:about')}">
                    <div class="alert alert-info">
                        <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                        <c:choose>
                            <c:when test="${empty picture}">
                                <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/>
                            </c:when>
                            <c:otherwise>
                                <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                     alt="${fn:escapeXml(person)}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>
            <div class="span8">
                <c:if test="${!fn:contains(publicPropertiesAsString, 'j:picture') and fn:contains(publicPropertiesAsString, 'j:about')}">
                    <div class="alert alert-info">
                        <h1>
                            <fmt:message key='jnt_user.j_about'/>
                        </h1>
                        <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                            ${user.properties['j:about'].string}
                        </div>
                        <br />
                        <button class="btn btn-small btn-primary btnMoreAbout">
                            <fmt:message key='mySettings.readMore'/>
                        </button>
                        <button class="btn btn-small btn-primary hide btnLessAbout">
                            <fmt:message key='mySettings.readLess'/>
                        </button>
                    </div>
                </c:if>


                <c:if test="${fn:contains(publicPropertiesAsString, 'j:title') or fn:contains(publicPropertiesAsString, 'j:firstName') or fn:contains(publicPropertiesAsString, 'j:lastName')}">
                    <h2><i class="icon-user"></i>&nbsp;<fmt:message key='mySettings.name'/></h2>
                    <div class="box-1">
                        <c:if test="${(!empty user.properties['j:title'].string)}">
                            <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                        </c:if>
                        <c:if test="${(!empty user.properties['j:firstName'].string)}">
                            ${user.properties['j:firstName'].string}&nbsp;
                        </c:if>
                        <c:if test="${(!empty user.properties['j:lastName'].string)}">
                            ${user.properties['j:lastName'].string}&nbsp;
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, '')}">
                            <div class="pull-right">
                                <strong><fmt:message key='mySettings.gender'/>&nbsp;:</strong>
                                &nbsp;${user.properties['j:gender'].string}
                            </div>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:function') or fn:contains(publicPropertiesAsString, 'j:organization')}">
                    <h2><i class="icon-briefcase"></i>&nbsp;<fmt:message key='mySettings.profession'/></h2>
                    <div class="box-1">
                        <c:if test="${(!empty user.properties['j:function'].string)}">
                            ${user.properties['j:function'].string}
                        </c:if>
                        <c:if test="${(!empty user.properties['j:organization'].string)}">
                            &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                            ${user.properties['j:organization'].string}
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:facebookID') or fn:contains(publicPropertiesAsString, 'j:skypeID') or fn:contains(publicPropertiesAsString, 'j:twitterID') or fn:contains(publicPropertiesAsString, 'j:linkedinID')}">
                    <h2><i class="icon-globe"></i>&nbsp;<fmt:message key='mySettings.social'/></h2>
                    <div class="box-1">
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:facebookID')}">
                            <c:choose>
                                <c:when test="${!empty user.properties['j:facebookID'].string}">
                                    <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:skypeID')}">
                            <c:choose>
                                <c:when test="${!empty user.properties['j:skypeID'].string}">
                                    <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:twitterID')}">
                            <c:choose>
                                <c:when test="${!empty user.properties['j:twitterID'].string}">
                                    <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:linkedinID')}">
                            <c:choose>
                                <c:when test="${!empty user.properties['j:linkedinID'].string}">
                                    <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:phoneNumber') or fn:contains(publicPropertiesAsString, 'j:mobileNumber')
                            or fn:contains(publicPropertiesAsString, 'j:email')}">
                    <h2><i class="icon-envelope"></i>&nbsp;<fmt:message key='mySettings.address'/></h2>
                    <div class="box-1">
                        <div class="row-fluid">
                            <div class="pull-left">
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:email')}">
                                    <div>
                                        <c:if test="${!empty user.properties['j:email']}">
                                        <strong><fmt:message key='jnt_user.j_email'/>&nbsp;:</strong>
                                            <c:if test="${(!empty user.properties['j:email'].string)}">
                                                &nbsp;${user.properties['j:email'].string}
                                            </c:if>
                                        </c:if>
                                    </div>
                                </c:if>
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:phoneNumber')}">
                                <div>
                                    <c:if test="${!empty user.properties['j:phoneNumber']}">
                                    <strong><fmt:message key='jnt_user.j_phoneNumber'/>&nbsp;:</strong>
                                        <c:if test="${(!empty user.properties['j:phoneNumber'].string)}">
                                            &nbsp;${user.properties['j:phoneNumber'].string}
                                        </c:if>
                                    </c:if>
                                </div>
                                </c:if>
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:mobileNumber')}">
                                <div>
                                    <c:if test="${!empty user.properties['j:mobileNumber']}">
                                    <strong><fmt:message key='jnt_user.j_mobileNumber'/>&nbsp;:</strong>
                                        <c:if test="${(!empty user.properties['j:mobileNumber'].string)}">
                                            &nbsp;${user.properties['j:mobileNumber'].string}
                                        </c:if>
                                    </c:if>
                                </div>
                                </c:if>
                            </div>
                            <div class="pull-right">
                                <div class="pull-left">
                                    <strong><fmt:message key='jnt_user.j_address'/>&nbsp;:</strong>&nbsp;
                                    <div class="pull-right">
                                        <c:if test="${!empty user.properties['j:address'].string}">
                                            ${user.properties['j:address'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:zipCode'].string}">
                                            ${user.properties['j:zipCode'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:city'].string}">
                                            ${user.properties['j:city'].string}
                                        </c:if>
                                        <br />
                                        <c:if test="${!empty user.properties['j:country'].string}">
                                            ${user.properties['j:country'].string}
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:birthDate') or fn:contains(publicPropertiesAsString, 'preferredLanguage')}">
                    <h2><i class="icon-info-sign"></i>&nbsp;<fmt:message key="jnt_user.j_gender.other"/></h2>
                    <div class="box-1">
                        <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                        <c:if test="${not empty birthDate}">
                            <div>
                                <strong><fmt:message key="jnt_user.age"/>&nbsp;:</strong>
                                <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/>
                            </div>
                            <div>
                                <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                                <strong><fmt:message key="jnt_user.j_birthDate"/>&nbsp;:</strong>
                                &nbsp;${displayBirthDate}
                            </div>
                        </c:if>
                        <div>
                            <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                            <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                            <c:if test="${!empty user.properties['preferredLanguage'].string}">
                                <strong><fmt:message key="jnt_user.preferredLanguage"/>&nbsp;:</strong>
                                &nbsp;${functions:displayLocaleNameWith(prefLang, prefLang)}
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>
            <div class="span2"></div>
        </div>
    </div>
</div>