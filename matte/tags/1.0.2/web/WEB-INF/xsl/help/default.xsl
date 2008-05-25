<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date">
	
	<xsl:template match="x:x-data">
		<div class="dynahelp">
			<xsl:apply-templates select="." mode="help"/>
		</div>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="help">
		<xsl:apply-templates select="x:x-messages/x:msg"/>
	</xsl:template>
	
</xsl:stylesheet>
