<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date"
	extension-element-prefixes="date">
	
	<xsl:import href="../../theme-util.xsl"/>
	
	<!-- NOTE: omit-xml-declaration must be "yes" for any result destined
		for AJAX response into browser running in XHTM mode (Firefox)
		otherwise .innerHTML called by Prototype will fail -->
	
	<xsl:output method="xml" indent="no" 
		omit-xml-declaration="yes"
		media-type="text/xml"/>
	
	<!-- standard data vars -->
	<xsl:variable name="ctx" select="x:x-data/x:x-context"/>
	<xsl:variable name="req" select="x:x-data/x:x-request/x:param"/>
	<xsl:variable name="aux" select="x:x-data/x:x-auxillary"/>
	<xsl:variable name="err" select="x:x-data/x:x-errors/x:error[@field]"/>
	
	<!-- message resource bundle defined as key for quick lookup -->
	<xsl:key name="i18n" match="x:x-data/x:x-msg/x:msg" use="@key"/>
	
	<!-- auxillaray params defined as key for quick lookup -->
	<xsl:key name="aux-param" match="x:x-data/x:x-auxillary/x:x-param" use="@key"/>
	
	<!-- request params defined as key for quick lookup -->
	<xsl:key name="req-param" match="x:x-data/x:x-request/x:param" use="@key"/>
	
	<!-- helper vars -->
	<xsl:variable name="server-name" select="string($ctx/x:server-name)"/>
	<xsl:variable name="server-port" select="string($ctx/x:server-port)"/>
	<xsl:variable name="user-locale" select="string($ctx/x:user-locale)"/>
	<xsl:variable name="web-context" select="string($ctx/x:web-context)"/>
	<xsl:variable name="acting-user" select="x:x-data/x:x-session/m:session/m:acting-user"/>
	
	<xsl:key name="item-meta" 
		match="x:x-data/x:x-model[1]/m:model[1]/m:item[1]/m:metadata" use="@key"/>
	
	<xsl:variable name="display-album" select="x:x-data/x:x-model/m:model/descendant::m:album
		[@album-id=key('aux-param','display.album.id')][1]"/>
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
	<xsl:variable name="theme" select="(x:x-data/x:x-model/m:model/m:theme | $display-album/m:theme)[1]"/>
	<xsl:variable name="page-size" select="5"/>
	<xsl:variable name="max-page" select="ceiling(count($display-album/m:item) div $page-size)"/>
	
	<!-- FIXME: place view-setting and thumbnail setting into model -->
	<xsl:variable name="single-quality">
		<xsl:choose>
			<xsl:when test="$acting-user/m:view-setting/@quality">
				<xsl:value-of select="$acting-user/m:view-setting/@quality"/>
			</xsl:when>
			<xsl:otherwise>GOOD</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="single-size">
		<xsl:choose>
			<xsl:when test="$acting-user/m:view-setting/@size">
				<xsl:value-of select="$acting-user/m:view-setting/@size"/>
			</xsl:when>
			<xsl:otherwise>NORMAL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="thumb-quality">
		<xsl:choose>
			<xsl:when test="$acting-user/m:thumbnail-setting/@quality">
				<xsl:value-of select="$acting-user/m:thumbnail-setting/@quality"/>
			</xsl:when>
			<xsl:otherwise>AVERAGE</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="thumb-size">
		<xsl:choose>
			<xsl:when test="$acting-user/m:thumbnail-setting/@size">
				<xsl:value-of select="$acting-user/m:thumbnail-setting/@size"/>
			</xsl:when>
			<xsl:otherwise>THUMB_NORMAL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!--
		Info entry point
	-->
	<xsl:template match="x:x-data">
		<xsl:apply-templates select="x:x-model/m:model" mode="meta"/>
	</xsl:template>

	<!--
		Slideshow metadata body.
		
		This template renders the meta data for a single slideshow item.
	-->
	<xsl:template match="m:model" mode="meta">
		<xsl:variable name="meta-item" select="m:item[1]"/>
		<xsl:variable name="meta-item-type-name">
			<xsl:variable name="name-key">
				<xsl:text>mime.</xsl:text>
				<xsl:value-of select="substring-before($meta-item/@mime,'/')"/>
				<xsl:text>.displayName</xsl:text>
			</xsl:variable>
			<xsl:value-of select="key('i18n',$name-key)"/>
		</xsl:variable>
		<div class="ii-content">
			<h2 id="ii-imageTitle">
				<xsl:choose>
					<xsl:when test="$meta-item/@name">
						<xsl:value-of select="$meta-item/@name"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$meta-item/@path"/>
					</xsl:otherwise>
				</xsl:choose>
			</h2>
			
			<div id="ii-imageDate">
				<xsl:choose>
					<xsl:when test="string-length($meta-item/@item-date) &gt;= 19">
						<xsl:value-of select="date:format-date(substring($meta-item/@item-date, 1, 19), 
							'd MMM yyyy H:mm')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="date:format-date(substring($meta-item/@creation-date, 1, 19), 
							'd MMM yyyy H:mm')"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="(not($acting-user/m:tz/@code = $meta-item/m:tz/@code))
					or not($meta-item/m:tz-display and $acting-user/m:tz/@code = $meta-item/m:tz-display/@code)">
					<!-- Display the TZ, as it is different from the user's time zone -->
					<xsl:text> (</xsl:text>
					<xsl:choose>
						<xsl:when test="$meta-item/m:tz-display">
							<xsl:value-of select="$meta-item/m:tz-display/@name"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$meta-item/m:tz/@name"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>)</xsl:text>
				</xsl:if>
			</div>
			
			<div id="ii-imageComment">
				<xsl:choose>
					<xsl:when test="string-length($meta-item/m:description) &gt; 0">
						<xsl:value-of select="$meta-item/m:description"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text> </xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			
			<div id="ii-EXIF">
				<h3>Extra Information</h3>
				<xsl:apply-templates select="$meta-item" mode="extra-info"/>
			</div>
			
			<div id="ii-downloadOptions">
				<h3><xsl:value-of select="key('i18n','actions.displayName')"/></h3>
				<ul>
					<!-- if MIME is an image, MediaServer can scale for download... -->
					<xsl:if test="starts-with($meta-item/@mime,'image')">
						<li>
							<a title="{key('i18n','action.download.title')}">
								<xsl:attribute name="href">
									<xsl:call-template name="render-media-server-url">
										<xsl:with-param name="item" select="$meta-item"/>
										<xsl:with-param name="album-key" select="m:album[1]/@anonymous-key"/>
										<xsl:with-param name="size" select="$single-size"/>
										<xsl:with-param name="quality" select="$single-quality"/>
										<xsl:with-param name="download" select="true()"/>
										<xsl:with-param name="web-context" select="$web-context"/>
									</xsl:call-template>
								</xsl:attribute>
								<xsl:value-of select="key('i18n','action.download')"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="$meta-item-type-name"/>
							</a>
						</li>
					</xsl:if>
					<xsl:if test="m:album[1]/@allow-original = 'true'">
						<li>
							<a title="{key('i18n','action.download.original.title')}">
								<xsl:attribute name="href">
									<xsl:call-template name="render-media-server-url">
										<xsl:with-param name="item" select="$meta-item"/>
										<xsl:with-param name="album-key" select="m:album[1]/@anonymous-key"/>
										<xsl:with-param name="download" select="true()"/>
										<xsl:with-param name="original" select="true()"/>
										<xsl:with-param name="web-context" select="$web-context"/>
									</xsl:call-template>
								</xsl:attribute>
								<xsl:value-of select="key('i18n','action.download.original')"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="$meta-item-type-name"/>
							</a> 
							<xsl:text> (</xsl:text>
							<xsl:call-template name="render-file-size">
								<xsl:with-param name="size" select="$meta-item/@file-size"/>
							</xsl:call-template>
							<xsl:text>)</xsl:text>
						</li>
					</xsl:if>
					<li>
						<div class="a" id="download-album-link"
							title="{key('i18n','action.download.album.form.title')}">
							<xsl:value-of select="key('i18n','action.download.album.form')"/>
						</div>
					</li>
					<li>
						<div class="a" id="add-comment-link"
							title="{key('i18n','action.add.comment.form.title')}">
							<xsl:value-of select="key('i18n','action.add.comment.form')"/>
						</div>
					</li>
					<!--
						<xsl:text> (</xsl:text>
						<xsl:call-template name="get-album-items-total-size">
						<xsl:with-param name="album" select="$meta-item/.."/>
						</xsl:call-template>
						<xsl:text>)</xsl:text>
					-->
				</ul>
			</div>
			
			<xsl:if test="$meta-item/m:user-comment">
				<div id="ii-comments">
					<h3>Comments</h3>
					<xsl:apply-templates select="$meta-item/m:user-comment" mode="meta"/>
				</div>
			</xsl:if>
			
			<xsl:if test="$meta-item/m:user-rating">
				<div id="ii-ratings">
					<h3>User Ratings</h3>
					<xsl:apply-templates select="$meta-item/m:user-rating" mode="meta"/>
					<xsl:variable name="num-ratings" select="count($meta-item/m:user-rating)"/>
					<xsl:if test="$num-ratings &gt; 1">
						<div class="sep"></div>
						
						<span class="ii-attribute">
							<xsl:text>Average (from </xsl:text>
							<xsl:value-of select="$num-ratings"/>
							<xsl:text> ratings): </xsl:text>
						</span>
						<xsl:value-of select="format-number(
							sum($meta-item/m:user-rating/@rating) div $num-ratings, '0.#')"/>
						<br />
					</xsl:if>
				</div>
			</xsl:if>
		</div>
	</xsl:template>
	
	<!--
		Render the album name and date as the page title and banner
	-->
	<xsl:template match="m:album" mode="title">
		<xsl:value-of select="@name"/>
		<xsl:if test="string-length(@name) &gt; 0">
			<xsl:text> - </xsl:text>
		</xsl:if>
		<xsl:variable name="date">
			<xsl:choose>
				<xsl:when test="@modify-date">
					<xsl:value-of select="@album-date"/>
				</xsl:when>
				<xsl:when test="@modify-date">
					<xsl:value-of select="@modify-date"/>
				</xsl:when>
				<xsl:when test="@creation-date">
					<xsl:value-of select="@creation-date"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($date,'.')">
				<xsl:value-of select="date:format-date(substring-before($date,'.'), 'yyyy.MM.dd')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="date:format-date($date, 'yyyy.MM.dd')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:model" mode="head-common">
		<xsl:call-template name="get-css-link">
			<xsl:with-param name="theme" select="$theme"/>
			<xsl:with-param name="css" select="'woosh.css'"/>
			<xsl:with-param name="web-context" select="$web-context"/>
			<!--xsl:with-param name="ua-type" select="$ua-type"/>
			<xsl:with-param name="ua-version" select="$ua-version"/>
			<xsl:with-param name="ua-platform" select="$ua-platform"/-->
		</xsl:call-template>
		<xsl:call-template name="get-css-link">
			<xsl:with-param name="theme" select="$theme"/>
			<xsl:with-param name="css" select="'woosh-common.css'"/>
			<xsl:with-param name="web-context" select="$web-context"/>
		</xsl:call-template>
		<script type="text/javascript">
			var webContext = '<xsl:value-of select="$web-context"/>';
			var serverName = '<xsl:value-of select="$server-name"/>'
			var serverPort = '<xsl:value-of select="$server-port"/>'
			var myLang = '<xsl:value-of select="$user-locale"/>'
		</script>
	</xsl:template>
	
	<xsl:template name="render-slider-page-contents">
		<xsl:param name="idx"/>
		<div class="tb-thumbBox"><a href="#"/></div>
		<xsl:if test="$idx &lt; $page-size">
			<xsl:call-template name="render-slider-page-contents">
				<xsl:with-param name="idx" select="$idx +1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:item" mode="extra-info">
		<xsl:apply-templates select="." mode="extra-extra-info"/>
		<xsl:if test="m:user-tag">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.tags')"/>: </span>
			<xsl:for-each select="m:user-tag">
				<xsl:if test="position() &gt; 1">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:value-of select="m:tag"/>
			</xsl:for-each>
			<br/>
		</xsl:if>
		<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.hits')"/>: </span>
		<xsl:choose>
			<xsl:when test="@hits &gt; 0">
				<xsl:value-of select="@hits"/>
				<xsl:text> </xsl:text>
				<xsl:choose>
					<xsl:when test="@hits &gt; 1">
						<xsl:value-of select="key('i18n','meta.hits.times')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n','meta.hits.time')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="key('i18n','meta.hits.never')"/>
			</xsl:otherwise>
		</xsl:choose>
		<br/>
	</xsl:template>
	
	<xsl:template match="m:item[starts-with(@mime,'audio')]" mode="extra-extra-info">
		<!-- FIXME needs to be updated -->
		<xsl:if test="m:metadata[@key='SONG_NAME']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.songName')"/>: </span>
			<xsl:value-of select="m:metadata[@key='SONG_NAME']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='ARTIST']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.artist')"/>: </span>
			<xsl:value-of select="m:metadata[@key='ARTIST']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='ALBUM']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.album')"/>: </span>
			<xsl:value-of select="m:metadata[@key='ALBUM']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='TRACK_NUM']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.track')"/>: </span>
			<xsl:value-of select="m:metadata[@key='TRACK_NUM']"/>
			<xsl:if test="m:metadata[@key='TRACK_TOTAL']">
				<xsl:text> </xsl:text>
				<xsl:value-of select="key('i18n','meta.of')"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="m:metadata[@key='TRACK_TOTAL']"/>
			</xsl:if>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='GENRE']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.genre')"/>: </span>
			<xsl:call-template name="render-id3-genre">
				<xsl:with-param name="genre" select="string(m:metadata[@key='GENRE'])"/>
			</xsl:call-template>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='AUDIO_FORMAT']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.format')"/>: </span>
			<xsl:value-of select="m:metadata[@key='AUDIO_FORMAT']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='BIT_RATE']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.bitRate')"/>: </span>
			<xsl:value-of select="m:metadata[@key='BIT_RATE']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='SAMPLE_RATE']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.sampleRate')"/>: </span>
			<xsl:value-of select="m:metadata[@key='SAMPLE_RATE']"/>
			<br />
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:item[starts-with(@mime,'video')]" mode="extra-extra-info">
		<xsl:if test="m:metadata[@key='DURATION_TIME']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.video.duration')"/>: </span>
			<xsl:value-of select="m:metadata[@key='DURATION_TIME']"/>
			<br />
		</xsl:if>
		<xsl:if test="@width &gt; 0 and @height &gt; 0">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.dimensions')"/>: </span>
			<xsl:value-of select="@width"/>
			<xsl:text> x </xsl:text>
			<xsl:value-of select="@height"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='FPS']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.video.fps')"/>: </span>
			<xsl:value-of select="format-number(m:metadata[@key='FPS'],'#0.#')"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='VIDEO_FORMAT']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.video.format')"/>: </span>
			<xsl:value-of select="m:metadata[@key='VIDEO_FORMAT']"/>
			<br />
		</xsl:if>
		<xsl:if test="m:metadata[@key='AUDIO_FORMAT']">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','meta.audio.format')"/>: </span>
			<xsl:value-of select="m:metadata[@key='AUDIO_FORMAT']"/>
			<br />
		</xsl:if>
		<xsl:if test="not(m:metadata[@key='VIDEO_FORMAT'] or m:metadata[@key='AUDIO_FORMAT'])">
			<span class="ii-attribute"><xsl:value-of select="key('i18n','mime.displayName')"/>: </span>
			<xsl:value-of select="@mime"/>
			<br />
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:item[starts-with(@mime,'image')]" mode="extra-extra-info">
		<!-- FIXME add ISO to meta
			<span class="ii-attribute">ISO: </span>100<br />
		-->
		<xsl:choose>
			<xsl:when test="key('item-meta','EXPOSURE_TIME')">
				<span class="ii-attribute">Shutter: </span>
				<xsl:value-of select="key('item-meta','EXPOSURE_TIME')"/>
				<xsl:text>s</xsl:text>
				<br />
			</xsl:when>
			<xsl:when test="key('item-meta','SHUTTER_SPEED')">
				<span class="ii-attribute">Shutter: </span>
				<xsl:value-of select="key('item-meta','SHUTTER_SPEED')"/>
				<br />
			</xsl:when>
		</xsl:choose>
		<xsl:if test="key('item-meta','F_STOP')">
			<span class="ii-attribute">F-Stop: </span>
			<xsl:value-of select="key('item-meta','F_STOP')"/>
			<br />
		</xsl:if>
		<xsl:if test="key('item-meta','FOCAL_LENGTH')">
			<span class="ii-attribute">Focal length: </span>
			<xsl:value-of select="key('item-meta','FOCAL_LENGTH')"/>
			<xsl:text>mm</xsl:text>
			<xsl:if test="key('item-meta','FOCAL_LENGTH_35MM_EQUIV')">
				<xsl:text> (</xsl:text>
				<xsl:value-of select="key('item-meta','FOCAL_LENGTH_35MM_EQUIV')"/>
				<xsl:text> @ 35mm)</xsl:text>
			</xsl:if>
			<br />
		</xsl:if>
		<!--
			<span class="ii-attribute">Exposure Bias: </span>+0.5<br />
		-->
		<xsl:if test="m:metadata[@key='CAMERA_MODEL']">
			<span class="ii-attribute">Camera: </span>
			<xsl:value-of select="m:metadata[@key='CAMERA_MODEL']"/>
			<br />
		</xsl:if>
		<span class="ii-attribute">File: </span><xsl:value-of select="@path"/><br />
		<xsl:if test="@width &gt; 0">
			<span class="ii-attribute">Original size: </span>
			<xsl:value-of select="@width"/>
			<xsl:text> x </xsl:text>
			<xsl:value-of select="@height"/>
			<br />
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:user-rating" mode="meta">
		<span class="ii-attribute">
			<xsl:choose>
				<xsl:when test="m:rating-user">
					<xsl:value-of select="m:rating-user/@name"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="key('i18n','anonymous.user')"/>
				</xsl:otherwise>
			</xsl:choose>
		</span>
		<xsl:text>: </xsl:text>
		<xsl:value-of select="@rating"/>
		<em>
			<xsl:text> (</xsl:text>
			<xsl:value-of select="date:format-date(substring(@creation-date,1,19),'d MMM yyyy, h:mm a')"/>
			<xsl:text>)</xsl:text>
		</em>
		<br />
	</xsl:template>
	
	<xsl:template match="m:user-comment" mode="meta">
		<span class="ii-attribute">
			<xsl:choose>
				<xsl:when test="m:commenting-user">
					<xsl:value-of select="m:commenting-user/@name"/>
				</xsl:when>
				<xsl:when test="string-length(@commenter) &gt; 0">
					<xsl:value-of select="@commenter"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="key('i18n','anonymous.user')"/>
				</xsl:otherwise>
			</xsl:choose>
		</span>
		<xsl:text>: </xsl:text>
		<xsl:value-of select="m:comment"/>
		<em>
			<xsl:text> (</xsl:text>
			<xsl:value-of select="date:format-date(substring(@creation-date,1,19),'d MMM yyyy, h:mm a')"/>
			<xsl:text>)</xsl:text>
		</em>
		<br />
	</xsl:template>
	
</xsl:stylesheet>
	
	