<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:eat="http://msqr.us/xsd/ieat"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="eat xweb">
	
    <!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	
	<!-- layout variables -->
	<xsl:variable name="layout.global.nav.page" select="'register'"/>
	
	<xsl:template match="xweb:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','registration-confirmed.title')"/>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body">
        <p>
			<xsl:value-of select="key('i18n','registration-confirmed.intro')"
				disable-output-escaping="yes"/>
        </p>
        
		<p>
			<a href="{$web-context}/home.do" title="{key('i18n','link.home.title')}">
				<xsl:value-of select="key('i18n','link.home')"/>
			</a>
		</p>
	</xsl:template>
	
</xsl:stylesheet>
