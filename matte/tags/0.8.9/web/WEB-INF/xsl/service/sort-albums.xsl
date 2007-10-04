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
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="sort-albums-form" 
			action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p>
				<xsl:value-of select="key('i18n','sort.albums.intro')"/>
			</p>
			<div class="label">
				<xsl:value-of select="key('i18n','album.displayName')"/>
				<xsl:text>: </xsl:text>
				<xsl:value-of select="x:x-model/m:model/m:search-results/m:album[1]/@name"/>
			</div>
			<div id="sort-albums-container">
				<xsl:choose>
					<xsl:when test="x:x-model/m:model/m:search-results/m:album[1]/m:search-album">
						<xsl:apply-templates 
							select="x:x-model/m:model/m:search-results/m:album[1]/m:search-album"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n', 'sort.albums.nochildren')"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<xsl:if test="x:x-model/m:model/m:search-results/m:album[1]/m:search-album">
				<div class="submit">
					<input type="hidden" name="albumId" id="sort-albums-parent"
						value="{x:x-model/m:model/m:search-results/m:album[1]/@album-id}"/>
					<input type="submit" value="{key('i18n','save.displayName')}"/>
				</div>
			</xsl:if>
		</form>
	</xsl:template>
	
	<xsl:template match="m:album | m:search-album">
		<div class="sort-albums-item" id="childalbum_{position()}">
			<img id="sort-albums-album-{@album-id}">
				<xsl:attribute name="src">
					<xsl:call-template name="render-media-server-url">
						<xsl:with-param name="item" select="m:search-poster"/>
						<xsl:with-param name="size" select="'THUMB_NORMAL'"/>
						<xsl:with-param name="quality" select="'GOOD'"/>
						<xsl:with-param name="web-context" select="$web-context"/>
					</xsl:call-template>
				</xsl:attribute>
			</img>
			<br/>
			<xsl:choose>
				<xsl:when test="string-length(@name) &gt; 20">
					<xsl:value-of select="substring(@name, 1, 20)"/>
					<xsl:text>&#8230;</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@name"/>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>

</xsl:stylesheet>