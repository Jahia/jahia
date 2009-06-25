<%@ include file="common/declarations.jspf" %>
<template:template doctype="html-transitional">
<template:templateHead>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</template:templateHead>

<template:templateBody style="background-color:#f0eee4">


<table style="font-size:80%;background-color:#f0eee4;width:100%;font-family: Arial, Helvetica, sans-serif;line-height:160%;"
       width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td align="center">

<table width="579" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td style="padding: 8px 0 8px 0;">
            <p style="font-size: 11px;font-weight: normal;font-style: italic;color: #333;text-align: center;">
                <fmt:message key="web_templates_newsletter.intro.1">
                    <fmt:param value="<a style='color: #900;text-decoration: none;' href='/' name='top'>${currentSite.title}</a>"/>
                </fmt:message>
                <br/>
                <fmt:message key="web_templates_newsletter.intro.2"/>
                <a href="${currentPage.url}" target="_blank" style="color: #900;text-decoration: none;">
                <fmt:message key="web_templates_newsletter.intro.3"/></a>.</p></td>
    </tr>
</table>

<table width="579" border="0" cellspacing="0" cellpadding="0" style="background-color:#4f5c79">
    <tr>
        <td align="center">

            <table width="579" height="108" border="0" cellspacing="0" cellpadding="0"
                   style="background-color:#4f5c79">
                <tr>
                    <td><template:absoluteContainerList name="logo" id="logoContainerList" pageLevel="1"
                                                        displayActionMenu="false">
                        <template:container id="logoContainer" displayActionMenu="false" cacheKey="logoNLTop">
                            <a href='<template:composePageURL pageID="${requestScope.currentSite.homepageID}"/>'
                               target="_blank"><template:image file="logo"/></a>
                        </template:container>
                    </template:absoluteContainerList>
                    </td>
                    <td align="right" style="height: 108px; padding-right: 20px;">
                        <h3 style="color:#fff; font-weight:normal; margin:0; padding:5px 20px; font-size:30px;font-family:Georgia, 'Times New Roman', Times, serif">
                            <em>${currentPage.title}</em></h3>
                        <span style="color:#fff; padding:0 20px; font-size:14px;font-family:Georgia, 'Times New Roman', Times, serif">
                            <em>
                                <jsp:useBean id="startDate" class="java.util.Date" />
                                <fmt:formatDate value="${startDate}" dateStyle="long" type="date"/>
                            </em>
                        </span>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<table width="579" border="0" cellspacing="0" cellpadding="0" style="background-color:#fff">
    <tr>
        <td width="254" align="center" valign="top"
            style="background-color: #f8f8f8;border-right: 1px solid #ccc;">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td align="left" style="padding: 10px;">

                        <h2 style="font-size: 16px;font-weight: normal;color: #464646;margin: 0 0 10px 0;border-bottom: 3px solid #ccc;text-transform: uppercase;">
                            In this issue</h2>
                        <template:containerList name="preferences" id="preferences"
                                                actionMenuNamePostFix="preferences"
                                                actionMenuNameLabelKey="preferences">
                            <template:container id="preferencesContainer" cache="off"
                                                actionMenuNamePostFix="preferences"
                                                actionMenuNameLabelKey="preferences.update">
                                <template:field name="filter" display="false"
                                                var="newsCategoriesFilter"/>
                                <c:set var="categoriesFilter" value=""/>
                                <c:set var="categoriesDisplay" value=""/>
                                <c:forEach items="${newsCategoriesFilter.category}"
                                           var="newsCategoryFilter" varStatus="status">
                                    <c:if test="${status.last}">
                                        <c:set var="categoriesFilter"
                                               value="${categoriesFilter}${newsCategoryFilter.category.key}"/>
                                        <c:set var="categoriesDisplay"
                                               value="${categoriesDisplay}${newsCategoryFilter.title}"/>
                                    </c:if>
                                    <c:if test="${!status.last}">
                                        <c:set var="categoriesFilter"
                                               value="${categoriesFilter}${newsCategoryFilter.category.key}$$$"/>
                                        <c:set var="categoriesDisplay"
                                               value="${categoriesDisplay}${newsCategoryFilter.title}, "/>
                                    </c:if>
                                </c:forEach>
                                <template:field name="maxNews" var="maxNews" defaultValue="10"
                                                display="false"/>
                                <c:if test="${requestScope.currentRequest.editMode}">
                                    <div style="border: 1px solid #cccccc;background-color: #eaeaea;padding: 5px;font-size: 90%;line-height: 100%; margin: 1em 0;">
                                        <h2><fmt:message
                                                key="web_templates_newsList.preferences"/></h2>

                                        <p class="preference-item"><span
                                                style=" font-weight: bold;"><fmt:message
                                                key="web_templates_newsList.categoryFilter"/>: </span><span
                                                class="preference-value">${categoriesDisplay} </span>
                                        </p>

                                        <p class="preference-item"><span
                                               style=" font-weight: bold;"><fmt:message
                                                key="web_templates_newsList.rowsDisplay"/>: </span><span
                                                class="preference-value">${maxNews.integer}</span>
                                        </p>
                                    </div>
                                </c:if>
                                <c:set var="newsCategoryFilter" value="${newsCategoryFilter}"/>
                                <c:set var="maxNews" value="${maxNews}"/>
                            </template:container>
                        </template:containerList>
                        <template:containerList maxSize="${maxNews.integer}" id="newsList"
                                                displayActionMenu="false">

                            <query:containerQuery>
                                <query:selector nodeTypeName="web_templates:newsContainer"
                                                selectorName="newsList"/>
                                <c:if test="${!empty categoriesFilter}">
                                    <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}"
                                                   value="${categoriesFilter}"
                                                   multiValue="true" metadata="true"/>
                                </c:if>
                                <c:if test="${empty categoriesFilter}">
                                    <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}"
                                                   value="noCatDef"
                                                   multiValue="true" metadata="true"/>
                                </c:if>
                                <query:descendantNode selectorName="newsList"
                                                      path="${currentSite.JCRPath}"/>
                                <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}"
                                                   value="${maxNews.integer}"/>
                                <query:sortBy propertyName="newsDate"
                                              order="${queryConstants.ORDER_DESCENDING}"/>
                            </query:containerQuery>
                            <ul style="margin: 0 0 20px 24px;padding: 0;font-size: 12px;font-weight: normal;color: #313131;">
                                <template:container id="newsContainer" cacheKey="newsletterShort"
                                                    displayActionMenu="false"
                                                    displayExtensions="false"
                                                    displayContainerAnchor="false">
                                    <li><a href="#news${newsContainer.ID}" target="_blank"
                                           style="font-size: 12px;font-weight: normal;color: #333;"><template:field
                                            name='newsTitle' inlineEditingActivated="false"/></a>
                                    </li>
                                </template:container>
                            </ul>
                        </template:containerList>
                        <h2 style="font-size: 16px;font-weight: normal;color: #464646;margin: 0 0 10px 0;border-bottom: 3px solid #ccc;text-transform: uppercase;">
                            In short</h2>
                        <template:containerList name="introduction" id="introductionContainerList"
                                                actionMenuNamePostFix="introduction"
                                                actionMenuNameLabelKey="introduction">
                            <template:container id="mainContent"
                                                emptyContainerDivCssClassName="mockup-introduction">


                                <p style="font-size: 12px;font-weight: normal;color: #313131;margin: 0 0 14px 0;padding: 0;">
                                    <template:field name='introduction'/>
                                </p>
                            </template:container>
                        </template:containerList>
                            <%--<table width="100%" height="173" border="0" cellspacing="0" cellpadding="0"
                                   style="border-top: 1px solid #ccc;border-bottom: 1px solid #ccc;">
                                <tr>
                                    <td align="center" valign="top"
                                        style="border-bottom: 1px solid #ccc;padding: 16px 22px 16px 22px;">
                                        <h3 style="font-size: 16px;font-weight: normal;color: #666;margin: 0 0 4px 0;padding: 0;">
                                            UNSUBSCRIBE</h3>

                                        <p style="font-size: 13px;font-weight: normal;color: #313131;margin: 0;	padding: 0;">
                                            <a href="#" target="_blank"
                                               style="color: #900;text-decoration: none; border: none;margin: 0;padding: 0;">Click
                                                to instantly unsubscribe from this email</a></p></td>
                                </tr>
                                <tr>
                                    <td align="center" valign="top"
                                        style="padding: 16px 22px 16px 22px;">
                                        <h3 style="font-size: 16px;font-weight: normal;color: #666;margin: 0 0 4px 0;padding: 0;">
                                            FORWARD</h3>

                                        <p style="font-size: 13px;font-weight: normal;color: #313131;margin: 0;	padding: 0;">
                                            <a href="#" target="_blank"
                                               style="color: #900;text-decoration: none; border: none;margin: 0;padding: 0;">Click
                                                to forward this email to a friend</a></p></td>
                                </tr>
                            </table>--%>
                    </td>
                </tr>
            </table>
        </td>

        <td width="325" align="center" valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td align="left" style=" padding: 20px;">
                        <template:containerList maxSize="${maxNews.integer}" id="newsList"
                                                displayActionMenu="false">

                        <query:containerQuery>
                            <query:selector nodeTypeName="web_templates:newsContainer"
                                            selectorName="newsList"/>
                        <c:if test="${!empty categoriesFilter}">
                            <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}"
                                           value="${categoriesFilter}"
                                           multiValue="true" metadata="true"/>
                        </c:if>
                            <c:if test="${empty categoriesFilter}">
                                <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}"
                                               value="noCatDef"
                                               multiValue="true" metadata="true"/>
                            </c:if>
                            <query:descendantNode selectorName="newsList"
                                                  path="${currentSite.JCRPath}"/>
                            <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}"
                                               value="${maxNews.integer}"/>
                            <query:sortBy propertyName="newsDate"
                                          order="${queryConstants.ORDER_DESCENDING}"/>
                        </query:containerQuery>
                        <template:container id="newsContainer" cacheKey="MediumNews"
                                            displayActionMenu="false" displayExtensions="false"
                                            displayContainerAnchor="false">
                            <template:getContentObjectCategories var="newsContainerCatKeys"
                                                                 objectKey="contentContainer_${pageScope.newsContainer.ID}"/>
                        <c:if test="${!empty newsContainerCatKeys }">
                        <h3 style="font-size: 11px;font-weight: normal;color: #333333;text-transform: uppercase;margin: 0 0 8px 0;padding: 0;">
                            <ui:displayCategoryTitle categoryKeys="${newsContainerCatKeys}"/></h3>
                        </c:if>
                        <a name="news${newsContainer.ID}"></a>

                        <h2 style="font-size: 20px;font-weight: bold;color: #333;margin: 0 0 10px 0;padding: 0;">
                            <a style="font-size: 20px;font-weight: bold;color: #333;text-decoration: none;"
                               href="#"><template:field name='newsTitle'/></a></h2>

                        <p style="font-size: 12px;font-weight: normal;color: #333333;margin: 0 0 16px 0;padding: 0;">
                            <template:field name="newsImage" var="newsImage" display="false"/>
                            <img src="${newsImage.file.downloadUrl}"
                                 alt='<template:field
                    name="newsTitle" inlineEditingActivated="false"/>' width="160" height="160" hspace="10" border="0"
                                 align="right"/>
                            <template:field name="newsDesc"/></p>

                        <p style="font-size: 10px;font-weight: normal;color: #44a0df;text-transform: uppercase;width: 100%;text-align: right;margin: 14px 0 4px 0;">
                            <a href="#top" style="color: #900;text-decoration: none;">Back to
                                top</a></p>
                        <img alt="separator"
                             src="<utility:resolvePath value='images/hr.gif'/>" width="326" height="19">
                        </template:container>
                        </template:containerList>
                </tr>
            </table>
        </td>
    </tr>
</table>

<table width="579" height="108" border="0" cellspacing="0" cellpadding="0"
       style="padding: 10px;background-color: #eaeaea;">
    <tr>
        <td align="center" valign="middle">

            <table width="559" height="88" border="0" cellspacing="0" cellpadding="0"
                   style="font-size: 11px;font-weight: normal;color: #999999;text-align: center;">
                <tr>
                    <td align="center" style="height: 88px;"><template:absoluteContainerList name="logoFooter"
                                                                                             id="logoContainerList"
                                                                                             pageLevel="1"
                                                                                             displayActionMenu="false">
                        <template:container id="logoContainer" displayActionMenu="false" cacheKey="logoNLBottom">
                            <a href='<template:composePageURL pageID="${requestScope.currentSite.homepageID}"/>'
                               target="_blank"><template:image file="logo"/></a>
                        </template:container>
                    </template:absoluteContainerList>
                        <br/>
                        &copy; Copyright 2002-2009 - ACME International Corp.
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</td>
</tr>
<tr>
    <td align="center">&nbsp;</td>
</tr>
</table>

</template:templateBody>
</template:template>
