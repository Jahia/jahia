<?xml version="1.0" encoding="UTF-8"?>

<!-- The kml.xsl is used to transfrom a Google Analytics Geo Map Overlay XML report to a KML -->
<!-- TODO cleanup as some of the templates are no longer used -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" version="1.0" omit-xml-declaration="yes"/>
	<xsl:template match="/">
		<kml xmlns="http://earth.google.com/kml/2.1">
			<Document>
				<name>
					<xsl:value-of select="AnalyticsReport/Report/Title/Name"/> for site <xsl:value-of select="AnalyticsReport/Report/Title/ProfileName"/> (<xsl:value-of select="AnalyticsReport/Report/Title/PrimaryDateRange"/>)</name>
				<open>1</open>
				<xsl:for-each select="AnalyticsReport/Report/GeoMap/Region">
					<xsl:variable name="city">
						<xsl:value-of select="Name"/>
					</xsl:variable>
					<xsl:variable name="lng">
						<xsl:value-of select="Lng"/>
					</xsl:variable>
					<xsl:variable name="lat">
						<xsl:value-of select="Lat"/>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="contains($city, '(not set)')">
							<!-- skip not set -->
						</xsl:when>
						<xsl:otherwise>
							<Placemark>
								<name>
									<xsl:value-of select="string($city)"/>
								</name>
								<description>Visits: <xsl:value-of select="Value"/>
								</description>
								<Point>
									<coordinates>
										<xsl:value-of select="string($lng)"/>,<xsl:value-of select="string($lat)"/>
									</coordinates>
								</Point>
							</Placemark>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</Document>
		</kml>
	</xsl:template>
	<!-- Get City Template -->
	<xsl:template name="getCity">
		<xsl:param name="stringIn"/>
		<xsl:call-template name="replaceCharsInString">
			<xsl:with-param name="stringIn" select="substring-before($stringIn,'|')"/>
			<xsl:with-param name="charsIn" select="'-'"/>
			<xsl:with-param name="charsOut" select="', '"/>
		</xsl:call-template>
	</xsl:template>
	<!-- Get Long -->
	<xsl:template name="getLong">
		<xsl:param name="stringIn"/>
		<xsl:call-template name="addDecimal">
			<xsl:with-param name="stringIn" select="substring-after(substring-after($stringIn,'|'), '|')"/>
		</xsl:call-template>
	</xsl:template>
	<!-- Get Lat-->
	<xsl:template name="getLat">
		<xsl:param name="stringIn"/>
		<xsl:call-template name="addDecimal">
			<!-- Lat -->
			<xsl:with-param name="stringIn" select="substring-before(substring-after($stringIn,'|'), '|')"/>
		</xsl:call-template>
	</xsl:template>
	<!-- Replacement  Template -->
	<xsl:template name="replaceCharsInString">
		<xsl:param name="stringIn"/>
		<xsl:param name="charsIn"/>
		<xsl:param name="charsOut"/>
		<xsl:choose>
			<xsl:when test="contains($stringIn,$charsIn)">
				<xsl:value-of select="concat(substring-before($stringIn,$charsIn),$charsOut)"/>
				<xsl:call-template name="replaceCharsInString">
					<xsl:with-param name="stringIn" select="substring-after($stringIn,$charsIn)"/>
					<xsl:with-param name="charsIn" select="$charsIn"/>
					<xsl:with-param name="charsOut" select="$charsOut"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$stringIn"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Add Decimal Template -->
	<xsl:template name="addDecimal">
		<xsl:param name="stringIn"/>
		<xsl:value-of select="concat(concat(substring($stringIn,0,string-length($stringIn)-3), '.'), substring($stringIn,string-length($stringIn)-3))"/>
	</xsl:template>
</xsl:stylesheet>
