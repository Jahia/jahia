<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml"/>
	
	<xsl:template match="/">
	<hibernate-custom datetime="{hibernate-generic/@datetime}">
		<xsl:apply-templates/>
	</hibernate-custom>
	</xsl:template>
	
	<xsl:template match="object">
		<xsl:element name="{@class}">
			<xsl:attribute name="id">
				<xsl:value-of select="generate-id(.)"/>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<!-- calculates the id of the associated entity object -->
	<xsl:template name="link">
		<xsl:variable name="idval" select="id"/>
		<xsl:variable name="classval" select="@class"/>
		<xsl:variable name="packageval" select="@package"/>
		<xsl:variable name="elem" select="//object[id=$idval and @class=$classval and @package=$packageval]"/>
		<xsl:if test="$elem">
			<xsl:attribute name="id">
				<xsl:value-of select="generate-id($elem)"/>
			</xsl:attribute>
		</xsl:if>
		<xsl:value-of select="$idval"/>
	</xsl:template>
	
	<xsl:template match="property[id]">
		<xsl:element name="{@name}">
			<xsl:call-template name="link"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="element[id]">
		<xsl:element name="{../@name|../@role}">
			<xsl:copy-of select="@index"/>
			<xsl:call-template name="link"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="array">
		<xsl:comment>elements of array <xsl:value-of select="@name|@role"/></xsl:comment>
		<xsl:apply-templates/>
		<xsl:comment>end of elements</xsl:comment>
	</xsl:template>
	
	<xsl:template match="collection">
		<xsl:comment>elements of collection <xsl:value-of select="@name|@role"/></xsl:comment>
		<xsl:apply-templates/>
		<xsl:comment>end of elements</xsl:comment>
	</xsl:template>
	
	<xsl:template match="property|id|component">
		<xsl:element name="{@name}">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="element|composite-element|subcollection">
		<xsl:element name="{../@name|../@role}">
			<xsl:copy-of select="@index"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>