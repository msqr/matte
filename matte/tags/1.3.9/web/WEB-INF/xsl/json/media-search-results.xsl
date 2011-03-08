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
		<xsl:apply-templates select="x:x-model/m:model/m:search-results"/>
		<xsl:text>)</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:search-results">
		<xsl:text>{"searchResults": {"totalResults": </xsl:text>
		<xsl:value-of select="@total-results"/>
		<xsl:text>, "returnedResults": </xsl:text>
		<xsl:value-of select="@returned-results"/>
		<xsl:text>, "searchTime": </xsl:text>
		<xsl:value-of select="@search-time"/>
		
		<!-- TODO: pagination -->
		
		<xsl:text>, "items": [&#xA;</xsl:text>
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