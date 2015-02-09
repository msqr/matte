<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m xweb">
	
    <!-- imports -->
	<xsl:import href="tmpl/global-variables.xsl"/>
		
	<xsl:template match="xweb:x-data">
		<html>
			<head>
				<script type="text/javascript" src="{$web-context}/js/popup.js"/>
			</head>
		</html>
	</xsl:template>
		
</xsl:stylesheet>
