<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:if test="${ !empty param.footerNav}">
    <div id="footerPart1"><!--start footerPart1-->
        <div class="columns5">
        <jcr:node var="bottomLinks" path="${rootPage.path}/bottomLinks"/>
        <template:module node="${bottomLinks}"/>
        <div class="clear"> </div>
        </div>
    </div>
</c:if>
<!--stop footerPart1-->
<!--start footerPart2-->
<div id="footerPart2"><!--start footerPart2content-->
    <div id="footerPart2content"></div>
    <div class="clear"> </div>
</div>
<!--stop footerPart2-->
<!--start footerPart3-->
<div id="footerPart3">
    <!--start 2columns -->
    <div class="columns2">
        <!--start column-item -->
        <div class="column-item1">
            <div class="spacer">
                <jcr:node var="logoFooter" path="${rootPage.path}/logoFooter"/>
                <template:module node="${logoFooter}"/>
            </div>
            <div class="clear"> </div>
        </div>
        <!--stop column-item -->
        <!--start column-item -->
        <div class="column-item2">
            <div class="spacer"><!--start bottomshortcuts-->
                <div id="bottomshortcuts">
                    <c:if test="${editUrl != null}">
                        <a href="${editUrl}.html"> <fmt:message key="edit"/> </a>
                    </c:if>
                </div><!--stop bottomshortcuts-->
<div class="clear"> </div>
                <!--start copyright-->
                <div id="copyright">
                    <p>
                        <jcr:node var="footer" path="${rootPage.path}/footerContainerList"/>
                        <template:module node="${footer}"/>
                    </p>
                </div><!--stop copyright-->
            </div>
            <div class="clear"> </div>
        </div><!--stop column-item -->

        <div class="clear"> </div>
    </div><!--stop 2 columns -->

    <div class="clear"> </div>
</div><!--stop footerPart3-->

<div class="clear"> </div>
