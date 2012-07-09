<?xml version="1.0" encoding="UTF-8" ?>

<!-- New document created with EditiX at Fri Apr 02 14:31:29 CEST 2010 -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jcr="http://www.jcp.org/jcr/1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="@*">
        <xsl:choose>
            <xsl:when test="name()='jcr:primaryType'">
                <xsl:copy/>
            </xsl:when>
            <xsl:when test="name()='j:fullpath'"/>
            <xsl:when test="name()='j:movedFrom'"/>
            <xsl:when test="name()='j:siteId'"/>
            <xsl:when test="../@jcr:primaryType='jnt:virtualsite'">
                <xsl:choose>
                    <xsl:when test="name()='j:title'"/>
                    <xsl:when test="name()='j:defaultLanguage'"/>
                    <xsl:when test="name()='j:description'"/>
                    <xsl:when test="name()='j:installedModules'"/>
                    <xsl:when test="name()='j:languages'"/>
                    <xsl:when test="name()='j:mandatoryLanguages'"/>
                    <xsl:when test="name()='j:mixLanguage'"/>
                    <xsl:when test="name()='j:serverName'"/>
                    <xsl:when test="name()='j:templatesSet'"/>
                    <xsl:otherwise>
                        <xsl:copy/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="*">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:variable name="vtheElem" select="."/>

            <xsl:for-each select="namespace::*">
                <xsl:variable name="vPrefix" select="name()"/>

                <xsl:if test="$vtheElem/descendant::*[namespace-uri()=current() and substring-before(name(),':') = $vPrefix or @*[substring-before(name(),':') = $vPrefix]]">
                    <xsl:copy/>
                </xsl:if>
            </xsl:for-each>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
