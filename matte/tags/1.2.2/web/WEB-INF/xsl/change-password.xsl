<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<!-- imports -->
	<xsl:import href="tmpl/default-layout.xsl"/>

	<!-- layout variables -->
	<xsl:variable name="layout.global.nav.page" select="'change-password'"/>
	
	<!-- helper vars -->
	<xsl:variable name="user" select="x:x-data/x:x-model/m:edit/m:user"/>

	<xsl:template match="x:x-data" mode="page-title">
		<xsl:value-of select="key('i18n','change-password.title')"/>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body-class">
		<xsl:text>no-sub-nav</xsl:text>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="page-body">
		<div class="intro">
			<xsl:value-of select="key('i18n','change-password.intro')"
				disable-output-escaping="yes"/>
		</div>
		<xsl:variable name="login">
			<xsl:choose>
				<xsl:when test="$user">
					<xsl:value-of select="$user/@login"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="key('req-param','login')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<form class="simple-form" method="post" action="{$web-context}{$ctx/x:path}">
			<xsl:if test="string-length($login) &gt; 0">
				<div>
					<label for="user.login">
						<xsl:value-of select="key('i18n','login.displayName')"/>
					</label>
					<div id="user.login" class="display-only">
						<xsl:value-of select="$login"/>
					</div>
				</div>
			</xsl:if>
			<div>
				<label for="user-password">
					<xsl:value-of select="key('i18n','password.displayName')"/>
				</label>
				<div>
					<input type="password" name="password" id="user-password" maxlength="64"/>
				</div>
			</div>
			<div class="submit">
				<xsl:if test="key('req-param','code')">
					<input type="hidden" name="code" value="{key('req-param','code')}"/>
				</xsl:if>
				<xsl:if test="string-length($login) &gt; 0">
					<input type="hidden" name="login" value="{$login}"/>
				</xsl:if>
				<input type="submit" value="{key('i18n','save.displayName')}"/>
			</div>
			
		</form>
		<script type="text/javascript" xml:space="preserve">
			$('user.password').focus();
		</script>
	</xsl:template>
    
</xsl:stylesheet>
