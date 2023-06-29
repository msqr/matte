<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web">
	
	<!-- imports -->
	<xsl:import href="../tmpl/global-variables.xsl"/>
	<xsl:import href="../tmpl/util.xsl"/>

	<xsl:output method="text"/>
	
	<xsl:template match="x:x-data">
		<xsl:text>(</xsl:text>
		<xsl:apply-templates select="x:x-model/m:model/m:album"/>
		<xsl:apply-templates select="x:x-model/m:model/m:collection"/>
		<xsl:text>)</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:album">
		<xsl:text>{"album" : {"albumId": </xsl:text>
		<xsl:value-of select="@album-id"/>
		<xsl:text>, "name": "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="@name"/>
		</xsl:call-template>
		<xsl:text>", "creationDate": "</xsl:text>
		<xsl:value-of select="@creation-date"/>
		<xsl:text>"</xsl:text>
		<xsl:if test="@modify-date">
			<xsl:text>, "modifyDate": "</xsl:text>
			<xsl:value-of select="@modify-date"/>
			<xsl:text>"</xsl:text>
		</xsl:if>
		<xsl:text>, "allowAnonymous": </xsl:text>
		<xsl:value-of select="@allow-anonymous"/>
		<xsl:text>, "allowBrowse": </xsl:text>
		<xsl:value-of select="@allow-browse"/>
		<xsl:text>, "allowFeed": </xsl:text>
		<xsl:value-of select="@allow-feed"/>
		<xsl:text>, "allowOriginal": </xsl:text>
		<xsl:value-of select="@allow-original"/>
		<xsl:text>, "anonymousKey": "</xsl:text>
		<xsl:value-of select="@anonymous-key"/>
		<xsl:text>"</xsl:text>
		<xsl:if test="m:theme">
			<xsl:text>, &#xA;"theme": </xsl:text>
			<xsl:apply-templates select="m:theme"/>
		</xsl:if>
		<xsl:text>, "items": [</xsl:text>
		<xsl:apply-templates select="m:item"/>
		<xsl:text>]}}</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:theme">
		<xsl:text>{"themeId": </xsl:text>
		<xsl:value-of select="@theme-id"/>
		<xsl:text>, "name": "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="@name"/>
		</xsl:call-template>
		<xsl:text>"}</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:collection">
		<xsl:text>{"collection" : {"collectionId": </xsl:text>
		<xsl:value-of select="@collection-id"/>
		<xsl:text>, "name": "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="@name"/>
		</xsl:call-template>
		<xsl:text>", "creationDate": "</xsl:text>
		<xsl:value-of select="@creation-date"/>
		<xsl:text>"</xsl:text>
		<xsl:if test="@modify-date">
			<xsl:text>, "modifyDate": "</xsl:text>
			<xsl:value-of select="@modify-date"/>
			<xsl:text>"</xsl:text>
		</xsl:if>
		<xsl:text>, "items": [</xsl:text>
		<xsl:apply-templates select="m:item"/>
		<xsl:text>]}}</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:item">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#xA;</xsl:text>
		</xsl:if>
		<xsl:text>{"itemId": </xsl:text>
		<xsl:value-of select="@item-id"/>
		<xsl:text>, "itemDate": "</xsl:text>
		<xsl:choose>
			<xsl:when test="@item-date">
				<xsl:value-of select="substring(@item-date, 1, 10)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="substring(@creation-date, 1, 10)"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>"</xsl:text>
		<xsl:if test="m:shared-album">
			<xsl:text>, "sharedAlbums": [</xsl:text>
			<xsl:apply-templates select="m:shared-album"/>
			<xsl:text>]</xsl:text>
		</xsl:if>
		<xsl:text>}</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:shared-album">
		<xsl:if test="position() &gt; 1">
			<xsl:text>, </xsl:text>
		</xsl:if>
		<xsl:text>{"anonymousKey": "</xsl:text>
		<xsl:value-of select="@anonymous-key"/>
		<xsl:text>", "name": "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="string(@name)"/>
		</xsl:call-template>
		<xsl:text>"}</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>