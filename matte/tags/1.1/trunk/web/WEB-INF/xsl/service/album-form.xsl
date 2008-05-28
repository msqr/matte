<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan"
	version="1.0">

	<xsl:import href="../tmpl/global.xsl"/>
			
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="edit-album" 
		select="x:x-data/x:x-model[1]/m:edit[1]/m:album[1]"/>
	<xsl:variable name="parent-album" 
		select="$aux/m:model[1]/m:album[1]"/>
	<xsl:variable name="is-new" 
		select="not(boolean(string($edit-album/@album-id)))"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="edit-album-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p style="max-width: 300px;">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<xsl:value-of select="key('i18n','add.album.intro')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="key('i18n','update.album.intro')"/>
					</xsl:otherwise>
				</xsl:choose>
			</p>
			<xsl:apply-templates select="$edit-album" mode="edit"/>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			<div class="submit">
				<xsl:choose>
					<xsl:when test="$is-new = 'true'">
						<input type="submit" value="{key('i18n','add.displayName')}"/>
					</xsl:when>
					<xsl:otherwise>
						<input type="submit" value="{key('i18n','save.displayName')}"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		</form>
	</xsl:template>
	
	<xsl:template match="m:album" mode="edit">
		<div>
			<label for="album-name">
				<xsl:if test="$err[@field='album.name']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','album.name.displayName')"/>
			</label>
			<div>
				<input id="album-name" type="text" name="album.name" value="{@name}"
					maxlength="64"/>
			</div>
		</div>
		<div>
			<label for="album-comments">
				<xsl:value-of select="key('i18n','album.comment.displayName')"/>
			</label>
			<div>
				<textarea name="album.comment" id="album-comments">
					<xsl:choose>
						<xsl:when test="string-length(m:comment) &gt; 0">
							<xsl:value-of select="m:comment"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text> </xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</textarea>
				<div class="caption"><xsl:value-of 
					select="key('i18n','optional.caption')"/></div>
			</div>
		</div>
		<div>
			<label for="album-date">
				<xsl:value-of select="key('i18n','album.date.displayName')"/>
			</label>
			<div>
				<input id="album-date" type="text" name="album.albumDate" 
					maxlength="10">
					<xsl:if test="@album-date">
						<xsl:attribute name="value">
							<xsl:value-of 
								select="date:format-date(string(@album-date),'yyyy-MM-dd')"/>
						</xsl:attribute>
					</xsl:if>
				</input>
				<div class="caption" style="max-width: 300px;">
					<xsl:value-of disable-output-escaping="yes"
						select="key('i18n','album.date.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="album-sort">
				<xsl:value-of select="key('i18n','album.sort.displayName')"/>
			</label>
			<div>
				<select id="album-sort" name="album.sortMode">
					<xsl:apply-templates select="//m:album-sort"/>
				</select>
				<div class="caption" style="max-width: 300px;">
					<xsl:value-of disable-output-escaping="yes"
						select="key('i18n','album.sort.caption')"/>
				</div>
			</div>
		</div>
		<xsl:if test="$parent-album">
			<div>
				<label for="parent-album">
					<xsl:value-of select="key('i18n','album.parent.displayName')"/>
				</label>
				<div>
					<div class="display-only">
						<xsl:value-of select="$parent-album/@name"/>
					</div>
					<div>
						<input type="hidden" 
							name="uiMetadata[0].key" 
							value="removeAlbumParent"/>
						<input type="checkbox" id="parent-album" 
							name="uiMetadata[0].value"
							value="true"/>
						<xsl:value-of select="key('i18n','album.parent.remove')"/>
					</div>
					<div class="caption" style="max-width:300px;">
						<xsl:value-of select="key('i18n','album.parent.remove.caption')"/>
					</div>
				</div>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:album-sort">
		<option value="{@key}">
			<xsl:if test="string-length(m:comment) &gt; 0">
				<xsl:attribute name="title">
					<xsl:value-of select="normalize-space(m:comment)"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@key = 1 and (string-length($edit-album/@sort-mode) &lt; 1)">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:when>
				<xsl:when test="number($edit-album/@sort-mode) = number(@key)">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:value-of select="@name"/>
		</option>
	</xsl:template>
	
</xsl:stylesheet>