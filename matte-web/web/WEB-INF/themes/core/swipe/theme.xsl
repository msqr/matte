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
	
	<xsl:variable name="display-album" select="x:x-data/x:x-model/m:model/m:album/descendant-or-self::m:album
		[@anonymous-key=key('aux-param','display.album.key')][1]"/>
	<xsl:variable name="root-album" select="x:x-data/x:x-model/m:model/m:album[1]"/>
	<xsl:variable name="album-hierarchy" select="x:x-data/x:x-model/m:model/m:search-results/m:album[1]"/>
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
	<xsl:variable name="theme" select="if (x:x-data/x:x-model/m:model/m:theme) 
		then x:x-data/x:x-model/m:model/m:theme 
		else $display-album/m:theme"/>
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
				<link href="http://fonts.googleapis.com/css?family={encode-for-uri('Alegreya:700,400|Alegreya Sans:400,300')}" rel="stylesheet" type="text/css"/>
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
							<xsl:if test="$album-hierarchy">
								<div class="dropdown">
               						<button data-toggle="dropdown" type="button" class="btn btn-default dropdown-toggle" id="album-dropdown">
               							<xsl:value-of select="key('i18n', 'albums.displayName')"/>
               							<xsl:text> </xsl:text>
               							<span class="caret"></span>
               						</button>
									<ul id="album-hierarchy" aria-labelledby="album-dropdown" role="menu">
										<xsl:apply-templates select="$album-hierarchy"/>
									</ul>
								</div>
							</xsl:if>
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
							<xsl:with-param name="resource" select="'js/swipe-jquery.js'"/>
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
			<xsl:text> </xsl:text>
			<small>
				<a id="play-slideshow" class="action" aria-label="{key('i18n', 'action.playSlideshow')}">
					<span class="glyphicon glyphicon-play-circle" aria-hidden="true"></span>
				</a>
			</small>
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
			<xsl:value-of select="m:item-count($total-item-count)"/>
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
				<span class="nowrap">
					<xsl:value-of select="format-date(xs:date(substring-before(@modify-date,'T')),$date.format)"/>
				</span>
			</p>
		</xsl:if>
	</xsl:template>
	
	<!-- Format an album's count of items into a string -->
	<xsl:function name="m:item-count" as="xs:string">
		<xsl:param name="count" as="xs:integer"/>
		<xsl:value-of select="concat($count, ' ', if ($count = 1) 
			then key('i18n','browse.items.count.single', $top) 
			else key('i18n','browse.items.count', $top))"/>
	</xsl:function>
	
	<xsl:template match="m:album" mode="title">
		<xsl:value-of select="@name"/>
		<xsl:if test="string-length(@name) &gt; 0">
			<xsl:text> - </xsl:text>
		</xsl:if>
		<xsl:value-of select="m:album-date(., $date.format)"/>
	</xsl:template>
	
	<!-- Render child albums -->
	<xsl:template match="m:search-album | m:search-results/m:album">
		<li>
			<h2>
				<a title="{concat(key('i18n','browse.album.view'), ' ', string(@name))}" 
					href="#{@anonymous-key}" class="{concat('child-album', if (local-name(.) eq 'album') then ' root' else ())}">
					<xsl:value-of select="@name"/>
				</a>
				<xsl:text> </xsl:text>
				<small class="nowrap"><xsl:value-of select="m:item-count(@item-count)"/></small>
			</h2>
			<xsl:if test="m:search-album">
				<ul>
					<xsl:apply-templates select="m:search-album"/>
				</ul>
			</xsl:if>
		</li>
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
			
			app.albumData = <xsl:apply-templates select="$display-album" mode="js-data"/>;
		</script>
	</xsl:template>
	
	<xsl:template match="m:album" mode="js-data">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#10;</xsl:text>
		</xsl:if>
		<xsl:text>{ </xsl:text>
		<xsl:text>albumId : </xsl:text><xsl:value-of select="@album-id"/>
		<xsl:text>, anonymousKey : </xsl:text><xsl:value-of select="m:js-string(@anonymous-key)"/>
		<xsl:text>, name : </xsl:text><xsl:value-of select="m:js-string(@name)"/>
		<xsl:if test="boolean(@allow-original) = true()">
			<xsl:text>, allowOriginal : true</xsl:text>
		</xsl:if>
		<xsl:text> }</xsl:text>
	</xsl:template>
	
	<xsl:template match="m:item" mode="js-data">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#10;</xsl:text>
		</xsl:if>
		<xsl:text>{ </xsl:text>
		<xsl:text>itemId : </xsl:text><xsl:value-of select="@item-id"/>
		<xsl:text>, width : </xsl:text><xsl:value-of select="@width"/>
		<xsl:text>, height : </xsl:text><xsl:value-of select="@height"/>
		<xsl:text>, name : </xsl:text><xsl:value-of select="m:js-string(@name)"/>
		<xsl:text>, path : </xsl:text><xsl:value-of select="m:js-string(@path)"/>
		<xsl:if test="m:description">
			<xsl:text>, description : </xsl:text><xsl:value-of select="m:js-string(normalize-space(m:description))"/>
		</xsl:if>
		<xsl:text>, date : </xsl:text><xsl:value-of select="m:js-string(m:item-date(., $date.format))"/>
		<xsl:if test="boolean(@use-icon) = true()">
			<xsl:text>, useIcon : true</xsl:text>
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
		                <div class="dropdown pswp__button item-actions">
			                <button type="button" class="dropdown-toggle" id="item-actions-dropdown" data-toggle="dropdown" aria-expanded="true">
			                	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
			                </button>
			                <ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="item-actions-dropdown">
								<li role="presentation" class="item-action-download">
									<a role="menuitem" tabindex="-1" title="{key('i18n', 'action.download.title')}">
										<xsl:value-of select="key('i18n', 'action.download')"/>
									</a>
								</li>
								<li role="presentation" class="item-action-download-original">
									<a role="menuitem" tabindex="-1" title="{key('i18n', 'action.download.original.title')}">
										<xsl:value-of select="key('i18n', 'action.download.original')"/>
									</a>
								</li>
							</ul>
						</div>
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
	
	