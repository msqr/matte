<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan"
	version="1.0">
	
	<xsl:import href="../tmpl/global.xsl"/>
	<xsl:import href="../tmpl/time-zone.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="mediaspec.thumb" select="$acting-user/m:thumbnail-setting"/>
	<xsl:variable name="mediaspec.view" select="$acting-user/m:view-setting"/>

		<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="user-prefs-form" action="{$web-context}{$ctx/x:path}" 
			method="post" enctype="multipart/form-data" class="simple-form">
			<p>
				<xsl:value-of select="key('i18n','user.prefs.intro')"/>
			</p>
			<xsl:apply-templates select="." mode="user.prefs"/>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			<div class="submit">
				<input type="submit" value="{key('i18n','save.displayName')}"/>
			</div>
		</form>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="user.prefs">
		<div>
			<label for="shared-albums-url">
				<xsl:value-of select="key('i18n','user.prefs.shared.albums.url')"/>
			</label>
			<div class="display-only">
				<span id="shared-albums-url">
					<xsl:call-template name="server-url"/>
					<xsl:value-of select="$web-context"/>
					<xsl:text>/browse.do?userKey=</xsl:text>
					<xsl:value-of select="$acting-user/@anonymous-key"/>
				</span>
				<div class="caption">
					<xsl:value-of select="key('i18n','user.prefs.shared.albums.url.caption')"/>
				</div>
			</div>
		</div>
		
		<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		<div>
			<label for="user-prefs-timeZone">
				<xsl:value-of select="key('i18n','user.prefs.time.zone')"/>
			</label>
			<div>
				<xsl:call-template name="time-zone-select">
					<xsl:with-param name="select.name" select="'timeZone'"/>
					<xsl:with-param name="selected.value" select="$acting-user/m:tz/@code"/>
					<xsl:with-param name="time.zones" select="$aux/m:model/m:time-zone"/>
					<xsl:with-param name="id.prefix" select="'user-prefs-'"/>
				</xsl:call-template>
				<div class="caption">
					<xsl:value-of select="key('i18n','user.prefs.time.zone.caption')"/>
				</div>
			</div>
		</div>
		
		<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		<div>
			<label for="user-prefs-locale">
				<xsl:value-of select="key('i18n','user.prefs.locale')"/>
			</label>
			<div>
				<select name="locale" id="user-prefs-locale">
					<xsl:for-each select="$aux/m:model/m:locale">
						<option value="{@code}">
							<xsl:if test="substring-before(@code, '_') = $acting-user/@language
								and substring-after(@code, '_') = $acting-user/@country">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
				<div class="caption">
					<xsl:value-of select="key('i18n','user.prefs.locale.caption')"/>
				</div>
			</div>
		</div>
		
		<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		<div>
			<label for="browse-theme">
				<xsl:value-of select="key('i18n','user.prefs.browse.theme')"/>
			</label>
			<div>
				<select name="browseThemeId" id="browse-theme">
					<xsl:for-each select="$aux/m:model/m:theme">
						<option value="{@theme-id}">
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
				<div class="caption">
					<xsl:value-of select="key('i18n','user.prefs.browse.theme.caption')"/>
				</div>
			</div>
		</div>
		
		<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		<div>
			<label for="user-watermark">
				<xsl:value-of select="key('i18n','user.prefs.watermark')"/>
			</label>
			<div>
				<input type="file" id="user-watermark" name="watermarkFile"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','user.prefs.watermark.caption')"
						disable-output-escaping="yes"/>
				</div>
			</div>
		</div>
		<xsl:if test="$acting-user/m:metadata[@key='watermark']">
			<div>
				<label for="user-current-watermark-delete">
					<xsl:value-of select="key('i18n','user.prefs.watermark.current')"/>
				</label>
				<div>
					<img src="{$web-context}/userResource.do?userId={$acting-user/@user-id}&amp;resource={$acting-user/m:metadata[@key='watermark']}"/>
					<div class="caption">
						<input type="checkbox" id="user-current-watermark-delete"
							name="deleteWatermark" value="true"/>
						<xsl:value-of select="key('i18n','user.prefs.watermark.delete.caption')"/>
					</div>
				</div>
			</div>
		</xsl:if>
					
		<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
		<fieldset>
			<legend><xsl:value-of select="key('i18n','viewsettings.thumb.displayName')"/></legend>
			<div>
				<label for="viewsettings-thumb-size">
					<xsl:value-of select="key('i18n','mediaspec.size.displayName')"/>
				</label>
				<div>
					<select name="thumb.size" id="viewsettings-thumb-size">
						<xsl:call-template name="render-i18n-options">
							<xsl:with-param name="value-list" select="'THUMB_BIGGER,THUMB_BIG,THUMB_NORMAL,THUMB_SMALL,'"/>
							<xsl:with-param name="content-key-prefix" select="'mediaspec.size.'"/>
							<xsl:with-param name="selected-value">
								<xsl:choose>
									<xsl:when test="$mediaspec.thumb/@size">
										<xsl:value-of select="$mediaspec.thumb/@size"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>THUMB_NORMAL</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
						</xsl:call-template>
					</select>
				</div>
			</div>	
			<div>
				<label for="viewsettings-thumb-quality">
					<xsl:value-of select="key('i18n','mediaspec.quality.displayName')"/>
				</label>
				<div>
					<select name="thumb.quality" id="viewsettings-thumb-quality">
						<xsl:call-template name="render-i18n-options">
							<xsl:with-param name="value-list" select="'HIGHEST,HIGH,GOOD,AVERAGE,LOW,'"/>
							<xsl:with-param name="content-key-prefix" select="'mediaspec.quality.'"/>
							<xsl:with-param name="selected-value">
								<xsl:choose>
									<xsl:when test="$mediaspec.thumb/@quality">
										<xsl:value-of select="$mediaspec.thumb/@quality"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>GOOD</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
						</xsl:call-template>
					</select>
				</div>
			</div>	
			<div class="caption" style="max-width: 300px;">
				<xsl:value-of select="key('i18n','viewsettings.thumb.caption')"/>
			</div>
		</fieldset>
		<fieldset>
			<legend><xsl:value-of select="key('i18n','viewsettings.view.displayName')"/></legend>
			<div>
				<label for="viewsettings-view-size">
					<xsl:value-of select="key('i18n','mediaspec.size.displayName')"/>
				</label>
				<div>
					<select name="view.size" id="viewsettings-view-size">
						<xsl:call-template name="render-i18n-options">
							<xsl:with-param name="value-list" select="'BIGGEST,BIGGER,BIG,NORMAL,SMALL,TINY,'"/>
							<xsl:with-param name="content-key-prefix" select="'mediaspec.size.'"/>
							<xsl:with-param name="selected-value">
								<xsl:choose>
									<xsl:when test="$mediaspec.view/@size">
										<xsl:value-of select="$mediaspec.view/@size"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>NORMAL</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
						</xsl:call-template>
					</select>
				</div>
			</div>	
			<div>
				<label for="viewsettings-view-quality">
					<xsl:value-of select="key('i18n','mediaspec.quality.displayName')"/>
				</label>
				<div>
					<select name="view.quality" id="viewsettings-view-quality">
						<xsl:call-template name="render-i18n-options">
							<xsl:with-param name="value-list" select="'HIGHEST,HIGH,GOOD,AVERAGE,LOW,'"/>
							<xsl:with-param name="content-key-prefix" select="'mediaspec.quality.'"/>
							<xsl:with-param name="selected-value">
								<xsl:choose>
									<xsl:when test="$mediaspec.view/@quality">
										<xsl:value-of select="$mediaspec.view/@quality"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>GOOD</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
						</xsl:call-template>
					</select>
				</div>
			</div>	
			<div class="caption" style="max-width: 300px;">
				<xsl:value-of select="key('i18n','viewsettings.view.caption')"/>
			</div>
		</fieldset>
	</xsl:template>
	
</xsl:stylesheet>