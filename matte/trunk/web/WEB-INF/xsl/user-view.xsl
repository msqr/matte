<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	exclude-result-prefixes="m x xalan"
	version="1.0">
	
	<xsl:import href="tmpl/display-user.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<div class="simple-form-validate user-view">
			<xsl:apply-templates select="x:x-model/m:model[1]/m:user[1]" 
				mode="display-internal"/>
		</div>
	</xsl:template>
	
</xsl:stylesheet>