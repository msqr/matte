<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	exclude-result-prefixes="m x xalan"
	version="1.0">
	
	<xsl:import href="../tmpl/global.xsl"/>
	
	<!-- NOTE: omit-xml-declaration must be "yes" for any result destined
		for AJAX response into browser running in XHTM mode (Firefox)
		otherwise .innerHTML called by Prototype will fail -->
	
	<xsl:output method="xml" omit-xml-declaration="yes" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="mediaspec.view" select="$acting-user/m:view-setting"/>
	<xsl:variable name="album" select="$mod/m:model/m:album[1]"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="item-download-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p>
				<xsl:value-of select="key('i18n','download.selected.items.intro')"/>
			</p>
			<xsl:if test="not($album) or ($album/@allow-original = 'true')
				or ($album/m:owner/@user-id = $acting-user/@user-id)">
				<div>
					<label for="download-originals">
						<xsl:value-of select="key('i18n','download.originals')"/>
					</label>
					<div class="display-only">
						<input type="checkbox" name="original" id="download-originals" 
							value="true"/>
						<div class="caption"><xsl:value-of 
							select="key('i18n','download.originals.caption')" 
							disable-output-escaping="yes"/></div>
					</div>
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</xsl:if>
			<fieldset>
				<legend><xsl:value-of select="key('i18n','download.settings.displayName')"/></legend>
				<div>
					<label for="download-size">
						<xsl:value-of select="key('i18n','mediaspec.size.displayName')"/>
					</label>
					<div>
						<select name="size" id="download-size">
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
							<xsl:call-template name="render-i18n-options">
								<xsl:with-param name="value-list" select="'THUMB_BIGGER,THUMB_BIG,THUMB_NORMAL,THUMB_SMALL,'"/>
								<xsl:with-param name="content-key-prefix" select="'mediaspec.size.'"/>
								<xsl:with-param name="selected-value">
									<xsl:value-of select="$mediaspec.view/@size"/>
								</xsl:with-param>
								<xsl:with-param name="value-key-prefix" 
									select="'viewsettings.thumb.displayName'"/>
							</xsl:call-template>
						</select>
						<div class="caption">
							<xsl:value-of select="key('i18n','download.selected.items.size.caption')"/>
						</div>
					</div>
				</div>	
				<div>
					<label for="download-quality">
						<xsl:value-of select="key('i18n','mediaspec.quality.displayName')"/>
					</label>
					<div>
						<select name="quality" id="download-quality">
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
						<div class="caption">
							<xsl:value-of select="key('i18n','download.selected.items.quality.caption')"/>
						</div>
					</div>
				</div>
				<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			</fieldset>
			<div class="submit">
				<xsl:for-each select="$req[@key='itemIds']">
					<input type="hidden" name="itemIds" value="{.}"/>
				</xsl:for-each>
				<xsl:if test="$req[@key='albumKey']">
					<input type="hidden" name="albumKey" value="{$req[@key='albumKey']}"/>
				</xsl:if>
				<xsl:if test="$req[@key='albumId']">
					<input type="hidden" name="albumId" value="{$req[@key='albumId']}"/>
				</xsl:if>
				<xsl:if test="$req[@key='mode']">
					<input type="hidden" name="mode" value="{$req[@key='mode']}"/>
				</xsl:if>
				<xsl:if test="$req[@key='userKey']">
					<input type="hidden" name="userKey" value="{$req[@key='userKey']}"/>
				</xsl:if>
				<xsl:if test="$req[@key='direct']">
					<input type="hidden" name="direct" value="{$req[@key='direct']}"/>
				</xsl:if>
				<input type="submit" value="{key('i18n','download.displayName')}"/>
			</div>
		</form>
	</xsl:template>
		
</xsl:stylesheet>