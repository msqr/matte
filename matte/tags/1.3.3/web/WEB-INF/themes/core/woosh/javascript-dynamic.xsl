<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date"
	extension-element-prefixes="date">
	
	<xsl:import href="../../theme-util.xsl"/>
	
	<xsl:output method="text"/>
	
	<xsl:template match="x:x-data">
		var i18n = {
			<xsl:apply-templates select="x:x-msg/x:msg"/>
		};
	</xsl:template>
	
	<xsl:template match="x:msg">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#xA;</xsl:text>
		</xsl:if>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="@key"/>
		<xsl:text>" : "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="string(.)"/>
		</xsl:call-template>
		<xsl:text>"</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>
