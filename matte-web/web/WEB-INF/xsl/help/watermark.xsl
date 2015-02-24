<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<xsl:import href="default.xsl"/>

	<xsl:template match="x:x-data" mode="help">
		
		<p>A watermark is a small image you can have "stamped" onto
		all your media items, to make sure your images are visibly 
		marked as your own.</p>
		
		<p>Matte will apply your watermark image to the lower-right
		corner of your media items. It must be a PNG image, and one
		with transparency will work well.</p>
		
	</xsl:template>
	
</xsl:stylesheet>
