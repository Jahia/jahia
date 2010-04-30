<?xml version="1.0" encoding="UTF-8" ?>

<!-- New document created with EditiX at Fri Apr 02 14:31:29 CEST 2010 -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jcr="http://www.jcp.org/jcr/1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="@*">
        <xsl:choose>
            <xsl:when test="name()='jcr:created'"/>
            <xsl:when test="name()='jcr:createdBy'"/>
            <xsl:when test="name()='jcr:lastModified'"/>
            <xsl:when test="name()='jcr:lastModifiedBy'"/>
            <xsl:when test="name()='j:lastPublished'"/>
            <xsl:when test="name()='j:lastPublishedBy'"/>
            <xsl:when test="name()='j:templateDeployed'"/>
            <xsl:when test="name()='jcr:uuid'"/>
            <xsl:when test="name()='j:fullpath'"/>
            <xsl:when test="name()='j:movedFrom'"/>
            <xsl:when test="name()='j:siteId'"/>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="node()">
        <xsl:choose>
            <xsl:when test="@jcr:primaryType='jnt:permissions'"/>
            <xsl:when test="@jcr:primaryType='jnt:roles'"/>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="child::node()|@*"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="/">
        <content>
            <templatesSet>
                <xsl:copy>
                    <xsl:apply-templates select="child::node()|@*"/>
                </xsl:copy>
            </templatesSet>
        </content>
    </xsl:template>
</xsl:stylesheet>
