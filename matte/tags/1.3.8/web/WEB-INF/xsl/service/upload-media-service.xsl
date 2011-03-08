<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	exclude-result-prefixes="m x"
	version="1.0">
	
	<xsl:import href="../tmpl/upload-media-form.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<xsl:apply-templates select="." mode="add-media-form"/>
	</xsl:template>
		
</xsl:stylesheet>