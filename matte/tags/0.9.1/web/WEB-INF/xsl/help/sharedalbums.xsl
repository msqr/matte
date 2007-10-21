<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date">
	
	<xsl:import href="default.xsl"/>
	<xsl:import href="../tmpl/global.xsl"/>
	
	<xsl:template match="x:x-data" mode="help">
		
		<p>Your shared albums are published at the following URL:</p>
		
		<p>
			<xsl:variable name="href">
				<xsl:call-template name="server-url"/>
				<xsl:value-of select="$web-context"/>
				<xsl:text>/browse.do?userKey=</xsl:text>
				<xsl:value-of select="$acting-user/@anonymous-key"/>				
			</xsl:variable>
			<a href="{$href}">
				<xsl:value-of select="$href"/>
			</a>
		</p>
		
		<p style="max-width: 400px;">You can change the theme used for this by updating your 
		Matte preferences (under the Actions &gt; Matte Settings menu).</p>
		
	</xsl:template>
	
</xsl:stylesheet>
