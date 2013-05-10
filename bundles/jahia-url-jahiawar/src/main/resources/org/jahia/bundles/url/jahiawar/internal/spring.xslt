<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:beans="http://www.springframework.org/schema/beans"
                xmlns="http://www.springframework.org/schema/beans">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="beans:property/@ref[.='sessionFactory']">
        <xsl:attribute name="ref">
            <xsl:value-of select="'moduleSessionFactory'"/>
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>