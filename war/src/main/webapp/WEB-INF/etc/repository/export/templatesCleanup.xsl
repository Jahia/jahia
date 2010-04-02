<?xml version="1.0" encoding="UTF-8" ?>

<!-- New document created with EditiX at Fri Apr 02 14:31:29 CEST 2010 -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="@*">
		<xsl:choose>
			<xsl:when test="name()='jcr:created'"/>
			<xsl:when test="name()='jcr:createdBy'"/>
			<xsl:when test="name()='jcr:lastModified'"/>
			<xsl:when test="name()='jcr:lastModifiedBy'"/>
			<xsl:when test="name()='j:lastPublished'"/>
			<xsl:when test="name()='j:lastPublishedBy'"/>
			<xsl:when test="name()='j:fullpath'"/>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="child::node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>