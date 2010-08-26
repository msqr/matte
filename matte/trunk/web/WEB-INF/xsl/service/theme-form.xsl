<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	exclude-result-prefixes="m x"
	version="1.0">
	
	<xsl:import href="../tmpl/global-variables.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>
	
	<xsl:variable name="edit-theme" 
		select="$aux/m:model[1]/m:theme[1]"/>
	<xsl:variable name="is-new" select="not(boolean(string($edit-theme/@theme-id)))"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="edit-theme-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form" enctype="multipart/form-data">
			<p style="max-width: 300px;">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<xsl:value-of select="key('i18n','add.theme.intro')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n','update.theme.intro')"/>
					</xsl:otherwise>
				</xsl:choose>
			</p>
			<xsl:apply-templates select="$edit-theme" mode="edit"/>
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
	
	<xsl:template match="m:theme" mode="edit">
		<div>
			<label for="theme.name">
				<xsl:if test="$err[@field='theme.name']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','name.displayName')"/>
			</label>
			<div>
				<input type="text" name="theme.name" value="{@name}"
					maxlength="64" id="theme.name"/>
			</div>
		</div>
		<div>
			<label for="theme.author">
				<xsl:if test="$err[@field='theme.author']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','theme.author.displayName')"/>
			</label>
			<div>
				<input type="text" name="theme.author" value="{@author}"
					maxlength="128" id="theme.author"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','optional.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="theme.authorEmail">
				<xsl:if test="$err[@field='theme.authorEmail']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','theme.authorEmail.displayName')"/>
			</label>
			<div>
				<input type="text" name="theme.authorEmail" value="{@author-email}"
					maxlength="32" id="theme.authorEmail"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','optional.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="theme.archive">
				<xsl:if test="$err[@field='tempFile']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','theme.archive.displayName')"/>
			</label>
			<div>
				<input type="file" name="tempFile" id="theme.archive"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','theme.archive.caption')" 
						disable-output-escaping="yes"/>
				</div>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>