<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
    <!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	<xsl:import href="tmpl/upload-media-form.xsl"/>
		
	<!-- helper vars -->
	<xsl:variable name="form.collectionId" 
		select="x:x-data/x:x-auxillary[1]/x:x-param[@key='collectionId']"/>
	
	<xsl:template match="x:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','sort.items.title')"/>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-main-nav">
		<xsl:call-template name="main-nav">
			<xsl:with-param name="page" select="'sort-items'"/>
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-head-content">
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/matte-main.css" media="screen"><xsl:text> </xsl:text></link>
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/matte-sort-items.css" media="screen"><xsl:text> </xsl:text></link>
		<script id="behaviour-js" type="text/javascript" 
			src="{$web-context}/js/matte-sort-items-behaviours.js"><xsl:text> </xsl:text></script>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body">
		<form id="sort-items-form" method="post" class="simple-form" 
			action="{$web-context}{$ctx/x:path}">
			<p>
				<xsl:value-of select="key('i18n','sort.items.intro')"/>
			</p>
			<div id="sort-items-container">
				<xsl:choose>
					<xsl:when test="x:x-model/m:model/m:album[1]/m:item">
						<xsl:apply-templates 
							select="x:x-model/m:model/m:album[1]/m:item"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n', 'sort.items.nochildren')"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<xsl:if test="x:x-model/m:model/m:album[1]/m:item">
				<div class="submit">
					<input type="submit" value="{key('i18n','save.displayName')}"/>
				</div>
			</xsl:if>
		</form>
		<div style="display: none">
			<form id="sort-items-submit-form" method="post" 
				action="{$web-context}{$ctx/x:path}">
				<input type="hidden" name="albumId"
					value="{x:x-model/m:model/m:album[1]/@album-id}"/>
				
			</form>
		</div>
	</xsl:template>
	
	<xsl:template match="m:item">
		<div class="sort-items-item" id="item_{position()}">
			<img id="sort-items-item-{@item-id}">
				<xsl:attribute name="src">
					<xsl:call-template name="render-media-server-url">
						<xsl:with-param name="item" select="."/>
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
