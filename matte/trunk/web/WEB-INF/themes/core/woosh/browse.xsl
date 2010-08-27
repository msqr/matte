<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x xs">
	
	<xsl:import href="../../theme-util.xsl"/>
	
	<xsl:output method="xml" indent="no" 
		omit-xml-declaration="no"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
		media-type="text/xml"/>
	
	<!-- helper vars -->
	<xsl:variable name="author" select="x:x-data/x:x-model[1]/m:model[1]/m:user[1]"/>
	<xsl:variable name="theme" select="x:x-data/x:x-model[1]/m:model[1]/m:theme[1]"/>
	<xsl:variable name="date.format" select="'[D] [MNn,*-3] [Y0001]'"/>
	<xsl:variable name="mode">
		<xsl:choose>
			<xsl:when test="$req[@key='mode']">
				<xsl:value-of select="$req[@key='mode']"/>
			</xsl:when>
			<xsl:otherwise>albums</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="years">
		<xsl:if test="$mode = 'albums'">
			<xsl:call-template name="browse-years">
				<xsl:with-param name="albums" select="$mod/m:model[1]/m:search-results/m:album"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="year">
		<xsl:if test="$mode = 'albums'">
			<xsl:choose>
				<xsl:when test="key('req-param','year') and contains($years, key('req-param','year'))">
					<xsl:value-of select="key('req-param','year')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="contains($years,' ')">
							<xsl:value-of select="substring-before($years,' ')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$years"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:variable>
	
	<xsl:variable name="single-quality">
		<xsl:choose>
			<xsl:when test="$ses/m:session[1]/m:view-setting/@quality">
				<xsl:value-of select="$ses/m:session[1]/m:view-setting/@quality"/>
			</xsl:when>
			<xsl:when test="$acting-user/m:view-setting/@quality">
				<xsl:value-of select="$acting-user/m:view-setting/@quality"/>
			</xsl:when>
			<xsl:otherwise>GOOD</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="single-size">
		<xsl:choose>
			<xsl:when test="$ses/m:session[1]/m:view-setting/@size">
				<xsl:value-of select="$ses/m:session[1]/m:view-setting/@size"/>
			</xsl:when>
			<xsl:when test="$acting-user/m:view-setting/@size">
				<xsl:value-of select="$acting-user/m:view-setting/@size"/>
			</xsl:when>
			<xsl:otherwise>NORMAL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="thumb-quality">
		<xsl:choose>
			<xsl:when test="$ses/m:session[1]/m:thumbnail-setting/@quality">
				<xsl:value-of select="$ses/m:session[1]/m:thumbnail-setting/@quality"/>
			</xsl:when>
			<xsl:when test="$acting-user/m:thumbnail-setting/@quality">
				<xsl:value-of select="$acting-user/m:thumbnail-setting/@quality"/>
			</xsl:when>
			<xsl:otherwise>AVERAGE</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="thumb-size">
		<xsl:choose>
			<xsl:when test="$ses/m:session[1]/m:thumbnail-setting/@size">
				<xsl:value-of select="$ses/m:session[1]/m:thumbnail-setting/@size"/>
			</xsl:when>
			<xsl:when test="$acting-user/m:thumbnail-setting/@size">
				<xsl:value-of select="$acting-user/m:thumbnail-setting/@size"/>
			</xsl:when>
			<xsl:otherwise>THUMB_NORMAL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!--
		Browse entry point
		
		This template is the entry point for the brose mode.
	-->
	<xsl:template match="x:x-data">
		<xsl:apply-templates select="x:x-model/m:model" mode="browse"/>
	</xsl:template>

	<!--
		Browse albums body.

		This template renders the main slideshow body.
	-->
	<xsl:template match="m:model" mode="browse" xml:space="default">
		<xsl:variable name="page.title">
			<xsl:value-of select="$author/@name"/>
			<xsl:value-of select="key('i18n','browse.author.posessive.suffix')"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="key('i18n','browse.album.title')"/>
		</xsl:variable>
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>
					<xsl:value-of select="$page.title"/>
				</title>
				<xsl:comment>
					thumb.size = <xsl:value-of select="$thumb-size"/>
					thumb.quality = <xsl:value-of select="$thumb-quality"/>
					xmlMode = <xsl:value-of select="contains(
						/x:x-data/x:x-request-headers/x:param[@key='accept'],
						'application/xhtml+xml')"/>
				</xsl:comment>
				<meta http-equiv="Content-Type" 
					content="text/html; charset=utf-8" />
				<link rel="alternate" type="application/atom+xml" lang="en-US">
					<xsl:attribute name="title">
						<xsl:value-of select="$author/@name"/>
						<xsl:value-of select="key('i18n','feed.author.posessive.suffix')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="key('i18n','feed.album.title')"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:call-template name="server-url"/>
						<xsl:value-of select="$web-context"/>
						<xsl:text>/feed/atom-1.0.do?userKey=</xsl:text>
						<xsl:value-of select="$author/@anonymous-key"/>
					</xsl:attribute>
				</link>
				<xsl:call-template name="get-css-link">
					<xsl:with-param name="theme" select="$theme"/>
					<xsl:with-param name="css" select="'woosh-browse.css'"/>
					<xsl:with-param name="web-context" select="$web-context"/>
				</xsl:call-template>
				<script type="text/javascript">
					var themeId = '<xsl:value-of select="$theme/@theme-id"/>';
					var webContext = '<xsl:value-of select="$web-context"/>';
					var serverName = '<xsl:value-of select="$server-name"/>';
					var serverPort = '<xsl:value-of select="$server-port"/>';
					var myLang = '<xsl:value-of select="$user-locale"/>';
					var userKey = '<xsl:value-of select="$author/@anonymous-key"/>';
					var thumbSpec = {
						size: '<xsl:value-of select="$thumb-size"/>',
						quality: '<xsl:value-of select="$thumb-quality"/>'
					}
					var xmlMode = <xsl:value-of select="contains(
						/x:x-data/x:x-request-headers/x:param[@key='accept'],
						'application/xhtml+xml')"/>;
					var mediaSizes = new Object();
					<xsl:for-each select="m:media-size">
						mediaSizes.<xsl:value-of select="@size"/> = {
						width: <xsl:value-of select="@width"/>,
						height: <xsl:value-of select="@height"/>
						}
						<xsl:if test="position() != last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'prototype.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'scriptaculous.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'effects.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'controls.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'builder.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'behaviour.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-dynamic-js-url">
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>	
					<xsl:text> </xsl:text>					
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'browse.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'yui/calendar.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</link>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'yui/yahoo-dom-event.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'yui/calendar-min.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href">
						<xsl:call-template name="get-dynamic-css-url">
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>	
				</link>
			</head>
			<body class="browse">
				<div id="browse-frame">
					<div class="frame-t">
						<img class="frame-tl" alt="frame">
							<xsl:attribute name="src">
								<xsl:call-template name="get-resource-url">
									<xsl:with-param name="resource" select="'img/tb_03.gif'"/>
									<xsl:with-param name="theme" select="$theme"/>
									<xsl:with-param name="web-context" select="$web-context"/>
								</xsl:call-template>
							</xsl:attribute>
						</img>
						<img class="frame-tr" alt="frame">
							<xsl:attribute name="src">
								<xsl:call-template name="get-resource-url">
									<xsl:with-param name="resource" select="'img/tb_06.gif'"/>
									<xsl:with-param name="theme" select="$theme"/>
									<xsl:with-param name="web-context" select="$web-context"/>
								</xsl:call-template>
							</xsl:attribute>							
						</img>
					</div>	
					<div>
						<h1>
							<xsl:value-of select="$page.title"/>
						</h1>
						<xsl:choose>
							<xsl:when test="$mode='albums'">
								<div class="browse-index">
									<xsl:call-template name="browse-years-links">
										<xsl:with-param name="years" select="$years"/>
										<xsl:with-param name="year" select="$year"/>
									</xsl:call-template>
									<xsl:call-template name="render-browse-modes-link"/>
									<span id="search-link" style="display: none;">
										<xsl:text> | </xsl:text>
										<span class="clickable">
											<xsl:value-of select="key('i18n','link.search')"/>
										</span>
									</span>
								</div>
								<div id="browse-modes" style="display: none;">
									<xsl:apply-templates select="m:ui-metadata[@key='browse-mode']"/>
								</div>
								<xsl:apply-templates select="m:search-results/m:album
									[substring-before(concat(@album-date,@creation-date),'-') = $year]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="m:search-results/m:index"/>
								<div id="browse-modes" style="display: none;">
									<xsl:apply-templates select="m:ui-metadata[@key='browse-mode']"/>
								</div>
								<xsl:apply-templates select="m:search-results/m:album"/>
							</xsl:otherwise>
						</xsl:choose>
						
						<div class="browse-clear"><xsl:text> </xsl:text></div>
					</div>
					<div class="frame-ml"><xsl:text> </xsl:text></div>
					<div class="frame-mr"><xsl:text> </xsl:text></div>
					<div class="frame-b">
						<img class="frame-bl" alt="frame">
							<xsl:attribute name="src">
								<xsl:call-template name="get-resource-url">
									<xsl:with-param name="resource" select="'img/tb_24.gif'"/>
									<xsl:with-param name="theme" select="$theme"/>
									<xsl:with-param name="web-context" select="$web-context"/>
								</xsl:call-template>
							</xsl:attribute>
						</img>
						<img class="frame-br" alt="frame">
							<xsl:attribute name="src">
								<xsl:call-template name="get-resource-url">
									<xsl:with-param name="resource" select="'img/tb_26.gif'"/>
									<xsl:with-param name="theme" select="$theme"/>
									<xsl:with-param name="web-context" select="$web-context"/>
								</xsl:call-template>
							</xsl:attribute>
						</img>
					</div>
				</div>
				<div id="search-frame" style="display: none;">
					<xsl:apply-templates select="." mode="search.frame"/>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="m:index">
		<div xmlns="http://www.w3.org/1999/xhtml" class="browse-index">
			<xsl:variable name="name-key">
				<xsl:text>browse.mode.</xsl:text>
				<xsl:value-of select="$mode"/>
				<xsl:text>.displayName</xsl:text>
			</xsl:variable>
			<xsl:value-of select="key('i18n',$name-key)"/>
			<xsl:text>: </xsl:text>
			<xsl:apply-templates select="m:index-section"/>
			<xsl:call-template name="render-browse-modes-link"/>
		</div>
	</xsl:template>
	
	<xsl:template name="render-browse-modes-link">
		<span xmlns="http://www.w3.org/1999/xhtml" id="browse-mode-link" style="display: none;">
			<xsl:text> | </xsl:text>
			<span class="clickable" title="{key('i18n','browse.modes.link.title')}">
				<xsl:value-of select="key('i18n','browse.modes.link')"/>
			</span>
		</span>
	</xsl:template>
	
	<xsl:template match="m:ui-metadata[@key='browse-mode']">
		<xsl:if test="position() &gt; 1">
			<xsl:text> | </xsl:text>
		</xsl:if>
		<xsl:variable name="msg-key">
			<xsl:text>browse.mode.</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>.displayName</xsl:text>
		</xsl:variable>
		<span xmlns="http://www.w3.org/1999/xhtml" id="browsemodelink-{.}">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="$mode = string(.)">
						<xsl:text>selected</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>clickable browse-mode-link</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="key('i18n',$msg-key)"/>
		</span>
	</xsl:template>
	
	<xsl:template match="m:index-section">
		<xsl:if test="position() &gt; 1">
			<xsl:text> | </xsl:text>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="@selected = 'true'">
				<span xmlns="http://www.w3.org/1999/xhtml" class="selected">
					<xsl:value-of select="@index-key"/>
				</span>
			</xsl:when>
			<xsl:otherwise>
				<a xmlns="http://www.w3.org/1999/xhtml">
					<xsl:attribute name="href">
						<xsl:value-of select="$web-context"/>
						<xsl:value-of select="$web-path"/>
						<xsl:text>?userKey=</xsl:text>
						<xsl:value-of select="key('req-param','userKey')"/>
						<xsl:text>&amp;mode=</xsl:text>
						<xsl:value-of select="$mode"/>
						<xsl:text>&amp;section=</xsl:text>
						<xsl:value-of select="@index-key"/>
					</xsl:attribute>
					<xsl:value-of select="@index-key"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:model" mode="search.frame">
		<!--div class="close-x">
			<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
		</div-->
		<form xmlns="http://www.w3.org/1999/xhtml" id="search-form" action="{$web-context}/find.do" method="post" class="simple-form">
			<div>
				<label for="quick-search">
					<xsl:value-of select="key('i18n','search.items.quick.displayName')"/>
				</label>
				<div>
					<input type="text" name="quickSearch" id="quick-search"/>
				</div>
			</div>
			<div>
				<label for="search-start-date">
					<xsl:value-of select="key('i18n', 'search.items.dateRange.displayName')"/>
				</label>
				<div>
					<input type="text" id="search-start-date" name="startDate" value=""/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="key('i18n', 'to')"/>
					<xsl:text> </xsl:text>
					<input type="text" name="endDate" id="search-end-date" value=""/>
					<img alt="{key('i18n','search.items.dateRange.calendar.title')}"
						id="date-range-calendar-toggle">
						<xsl:attribute name="src">
							<xsl:call-template name="get-resource-url">
								<xsl:with-param name="resource" select="'img/date.png'"/>
								<xsl:with-param name="theme" select="$theme"/>
								<xsl:with-param name="web-context" select="$web-context"/>
							</xsl:call-template>
						</xsl:attribute>
					</img>
					<div id="date-range-caption" class="caption">
						<xsl:value-of select="key('i18n','search.items.dateRange.caption')"
							disable-output-escaping="yes"/>
					</div>
					<div id="date-range-calendar-container" style="display: none;">
						<div id="start-date-calendar"><xsl:text> </xsl:text></div>
						<div id="end-date-calendar"><xsl:text> </xsl:text></div>
					</div>
				</div>
			</div>
			<div>
				<div class="submit">
					<input type="hidden" name="userAnonymousKey" value="{$author/@anonymous-key}"/>				
					<input value="{key('i18n','find.displayName')}" type="submit" />
				</div>
			</div>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		</form>
		
		<div xmlns="http://www.w3.org/1999/xhtml" id="search-results"/>
	</xsl:template>
	
	<xsl:template match="m:album">
		<xsl:variable name="is.odd" select="boolean(position() mod 2 = 1)"/>
		<xsl:variable name="oddness">
			<xsl:choose>
				<xsl:when test="$is.odd">
					<xsl:text>odd</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>even</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="album.date">
			<xsl:choose>
				<xsl:when test="@album-date">
					<xsl:value-of select="@album-date"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@creation-date"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="total-item-count" select="sum(.//@item-count)"/>
		<xsl:variable name="min-date">
			<xsl:for-each select=".//@item-min-date">
				<xsl:sort select="." order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="max-date">
			<xsl:for-each select=".//@item-max-date">
				<xsl:sort select="." order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="total-album-count" select="count(m:search-album) + 1"/>
		<div xmlns="http://www.w3.org/1999/xhtml" class="browse-album-frame {$oddness}">
			<div class="browse-{$oddness}">
				<h2>
					<xsl:value-of select="@name"/>
				</h2>
				<div class="browse-album-info">
					<xsl:if test="string-length($album.date) &gt; 0">
						<xsl:value-of select="format-date(xs:date(substring-before($album.date,'T')),$date.format)"/>
						<xsl:text> - </xsl:text>
					</xsl:if>
					<xsl:value-of select="$total-item-count"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="@total-item-count = 1">
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
						<span class="tolower">
							<xsl:value-of select="key('i18n','albums.displayName')"/>
						</span>
					</xsl:if>
					<xsl:if test="@modify-date">
						<xsl:text> - </xsl:text>
						<xsl:value-of select="key('i18n','browse.album.lastupdated')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="format-date(xs:date(substring-before(@modify-date,'T')),$date.format)"/>
					</xsl:if>
					<xsl:if test="@item-count &gt; 0 and $min-date != $max-date">
						<div class="browse-album-info">
							<xsl:value-of select="key('i18n', 'browse.items.itemrange')"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="format-date(xs:date(substring-before($min-date,'T')), $date.format)"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="key('i18n', 'to')"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="format-date(xs:date(substring-before($max-date,'T')), $date.format)"/>
						</div>
					</xsl:if>
				</div>
				<xsl:if test="string-length(m:comment) &gt; 0">
					<div class="browse-album-text">
						<xsl:value-of select="m:comment"/>
					</div>
				</xsl:if>
			</div>
			<xsl:apply-templates select="m:search-poster"/>
		</div>
		<xsl:if test="position() != last()">
			<div xmlns="http://www.w3.org/1999/xhtml" class="album-sep"><xsl:text> </xsl:text></div>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template match="m:search-poster">
		<xsl:variable name="oddness">
			<xsl:choose>
				<xsl:when test="count(../preceding-sibling::m:album[substring-before(concat(@album-date,@creation-date),'-') = $year]) mod 2 = 0">
					<xsl:text>odd</xsl:text>
				</xsl:when>
				<xsl:otherwise>even</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<div xmlns="http://www.w3.org/1999/xhtml" class="poster-{$oddness}">
			<a>
				<xsl:attribute name="title">
					<xsl:value-of select="key('i18n','browse.album.view')"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="../@name"/>
				</xsl:attribute>
				<xsl:attribute name="href">
					<xsl:call-template name="render-shared-album-url">
						<xsl:with-param name="album" select=".."/>
						<xsl:with-param name="user" select="$author"/>
						<xsl:with-param name="mode" select="$mode"/>
						<xsl:with-param name="web-context" select="$web-context"/>
					</xsl:call-template>
				</xsl:attribute>
				<img class="poster {$oddness}" alt="{@name}" onload="setShadow(this)" 
					title="{@name}">
					<xsl:attribute name="src">
						<xsl:call-template name="server-url"/>
						<xsl:call-template name="render-media-server-url">
							<xsl:with-param name="item" select="."/>
							<xsl:with-param name="size" select="'THUMB_BIGGER'"/>
							<xsl:with-param name="quality" select="$thumb-quality"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
			</a>
		</div>
	</xsl:template>

	<xsl:template name="browse-years">
		<xsl:param name="albums"/>
		<xsl:param name="years"/>
		<xsl:variable name="album.year">
			<xsl:choose>
				<xsl:when test="$albums[1]/@album-date">
					<xsl:value-of select="substring-before($albums[1]/@album-date,'-')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring-before($albums[1]/@creation-date,'-')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="album.years">
			<xsl:choose>
				<xsl:when test="contains($years,$album.year)">
					<xsl:value-of select="$years"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$years"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$album.year"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="count($albums) &gt; 1">
				<xsl:call-template name="browse-years">
					<xsl:with-param name="albums" select="$albums[position() != 1]"/>
					<xsl:with-param name="years" select="$album.years"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="normalize-space($album.years)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
		browse-years-links: generate list of years as links to browse
	-->
	<xsl:template name="browse-years-links">
		<xsl:param name="years"/>
		<xsl:param name="year"/>
		<xsl:variable name="sel.year">
			<xsl:choose>
				<xsl:when test="contains($years,' ')">
					<xsl:value-of select="substring-before($years,' ')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$years"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$sel.year = $year">
				<span xmlns="http://www.w3.org/1999/xhtml" class="selected">
					<xsl:value-of select="$sel.year"/>
				</span>
			</xsl:when>
			<xsl:otherwise>
				<a xmlns="http://www.w3.org/1999/xhtml">
					<xsl:attribute name="href">
						<xsl:value-of select="$web-context"/>
						<xsl:value-of select="$web-path"/>
						<xsl:text>?userKey=</xsl:text>
						<xsl:value-of select="key('req-param','userKey')"/>
						<xsl:text>&amp;year=</xsl:text>
						<xsl:value-of select="$sel.year"/>
					</xsl:attribute>
					<xsl:value-of select="$sel.year"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="contains($years,' ')">
			<xsl:text> | </xsl:text>
			<xsl:call-template name="browse-years-links">
				<xsl:with-param name="years" select="substring-after($years,' ')"/>
				<xsl:with-param name="year" select="$year"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
