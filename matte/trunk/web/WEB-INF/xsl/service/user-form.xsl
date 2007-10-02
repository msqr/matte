<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	exclude-result-prefixes="m x xalan"
	version="1.0">
	
	<xsl:import href="../tmpl/display-user.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="edit-user" 
		select="x:x-data/x:x-model[1]/m:edit[1]/m:user[1]"/>
	<xsl:variable name="is-new" select="not(boolean(string($edit-user/@user-id)))"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="edit-user-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p style="max-width: 300px;">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<xsl:value-of select="key('i18n','add.user.intro')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n','update.user.intro')"/>
					</xsl:otherwise>
				</xsl:choose>
			</p>
			<xsl:apply-templates select="$edit-user" mode="edit"/>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			<div class="submit">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<input type="submit" value="{key('i18n','add.displayName')}"/>
					</xsl:when>
					<xsl:otherwise>
						<input type="submit" value="{key('i18n','save.displayName')}"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		</form>
	</xsl:template>
	
</xsl:stylesheet>