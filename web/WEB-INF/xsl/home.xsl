<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date">
	
	<!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	<xsl:import href="tmpl/time-zone.xsl"/>
	
	<!-- Selected items -->
	<xsl:variable name="display.items" select="x:x-data/x:x-model/m:model/m:item"/>
	
	<!-- Are there any items to display? -->
	<xsl:variable name="display.items.count" select="count($display.items)"/>
	
	<!-- Selected Collection -->
	<xsl:variable name="display.collection.id" select="x:x-data/x:x-request/x:param[@key='collectionId']"/>
	<xsl:variable name="display.collection" select="x:x-data/x:x-model/m:model/m:collection[@collection-id = $display.collection.id]"/>
	
	<!-- Selected Album -->
	<xsl:variable name="display.album.id" select="x:x-data/x:x-request/x:param[@key='albumId']"/>
	<xsl:variable name="display.album" select="x:x-data/x:x-model/m:model/m:album[@album-id = $display.album.id]"/>
	
	<!-- Alert message, work ticket -->
	<xsl:variable name="alert.message" select="x:x-data/x:x-messages[1]/x:msg[1]"/>
	<xsl:variable name="work.ticket">
		<xsl:choose>
			<xsl:when test="key('aux-param','work.ticket')">
				<xsl:value-of select="'aux-param'"/>
			</xsl:when>
			<xsl:when test="key('req-param','work.ticket')">
				<xsl:value-of select="'req-param'"/>
			</xsl:when>
		</xsl:choose>
	</xsl:variable>
	
	<!-- MediaSpec -->
	<xsl:variable name="mediaspec.thumb" select="$acting-user/m:thumbnail-setting"/>
	<xsl:variable name="mediaspec.view" select="$acting-user/m:view-setting"/>
	
	<xsl:template match="x:x-data" mode="page-head-content">
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/matte-main.css" media="screen"><xsl:text> </xsl:text></link>
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/listmenu.css" media="screen"><xsl:text> </xsl:text></link>
		<link rel="stylesheet" type="text/css" href="{$web-context}/js/yui/calendar.css" media="screen"><xsl:text> </xsl:text></link>
		<style type="text/css" xml:space="preserve">
			.yui-calcontainer .calnavleft {
			background: url('<xsl:value-of select="$web-context"/>/js/yui/callt.gif') no-repeat;
			}			
			.yui-calcontainer .calnavright {
			background: url('<xsl:value-of select="$web-context"/>/js/yui/calrt.gif') no-repeat;
			}			
		</style>
		<script type="text/javascript" src="{$web-context}/js/fsmenu.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="{$web-context}/js/date.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="{$web-context}/js/yui/yahoo-dom-event.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="{$web-context}/js/yui/calendar-min.js"><xsl:text> </xsl:text></script>
		<script id="behaviour-js" type="text/javascript" src="{$web-context}/js/matte-behaviours.js"><xsl:text> </xsl:text></script>
		<script id="app-js" type="text/javascript" xml:space="preserve">
			var APP_INFO = new Object();
			
			<xsl:if test="string-length($work.ticket) &gt; 0">
				APP_INFO.workTicket = <xsl:value-of 
					select="key($work.ticket,'work.ticket')"/>;
				APP_INFO.workDisplayName = "<xsl:value-of 
					select="key($work.ticket,'work.displayName')"/>";
				APP_INFO.workSubmitTime = "<xsl:value-of 
					select="key($work.ticket,'work.submitTime')"/>";
				APP_INFO.workCompleted = <xsl:value-of 
					select="key($work.ticket,'work.completed')"/>;
				APP_INFO.workMessage = "<xsl:value-of 
					select="key($work.ticket,'work.message')"/>";
			</xsl:if>

			<xsl:if test="$alert.message">
				APP_INFO.alertMessage = "<xsl:call-template name="javascript-string">
					<xsl:with-param name="output-string" select="$alert.message"/>
				</xsl:call-template>";
			</xsl:if>
			<xsl:if test="$display.collection.id">
				APP_INFO.displayCollectionId = <xsl:value-of select="$display.collection.id"/>;
			</xsl:if>
			<xsl:if test="$display.album.id">
				APP_INFO.displayAlbumId = <xsl:value-of select="$display.album.id"/>;
			</xsl:if>
			APP_INFO.thumbSpec = {
				size : "<xsl:value-of select="$mediaspec.thumb/@size"/>",
				quality : "<xsl:value-of select="$mediaspec.thumb/@quality"/>"};
			APP_INFO.viewSpec = {
				size : "<xsl:value-of select="$mediaspec.view/@size"/>",
				quality : "<xsl:value-of select="$mediaspec.view/@quality"/>"};

			var mediaSizes = new Object();
			<xsl:for-each select="x:x-model/m:model/m:media-size">
				mediaSizes.<xsl:value-of select="@size"/> = {
					width: <xsl:value-of select="@width"/>,
					height: <xsl:value-of select="@height"/>
				}
			</xsl:for-each>
		</script>
	</xsl:template>
	
	<!--xsl:template match="x:x-data" mode="page-body-class">
		<xsl:if test="$display.items.count = 0">
			<xsl:text>no-sub-nav</xsl:text>
		</xsl:if>
	</xsl:template-->
	
	<xsl:template match="x:x-data" mode="page-main-nav">
		<xsl:call-template name="main-nav">
			<xsl:with-param name="page" select="'home'"/>
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-sub-nav">
		<xsl:comment>SUB NAV</xsl:comment>
		<ul class="menulist" id="listMenuRoot">
			<li class="action-action">
				<a><xsl:value-of select="key('i18n','link.select')"/></a>
				<ul>
					<li>
						<span title="{key('i18n','link.select.all.title')}"
								class="a link-select-all">
							<xsl:value-of select="key('i18n','link.select.all')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.select.none.title')}"
								class="a link-select-none">
							<xsl:value-of select="key('i18n','link.select.none')"/>
						</span>
					</li>
				</ul>
			</li>
			<li>
				<a><xsl:value-of select="key('i18n','link.actions')"/></a>
				<ul>
					<li>
						<span title="{key('i18n','link.search.items.title')}" 
							class="a link-search-item">
							<xsl:value-of select="key('i18n','link.search.items')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.delete.album.title')}" 
								class="a link-delete-album">
							<xsl:value-of select="key('i18n','link.delete.album')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.share.album.title')}" 
							class="a link-share-album">
							<xsl:value-of select="key('i18n','link.share.album')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.download.album.title')}" 
							class="a link-download-album">
							<xsl:value-of select="key('i18n','link.download.album')"/>
						</span>
					</li>
					<li class="action-item">
						<span title="{key('i18n','link.download.selected.items.title')}"
							class="a link-download-items">
							<xsl:value-of select="key('i18n','link.download.selected.items')"/>
						</span>
					</li>
					<li class="action-item context-album">
						<span title="{key('i18n','link.removefrom.album.title')}" 
							class="a link-removefrom-album">
							<xsl:value-of select="key('i18n','link.removefrom.album')"/>
						</span>
					</li>
					<li class="action-item context-album">
						<span title="{key('i18n','link.set.album.poster.title')}" 
							class="a link-setposter-album">
							<xsl:value-of select="key('i18n','link.set.album.poster')"/>
						</span>
					</li>
					<li class="action-collection">
						<span title="{key('i18n','link.upload.collection.title')}" 
							class="a link-upload-collection">
							<xsl:value-of select="key('i18n','link.upload.collection')"/>
						</span>
					</li>
					<li class="action-collection">
						<span title="{key('i18n','link.delete.collection.title')}"
								class="a link-delete-collection">
							<xsl:value-of select="key('i18n','link.delete.collection')"/>
						</span>
					</li>
					<li class="action-item context-collection">
						<span title="{key('i18n','link.removefrom.collection.title')}"
							class="a link-removefrom-collection">
							<xsl:value-of select="key('i18n','link.removefrom.collection')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.new.album.title')}" 
								class="a link-add-album">
							<xsl:value-of select="key('i18n','link.new.album')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.update.album.title')}"
							class="a link-update-album">
							<xsl:value-of select="key('i18n','link.update.album')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.sort.album.title')}" 
							class="a link-sort-album">
							<xsl:value-of select="key('i18n','link.sort.album')"/>
						</span>
					</li>
					<li class="action-album">
						<span title="{key('i18n','link.sort.items.title')}" 
							class="a link-sort-items">
							<xsl:value-of select="key('i18n','link.sort.items')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.new.collection.title')}" 
								class="a link-add-collection">
							<xsl:value-of select="key('i18n','link.new.collection')"/>
						</span>
					</li>
					<li class="action-collection">
						<span title="{key('i18n','link.update.collection.title')}"
							class="a link-update-collection">
							<xsl:value-of select="key('i18n','link.update.collection')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.user.prefs.title')}"
							class="a link-user-prefs">
							<xsl:value-of select="key('i18n','link.user.prefs')"/>
						</span>
					</li>
				</ul>
			</li>
		</ul>
		<!--xsl:if test="$display.items.count != 0">
		</xsl:if-->
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-sub-nav-data">
		<xsl:text> </xsl:text>
		<xsl:comment>
			<xsl:text>sub nav data: collection = [</xsl:text>
			<xsl:value-of select="$display.collection.id"/>
			<xsl:text>]; album = [</xsl:text>
			<xsl:value-of select="$display.album.id"/>
			<xsl:text>]</xsl:text>
		</xsl:comment>
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-body">
		<div id="left-pane">
			<!--xsl:if test="$display.items.count = 0">
				<xsl:attribute name="class">
					<xsl:text>no-sub-nav</xsl:text>
				</xsl:attribute>
			</xsl:if-->
			<div id="progress-pane" style="display: none;">
				<h2><xsl:value-of select="key('i18n','progress.displayName')"/></h2>
				<ol id="progress-list" class="collapsing">
					<!-- progress items populate here -->
				</ol>
			</div>
			<div id="preview-pane" style="display: none;">
				<h2><xsl:value-of select="key('i18n','preview.displayName')"/></h2>
				<div id="preview-container" class="collapsing">
					<xsl:text> </xsl:text>
				</div>
			</div>
			<div id="info-pane">
				<h2><xsl:value-of select="key('i18n','info.displayName')"/></h2>
				<form id="info-form" action="{$web-context}/saveMediaInfo.do" 
					method="post" class="collapsing">
					<div class="single">
						<label for="item-name">
							<xsl:value-of select="key('i18n','item.name')"/>
						</label>
						<input type="text" id="item-name" name="name"/>
					</div>
					<div class="single">
						<label for="item-date">
							<xsl:value-of select="key('i18n','item.date')"/>
						</label>
						<input type="text" id="item-date" name="date"/>
					</div>
					<div class="single">
						<label for="item-comments">
							<xsl:value-of select="key('i18n','item.comments')"/>
						</label>
						<textarea id="item-comments" name="comments">
							<xsl:text> </xsl:text>
						</textarea>
					</div>
					<div class="single multi">
						<label for="item-tags"><xsl:value-of select="key('i18n','meta.tags')"/></label>
						<textarea id="item-tags" name="tags">
							<xsl:text> </xsl:text>
						</textarea>
					</div>
					<div class="single multi">
						<label for="item-copyright">
							<xsl:value-of select="key('i18n','item.copyright')"/>
						</label>
						<input type="text" id="item-copyright" name="copyright"/>
					</div>
					<div class="single multi">
						<label for="item-tz-container">
							<xsl:value-of select="key('i18n','item.timeZone')"/>
						</label>
						<span id="item-tz-container" class="a"/>
					</div>
					<div class="submit">
						<input value="{key('i18n','save.displayName')}" type="submit" />
					</div>
					<hr />
					<div class="single multi">
						<label for="item-tags">
							<xsl:value-of select="key('i18n','item.rating')"/>
						</label>
						<span class="rating-stars" id="item-rating"/>
					</div>
					<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
					<input type="hidden" id="item-tz" name="mediaTimeZone"/>
					<input type="hidden" id="item-tz-display" name="displayTimeZone"/>
				</form>
			</div>
			<h2><xsl:value-of select="key('i18n','collections.displayName')"/></h2>
			<ol id="collection-list" class="collapsing">
				<xsl:apply-templates select="x:x-model/m:model/m:collection" mode="collection.list"/>
				<xsl:if test="count(x:x-model/m:model/m:collection) = 0">
					<xsl:comment>There are no collections.</xsl:comment>
				</xsl:if>
			</ol>
			<h2><xsl:value-of select="key('i18n','albums.displayName')"/></h2>
			<ol id="album-list" class="collapsing">
				<xsl:apply-templates select="x:x-model/m:model/m:album" mode="album.list"/>
				<xsl:if test="count(x:x-model/m:model/m:album) = 0">
					<xsl:comment>There are no albums.</xsl:comment>
				</xsl:if>
			</ol>
		</div>
		
		<div id="search-pane" style="display: none;">
			<div class="close-x">
				<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
			</div>
			<form id="search-item-form" action="{$web-context}/find.do" 
					method="post" class="simple-form">
				<!--p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','share.album.intro')"
						disable-output-escaping="yes"/>
				</p-->
				<div>
					<label for="quick-search">
						<xsl:value-of select="key('i18n','search.items.quick.displayName')"/>
					</label>
					<div>
						<input type="text" name="quickSearch" id="quick-search"/>
						<input value="{key('i18n','find.displayName')}" type="submit" />
					</div>
				</div>
				<h3>
					<xsl:value-of select="key('i18n','search.advanced.displayName')"/>
				</h3>
				<div class="collapsing" id="advanced-pane">
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
							src="{$web-context}/img/date.png"
							id="date-range-calendar-toggle"/>
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
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
		</div>
		
		<div id="main-pane" class="main-pane-normal">
			<xsl:comment>main-pane content here</xsl:comment>
		</div>
	
		<div id="message-pane" style="display: none;">
			<div class="close-x">
				<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
			</div>
			<div id="message-content-pane" class="message-box">
				<xsl:text> </xsl:text>
			</div>
		</div>
		
		<div id="dialog-pane" style="display: none;">
			<div class="close-x">
				<span class="alt-hide"><xsl:value-of select="key('i18n','close')"/></span>
			</div>
			<div id="dialog-content-pane" class="dialog-box">
				<xsl:text> </xsl:text>
			</div>
		</div>
		
		<div id="system-working" style="display: none;">
			<xsl:value-of select="key('i18n','working.displayName')"/>
		</div>
		
		<div id="ui-elements">
			<!-- Dialog: delete album form -->
			<form id="delete-album-form" action="{$web-context}/deleteAlbum.do" method="post" 
					class="simple-form-validate">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','delete.album.intro')"/>
				</p>
				<div>
					<div class="label"><xsl:value-of select="key('i18n','album.name.displayName')"/></div>
					<div id="delete-album-name" style="max-width: 240px;">
						<xsl:value-of select="$display.album/@name"/>
						<xsl:text> </xsl:text>
					</div>
				</div>
				<div class="submit">
					<input type="hidden" name="albumId" id="delete-album-id" value="{$display.album.id}" />
					<input value="{key('i18n','delete.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<!-- Dialog: delete collection form -->
			<form id="delete-collection-form" action="{$web-context}/deleteCollection.do" method="post" 
				class="simple-form-validate">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','delete.collection.intro')" 
						disable-output-escaping="yes"/>
				</p>
				<div>
					<div class="label"><xsl:value-of select="key('i18n','collection.name.displayName')"/></div>
					<div id="delete-collection-name" style="max-width: 240px;">
						<xsl:value-of select="$display.collection/@name"/>
						<xsl:text> </xsl:text>
					</div>
				</div>
				<div class="submit">
					<input type="hidden" name="collectionId" id="delete-collection-id" 
						value="{$display.collection/@collection-id}" />
					<input value="{key('i18n','delete.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>

			<!-- Dialog: remove from album form -->
			<form id="removefrom-album-form" action="{$web-context}/removeFromAlbum.do" method="post" class="simple-form">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','removefrom.album.intro')"/>
				</p>
				<div class="submit">
					<input type="hidden" name="albumId" id="removefrom-album-id" 
						value="{$display.album/@album-id}"/>
					<input value="{key('i18n','remove.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<!-- Dialog: remove from collection form -->
			<form id="removefrom-collection-form" action="{$web-context}/deleteItems.do" method="post" class="simple-form">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','removefrom.collection.intro')"
						disable-output-escaping="yes"/>
				</p>
				<div class="submit">
					<input type="hidden" name="collectionId" id="removefrom-collection-id" 
						value="{$display.collection/@collection-id}"/>
					<input value="{key('i18n','delete.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<!-- Dialog: share album form -->
			<form id="share-album-form" action="{$web-context}/shareAlbum.do" method="post" class="simple-form">
				<p>
					<xsl:value-of select="key('i18n','share.album.intro')"
						disable-output-escaping="yes"/>
				</p>
				<div>
					<label for="share-album-shared">
						<xsl:value-of select="key('i18n','share.album.shared.displayName')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="shared" id="share-album-shared" 
							value="true"/>
						<span><xsl:value-of 
							select="key('i18n','share.album.shared.caption')" 
							disable-output-escaping="yes"/></span>
					</div>
				</div>
				<div>
					<label for="share-album-browse">
						<xsl:value-of select="key('i18n','share.album.browse.displayName')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="browse" id="share-album-browse" 
							value="true"/>
						<span><xsl:value-of 
							select="key('i18n','share.album.browse.caption')" 
							disable-output-escaping="yes"/></span>
					</div>
				</div>
				<div>
					<label for="share-album-feed">
						<xsl:value-of select="key('i18n','share.album.feed.displayName')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="feed" id="share-album-feed" 
							value="true"/>
						<span><xsl:value-of 
							select="key('i18n','share.album.feed.caption')" 
							disable-output-escaping="yes"/></span>
					</div>
				</div>
				<div>
					<label for="share-album-original">
						<xsl:value-of select="key('i18n','share.album.original.displayName')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="original" id="share-album-original" 
							value="true"/>
						<span><xsl:value-of 
							select="key('i18n','share.album.original.caption')" 
							disable-output-escaping="yes"/></span>
					</div>
				</div>
				<div>
					<label for="share-album-theme">
						<xsl:value-of select="key('i18n','theme.displayName')"/>
					</label>
					<div>
						<select name="themeId" id="share-album-theme">
							<xsl:comment>themes populated here</xsl:comment>
							<xsl:for-each select="x:x-model/m:model/m:theme">
								<option value="{@theme-id}">
									<xsl:value-of select="@name"/>
								</option>
							</xsl:for-each>
						</select>
					</div>
				</div>
				<div>
					<label for="share-album-apply-children">
						<xsl:value-of select="key('i18n','share.album.applychildren.displayName')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="applyToChildren" id="share-album-apply-children" 
							value="true"/>
						<span><xsl:value-of 
							select="key('i18n','share.album.applychildren.caption')" 
							disable-output-escaping="yes"/></span>
					</div>
				</div>
				<div class="submit">
					<input type="hidden" name="albumId" id="share-album-id" 
						value="{$display.album/@album-id}"/>
					<input value="{key('i18n','share.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<!-- Dialog: set media time zone -->
			<form id="tz-item-form" action="{$web-context}/noop.do" method="post" 
				class="simple-form">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','media.timeZone.intro')"/>
				</p>

				<xsl:call-template name="time-zone-selects">
					<xsl:with-param name="time.zones" 
						select="x:x-model/m:model/m:time-zone"/>
					<xsl:with-param name="id.prefix" select="'home.'"/>
				</xsl:call-template>
				<div class="submit">
					<input value="{key('i18n','set.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<div id="service-dialog-container">
				<xsl:text> </xsl:text>
			</div>

		</div>
		
	</xsl:template>
	
	<!-- Generate the list of albums -->
	<xsl:template match="m:album" mode="album.list">
		<li>
			<xsl:if test="@album-id = $display.album.id">
				<xsl:attribute name="class">selected</xsl:attribute>
			</xsl:if>
			<a href="{$web-context}/home.do?albumId={@album-id}" title="{@name}">
				<xsl:value-of select="@name"/>
			</a>
			<xsl:if test="m:album">
				<ol class="collapsing">
					<xsl:apply-templates select="m:album" mode="album.list"/>
				</ol>
			</xsl:if>
		</li>
	</xsl:template>
	
	<!-- Generate the list of collections -->
	<xsl:template match="m:collection" mode="collection.list">
		<li>
			<xsl:if test="@collection-id = $display.collection.id">
				<xsl:attribute name="class">selected</xsl:attribute>
			</xsl:if>
			<a href="{$web-context}/home.do?collectionId={@collection-id}" title="{@name}">
				<xsl:value-of select="@name"/>
			</a>
		</li>
	</xsl:template>
	
</xsl:stylesheet>
