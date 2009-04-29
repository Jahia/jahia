

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>
<!-- Stylesheet performing identity transformation on XML
	file. The output XML file is equivalent to input 
	XML file 
-->

<!-- According to current XSLT standard, template match 
	"@*|node()" should have worked. Current implementation
	of XT has a known bug that the node() node-test does 
	not work in match patterns. However, we can get our
	job done by using "@*|*" instead"
-->

<xsl:template match="@*|*">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="/">
  <html>
    <head>
      <title>Testing XSL transformations from Jahia XMLSource engine</title>
    </head>
    <body>
      <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

</xsl:stylesheet>