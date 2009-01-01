<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan date"
	version="1.0">
	
	<xsl:import href="tmpl/global.xsl"/>
	
	<xsl:output method="xml" indent="no" 
		omit-xml-declaration="no"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
		media-type="text/xml"/>
	
	<xsl:variable name="mediaspec.thumb" select="$ses/m:session[1]/m:thumbnail-setting"/>
	<xsl:variable name="mediaspec.view" select="$ses/m:session[1]/m:view-setting"/>
	
	<xsl:template match="x:x-data">
		<html>
			<head>
				<title>
					<xsl:value-of select="key('i18n','view.prefs.title')"/>
				</title>
				<link rel="stylesheet" type="text/css" href="{$web-context}/css/matte-global.css" media="screen"><xsl:text> </xsl:text></link>				
			</head>
			<body>
				<h1><xsl:value-of select="key('i18n','view.prefs.title')"/></h1>
				<div id="main-pane" style="max-width: 450px;">
					<p><xsl:value-of select="key('i18n','view.prefs.intro')"/></p>
					<form id="change-viewsetting-form" action="{$web-context}/viewPreferences.do" method="post" class="simple-form">
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
						<div class="submit">
							<input type="hidden" name="updateUser" value="true" />
							<input value="{key('i18n','submit.displayName')}" type="submit" />
						</div>
						<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
					</form>
				</div>	
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>