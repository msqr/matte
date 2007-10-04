<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan"
	version="1.0">

	<xsl:import href="../tmpl/global.xsl"/>
			
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="edit-collection" 
		select="x:x-data/x:x-model[1]/m:edit[1]/m:collection[1]"/>
	<xsl:variable name="is-new" select="not(boolean(string($edit-collection/@collection-id)))"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="edit-collection-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p style="max-width: 300px;">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<xsl:value-of select="key('i18n','add.collection.intro')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n','update.collection.intro')"/>
					</xsl:otherwise>
				</xsl:choose>
			</p>
			<xsl:apply-templates select="$edit-collection" mode="edit"/>
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
	
	<xsl:template match="m:collection" mode="edit">
		<div>
			<label for="collection-name">
				<xsl:if test="$err[@field='collection.name']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','collection.name.displayName')"/>
			</label>
			<div>
				<input id="collection-name" type="text" name="collection.name" value="{@name}"
					maxlength="64"/>
			</div>
		</div>
		<div>
			<label for="collection-comments">
				<xsl:value-of select="key('i18n','collection.comment.displayName')"/>
			</label>
			<div>
				<textarea name="collection.comment" id="collection-comments">
					<xsl:choose>
						<xsl:when test="string-length(m:comment) &gt; 0">
							<xsl:value-of select="m:comment"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text> </xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</textarea>
				<div class="caption"><xsl:value-of 
					select="key('i18n','optional.caption')"/></div>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>