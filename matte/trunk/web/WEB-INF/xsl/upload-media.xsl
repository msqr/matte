<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m xweb">
	
    <!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	<xsl:import href="tmpl/upload-media-form.xsl"/>
		
	<!-- helper vars -->
	<xsl:variable name="form.collectionId" 
		select="xweb:x-data/xweb:x-auxillary[1]/xweb:x-param[@key='collectionId']"/>
	<xsl:variable name="form.localTz" 
		select="xweb:x-data/xweb:x-auxillary[1]/xweb:x-param[@key='localTz']"/>
	<xsl:variable name="form.mediaTz" 
		select="xweb:x-data/xweb:x-auxillary[1]/xweb:x-param[@key='mediaTz']"/>
	
	<xsl:template match="xweb:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','upload.media.title')"/>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="xweb:x-data" mode="page-main-nav">
		<xsl:call-template name="main-nav">
			<xsl:with-param name="page" select="'upload'"/>
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="xweb:x-data" mode="page-body">
		<xsl:choose>
			<xsl:when test="key('appenv','feature.upload.applet') = 'true'">
				<applet code="magoffin.matt.ma2.web.applet.UploadMedia.class" 
					codebase="applet/" width="600" height="400">
					<xsl:apply-templates select="." mode="add-media-form"/>
					<script type="text/javascript" xml:space="preserve">
						<xsl:comment>
						document.forms[0].elements['tempFile'].focus();
						//</xsl:comment>
					</script>
				</applet>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="." mode="add-media-form"/>
				<script type="text/javascript" xml:space="preserve">
					<xsl:comment>
					document.forms[0].elements['tempFile'].focus();
					//</xsl:comment>
				</script>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
