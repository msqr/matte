<?xml version="1.0"?> 
<!--
  Copyright (c) 2006 Matt Magoffin
  
  This program is free software; you can redistribute it and/or 
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 of 
  the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful, 
  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License 
  along with this program; if not, write to the Free Software 
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  02111-1307 USA

  $Id: theme-util.xsl,v 1.15 2007/09/30 08:11:10 matt Exp $   
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">

	<!-- standard data vars -->
	<xsl:variable name="aux" select="x:x-data/x:x-auxillary"/>
	<xsl:variable name="ctx" select="x:x-data/x:x-context"/>
	<xsl:variable name="mod" select="x:x-data/x:x-model"/>
	<xsl:variable name="err" select="x:x-data/x:x-errors/x:error[@field]"/>
	<xsl:variable name="req" select="x:x-data/x:x-request/x:param"/>
	<xsl:variable name="ses" select="x:x-data/x:x-session"/>
	
	<!-- helper vars -->
	<xsl:variable name="acting-user" select="x:x-data/x:x-session[1]/m:session[1]/m:acting-user[1]"/>
	<xsl:variable name="server-name" select="string($ctx/x:server-name)"/>
	<xsl:variable name="server-port" select="string($ctx/x:server-port)"/>
	<xsl:variable name="user-locale" select="string($ctx/x:user-locale)"/>
	<xsl:variable name="web-context" select="string($ctx/x:web-context)"/>
	<xsl:variable name="web-path" select="string($ctx/x:path)"/>
	<xsl:variable name="theme" select="x:x-data/x:x-model[1]/m:model[1]/m:theme[1]"/>
	
	<!-- application context defined as key for quick lookup -->
	<xsl:key name="appenv" match="x:x-data/x:x-auxillary/x:x-app-context/x:param" use="@key"/>
	
	<!-- auxillaray params defined as key for quick lookup -->
	<xsl:key name="aux-param" match="x:x-data/x:x-auxillary/x:x-param" use="@key"/>
	
	<!-- message resource bundle defined as key for quick lookup -->
	<xsl:key name="i18n" match="x:x-data/x:x-msg/x:msg" use="@key"/>
	
	<!-- request params defined as key for quick lookup -->
	<xsl:key name="req-param" match="x:x-data/x:x-request/x:param" use="@key"/>
	
	<!--
		Named Template: javascript-string
		
		Replace occurances of " in a string with \".
		
		Parameters:
		output-string	- the text to seach/replace in
	-->
	<xsl:template name="javascript-string">
		<xsl:param name="output-string"/>
		<xsl:call-template name="global-replace">
			<xsl:with-param name="output-string" select="$output-string"/>
			<xsl:with-param name="target"><xsl:text>"</xsl:text></xsl:with-param>
			<xsl:with-param name="replacement"><xsl:text>\"</xsl:text></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
		Named Template: single-quote-string
		
		Replace occurances of ' in a string with \'.
		
		Parameters:
		output-string	- the text to seach/replace in
	-->
	<xsl:template name="single-quote-string">
		<xsl:param name="output-string"/>
		<xsl:call-template name="global-replace">
			<xsl:with-param name="output-string" select="$output-string"/>
			<xsl:with-param name="target"><xsl:text>'</xsl:text></xsl:with-param>
			<xsl:with-param name="replacement"><xsl:text>\'</xsl:text></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
		Named Template: escape-string
		
		Replace occurances of a string with that string preceeded by a '\' 
		character.
		
		Parameters:
		output-string	- the text to seach/replace in
		target			- the text to search for
	-->
	<xsl:template name="escape-string">
		<xsl:param name="output-string"/>
		<xsl:param name="target"/>
		<xsl:call-template name="global-replace">
			<xsl:with-param name="output-string" select="$output-string"/>
			<xsl:with-param name="target" select="$target"/>
			<xsl:with-param name="replacement">
				<xsl:text>\</xsl:text>
				<xsl:value-of select="$target"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
		Named Template: global-replace
		
		Replace occurances of one string with another.
		
		Parameters:
		output-string	- the text to seach/replace in
		target			- the text to search for
		replacement		- the text to replace occurances of 'target' with
	-->
	<xsl:template name="global-replace">
		<xsl:param name="output-string"/>
		<xsl:param name="target"/>
		<xsl:param name="replacement"/>
		<xsl:choose>
			<xsl:when test="contains($output-string,$target)">
				
				<xsl:value-of select=
					"concat(substring-before($output-string,$target), $replacement)"/>
				<xsl:call-template name="global-replace">
					<xsl:with-param name="output-string" 
						select="substring-after($output-string,$target)"/>
					<xsl:with-param name="target" select="$target"/>
					<xsl:with-param name="replacement" 
						select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$output-string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
		Named Template: truncate-at-word
		
		Truncate a string at a word break (space). If the input text
		is shorter than max-length the text is returned unchanged.
		Otherwise the text is truncated at the max-length plus any 
		characters up to the next space, and a ellipsis character is
		appended.
		
		Parameters:
		text       - the text to truncate
		max-length - the maximum number of characters to allow
	-->
	<xsl:template name="truncate-at-word">
		<xsl:param name="text"/>
		<xsl:param name="max-length">350</xsl:param>
		<xsl:choose>
			<xsl:when test="string-length($text) &lt; $max-length">
				<xsl:value-of select="$text" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="start" select="substring($text,1,$max-length)"/>
				<xsl:variable name="after" select="substring($text,($max-length+1))"/>
				<xsl:variable name="word" select="substring-before($after,' ')"/>
				<xsl:value-of select="$start" disable-output-escaping="yes"/>
				<xsl:value-of select="$word" disable-output-escaping="yes"/>
				<xsl:text>&#x2026;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
		Named Template: get-css-link
		
		Generate an HTML <link> tag for the appropriate CSS given the user
		agent type and platform.  For example:
		
		<link rel="stylsheet" href="theme/inc/default/album-ie-win.css">
		
		Currently supported combinations:
		
		Type   Platform   HREF Output
		=========================================
		ie     win        ie-win
		
		All others will not change the CSS HREF.
		
		Parameters:
		theme - the theme node
		css - the CSS path to use
		ua-type - the user-agent type (from get-browser-type)
		ua-version - the user-agent version (from get-browser-version)
		ua-platform - the user-agent platform (from get-browser-platform)
		web-context - the current web context
	-->
	<xsl:template name="get-css-link">
		<xsl:param name="theme"/>
		<xsl:param name="css"/>
		<xsl:param name="ua-type"/>
		<xsl:param name="ua-version"/>
		<xsl:param name="ua-platform"/>
		<xsl:param name="web-context"/>
		<link  xmlns="http://www.w3.org/1999/xhtml" rel="stylesheet">
			<xsl:attribute name="href">
				<xsl:call-template name="get-resource-url">
					<xsl:with-param name="theme" select="$theme"/>
					<xsl:with-param name="resource">
						<xsl:choose>
							<xsl:when test="$ua-type = 'ie' and $ua-platform = 'win'">
								<xsl:value-of select="substring-before($css,'.css')"/>
								<xsl:text>-ie-win.css</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$css"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:with-param>
					<xsl:with-param name="web-context" select="$web-context"/>
				</xsl:call-template>
			</xsl:attribute>
		</link>
	</xsl:template>
	
	<!--
		Named Template: get-resource-url
		
		Generate a URL for a theme resource.
		
		Parameters:
		theme - the theme node
		resource - the resource name
		web-context - the web context path
	-->
	<xsl:template name="get-resource-url">
		<xsl:param name="theme"/>
		<xsl:param name="resource"/>
		<xsl:param name="web-context"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/themeResource.do?themeId=</xsl:text>
		<xsl:value-of select="$theme/@theme-id"/>
		<xsl:text>&amp;resource=</xsl:text>
		<xsl:value-of select="$resource"/>
	</xsl:template>
	
	<!--
		Named Template: get-dynamic-css-url
		
		Generate a URL for a dynamic theme CSS resource.
		
		Parameters:
		theme - the theme node
		web-context - the web context path
	-->
	<xsl:template name="get-dynamic-css-url">
		<xsl:param name="theme"/>
		<xsl:param name="web-context"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/themeCss.do?themeId=</xsl:text>
		<xsl:value-of select="$theme/@theme-id"/>
	</xsl:template>
	
	<!--
		Named Template: get-dynamic-js-url
		
		Generate a URL for a dynamic theme JavaScript resource.
		
		Parameters:
		theme - the theme node
		web-context - the web context path
	-->
	<xsl:template name="get-dynamic-js-url">
		<xsl:param name="theme"/>
		<xsl:param name="web-context"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/themeJavaScript.do?themeId=</xsl:text>
		<xsl:value-of select="$theme/@theme-id"/>
	</xsl:template>
	
	<!--
		Template: m:item, mode album-data
		
		Render album data JavaScript array. The data array is ordered as follows:
		
		0: itemId
		1: original width
		2: original height
		3: collection-relative path
		4: name
		5: comment
		6: create date
		7: icon width
		8: icon height
		9: mime type
	-->
	<xsl:template match="m:item" mode="album-data">
		<xsl:text>[</xsl:text>
		<xsl:value-of select="@item-id"/>
		<xsl:text>,</xsl:text>
		<xsl:value-of select="@width"/>
		<xsl:text>,</xsl:text>
		<xsl:value-of select="@height"/>
		<xsl:text>,"</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="@path"/>
		</xsl:call-template>
		<xsl:text>","</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="@name"/>
		</xsl:call-template>
		<xsl:text>","</xsl:text>
		<xsl:if test="m:comment">
			<xsl:call-template name="javascript-string">
				<xsl:with-param name="output-string" select="normalize-space(m:comment)"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:text>","</xsl:text>
		<xsl:value-of select="format-date(xs:date(substring-before(@creation-date,'T')),'[D01] [MNn,*-3] [Y0001]')"/>
		<xsl:text>",</xsl:text>
		<xsl:value-of select="@icon-width"/>
		<xsl:text>,</xsl:text>
		<xsl:value-of select="@icon-height"/>
		<xsl:text>,"</xsl:text>
		<xsl:value-of select="@mime"/>
		<xsl:text>"]</xsl:text>
		<xsl:if test="position() != last()">
			<xsl:text>,</xsl:text>
		</xsl:if>
	</xsl:template>
	
	<!-- 
		Generate <div> with error messages, if errors present.
		
		@param errors-node the x:errors element (optional)
	-->
	<xsl:template name="error-intro">
		<xsl:param name="errors-node" select="/x:x-data/x:x-errors"/>
		<xsl:if test="$errors-node/x:error">
			<div class="error-intro">
				<xsl:if test="$errors-node/x:error[not(@field)]">
					<!--<xsl:value-of select="$messages[@key='global.error.intro']"/>
						<xsl:text> </xsl:text>-->
					<xsl:apply-templates select="$errors-node/x:error[not(@field)]"/>
				</xsl:if>
				<xsl:if test="$errors-node/x:error[@field]">
					<xsl:value-of select="key('i18n','field.error.intro')"/>
					<ul>
						<xsl:for-each select="$errors-node/x:error[@field]">
							<li><xsl:value-of select="."/></li>
						</xsl:for-each>
					</ul>
				</xsl:if>
			</div>
		</xsl:if>
	</xsl:template>
	
	<!--
		Generate a server URL, eg. http://myhost
	-->
	<xsl:template name="server-url">
		<xsl:variable name="port" select="$ctx/x:server-port"/>
		<xsl:text>http</xsl:text>
		<xsl:if test="$port = '443'">
			<xsl:text>s</xsl:text>
		</xsl:if>
		<xsl:text>://</xsl:text>
		<xsl:value-of select="$ctx/x:server-name"/>
		<xsl:if test="$port != '80' and $port != '443'">
			<xsl:text>:</xsl:text>
			<xsl:value-of select="$port"/>
		</xsl:if>
	</xsl:template>
	
	<!--
		Generate the public absolute URL for viewing an album.
	-->
	<xsl:template match="m:album" mode="view.album.absolute.url">
		<xsl:call-template name="server-url"/>
		<xsl:apply-templates select="." mode="view.album.relative.url"/>
	</xsl:template>
	
	<!--
		Generate the public relative URL for viewing an album.
	-->
	<xsl:template match="m:album" mode="view.album.relative.url">
		<xsl:value-of select="$web-context"/>
		<xsl:text>/album.do?key=</xsl:text>
		<xsl:value-of select="@anonymous-key"/>
	</xsl:template>
	
	
	<!--
		Named Template: render-media-server-url
		
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
	<xsl:template name="render-media-server-url">
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
			<xsl:value-of select="$album-key"/>
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
	<xsl:template name="render-shared-album-url">
		<xsl:param name="album"/>
		<xsl:param name="user"/>
		<xsl:param name="web-context"/>
		<xsl:param name="item-id"/>
		<xsl:param name="mode"/>
		<xsl:value-of select="$web-context"/>
		<xsl:text>/album.do?key=</xsl:text>
		<xsl:value-of select="$album/@anonymous-key"/>
		<xsl:if test="string-length($album/@album-id) &lt; 1">
			<xsl:text>&amp;userKey=</xsl:text>
			<xsl:value-of select="$user/@anonymous-key"/>
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
		Named Template: render-file-size
		
		Generate text representation of the size of a file. For example:
		
		render-file-size(size = 14875) => 14.53 KB
		
		Parameters:
		size - an integer, assumed to be the number of bytes of the file
	-->
	<xsl:template name="render-file-size">
		<xsl:param name="size"/>
		<xsl:choose>
			<xsl:when test="$size &gt; 1048576">
				<xsl:value-of select="format-number($size div 1048576,'#,##0.##')"/>
				<xsl:text> MB</xsl:text>
			</xsl:when>
			<xsl:when test="$size &gt; 1024">
				<xsl:value-of select="format-number($size div 1024,'#,##0.##')"/>
				<xsl:text> KB</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="format-number($size div 1024,'#,##0')"/>
				<xsl:text> bytes</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
		Named Template: render-download-album-url
		
		Generate the URL to download an album.
		
		Parameters:
		key - the album anonymous key
		albumId - (optional) the album ID
		orig - if true, download original media items
	-->
	<xsl:template name="render-download-album-url">
		<xsl:param name="quality"/>
		<xsl:param name="size"/>
		<xsl:param name="download"/>
		<xsl:param name="album-key"/>
		<xsl:param name="original"/>
		<xsl:param name="web-context"/>
		<xsl:value-of select="$web-context"/>
		
		<xsl:text>/downloadAlbum.do?albumKey=</xsl:text>
		<xsl:value-of select="$album-key"/>
		<xsl:choose>
			<xsl:when test="$original">
				<xsl:text>&amp;original=true</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="$size">
					<xsl:text>&amp;size=</xsl:text>
					<xsl:value-of select="$size"/>
				</xsl:if>
				<xsl:if test="$quality">
					<xsl:text>&amp;quality=</xsl:text>
					<xsl:value-of select="$quality"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
		Named Template: render-i18n-options
		
		Render a set of <option> elements for a list of items, 
		using i18n keys for the display values.
		
		Parameters:
		content-key-prefix: the prefix for the i18n key values, to prepend to 
		each item in the value-list
		value-list:         a comma-delimited list of key values
		selected-value:     the value to mark as selected
		value-key-prefix:   an optional message key to prepend to every value
	-->
	<xsl:template name="render-i18n-options">
		<xsl:param name="content-key-prefix"/>
		<xsl:param name="selected-value"/>
		<xsl:param name="value-list"/>
		<xsl:param name="value-key-prefix"/>
		
		<xsl:variable name="first" select="substring-before($value-list,',')"/>
		<xsl:variable name="rest" select="substring-after($value-list,',')"/>
		
		<option  xmlns="http://www.w3.org/1999/xhtml" value="{$first}">
			<xsl:if test="$first = $selected-value">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="$value-key-prefix">
				<xsl:value-of select="key('i18n',$value-key-prefix)"/>
				<xsl:text> </xsl:text>
			</xsl:if>
			<xsl:value-of select="key('i18n',concat($content-key-prefix,$first))"/>
		</option>
		
		<xsl:if test="$rest">
			<xsl:call-template name="render-i18n-options">
				<xsl:with-param name="selected-value" select="$selected-value"/>
				<xsl:with-param name="value-list" select="$rest"/>
				<xsl:with-param name="content-key-prefix" select="$content-key-prefix"/>
				<xsl:with-param name="value-key-prefix" select="$value-key-prefix"/>
			</xsl:call-template>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template name="render-id3-genre">
		<xsl:param name="genre"/>
		<xsl:choose>
			<xsl:when test="starts-with($genre,'(')">
				<xsl:variable name="code" select="concat('id3.',substring-before(substring-after($genre,'('),')'))"/>
				<xsl:choose>
					<xsl:when test="key('i18n',$code)">
						<xsl:value-of select="key('i18n',$code)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$genre"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$genre"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
