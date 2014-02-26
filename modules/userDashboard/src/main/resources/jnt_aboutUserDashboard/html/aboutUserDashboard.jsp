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
<%--<c:set var="publicProperties" value="<%=getPublicProperties(pageContext)%>"/>--%>

<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function(){

            $(".btnMoreAbout").click(function(){
                $(".aboutMeText").animate( { height:"100%" }, { queue:false, duration:500 });
                $(".btnMoreAbout").hide();
                $(".btnLessAbout").show();
            });

            $(".btnLessAbout").click(function(){
                $(".aboutMeText").animate( { height:"100px" }, { queue:false, duration:500 });
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
    <div class="tab-pane active well" id="private">
        <div class="media">
            <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <c:choose>
                    <c:when test="${empty picture}">
                        <img class="img-polaroid pull-left media-object" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                            alt="" border="0"/>
                    </c:when>
                    <c:otherwise>
                        <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                            alt="${fn:escapeXml(person)}"/>
                    </c:otherwise>
                </c:choose>
            <div class="media-body">
                    <h4 class="media-heading">
                        <fmt:message key='jnt_user.j_about'/>
                    </h4>
                    <div class="aboutMeText lead" style="height: 100px">
                        ${user.properties['j:about'].string}
                    </div>
                    <br />
                    <button class="btn btn-small btn-primary btnMoreAbout"><fmt:message key='mySettings.readMore'/></button>
                    <button class="btn btn-small btn-primary hide btnLessAbout"><fmt:message key='mySettings.readLess'/></button>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span2"></div>
            <div class="span8">
                <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered">
                    <tbody>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.name'/>
                            </td>
                            <td>
                                <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                                ${user.properties['j:firstName'].string}&nbsp;
                                ${user.properties['j:lastName'].string}&nbsp;
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.profession'/>
                            </td>
                            <td>
                                ${user.properties['j:function'].string}
                                <c:if test="${(!empty user.properties['j:organization'])}">
                                    &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                                </c:if>
                                ${user.properties['j:organization'].string}
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.social'/>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${!empty user.properties['j:facebookID']}">
                                        <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                                    </c:when>
                                    <c:otherwise>
                                        <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${!empty user.properties['j:skypeID']}">
                                        <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                                    </c:when>
                                    <c:otherwise>
                                        <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${!empty user.properties['j:twitterID']}">
                                        <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                                    </c:when>
                                    <c:otherwise>
                                        <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${!empty user.properties['j:linkedinID']}">
                                        <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                    </c:when>
                                    <c:otherwise>
                                        <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.address'/>
                            </td>
                            <td>
                                <div class="row-fluid">
                                    <c:if test="${(!empty user.properties['j:phoneNumber']) or (!empty user.properties['j:mobileNumber']) or (!empty user.properties['j:email'])}">
                                        <div class="pull-left">
                                            <div>
                                                <fmt:message key='mySettings.addressForm.email'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:email']}">
                                                    ${user.properties['j:email'].string}
                                                </c:if>
                                            </div>
                                            <div>
                                                <fmt:message key='mySettings.addressForm.mobile'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:mobileNumber']}">
                                                    ${user.properties['j:mobileNumber'].string}
                                                </c:if>
                                            </div>
                                            <div>
                                                <fmt:message key='mySettings.addressForm.phone'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:phoneNumber']}">
                                                    ${user.properties['j:phoneNumber'].string}
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                    <c:if test="${(!empty user.properties['j:address']) or (!empty user.properties['j:zipCode']) or (!empty user.properties['j:city']) or (!empty user.properties['j:country'])}">
                                        <div class="pull-right">
                                            <div class="pull-left">
                                                <fmt:message key='mySettings.addressForm.address'/>&nbsp;:&nbsp;
                                                <div class="pull-right">
                                                    <c:if test="${!empty user.properties['j:address']}">
                                                        ${user.properties['j:address'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:zipCode']}">
                                                        ${user.properties['j:zipCode'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:city']}">
                                                        ${user.properties['j:city'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:country']}">
                                                        ${user.properties['j:country'].string}
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="jnt_user.j_gender.other"/>
                            </td>
                            <td>
                                <div>
                                    <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                                    <c:if test="${not empty birthDate}">
                                        <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                                    </c:if>
                                    <span><fmt:message key="jnt_user.j_birthDate"/> : </span>
                                    <span>${displayBirthDate}</span>
                                </div>
                                <div>
                                    <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                                    <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                                    <span><fmt:message key="jnt_user.preferredLanguage"/> : </span>
                                    <span>${functions:displayLocaleNameWith(prefLang, prefLang)}</span>
                                </div>
                                <div>
                                    <span><fmt:message key="jnt_user.age"/>&nbsp;:</span>
                                    <span><utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/></span>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="span2"></div>
        </div>
    </div>
    <div class="tab-pane well" id="public">
        <div class="media">
            <c:if test="${publicProperties.properties['j:picture']}">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <c:choose>
                    <c:when test="${empty picture}">
                        <img class="img-polaroid pull-left media-object" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                             alt="" border="0"/>
                    </c:when>
                    <c:otherwise>
                        <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                             alt="${fn:escapeXml(person)}"/>
                    </c:otherwise>
                </c:choose>
            </c:if>
            <div class="media-body">
                <c:if test="${currentNode.properties['j:about'].boolean}">
                    <h4 class="media-heading">
                        <fmt:message key='jnt_user.j_about'/>
                    </h4>
                    <div class="aboutMeText lead" style="height: 100px">
                        ${user.properties['j:about'].string}
                    </div>
                    <br />
                    <button class="btn btn-small btn-primary btnMoreAbout"><fmt:message key='mySettings.readMore'/></button>
                    <button class="btn btn-small btn-primary hide btnLessAbout"><fmt:message key='mySettings.readLess'/></button>
                </c:if>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span2"></div>
            <div class="span8">
                <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered">
                    <tbody>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.name'/>
                            </td>
                            <td>
                                <c:if test="${currentNode.properties['j:title'].boolean}">
                                    <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                                </c:if>
                                <c:if test="${currentNode.properties['j:firstName'].boolean}">
                                    ${user.properties['j:firstName'].string}&nbsp;
                                </c:if>
                                <c:if test="${currentNode.properties['j:lastName'].boolean}">
                                    ${user.properties['j:lastName'].string}&nbsp;
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.profession'/>
                            </td>
                            <td>
                                <c:if test="${currentNode.properties['j:function'].boolean}">
                                    ${user.properties['j:function'].string}
                                </c:if>
                                <c:if test="${(!empty user.properties['j:organization'])}">
                                    &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                                </c:if>
                                <c:if test="${currentNode.properties['j:organization'].boolean}">
                                    ${user.properties['j:organization'].string}
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.social'/>
                            </td>
                            <td>
                                <c:if test="${currentNode.properties['j:facebookID'].boolean}">
                                    <c:choose>
                                        <c:when test="${!empty user.properties['j:facebookID']}">
                                            <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${currentNode.properties['j:skypeID'].boolean}">
                                    <c:choose>
                                        <c:when test="${!empty user.properties['j:skypeID']}">
                                            <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${currentNode.properties['j:twitterID'].boolean}">
                                    <c:choose>
                                        <c:when test="${!empty user.properties['j:twitterID']}">
                                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${currentNode.properties['j:linkedinID'].boolean}">
                                    <c:choose>
                                        <c:when test="${!empty user.properties['j:linkedinID']}">
                                            <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key='mySettings.address'/>
                            </td>
                            <td>
                                <div class="row-fluid">
                                    <c:if test="${(!empty user.properties['j:address']) or (!empty user.properties['j:zipCode']) or (!empty user.properties['j:city']) or (!empty user.properties['j:country'])}">
                                        <div class="pull-left">
                                            <div>
                                                <fmt:message key='mySettings.addressForm.email'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:email']}">
                                                    ${user.properties['j:email'].string}
                                                </c:if>
                                            </div>
                                            <div>
                                                <fmt:message key='mySettings.addressForm.mobile'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:mobileNumber']}">
                                                    ${user.properties['j:mobileNumber'].string}
                                                </c:if>
                                            </div>
                                            <div>
                                                <fmt:message key='mySettings.addressForm.phone'/>&nbsp;:&nbsp;
                                                <c:if test="${!empty user.properties['j:phoneNumber']}">
                                                    ${user.properties['j:phoneNumber'].string}
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                    <c:if test="${(!empty user.properties['j:phoneNumber']) or (!empty user.properties['j:mobileNumber']) or (!empty user.properties['j:email'])}">
                                        <div class="pull-right">
                                            <div class="pull-left">
                                                <fmt:message key='mySettings.addressForm.address'/>&nbsp;:&nbsp;
                                                <div class="pull-right">
                                                    <c:if test="${!empty user.properties['j:address']}">
                                                        ${user.properties['j:address'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:zipCode']}">
                                                        ${user.properties['j:zipCode'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:city']}">
                                                        ${user.properties['j:city'].string}
                                                    </c:if>
                                                    <br />
                                                    <c:if test="${!empty user.properties['j:country']}">
                                                        ${user.properties['j:country'].string}
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="jnt_user.j_gender.other"/>
                            </td>
                            <td>
                                <c:if test="${currentNode.properties['j:birthDate'].boolean}">
                                    <div>
                                        <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                                        <c:if test="${not empty birthDate}">
                                            <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                                            <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy-MM-dd" var="dateforCal"/>
                                        </c:if>
                                        <span><fmt:message key="jnt_user.j_birthDate"/> : </span>
                                        <span>${displayBirthDate}</span>
                                    </div>
                                </c:if>
                                <c:if test="${currentNode.properties['preferredLanguage'].boolean}">
                                    <div>
                                        <span><fmt:message key="jnt_user.preferredLanguage"/> : </span>
                                        <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                                        <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                                        <span>${functions:displayLocaleNameWith(prefLang, prefLang)}</span>
                                    </div>
                                </c:if>
                                <c:if test="${currentNode.properties['age'].boolean}">
                                    <div>
                                        <span><fmt:message key="jnt_user.age"/>&nbsp;:</span>
                                        <span><utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/></span>
                                    </div>
                                </c:if>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="span2"></div>
        </div>
    </div>
</div>