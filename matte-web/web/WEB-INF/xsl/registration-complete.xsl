<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m xweb">
	
    <!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	<xsl:import href="tmpl/display-user.xsl"/>
	
	<!-- layout variables -->
	<xsl:variable name="layout.global.nav.page" select="'register'"/>
	
	<!-- helper vars -->
	<xsl:variable name="user" select="xweb:x-data/xweb:x-model/m:edit/m:user"/>

	<xsl:template match="xweb:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','registration-complete.title')"/>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body">
		<div class="intro">
			<xsl:value-of select="key('i18n','registration-complete.intro')"
				disable-output-escaping="yes"/>
		</div>
        
		<div class="simple-form-validate">
			<xsl:apply-templates select="$user" mode="validate"/>
		</div>
		
	</xsl:template>
	
</xsl:stylesheet>
