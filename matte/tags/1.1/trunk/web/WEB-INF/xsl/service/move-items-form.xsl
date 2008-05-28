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
	
	<!-- Entry point -->
	<xsl:template match="x:x-data">
		<form id="move-items-form" action="{$web-context}{$ctx/x:path}" method="post" 
			class="simple-form">
			<p>
				<xsl:value-of select="key('i18n','move.items.intro')"/>
			</p>
			<div>
				<label for="collection-name">
					<xsl:value-of select="key('i18n','collection.displayName')"/>
				</label>
				<div class="display-only">
					<span id="collection-name">
						<xsl:value-of select="$aux/m:model[1]/m:collection[1]/@name"/>
					</span>
				</div>
			</div>
			<div><xsl:comment>This is here to "clear" the floats.</xsl:comment></div>
			<div class="submit">
				<input type="hidden" name="collectionId" 
					value="{$aux/m:model[1]/m:collection[1]/@collection-id}"/>
				<xsl:for-each select="$aux/m:model[1]/m:search-results[1]/m:item">
					<input type="hidden" name="itemIds" value="{@item-id}"/>
				</xsl:for-each>
				<input type="submit" value="{key('i18n','submit.displayName')}"/>
			</div>
		</form>
	</xsl:template>
	
</xsl:stylesheet>