<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>

	<!-- layout variables -->
	<xsl:variable name="layout.global.nav.page" select="'forgot-password'"/>
	
	<xsl:template match="x:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','forgot-password-complete.title')"/>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body">
		<div class="intro">
			<xsl:value-of select="key('i18n','forgot-password-complete.intro')"
				disable-output-escaping="yes"/>
		</div>
	</xsl:template>
    
</xsl:stylesheet>
