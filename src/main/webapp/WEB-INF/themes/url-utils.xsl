<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">

	<!--
		Named Template: theme-resource-url
		
		Generate a URL for a theme resource, e.g. /matte/theme/1/theme.js
		
		Parameters:
		theme - the theme node
		resource - the resource name string
		web-context - the web context path string
	-->
	<xsl:template name="theme-resource-url">
		<xsl:param name="theme"/>
		<xsl:param name="resource"/>
		<xsl:param name="web-context"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/theme/</xsl:text>
		<xsl:value-of select="$theme/@theme-id"/>
		<xsl:text>/</xsl:text>
		<xsl:value-of select="$resource"/>
	</xsl:template>
	
	<!--
		Named Template: render-view-album-url
		
		Generate the URL for viewing a public album.
		
		=> /album.do?key=ABC
		
		If the album has no ID, it is treated as a virtual album
		for the given user, and the 'user' parameter must be supplied.
		It will then generate a virtual album link like
		
		=> /album.do?userKey=XYZ&key=ABC&mode=DFK
		
		Parameters:
		album - an Album
		user - the owner of the Album
		mode - a virtual browse mode
		web-context - the web context
		item-id - (opt) the ID of an item to display
	-->
	<xsl:template name="shared-album-url">
		<xsl:param name="album"/>
		<xsl:param name="user"/>
		<xsl:param name="web-context"/>
		<xsl:param name="item-id"/>
		<xsl:param name="mode"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/album.do?key=</xsl:text>
		<xsl:value-of select="encode-for-uri($album/@anonymous-key)" />
		<xsl:if test="string-length($album/@album-id) &lt; 1">
			<xsl:text>&amp;userKey=</xsl:text>
			<xsl:value-of select="encode-for-uri($user/@anonymous-key)"/>
			<xsl:if test="$mode">
				<xsl:text>&amp;mode=</xsl:text>
				<xsl:value-of select="$mode"/>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$item-id">
			<xsl:text>&amp;itemId=</xsl:text>
			<xsl:value-of select="$item-id"/>
		</xsl:if>
	</xsl:template>

	<!--
		Named Template: media-server-url
		
		Generate the URL for an image for the MediaServer server. For example:
		
		render-media-server-url(item = $MediaItem{id = 1565}, quality = 'GOOD', size = 'THUMB_NORMAL')
		
		=> media.do?id=1565&size=THUMB_NORMAL&quality=GOOD
		
		Parameters:
		item - a MediaItem node
		quality (opt) - value to use for the MediaServer quality parameter
		size (opt) - value to use for the MediaServer size parameter
		download (opt) - if set, add download=true flag
		album-key (opt) - if set and original = true, then add for original downloading
		original (opt) - if set, then generate URL for downloading original media
		web-context - the web context
	-->
	<xsl:template name="media-server-url">
		<xsl:param name="item"/>
		<xsl:param name="quality"/>
		<xsl:param name="size"/>
		<xsl:param name="download"/>
		<xsl:param name="album-key"/>
		<xsl:param name="original"/>
		<xsl:param name="web-context"/>
		
		<xsl:value-of select="$web-context"/>
		<xsl:text>/media.do?id=</xsl:text>
		<xsl:value-of select="$item/@item-id"/>
		<xsl:if test="$album-key">
			<xsl:text>&amp;albumKey=</xsl:text>
			<xsl:value-of select="encode-for-uri($album-key)"/>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="$original">
				<xsl:text>&amp;original=true</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>&amp;size=</xsl:text>
				<xsl:value-of select="$size"/>
				<xsl:if test="$quality">
					<xsl:text>&amp;quality=</xsl:text>
					<xsl:value-of select="$quality"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="$download">
			<xsl:text>&amp;download=true</xsl:text>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
