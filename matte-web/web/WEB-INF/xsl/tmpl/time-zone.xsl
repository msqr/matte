<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	exclude-result-prefixes="m">

	<xsl:import href="global-variables.xsl"/>
	
	<xsl:template name="time-zone-selects">
		<xsl:param name="form.localTz"/>
		<xsl:param name="form.mediaTz"/>
		<xsl:param name="time.zones"/>
		<xsl:param name="id.prefix" select="''"/>
		<div>
			<label for="{$id.prefix}mediaTz">
				<xsl:if test="$err[@field='mediaTz']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','upload.media.timeZone.displayName')"/>
			</label>
			<div>
				<select name="mediaTz" id="{$id.prefix}mediaTz">
					<xsl:for-each select="$time.zones">
						<option value="{@code}">
							<xsl:if test="$form.mediaTz = @code">
								<xsl:attribute name="selected">
									<xsl:text>selected</xsl:text>
								</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
				<div class="caption">
					<xsl:value-of select="key('i18n','upload.media.timeZone.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="{$id.prefix}localTz">
				<xsl:if test="$err[@field='localTz']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','upload.media.localTimeZone.displayName')"/>
			</label>
			<div>
				<select name="localTz" id="{$id.prefix}localTz">
					<xsl:for-each select="$time.zones">
						<option value="{@code}">
							<xsl:if test="$form.localTz = @code">
								<xsl:attribute name="selected">
									<xsl:text>selected</xsl:text>
								</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
				<div class="caption">
					<xsl:value-of select="key('i18n','upload.media.localTimeZone.caption')"/>
				</div>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="time-zone-select">
		<xsl:param name="select.name" select="'mediaTz'"/>
		<xsl:param name="selected.value"/>
		<xsl:param name="time.zones"/>
		<xsl:param name="id.prefix" select="''"/>
		<select name="{$select.name}" id="{$id.prefix}{$select.name}">
			<xsl:for-each select="$time.zones">
				<option value="{@code}">
					<xsl:if test="$selected.value = @code">
						<xsl:attribute name="selected">
							<xsl:text>selected</xsl:text>
						</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="@name"/>
				</option>
			</xsl:for-each>
		</select>
	</xsl:template>
		
</xsl:stylesheet>