<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x xs">
	
	<xsl:import href="../../standard-variables.xsl"/>
	<xsl:import href="../../string-utils.xsl"/>
	<xsl:import href="../../url-utils.xsl"/>
	
	<xsl:output method="html" version="5.0" indent="yes" omit-xml-declaration="yes"/>
	
	<xsl:key name="item-meta" 
		match="x:x-data/x:x-model[1]/m:model[1]/m:item[1]/m:metadata" use="@key"/>
	
	<xsl:variable name="display-album" select="x:x-data/x:x-model/m:model/descendant::m:album
		[@anonymous-key=key('aux-param','display.album.key')][1]"/>
	<xsl:variable name="root-album" select="x:x-data/x:x-model/m:model/descendant::m:album[1]"/>
	<xsl:variable name="display-item-id">
		<xsl:choose>
			<xsl:when test="$display-album/m:item[@item-id=key('aux-param','display.item.id')]">
				<xsl:value-of select="key('aux-param','display.item.id')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$display-album/m:item[1]/@item-id"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="display-item" select="$display-album/m:item[@item-id = $display-item-id]"/>
	<xsl:variable name="author" select="x:x-data/x:x-model[1]/m:model[1]/m:user[1]"/>
	<xsl:variable name="theme" select="(x:x-data/x:x-model/m:model/m:theme | $display-album/m:theme)[1]"/>
	<xsl:variable name="date.format" select="'[D] [MNn,*-3] [Y0001]'"/>
	
	<xsl:variable name="browse-mode" select="string($req[@key='mode'])"/>
	<xsl:variable name="user-key"  select="string($req[@key='userKey'])"/>
	
	<!--
		Slideshow entry point
		
		This template is the entry point for the album mode. It decides 
		either to display the main body content or the meta data 
		content.
	-->
	<xsl:template match="x:x-data">
		<xsl:apply-templates select="x:x-model/m:model" mode="album"/>
	</xsl:template>

	<!--
		Slideshow album body.

		This template renders the main slideshow body.
	-->
	<xsl:template match="m:model" mode="album" xml:space="default">
		<html>
			<head>
				<title>
					<xsl:apply-templates select="$display-album" mode="title"/>
				</title>
				<meta charset="utf-8" />
				<meta name="viewport" content="width=device-width, initial-scale=1" />
				<link href="http://fonts.googleapis.com/css?family={encode-for-uri('Alice|Alegreya:700,400|Alegreya Sans:400,300')}" rel="stylesheet" type="text/css"/>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'css/bootstrap.min.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'photoswipe/photoswipe.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'photoswipe/default-skin/default-skin.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'css/swipe-mosaic.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'css/browse.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'css/album.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
			</head>
			<body>
				<div class="container-fluid">
					<div class="row">
						<div class="col-md-3 album-details">
							<xsl:apply-templates select="$display-album"/>
						</div>
						<div class="col-md-9">
							<div class="mosaic"></div>
						</div>
					</div>
				</div>
				
				<xsl:apply-templates select="." mode="photoswipe"/>
				
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'js/jquery.min.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'js/bootstrap.min.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'photoswipe/photoswipe.min.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'photoswipe/photoswipe-ui-default.min.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'js/swipe-mosaic.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="theme-resource-url">
							<xsl:with-param name="resource" select="'js/album.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
				<xsl:apply-templates select="." mode="js-data"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="m:album">
		<xsl:variable name="album-date" select="m:album-date(., $date.format)"/>
		<h1>
			<xsl:value-of select="@name"/>
			<xsl:if test="string-length(@name) &gt; 0">
				<xsl:text> </xsl:text>
				<small class="nowrap"><xsl:value-of select="$album-date"/></small>
			</xsl:if>
		</h1>
		<xsl:if test="m:comment">
			<p><xsl:value-of select="m:comment"/></p>
		</xsl:if>
		<xsl:variable name="total-item-count" select="count(m:item)"/>
		<xsl:variable name="item-dates" as="xs:string*">
			<xsl:for-each select="for $item in m:item return 
					(if ($item/@item-date) then string($item/@item-date) else string($item/@creation-date))">
				<xsl:sort select="." order="ascending"/>
				<xsl:sequence select="."/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="min-date" select="$item-dates[1]"/>
		<xsl:variable name="max-date" select="$item-dates[position() eq last()]"/>
		<xsl:variable name="total-album-count" select="count(m:album) + 1"/>
		<p class="album-info">
			<xsl:value-of select="$total-item-count"/>
			<xsl:text> </xsl:text>
			<xsl:choose>
				<xsl:when test="$total-item-count = 1">
					<xsl:value-of select="key('i18n','browse.items.count.single')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="key('i18n','browse.items.count')"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="$total-album-count &gt; 1">
				<xsl:text> </xsl:text>
				<xsl:value-of select="key('i18n','in')"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="$total-album-count"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="lower-case(key('i18n','albums.displayName'))"/>
			</xsl:if>
			<xsl:if test="$total-item-count &gt; 0 and $min-date != $max-date">
				<xsl:text>, </xsl:text>
				<xsl:value-of select="key('i18n', 'browse.items.ranging')"/>
				<xsl:text> </xsl:text>
				<span class="nowrap">
					<xsl:value-of select="format-date(xs:date(substring-before($min-date,'T')), $date.format)"/>
				</span>
				<xsl:text> </xsl:text>
				<xsl:value-of select="key('i18n', 'to')"/>
				<xsl:text> </xsl:text>
				<span class="nowrap">
					<xsl:value-of select="format-date(xs:date(substring-before($max-date,'T')), $date.format)"/>
				</span>			
			</xsl:if>
		</p>
		<xsl:if test="@modify-date">
			<p class="album-info">
				<xsl:value-of select="key('i18n','browse.album.lastupdated')"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="format-date(xs:date(substring-before(@modify-date,'T')),$date.format)"/>
			</p>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:album" mode="title">
		<xsl:value-of select="@name"/>
		<xsl:if test="string-length(@name) &gt; 0">
			<xsl:text> - </xsl:text>
		</xsl:if>
		<xsl:value-of select="m:album-date(., $date.format)"/>
	</xsl:template>
	
	<xsl:template match="m:album" mode="child-albums">
		<li>
			<a href="{$web-context}/album.do?key={$root-album/@anonymous-key}&amp;childKey={@anonymous-key}">
				<xsl:value-of select="@name"/>
			</a>
		</li>
	</xsl:template>

	<xsl:template match="m:album" mode="album-crumbs">
		<xsl:if test="position() &gt; 1">
			<xsl:text> &#187; </xsl:text>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="$display-album = .">
				<span class="current"><xsl:value-of select="@name"/></span>
			</xsl:when>
			<xsl:otherwise>
				<a href="{$web-context}/album.do?key={$root-album/@anonymous-key}&amp;childKey={@anonymous-key}">
					<xsl:value-of select="@name"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:model" mode="js-data">
		<script type="text/javascript">
			'use strict';
			
			if ( 'app' in window === false ) {
				window.app = {};
			}
			
			app.config = {
				themeId : '<xsl:value-of select="$theme/@theme-id"/>',
				webContext : '<xsl:value-of select="$web-context"/>',
				serverName : '<xsl:value-of select="$server-name"/>',
				serverPort : '<xsl:value-of select="$server-port"/>',
				myLang : '<xsl:value-of select="$user-locale"/>',
				
				albumKey : '<xsl:value-of select="$root-album/@anonymous-key"/>',
				browseMode : '<xsl:value-of select="$browse-mode"/>',
				userKey : '<xsl:value-of select="$user-key"/>',

				thumbSpec : {
					size: '<xsl:value-of select="$thumb-size"/>',
					quality: '<xsl:value-of select="$thumb-quality"/>'
				},
				singleSpec : {
					size: '<xsl:value-of select="$single-size"/>',
					quality: '<xsl:value-of select="$single-quality"/>'
				},
				
				specs : {
					<xsl:for-each select="m:media-size">
						<xsl:if test="position() &gt; 1">
							<xsl:text>,&#10;</xsl:text>
						</xsl:if>
						<xsl:value-of select="@size"/>
						<xsl:text> : { width : </xsl:text><xsl:value-of select="@width"/>
						<xsl:text>, height: </xsl:text><xsl:value-of select="@height"/>
						<xsl:text> }</xsl:text>
					</xsl:for-each>
				}
			};
			
			app.imageData = [
			<xsl:apply-templates select="$display-album/m:item" mode="js-data"/>
			];
		</script>
	</xsl:template>
	
	<xsl:template match="m:item" mode="js-data">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#10;</xsl:text>
		</xsl:if>
		<xsl:text>{ </xsl:text>
		<xsl:text>id : </xsl:text><xsl:value-of select="@item-id"/>
		<xsl:text>, w : </xsl:text><xsl:value-of select="@width"/>
		<xsl:text>, h : </xsl:text><xsl:value-of select="@height"/>
		<xsl:text>, name : </xsl:text><xsl:value-of select="m:js-string(@name)"/>
		<xsl:if test="m:comment">
			<xsl:text>, comment : </xsl:text><xsl:value-of select="m:js-string(normalize-space(m:comment))"/>
		</xsl:if>
		<xsl:text>, date : </xsl:text><xsl:value-of select="m:js-string(m:item-date(., $date.format))"/>
		<xsl:if test="@icon-width">
			<xsl:text>, iw : </xsl:text><xsl:value-of select="@icon-width"/>
		</xsl:if>
		<xsl:if test="@icon-height">
			<xsl:text>, ih : </xsl:text><xsl:value-of select="@icon-height"/>
		</xsl:if>
		<xsl:text>, mime : </xsl:text><xsl:value-of select="m:js-string(@mime)"/>
		<xsl:text> }</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:model" mode="photoswipe">
		<div id="pswp" class="pswp" tabindex="-1" role="dialog" aria-hidden="true">
		    <div class="pswp__bg"></div>
		    <div class="pswp__scroll-wrap">
		        <div class="pswp__container">
		            <div class="pswp__item"></div>
		            <div class="pswp__item"></div>
		            <div class="pswp__item"></div>
		        </div>
		        <div class="pswp__ui pswp__ui--hidden">
		            <div class="pswp__top-bar">
		                <div class="pswp__counter"></div>
		                <button class="pswp__button pswp__button--close" title="Close (Esc)"></button>
		                <button class="pswp__button pswp__button--share" title="Share"></button>
		                <button class="pswp__button pswp__button--fs" title="Toggle fullscreen"></button>
		                <button class="pswp__button pswp__button--zoom" title="Zoom in/out"></button>
		                <div class="pswp__preloader">
		                    <div class="pswp__preloader__icn">
		                      <div class="pswp__preloader__cut">
		                        <div class="pswp__preloader__donut"></div>
		                      </div>
		                    </div>
		                </div>
		            </div>
		            <div class="pswp__share-modal pswp__share-modal--hidden pswp__single-tap">
		                <div class="pswp__share-tooltip"></div> 
		            </div>
		            <button class="pswp__button pswp__button--arrow--left" title="Previous (arrow left)"></button>
		            <button class="pswp__button pswp__button--arrow--right" title="Next (arrow right)"></button>
		            <div class="pswp__caption">
		                <div class="pswp__caption__center"></div>
		            </div>
		        </div>
		    </div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>
	
	