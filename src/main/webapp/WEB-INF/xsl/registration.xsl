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
		<xsl:value-of select="key('i18n','registration.title')"/>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body">
		<div class="intro">
			<xsl:value-of select="key('i18n','registration.intro')" disable-output-escaping="yes"/>
		</div>
		
		<form class="simple-form" method="post" action="{$web-context}{$ctx/xweb:path}">
			<xsl:apply-templates select="$user" mode="edit"/>
			<div class="submit">
				<input type="submit" name="_target1" value="{key('i18n','continue.displayName')}"/>
				<xsl:text> </xsl:text>
				<input type="submit" name="_cancel" value="{key('i18n','cancel.displayName')}"/>
			</div>
		</form>
		<script type="text/javascript" xml:space="preserve">
			<xsl:comment>
			document.forms[0].elements['user.name'].focus();
			//</xsl:comment>
		</script>
	
	</xsl:template>
    
</xsl:stylesheet>
