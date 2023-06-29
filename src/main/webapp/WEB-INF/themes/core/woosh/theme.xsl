<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xpath-default-namespace="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="m x xs">
	
	<xsl:import href="../../theme-util.xsl"/>
	
	<xsl:output method="xml" indent="no" 
		omit-xml-declaration="no"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
		media-type="text/xml"/>
	
	<!-- turn off indentation because IE doesn't exactly ignore whitespace>
		<xsl:output method="html" indent="no" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/-->
	
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
	
	<!--xsl:param name="album-mode"/>
	<xsl:param name="user-agent"/>
	<xsl:param name="url-path"/>
	<xsl:param name="web-context"/>
	<xsl:param name="server-name"/-->
	
	<!--xsl:variable name="thumbCompress" select="/*/m:settings/m:thumbnail/@compress"/>
	<xsl:variable name="thumbSize">t<xsl:value-of select="/*/m:settings/m:thumbnail/@size"/></xsl:variable>
	<xsl:variable name="thumbMaxWidth" select="number(/*/m:settings/m:thumbnail/@width)"/>
	<xsl:variable name="thumbMaxHeight" select="number(/*/m:settings/m:thumbnail/@height)"/>
	<xsl:variable name="thumbMaxRatio" select="$thumbMaxHeight div $thumbMaxWidth"/>
	
	<xsl:variable name="singleCompress" select="/*/m:settings/m:single/@compress"/>
	<xsl:variable name="singleSize" select="/*/m:settings/m:single/@size"/>
	<xsl:variable name="singleMaxWidth" select="number(/*/m:settings/m:single/@width)"/>
	<xsl:variable name="singleMaxHeight" select="number(/*/m:settings/m:single/@height)"/>
	
	<xsl:variable name="rootAlbum" select="/*/m:album[1]"/>
	<xsl:variable name="displayAlbumId" select="/*/@displayAlbum"/>
	<xsl:variable name="displayAlbum" select="//m:album[@albumId = $display-albumId]"/>
	<xsl:variable name="item" select="//m:item[parent::m:album][1]"/>
	<xsl:variable name="user" select="/*/m:user[1]"/>
	<xsl:variable name="userId" select="/*/m:user[1]/@userId"/-->
	
	<!--xsl:variable name="ua-type">
		<xsl:call-template name="get-browser-type">
			<xsl:with-param name="ua" select="$user-agent"/>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:variable name="ua-platform">
		<xsl:call-template name="get-browser-platform">
			<xsl:with-param name="ua" select="$user-agent"/>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:variable name="ua-version">
		<xsl:call-template name="get-browser-version">
			<xsl:with-param name="ua" select="$user-agent"/>
		</xsl:call-template>
	</xsl:variable-->
	
	<!--xsl:variable name="msg" select="/*/x-msg/msg"/-->
	
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
	<xsl:variable name="theme" select="(x:x-data/x:x-model[1]/m:model[1]/m:theme[1], $display-album/m:theme)[1]"/>
	<xsl:variable name="page-size" select="5"/>
	<xsl:variable name="max-page" select="ceiling(count($display-album/m:item) div $page-size)"/>
	
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
	
	<xsl:variable name="browse-mode" select="string($req[@key='mode'])"/>
	<xsl:variable name="user-key"  select="string($req[@key='userKey'])"/>
	
	<!--xsl:variable name="selected-item-id" select="$req[@key='item']"/>
	<xsl:variable name="displayItemId">
		<xsl:choose>
			<xsl:when test="$mitem">
				<xsl:value-of select="$mitem"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$item/@itemId"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="displayItem" select="$display-album/m:item[@itemId = $displayItemId]"/-->

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
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>
					<xsl:apply-templates select="$display-album" mode="title"/>
				</title>
				<!-- FIXME meta http-equiv="Content-Type" 
					content="application/xhtml+xml; charset=utf-8" /-->
				<meta http-equiv="Content-Type" 
					content="text/html; charset=utf-8" />
				<xsl:comment>
					thumb.size = <xsl:value-of select="$thumb-size"/>
					thumb.quality = <xsl:value-of select="$thumb-quality"/>
					view.size = <xsl:value-of select="$single-size"/>
					view.quality = <xsl:value-of select="$single-quality"/>
				</xsl:comment>
				
				<script type="text/javascript">
					var haveQuickTime = false;
				</script>
				<script type="text/vbscript">
					On Error Resume Next
					Set theObject = CreateObject("QuickTimeCheckObject.QuickTimeCheck.1")
					On Error goto 0
					If IsObject(theObject) Then
						If theObject.IsQuickTimeAvailable(0) Then
							haveQuickTime = true
						End If
					End If
				</script>
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'ac/qt-test.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'ac/browserdetect.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
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
							<xsl:with-param name="resource" select="'ac/ac_quicktime.js'"/>
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
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'builder.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				
				<xsl:apply-templates select="." mode="head-common"/>
				
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
							<xsl:with-param name="resource" select="'imageKeyNavigation.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				
				<script type="text/javascript">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'slideshow.js'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:text> </xsl:text>
				</script>
				<xsl:apply-templates select="." mode="js-data"/>
			</head>
			<body class="woosh" id="woosh-body">
	
				<div id="system-working" style="display: none;">
					<xsl:text>Working...</xsl:text>
				</div>

				<!--div id="main-frame"-->
				<!--div id="controls-frame"-->
				<div id="tb-frame">
	
				<!-- content_box -->
				<div id="contentBox">
	
				<!-- two slider pages -->
				<div id="slider_page_a" style="position:absolute; width:468px; left:4px; top: 10px; border: 0px solid #000;">
					<xsl:call-template name="render-slider-page-contents">
						<xsl:with-param name="idx" select="1"/>
					</xsl:call-template>
				</div>
				<div id="slider_page_b" style="position:absolute; width:468px; left:4px; top: -400px; border: 0px solid #000;">
					<xsl:call-template name="render-slider-page-contents">
						<xsl:with-param name="idx" select="1"/>
					</xsl:call-template>
				</div>
				<div id="slider_page_c" style="position:absolute; width:468px; left:4px; top: -400px; border: 0px solid #000;">
					<xsl:call-template name="render-slider-page-contents">
						<xsl:with-param name="idx" select="1"/>
					</xsl:call-template>
				</div>
	
				</div> <!-- contentBox -->
	
				<img id="tb-tl" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_03.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-tm" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_05.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-tr" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_06.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-lm" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_18.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
	
				<img id="tb-rm" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_23.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-bl" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_24.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-bm" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_27.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				<img id="tb-br" alt="frame">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_26.gif'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
	
				<div id="tb-indexSwitcher">
					<xsl:call-template name="render-index-links">
						<xsl:with-param name="page" select="1"/>
						<xsl:with-param name="selected-page" select="1"/>
						<!--xsl:with-param name="theme" select="$theme"/-->
					</xsl:call-template>
				</div>
				
				<div id="tb-infoToggle">
					<!--a href="javascript:viewLightboxToggleDialog();" title="Show/Hide [L]ightbox">l</a>
					<xsl:text> | </xsl:text>
					<a href="javascript:addCommentToggleDialog();" title="Show/Hide Comment Dialog">c</a>
					<xsl:text> | </xsl:text-->
					<span class="a" id="link-toggle-info" title="Show/Hide [I]nfo Panel">i</span>
					<xsl:text> - </xsl:text>
					<span class="a" id="link-toggle-help" title="Show/Hide [H]elp">?</span>
				</div>
	
					<div id="tb-lsbContainer" title="Page [B]ack">
						<xsl:text> </xsl:text>
					</div>
					<div id="tb-rsbContainer" title="Page [F]orward">
						<xsl:text> </xsl:text>
					</div>
				<img alt="thumbnail mark" id="tb-thumbnailMark" title="Current Image. Options: [N]ext, [P]revious">
					<xsl:attribute name="src">
						<xsl:call-template name="get-resource-url">
							<xsl:with-param name="resource" select="'img/tb_13.png'"/>
							<xsl:with-param name="theme" select="$theme"/>
							<xsl:with-param name="web-context" select="$web-context"/>
						</xsl:call-template>
					</xsl:attribute>
				</img>
				</div><!-- tb-frame -->
	
				<!--/div>< controls -->
				
				<div id="image-frame">
					<img id="image-content" alt="photograph" title="{$display-item/@name}">
						<xsl:attribute name="src">
							<xsl:call-template name="render-media-server-url">
								<xsl:with-param name="item" select="$display-item"/>
								<xsl:with-param name="album-key" select="$display-album/@anonymous-key"/>
								<xsl:with-param name="size" select="$single-size"/>
								<xsl:with-param name="quality" select="$single-quality"/>
								<xsl:with-param name="web-context" select="$web-context"/>
							</xsl:call-template>
						</xsl:attribute>
					</img>
				</div>
	
				<!--/div> main-frame -->
	
				<div id="ii-frame">
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
					<div class="frame-m" id="ii-middleFrame">
						<div id="ii-contentFrame">
							<xsl:attribute name="class">
								<xsl:text>ii-content</xsl:text>
								<xsl:if test="not($root-album = $display-album) or $display-album/m:album">
									<xsl:text> with-child</xsl:text>
								</xsl:if>
							</xsl:attribute>
							<h1>Album: <xsl:value-of select="$display-album/@name"/></h1>
							<xsl:if test="not($root-album = $display-album)">
								<!-- display album bread crumbs by selecting descentands of root-album who have display-album as a descendant -->
								<div class="album-crumbs">
									<xsl:apply-templates select="descendant-or-self::m:album[descendant-or-self::m:album
										[@album-id = $display-album/@album-id]]" mode="album-crumbs"/>
								</div>
							</xsl:if>
							<xsl:if test="$display-album/m:album">
								<!-- display child albums -->
								<div class="child-albums">
									<table>
										<tr>
											<td>
												<ul>
													<xsl:choose>
														<xsl:when test="count($display-album/m:album) = 1">
															<xsl:apply-templates select="$display-album/m:album" mode="child-albums"/>
														</xsl:when>
														<xsl:otherwise>
															<xsl:apply-templates select="$display-album/m:album[not(position() &gt; (ceiling(count($display-album/m:album) div 2)))]"
																mode="child-albums"/>
														</xsl:otherwise>
													</xsl:choose>
												</ul>
											</td>
											<xsl:if test="count($display-album/m:album) &gt; 1">
												<td>
													<ul>
														<xsl:apply-templates select="$display-album/m:album[position() &gt; (ceiling(count($display-album/m:album) div 2))]"
															mode="child-albums"/>
													</ul>
												</td>
											</xsl:if>
										</tr>
									</table>
								</div>
							</xsl:if>
						</div>
						<xsl:variable name="metaItemId" select="$display-item/@item-id"/>
						<div id="ii-imageMetaFrame">
							<xsl:comment>
								<xsl:value-of select="$web-context"/>
								<xsl:text>/viewMediaItemInfo.do?key=</xsl:text>
								<xsl:value-of select="$root-album/@anonymous-key"/>
								<xsl:text>&amp;itemId=</xsl:text>
								<xsl:value-of select="$metaItemId"/>
								<xsl:text>&amp;themeId=</xsl:text>
								<xsl:value-of select="$display-album/m:theme[1]/@theme-id"/>
							</xsl:comment>
						</div>	
					</div>
					<div class="frame-ml">
						<xsl:text> </xsl:text>
					</div>
					<div class="frame-mr">
						<xsl:text> </xsl:text>
					</div>
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
	
				<!-- ADD COMMENT DIALOG >
	
				<div id="add-comment-dialog" class="dialog-content">
					<form name="addCommentForm" method="POST" action="{$web-context}/AddItemComment.do"
						onsubmit="return addCommentSubmit(this)">
						<input type="hidden" name="key" value="{$display-album/@anonymousKey}"/>
						<input type="hidden" name="mitem" value="{$display-item/@item-id}"/>
						<table class="form-table">
							<tr>
								<th>Comment:</th>
								<td>
									<textarea name="comment" rows="3" cols="40" onfocus="focusComments()" onblur="blurComments()"></textarea>
								</td>
							</tr>
							<tr>
								<td colspan="2"><br /></td>
							</tr>
							<tr>
								<td></td>
								<td>
									<input class="button" type="submit" value="Add Comment"/>
									<xsl:text> </xsl:text>
									<input class="button" type="submit" value="Cancel" onclick="cancelAddComment = true;"/>
								</td>
							</tr>
						</table>
					</form>
				</div>
				<div id="add-comment-shadow" class="dialog-shadow"/-->
				
				<div id="dialog-pane" style="display: none;">
					<div class="close-x">
						<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
					</div>
					<div id="dialog-content-pane" class="dialog-box">
						<xsl:text> </xsl:text>
					</div>
				</div>
				
				<div id="ui-elements" style="display: none;">

					<div id="service-dialog-container">
						<xsl:text> </xsl:text>
					</div>
					
				</div>
				
				<!-- HELP DIALOG -->
				
				<div id="help-dialog" style="display: none;">
					<div class="close-x">
						<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
					</div>
					<div class="dialog-box">
						<h3 class="dialog-title">
							<xsl:value-of select="key('i18n','woosh.help')"/>
						</h3>
						<p>
							Click on the small thumbnail images to view the fullsize
							version of that image. Click the gray arrows in the top-left
							<img>
								<xsl:attribute name="src">
									<xsl:call-template name="get-resource-url">
										<xsl:with-param name="resource" select="'img/tb_08_1.png'"/>
										<xsl:with-param name="theme" select="$theme"/>
										<xsl:with-param name="web-context" select="$web-context"/>
									</xsl:call-template>
								</xsl:attribute>
							</img>
							
							and top-right 
							
							<img>
								<xsl:attribute name="src">
									<xsl:call-template name="get-resource-url">
										<xsl:with-param name="resource" select="'img/tb_10_1.png'"/>
										<xsl:with-param name="theme" select="$theme"/>
										<xsl:with-param name="web-context" select="$web-context"/>
									</xsl:call-template>
								</xsl:attribute>
							</img>
		
							of the thumbnail image browser to view the 
							next or previous page of thumbnails. You can also click on 
							the small dots 
							
							<img>
								<xsl:attribute name="src">
									<xsl:call-template name="get-resource-url">
										<xsl:with-param name="resource" select="'img/tb_13_1.png'"/>
										<xsl:with-param name="theme" select="$theme"/>
										<xsl:with-param name="web-context" select="$web-context"/>
									</xsl:call-template>
								</xsl:attribute>
							</img>
		
							centered above the thumbnails to jump between
							the thumbnail pages.
						</p>
						<p>
						You can add comments to images by clicking the small <b>c</b> near the <b>?</b>
						that you clicked to show this help.
						</p>
						<p>
						You can add show or hide the Information Panel (the area on the right displaying
						the image name, date, etc.) by clicking the small <b>i</b> near the <b>?</b>
						that you clicked to show this help.
						</p>
						<p>
						If you would like to download the album or a single image to your computer, you can 
						do so by clicking the <b><xsl:value-of select="key('i18n','action.download.album')"/></b> or 
							<b><xsl:value-of select="key('i18n','action.download')"/></b>
						links in the Information Panel.
						</p>
						<p>
						Finally, you can use the following keyboard shortcuts (just tap the letter 
						for the task you'd like to perform):
						</p>
						<table class="data-table"><tbody>
							<tr>
								<th>n</th>
								<td>show next image (to the right)</td>
								<th>h</th>
								<td>show/hide this help</td>
							</tr>
							<tr>
								<th>p</th>
								<td>show previous image (to the left)</td>
								<th>i</th>
								<td>show/hide the Information Panel</td>
							</tr>
							<tr>
								<th>f</th>
								<td>show next thumbnail page (to the right)</td>
								<th><!--c--></th>
								<td><!--show/hide the Add Comment dialog--></td>
							</tr>
							<tr>
								<th>b</th>
								<td>show previous thumbnail page (to the left)</td>
								<th></th>
								<td></td>
							</tr>
							<tr>
								<th>1-5</th>
								<td>show the fullsize image for thumbnail 1 - 5</td>
								<th></th>
								<td></td>
							</tr>
						</tbody></table>
					</div>
				</div>
				
			</body>
		</html>
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
				<xsl:when test="@album-date">
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
		<xsl:value-of select="format-date(xs:date(substring-before($date, 'T')), '[Y0001].[M01].[D01]')"/>
	</xsl:template>
	
	<xsl:template match="m:album" mode="child-albums">
		<li xmlns="http://www.w3.org/1999/xhtml">
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
				<span xmlns="http://www.w3.org/1999/xhtml" class="current"><xsl:value-of select="@name"/></span>
			</xsl:when>
			<xsl:otherwise>
				<a xmlns="http://www.w3.org/1999/xhtml" href="{$web-context}/album.do?key={$root-album/@anonymous-key}&amp;childKey={@anonymous-key}">
					<xsl:value-of select="@name"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:model" mode="head-common">
		<!--xsl:call-template name="get-css-link">
			<xsl:with-param name="theme" select="$theme"/>
			<xsl:with-param name="css" select="'woosh.css'"/>
			<xsl:with-param name="web-context" select="$web-context"/>
			<xsl:with-param name="ua-type" select="$ua-type"/>
			<xsl:with-param name="ua-version" select="$ua-version"/>
			<xsl:with-param name="ua-platform" select="$ua-platform"/>
		</xsl:call-template-->
		<xsl:call-template name="get-css-link">
			<xsl:with-param name="theme" select="$theme"/>
			<xsl:with-param name="css" select="'woosh.css'"/>
			<xsl:with-param name="web-context" select="$web-context"/>
		</xsl:call-template>
		<script xmlns="http://www.w3.org/1999/xhtml" type="text/javascript">
			var webContext = '<xsl:value-of select="$web-context"/>';
			var serverName = '<xsl:value-of select="$server-name"/>'
			var serverPort = '<xsl:value-of select="$server-port"/>'
			var myLang = '<xsl:value-of select="$user-locale"/>'
		</script>
	</xsl:template>
	
	<xsl:template name="render-slider-page-contents">
		<xsl:param name="idx"/>
		<div xmlns="http://www.w3.org/1999/xhtml" class="tb-thumbBox"><a href="#">
			<xsl:text> </xsl:text>
		</a></div>
		<xsl:if test="$idx &lt; $page-size">
			<xsl:call-template name="render-slider-page-contents">
				<xsl:with-param name="idx" select="$idx +1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:model" mode="js-data">
		<script xmlns="http://www.w3.org/1999/xhtml" type="text/javascript">
			var themeId = <xsl:value-of select="$theme/@theme-id"/>;
			var slider_width = 468;
			var max_step = 40;
			var min_step = 5;
			var slider_step = max_step;
			var slider_speed = 20;
			var pageSize = <xsl:value-of select="$page-size"/>;
			
			var sliderLock = 0;
			var distanceMoved = 0;
			
			var currentImage = 1;
			var totalImages = <xsl:value-of select="count($display-album/m:item)"/>;
			var currentIndex = 1;
			var totalIndexes = Math.ceil(totalImages / pageSize);
			var initialId = <xsl:value-of select="$display-item/@item-id"/>;
			
			var offscreenTop = '-400px';
			var offscreenLeft = '0px';
			
			var albumKey = '<xsl:value-of select="$root-album/@anonymous-key"/>';
			var browseMode = '<xsl:value-of select="$browse-mode"/>';
			var userKey = '<xsl:value-of select="$user-key"/>';
			var imgSize = '<xsl:value-of select="$single-size"/>';
			var imgCompress = '<xsl:value-of select="$single-quality"/>';
			var cancelAddComment = false;
			var imageData = [
			[-1,-1,-1,"dummy/path","dummy name","dummy comment","dummy date"],
			<xsl:apply-templates select="$display-album/m:item" mode="album-data"/>
			];
			
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
	</xsl:template>
	
	<xsl:template name="render-index-links">
		<xsl:param name="page"/>
		<xsl:param name="selected-page"/>
		<a xmlns="http://www.w3.org/1999/xhtml" href="javascript:showIndex({$page});" title="Show Page {$page}">
			<img id="index{$page}" alt="Show Page {$page}">
				<xsl:attribute name="src">
					<xsl:call-template name="get-resource-url">
						<xsl:with-param name="resource">
							<xsl:choose>
								<xsl:when test="$page = $selected-page">
									<xsl:text>img/tb_12.gif</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>img/tb_13.gif</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
						<xsl:with-param name="theme" select="$theme"/>
						<xsl:with-param name="web-context" select="$web-context"/>
					</xsl:call-template>
				</xsl:attribute>
			</img>
		</a>
		<xsl:if test="$page &lt; $max-page">
			<xsl:call-template name="render-index-links">
				<xsl:with-param name="page" select="$page + 1"/>
				<xsl:with-param name="selected-page" select="$selected-page"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="error-dialog">
		<xsl:if test="m:action-messages[@key='org.apache.struts.action.GLOBAL_ERROR'] or m:action-messages[@key='org.apache.struts.action.GLOBAL_MESSAGE']">
			<div xmlns="http://www.w3.org/1999/xhtml" id="msg-box-dialog" class="dialog-content">
				<h3 class="dialog-title">Message</h3>
				
				<xsl:if test="m:action-messages[@key='org.apache.struts.action.GLOBAL_ERROR']">
					<p>There were errors processing your request:</p>
					<ul>
						<xsl:apply-templates select="m:action-messages[@key='org.apache.struts.action.GLOBAL_ERROR']/msg" 
							mode="global-error"/>
					</ul>
				</xsl:if>
				<xsl:if test="m:action-messages[@key='org.apache.struts.action.GLOBAL_MESSAGE']">
					<xsl:choose>
						<xsl:when test="count(m:action-messages[@key='org.apache.struts.action.GLOBAL_MESSAGE']) &gt; 1">
							<ul>
								<xsl:apply-templates select="m:action-messages[@key='org.apache.struts.action.GLOBAL_MESSAGE']/m:msg" 
									mode="global-message-list"/>
							</ul>
						</xsl:when>
						<xsl:otherwise>
							<p>
								<xsl:apply-templates select="m:action-messages[@key='org.apache.struts.action.GLOBAL_MESSAGE']/msg"
									mode="global-message"/>
							</p>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!--
					<br/>
					<button class="button" onclick="toggleDialog('msg-box')">Close</button>
				-->
				
				<div id="msg-box-close" class="close-box">
					<h3 class="dialog-title-close"><a href="javascript:msgBoxToggleDialog()" title="Close">|X|</a></h3>
				</div>
				
			</div>
			<div xmlns="http://www.w3.org/1999/xhtml" id="msg-box-shadow" class="dialog-shadow">
				<xsl:text> </xsl:text>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="msg" mode="global-error">
		<li xmlns="http://www.w3.org/1999/xhtml"><xsl:value-of select="." disable-output-escaping="yes"/></li>
	</xsl:template>
	
	<xsl:template match="msg" mode="global-message">
		<xsl:value-of select="." disable-output-escaping="yes"/>
	</xsl:template>
	
	<xsl:template match="msg" mode="global-messsage-list">
		<li xmlns="http://www.w3.org/1999/xhtml"><xsl:value-of select="." disable-output-escaping="yes"/></li>
	</xsl:template>
	
</xsl:stylesheet>
	
	