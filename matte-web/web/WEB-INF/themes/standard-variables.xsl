<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">

	<!-- standard data vars -->
	<xsl:variable name="aux" select="x:x-data/x:x-auxillary"/>
	<xsl:variable name="ctx" select="x:x-data/x:x-context"/>
	<xsl:variable name="mod" select="x:x-data/x:x-model"/>
	<xsl:variable name="err" select="x:x-data/x:x-errors/x:error[@field]"/>
	<xsl:variable name="req" select="x:x-data/x:x-request/x:param"/>
	<xsl:variable name="ses" select="x:x-data/x:x-session"/>
	
	<!-- helper vars -->
	<xsl:variable name="acting-user" select="x:x-data/x:x-session[1]/m:session[1]/m:acting-user[1]"/>
	<xsl:variable name="server-name" select="string($ctx/x:server-name)"/>
	<xsl:variable name="server-port" select="string($ctx/x:server-port)"/>
	<xsl:variable name="user-locale" select="string($ctx/x:user-locale)"/>
	<xsl:variable name="web-context" select="string($ctx/x:web-context)"/>
	<xsl:variable name="web-path" select="string($ctx/x:path)"/>
	<xsl:variable name="theme" select="x:x-data/x:x-model[1]/m:model[1]/m:theme[1]"/>
	
	<!-- application context defined as key for quick lookup -->
	<xsl:key name="appenv" match="x:x-data/x:x-auxillary/x:x-app-context/x:param" use="@key"/>
	
	<!-- auxillaray params defined as key for quick lookup -->
	<xsl:key name="aux-param" match="x:x-data/x:x-auxillary/x:x-param" use="@key"/>
	
	<!-- message resource bundle defined as key for quick lookup -->
	<xsl:key name="i18n" match="x:x-data/x:x-msg/x:msg" use="@key"/>
	
	<!-- request params defined as key for quick lookup -->
	<xsl:key name="req-param" match="x:x-data/x:x-request/x:param" use="@key"/>
	
	<xsl:variable name="server-url" select="concat(
		'http', 
		if ($server-port eq '443') then 's' else (), 
		'://',
		$server-name,
		if ($server-port ne '80' and $server-port ne '443') then concat(':', $server-port) else ()
		)"/>

	<!-- Media quality names, uses the session defined quality if available, then
		the acting user's quality if available, otherwise a standard default. -->
	
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
	
	<!-- Media size names, uses the session defined quality if available, then
		the acting user's quality if available, otherwise a standard default. -->
	
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
		Generate a server URL, eg. http://myhost
	-->
	<xsl:template name="server-url">
		<xsl:text>http</xsl:text>
		<xsl:if test="$server-port = '443'">
			<xsl:text>s</xsl:text>
		</xsl:if>
		<xsl:text>://</xsl:text>
		<xsl:value-of select="$server-name"/>
		<xsl:if test="$server-port != '80' and $server-port != '443'">
			<xsl:text>:</xsl:text>
			<xsl:value-of select="$server-port"/>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
