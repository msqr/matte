<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan date"
	version="1.0">
	
	<xsl:import href="tmpl/global-variables.xsl"/>
	<xsl:import href="../themes/theme-util.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<table>
			<tr>
				<td>
					<img class="theme-thumbnail" 
						id="theme-thumbnail-{x:x-model/m:model[1]/m:theme[1]/@theme-id}">
						<xsl:attribute name="src">
							<xsl:call-template name="get-resource-url">
								<xsl:with-param name="web-context" select="$web-context"/>
								<xsl:with-param name="theme" select="x:x-model/m:model[1]/m:theme[1]"/>
								<xsl:with-param name="resource" select="'thumbnail.png'"/>
							</xsl:call-template>
						</xsl:attribute>
						<xsl:attribute name="alt">
							<xsl:value-of select="key('i18n','theme.thumbnail.displayName')"/>
						</xsl:attribute>
					</img>
				</td>
				<td>
					<div class="simple-form-validate theme-view">
						<xsl:apply-templates select="x:x-model/m:model[1]/m:theme[1]" mode="view"/>
					</div>
				</td>
			</tr>
		</table>
	</xsl:template>
	
	<xsl:template match="m:theme" mode="view">
		<div>
			<div class="label">
				<xsl:value-of select="key('i18n','name.displayName')"/>
			</div>
			<div>
				<xsl:value-of select="@name"/>
			</div>
		</div>
		<div>
			<div class="label">
				<xsl:value-of select="key('i18n','theme.author.displayName')"/>
			</div>
			<div>
				<xsl:value-of select="@author"/>
			</div>
		</div>
		<div>
			<div class="label">
				<xsl:value-of select="key('i18n','theme.authorEmail.displayName')"/>
			</div>
			<div>
				<xsl:value-of select="@author-email"/>
			</div>
		</div>
		<div>
			<div class="label">
				<xsl:value-of select="key('i18n','createdDate.displayName')"/>
			</div>
			<div>
				<xsl:value-of select="date:format-date(substring-before(
					@creation-date,'.'),'dd MMM yyyy')"/>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>