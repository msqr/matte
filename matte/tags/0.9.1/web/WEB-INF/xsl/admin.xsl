<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date">
	
	<!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>
	
	<!-- Alert message, work ticket -->
	<xsl:variable name="alert.message" select="x:x-data/x:x-messages[1]/x:msg[1]"/>
	<xsl:variable name="work.ticket" select="x:x-data/x:x-auxillary[1]/x:x-param[@key='work.ticket']"/>
	
	<xsl:template match="x:x-data" mode="page-head-content">
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/matte-admin.css" media="screen"><xsl:text> </xsl:text></link>
		<link rel="stylesheet" type="text/css" href="{$web-context}/css/listmenu.css" media="screen"><xsl:text> </xsl:text></link>
		<script type="text/javascript" src="{$web-context}/js/fsmenu.js"><xsl:text> </xsl:text></script>
		<script id="behaviour-js" type="text/javascript" src="{$web-context}/js/matte-admin-behaviours.js"><xsl:text> </xsl:text></script>
		<script id="app-js" type="text/javascript" xml:space="preserve">
			var APP_INFO = new Object();
			<xsl:if test="$alert.message">
				APP_INFO.alertMessage = "<xsl:value-of select="$alert.message" 
					disable-output-escaping="yes"/>";
			</xsl:if>
		</script>
	</xsl:template>
	
	<!--xsl:template match="x:x-data" mode="page-body-class">
		<xsl:if test="$display.items.count = 0">
			<xsl:text>no-sub-nav</xsl:text>
		</xsl:if>
	</xsl:template-->
	
	<xsl:template match="x:x-data" mode="page-main-nav">
		<xsl:call-template name="main-nav">
			<xsl:with-param name="page" select="'admin'"/>
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
					<li class="action-item context-user">
						<span title="{key('i18n','link.update.user.title')}" 
								class="a link-update-user">
							<xsl:value-of select="key('i18n','link.update.user')"/>
						</span>
					</li>
					<li class="context-user">
						<span title="{key('i18n','link.add.user.title')}" 
							class="a link-add-user">
							<xsl:value-of select="key('i18n','link.add.user')"/>
						</span>
					</li>
					<li class="action-item context-theme">
						<span title="{key('i18n','link.download.theme.title')}" 
							class="a link-download-theme">
							<xsl:value-of select="key('i18n','link.download.theme')"/>
						</span>
					</li>
					<li class="action-item context-theme">
						<span title="{key('i18n','link.update.theme.title')}" 
							class="a link-update-theme">
							<xsl:value-of select="key('i18n','link.update.theme')"/>
						</span>
					</li>
					<li class="action-item context-theme">
						<span title="{key('i18n','link.delete.theme.title')}" 
							class="a link-delete-theme">
							<xsl:value-of select="key('i18n','link.delete.theme')"/>
						</span>
					</li>
					<li class="context-theme">
						<span title="{key('i18n','link.add.theme.title')}" 
							class="a link-add-theme">
							<xsl:value-of select="key('i18n','link.add.theme')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.setup.wizard.title')}" 
							class="a link-setup-wizard">
							<xsl:value-of select="key('i18n','link.setup.wizard')"/>
						</span>
					</li>
				</ul>
			</li>
			<li>
				<a><xsl:value-of select="key('i18n','link.index.actions')"/></a>
				<ul>
					<li>
						<span title="{key('i18n','link.reindex.items.title')}" 
							class="a link-reindex-items">
							<xsl:value-of select="key('i18n','link.reindex.items')"/>
						</span>
					</li>
					<li>
						<span title="{key('i18n','link.reindex.users.title')}" 
							class="a link-reindex-users">
							<xsl:value-of select="key('i18n','link.reindex.users')"/>
						</span>
					</li>
				</ul>
			</li>
		</ul>
		
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-sub-nav-data">
		<xsl:text> </xsl:text>
	</xsl:template>	
	
	<xsl:template match="x:x-data" mode="page-body">
		<div id="left-pane">
			<div id="progress-pane" style="display: none;">
				<h2><xsl:value-of select="key('i18n','progress.displayName')"/></h2>
				<ol id="progress-list" class="collapsing">
				</ol>
			</div>
			
			<h2><xsl:value-of select="key('i18n','users.displayName')"/></h2>
			<ul id="user-list" class="collapsing">
				<xsl:comment>Users here.</xsl:comment>
			</ul>
			
			<h2><xsl:value-of select="key('i18n','themes.displayName')"/></h2>
			<ol id="theme-list" class="collapsing">
				<xsl:comment>Themes here</xsl:comment>
			</ol>
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
			<!-- Dialog: reindex user -->
			<form id="reindex-user-form" action="{$web-context}/reindex.do" method="post" class="simple-form">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','reindex.users.intro')"/>
				</p>
				<div class="submit">
					<input type="hidden" name="indexType" value="USER"/>
					<input value="{key('i18n','submit.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<!-- Dialog: reindex items -->
			<form id="reindex-item-form" action="{$web-context}/reindex.do" method="post" class="simple-form">
				<p style="max-width: 300px;">
					<xsl:value-of select="key('i18n','reindex.items.intro')"/>
				</p>
				<div class="submit">
					<input type="hidden" name="indexType" value="MEDIA_ITEM"/>
					<input value="{key('i18n','submit.displayName')}" type="submit" />
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</form>
			
			<div id="service-dialog-container">
				<xsl:text> </xsl:text>
			</div>
		</div>
		
	</xsl:template>
	
</xsl:stylesheet>
