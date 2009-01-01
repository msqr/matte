<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:x="http://msqr.us/xsd/jaxb-web" 
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x xalan"
	version="1.0">
	
	<xsl:import href="../tmpl/global.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="yes" indent="yes" 
		xalan:indent-amount="2"/>
	
	<xsl:variable name="item" select="$mod/m:model/m:item[1]"/>
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="add-comment-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p>
				<xsl:value-of select="key('i18n','add.comment.intro')"/>
			</p>
			<xsl:apply-templates select="." mode="add.comment"/>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			<div class="submit">
				<input type="hidden" name="itemId" value="{$item/@item-id}"/>
				<input type="submit" value="{key('i18n','add.displayName')}"/>
			</div>
		</form>
	</xsl:template>
	
	<xsl:template match="x:x-data" mode="add.comment">
		<div>
			<label for="add-comment-item-name">
				<xsl:value-of select="key('i18n','comment.item.displayName')"/>
			</label>
			<div class="display-only">
				<span id="add-comment-item-name">
					<xsl:value-of select="$item/@name"/>
				</span>
			</div>
		</div>
		<xsl:if test="not($acting-user/@user-id &gt;= 0)">
			<div>
				<label for="add-comment-name">
					<xsl:value-of select="key('i18n','comment.name.displayName')"/>
				</label>
				<div>
					<input type="text" id="add-comment-name" name="name" maxlength="64"/>
					<div class="caption">
						<xsl:value-of select="key('i18n','comment.name.caption')"/>
					</div>
				</div>
			</div>
			<div>
				<label for="add-comment-email">
					<xsl:value-of select="key('i18n','comment.email.displayName')"/>
				</label>
				<div>
					<input type="text" id="add-comment-email" name="email" maxlength="128"/>
					<div class="caption">
						<xsl:value-of select="key('i18n','comment.email.caption')"/>
					</div>
				</div>
			</div>
		</xsl:if>
		<div>
			<label for="add-comment-comment">
				<xsl:value-of select="key('i18n','comment.comment.displayName')"/>
			</label>
			<div>
				<textarea id="add-comment-comment" name="comment">
					<xsl:text> </xsl:text>
				</textarea>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>