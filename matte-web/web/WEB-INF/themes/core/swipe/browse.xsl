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
	
	<!-- helper vars -->
	<xsl:variable name="author" select="x:x-data/x:x-model[1]/m:model[1]/m:user[1]"/>
	<xsl:variable name="theme" select="x:x-data/x:x-model[1]/m:model[1]/m:theme[1]"/>
	<xsl:variable name="date.format" select="'[D] [MNn,*-3] [Y0001]'"/>
	<xsl:variable name="mode" select="if ($req[@key='mode']) then $req[@key='mode'] else 'albums'"/>
	
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
		<html>
			<head>
				<title>
					<xsl:value-of select="$page.title"/>
				</title>
				<meta charset="utf-8" />
				<meta name="viewport" content="width=device-width, initial-scale=1" />
				<link rel="alternate" type="application/atom+xml" lang="en-US">
					<xsl:attribute name="title">
						<xsl:value-of select="$author/@name"/>
						<xsl:value-of select="key('i18n','feed.author.posessive.suffix')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="key('i18n','feed.album.title')"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="$server-url"/>
						<xsl:value-of select="$web-context"/>
						<xsl:text>/feed/atom-1.0.do?userKey=</xsl:text>
						<xsl:value-of select="$author/@anonymous-key"/>
					</xsl:attribute>
				</link>
				<link href="http://fonts.googleapis.com/css?family={encode-for-uri('Alegreya:700,400|Alegreya Sans:500,400,300')}" rel="stylesheet" type="text/css"/>
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
							<xsl:with-param name="resource" select="'css/browse.css'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</link>
				<script type="text/javascript">
					var app = {};
					app.config = {
						themeId : '<xsl:value-of select="$theme/@theme-id"/>',
						webContext : '<xsl:value-of select="$web-context"/>',
						serverName : '<xsl:value-of select="$server-name"/>',
						serverPort : '<xsl:value-of select="$server-port"/>',
						myLang : '<xsl:value-of select="$user-locale"/>',
						userKey : '<xsl:value-of select="$author/@anonymous-key"/>',
						thumbSpec : {
							size: '<xsl:value-of select="$thumb-size"/>',
							quality: '<xsl:value-of select="$thumb-quality"/>'
						},
						sections : [<xsl:value-of select='string-join(for $key in m:search-results/m:index/m:index-section/@index-key
							return concat("""", $key, """"), ",")'/>]
					};
				</script>
			</head>
			<body>
				<div class="container heading">
					<div class="row">
						<div class="col-xs-9 col-sm-10 col-md-11">
							<h1><xsl:value-of select="$page.title"/></h1>
							<div class="browse-index">
								<xsl:apply-templates select="m:search-results/m:index/m:index-section" mode="section-index-link"/>
							</div>
						</div>
						<div class="col-xs-3 col-sm-2 col-md-1 text-right">
							<div class="dropdown">
								<button class="btn btn-default dropdown-toggle" type="button" id="browse-mode-dropdown" data-toggle="dropdown" aria-expanded="true">
									<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
								</button>
								<ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="browse-mode-dropdown">
									<li role="presentation" class="dropdown-header">
										<xsl:value-of select="key('i18n','browse.modes.link')"/>
									</li>
									<xsl:apply-templates select="m:ui-metadata[@key='browse-mode']"/>
								</ul>
							</div>
						</div>
					</div>
				</div>
				
				<!-- Render search form -->
				<div class="container" id="search-container">
					<xsl:apply-templates select="." mode="search-form"/>
				</div>
				
				<!-- Render albums -->
				<div class="container" id="albums-container">
					<xsl:apply-templates select="m:search-results/m:album"/>
				</div>
				
				<!-- Render download album modal -->
				<xsl:apply-templates select="." mode="album-download-modal"/>
				
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
							<xsl:with-param name="resource" select="'js/browse.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</script>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="m:ui-metadata[@key='browse-mode']">
		<li role="presentation">
			<a role="menuitem" tabindex="-1">
				<xsl:attribute name="href" select="concat($web-context, $web-path, 
						'?userKey=', key('req-param','userKey'),
						'&amp;mode=', string(.))"/>
				<xsl:if test="$mode = string(.)">
					<xsl:attribute name="class" select="'selected'"/>
				</xsl:if>
				<xsl:value-of select="key('i18n',concat('browse.mode.', string(.), '.displayName'))"/>
			</a>
		</li>
	</xsl:template>
	
	<xsl:template match="m:index-section" mode="section-index-link">
		<xsl:if test="position() > 1">
			<span class="sep"> · </span>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="@selected = 'true'">
				<span class="selected"><xsl:value-of select="@index-key"/></span>
			</xsl:when>
			<xsl:otherwise>
				<a>
					<xsl:attribute name="href" select="concat($web-context, $web-path, 
							'?userKey=', key('req-param','userKey'),
							'&amp;mode=', $mode,
							'&amp;section=', @index-key)"/>
					<xsl:value-of select="@index-key"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:album">
		<xsl:variable name="album-date" select="m:album-date(., $date.format)"/>
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
		<div class="row browse-album">
			<div class="col-md-4 text-center">
				<xsl:apply-templates select="m:search-poster"/>
			</div>		
			<div class="col-md-8">
				<div class="row">
					<h2 class="col-xs-10">
						<a title="{concat(key('i18n','browse.album.view'), ' ', string(@name))}">
							<xsl:attribute name="href">
								<xsl:call-template name="shared-album-url">
									<xsl:with-param name="album" select="."/>
									<xsl:with-param name="user" select="$author"/>
									<xsl:with-param name="mode" select="$mode"/>
									<xsl:with-param name="web-context" select="$web-context"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:value-of select="@name"/>
						</a>
					</h2>
					<div class="col-xs-2 album-actions text-right">
						<a class="download-album" href="{concat($web-context, '/downloadItems.do')}" 
							data-album-key="{@anonymous-key}"
							title="{key('i18n', 'action.download.album.form.title')}">
							<span class="glyphicon glyphicon-download"></span>
						</a>
					</div>
				</div>
				<div class="album-info">
					<xsl:if test="string-length($album-date) &gt; 0">
						<xsl:value-of select="$album-date"/>
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
						<xsl:value-of select="lower-case(key('i18n','albums.displayName'))"/>
					</xsl:if>
					<xsl:if test="@item-count &gt; 0 and $min-date != $max-date">
						<xsl:text> </xsl:text>
						<xsl:value-of select="key('i18n', 'browse.items.ranging')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="format-date(xs:date(substring-before($min-date,'T')), $date.format)"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="key('i18n', 'to')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="format-date(xs:date(substring-before($max-date,'T')), $date.format)"/>
					</xsl:if>
					<xsl:if test="@modify-date">
						<xsl:text> - </xsl:text>
						<xsl:value-of select="key('i18n','browse.album.lastupdated')"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="format-date(xs:date(substring-before(@modify-date,'T')),$date.format)"/>
					</xsl:if>
				</div>
				<xsl:if test="string-length(m:comment) &gt; 0">
					<p>
						<xsl:value-of select="m:comment"/>
					</p>
				</xsl:if>
			</div>
		</div>
		<xsl:if test="position() != last()">
			<div class="row">
				<div class="col-md-6 col-md-offset-3 browse-album-sep"></div>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:search-poster">
		<a title="{concat(key('i18n','browse.album.view'), ' ', ../@name)}">
			<xsl:attribute name="href">
				<xsl:call-template name="shared-album-url">
					<xsl:with-param name="album" select=".."/>
					<xsl:with-param name="user" select="$author"/>
					<xsl:with-param name="mode" select="$mode"/>
					<xsl:with-param name="web-context" select="$web-context"/>
				</xsl:call-template>
			</xsl:attribute>
			<img class="poster" alt="{@name}">
				<xsl:attribute name="src">
					<xsl:call-template name="server-url"/>
					<xsl:call-template name="media-server-url">
						<xsl:with-param name="item" select="."/>
						<xsl:with-param name="size" select="'THUMB_BIGGER'"/>
						<xsl:with-param name="quality" select="$thumb-quality"/>
						<xsl:with-param name="web-context" select="$web-context"/>
					</xsl:call-template>
				</xsl:attribute>
			</img>
		</a>
	</xsl:template>
	
	<xsl:template match="m:model" mode="album-download-modal">
		<form class="modal fade form-horizontal" id="album-download-modal" action="{concat($web-context, '/downloadAlbum.do')}" method="post">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-label="{key('i18n', 'close')}">
							<span aria-hidden="true">&#xD7;</span>
						</button>
						<h4 class="modal-title"><xsl:value-of select="key('i18n', 'action.download.album.form')"/></h4>
					</div>
					<div class="modal-body">
						<div class="form-group">
							<label class="col-xs-2 control-label" for="album-download-size">
								<xsl:value-of select="key('i18n', 'mediaspec.size.displayName')"/>
							</label>
							<div class="col-xs-10">
					            <select name="size" class="form-control" id="album-download-size" aria-describedby="album-download-size-help">
					            	<xsl:apply-templates select="m:media-size" mode="album-download-modal"/>
					            </select>
					            <div id="album-download-size-help" class="help-block"><xsl:value-of select="key('i18n', 'download.selected.items.size.caption')"/></div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-2 control-label" for="album-download-quality">
								<xsl:value-of select="key('i18n', 'mediaspec.quality.displayName')"/>
							</label>
							<div class="col-xs-10">
					            <select name="quality" class="form-control" id="album-download-quality" aria-describedby="album-download-quality-help">
					            	<xsl:for-each select="('HIGHEST', 'HIGH', 'GOOD', 'AVERAGE', 'LOW')">
					            		<option value="{.}">
					            			<xsl:if test="$single-quality = .">
					            				<xsl:attribute name="selected">selected</xsl:attribute>
					            			</xsl:if>
					            			<xsl:value-of select="key('i18n', concat('mediaspec.quality.', .), $top)"/>
					            		</option>
					            	</xsl:for-each>
					            </select>
					            <div id="album-download-quality-help" class="help-block"><xsl:value-of select="key('i18n', 'download.selected.items.quality.caption')"/></div>
							</div>
						</div>
						
						<div class="form-group">
							<div class="col-xs-offset-2 col-xs-10">
								<div class="checkbox">
									<label>
										<input type="checkbox" name="original" value="true"/>
										<xsl:text> </xsl:text>
										<xsl:value-of select="key('i18n', 'download.originals.caption')"/>
									</label>
								</div>
							</div>
						</div>
						
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">
							<xsl:value-of select="key('i18n', 'close')"/>
						</button>
						<button type="submit" class="btn btn-primary">
							<xsl:value-of select="key('i18n', 'link.download.album')"/>
						</button>
					</div>
					<input type="hidden" name="albumKey"/>
					<input type="hidden" name="direct" value="true"/>
				</div>
			</div>
		</form>
	</xsl:template>
	
	<xsl:template match="m:media-size" mode="album-download-modal">
		<option value="{@size}">
			<xsl:if test="$single-size = string(@size)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="key('i18n', concat('mediaspec.size.', @size))"/>
		</option>
	</xsl:template>
	
	<xsl:template match="m:model" mode="search-form">
		<div class="row">
			<form class="col-xs-12 form-inline" id="search-form" method="get" 
				action="{concat($web-context,'/api/v1/media/search')}">
				<div class="input-group">
					<span class="input-group-addon glyphicon glyphicon-search"></span>
					<input type="search" class="form-control" name="query"/>
				</div>
				<button type="submit" class="btn btn-primary">
					<xsl:value-of select="key('i18n','search.displayName')"/>
				</button>
				<input type="hidden" name="userKey" value="{$author/@anonymous-key}"/>
			</form>		
		</div>
		<div class="row" id="search-results-none">
			<div class="col-xs-12">
				<xsl:value-of select="key('i18n', 'search.results.none')"/>
			</div>
		</div>
		<div class="row" id="search-results">
			<div class="col-xs-12" id="search-results-container">
				<xsl:value-of select="key('i18n', 'search.results.placeholder')"/>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>
