<?xml version="1.0"?>
<!-- ======================================================== -->
<!-- Stylesheet: Analyzer12.xsl                               -->
<!--    Version: 1.3 MS (2002-06-24)                          -->
<!--    Purpose: Analyse the structure of any given input XML -->
<!--             document - outputting the distinct elements  -->
<!--             (according to their path).                   -->
<!-- ======================================================== -->
<!--  Copyright: (c)2002 MarrowSoft Limited                   -->
<!--             ALL RIGHTS RESERVED.                         -->
<!--             This stylesheet may be used within an        -->
<!--             application - but must not be modified or    -->
<!--             published in any way without the prior       -->
<!--             prior written consent of MarrowSoft Ltd.     -->
<!-- ======================================================== -->
<xsl:stylesheet version="1.0" exclude-result-prefixes="msxsl analyzed"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:msxsl="urn:schemas-microsoft-com:xslt"
 xmlns:analyzed="urn:analysis-of-xml-structure">
	<!-- param for test purposes -->
	<xsl:param name="test"></xsl:param>
	<!-- key for selecting nodes by their parent path -->
	<xsl:key name="kByPath" match="*" use="@analyzed:parent-path"/>
	<!-- key for selecting distinct nodes by their path -->
	<xsl:key name="kDistinctPath" match="*" use="@analyzed:path"/>
	
	<!-- attribute set used to add pathing info attributes -->
	<xsl:attribute-set name="get-paths">
		<!-- add attribute containing the path of this element -->
		<xsl:attribute name="analyzed:path" namespace="urn:analysis-of-xml-structure">
			<xsl:apply-templates select="ancestor-or-self::*" mode="analyze-path"/>
		</xsl:attribute>
		<!-- add attribute containing the path of parent of this element -->
		<xsl:attribute name="analyzed:parent-path" namespace="urn:analysis-of-xml-structure">
			<xsl:apply-templates select="ancestor::*" mode="analyze-path"/>
		</xsl:attribute>
	</xsl:attribute-set>

	<!-- attribute set used to add pathing info attributes (for attributes) -->
	<xsl:attribute-set name="get-att-paths">
		<!-- add attribute containing the path of this element -->
		<xsl:attribute name="analyzed:path" namespace="urn:analysis-of-xml-structure">
			<xsl:apply-templates select="ancestor-or-self::*" mode="analyze-path"/>
			<xsl:text>/@</xsl:text>
			<xsl:value-of select="name()"/>
		</xsl:attribute>
		<!-- add attribute containing the path of parent of this element -->
		<xsl:attribute name="analyzed:parent-path" namespace="urn:analysis-of-xml-structure">
			<xsl:apply-templates select="ancestor::*" mode="analyze-path"/>
		</xsl:attribute>
	</xsl:attribute-set>

	<!-- =============================== -->
	<!-- Document root handling template -->
	<xsl:template match="/">
		<!-- build RTF -->
		<xsl:variable name="raw-rtf">
			<xsl:copy>
				<xsl:apply-templates select="*|@*" mode="build-rtf"/>
			</xsl:copy>
		</xsl:variable>
		<!-- convert RTF to node-set -->
		<xsl:variable name="first-pass" select="msxsl:node-set($raw-rtf)"/>
		<!-- test or live execution -->
		<xsl:choose>
			<xsl:when test="$test">
				<!-- test first pass output -->
				<xsl:copy-of select="$first-pass"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- process distinct paths on second pass -->
				<xsl:apply-templates select="$first-pass" mode="output"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ====================================== -->
	<!-- Template for building RTF for elements -->
	<xsl:template match="*" mode="build-rtf">
		<xsl:element name="{name()}" namespace="{namespace-uri()}" use-attribute-sets="get-paths">
			<xsl:attribute name="analyzed:type" namespace="urn:analysis-of-xml-structure">E</xsl:attribute>
			<xsl:apply-templates select="*|@*" mode="build-rtf"/>
		</xsl:element>
	</xsl:template>
	
	<!-- ====================================== -->
	<!-- Template for building RTF for elements -->
	<xsl:template match="@*" mode="build-rtf">
		<xsl:element name="{name()}" namespace="{namespace-uri()}" use-attribute-sets="get-att-paths">
			<xsl:attribute name="analyzed:type" namespace="urn:analysis-of-xml-structure">A</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- ===================================== -->
	<!-- Template for creating path attributes -->
	<xsl:template match="*" mode="analyze-path">
		<xsl:value-of select="name()"/>
		<xsl:if test="position() != last()">
			<xsl:text>/</xsl:text>
		</xsl:if>
	</xsl:template>
	
	<!-- ================================================== -->
	<!-- Template for handling root on second (output) pass -->
	<xsl:template match="/" mode="output">
		<!-- root only ever has one child -->
		<xsl:apply-templates select="*" mode="output"/>
	</xsl:template>
	
	<!-- ================================================================= -->
	<!-- Template for distinct elements/attributes on second (output) pass -->
	<xsl:template match="*" mode="output">
		<xsl:choose>
			<xsl:when test="@analyzed:type = 'A'">
				<!-- output this attribute -->
				<xsl:attribute name="{name()}" namespace="{namespace-uri()}"/>
				<!-- NB. no apply on children as attributes can't have children -->
			</xsl:when>
			<xsl:otherwise>
				<!-- output this distinct element -->
				<xsl:element name="{name()}" namespace="{namespace-uri()}">
					<!-- out all distinct elements that have this same parental path -->
					<xsl:apply-templates select="key('kByPath',@analyzed:path)[generate-id() = generate-id(key('kDistinctPath',@analyzed:path))]" mode="output">
						<!-- order them by their type and original position within their parent -->
						<xsl:sort select="@analyzed:type"/>
						<xsl:sort select="count(preceding-sibling::*)" data-type="number"/>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
