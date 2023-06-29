<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<xsl:import href="default.xsl"/>

	<xsl:template match="x:x-data" mode="help">
		
		<p>A Matte ThemePak is a zip archive that contains:</p>
		
		<ol>
			<li>The theme XSLT file, named <b>theme.xsl</b>. This is required.</li>
			
			<li>Album browse XSLT file, named <b>browse.xsl</b>. This is required if
			the theme is to be used for album browsing.</li>

			<li>Item detail XSLT file, named <b>info.xsl</b>. This is optional and
			can be used by the theme for dynamically displaying detailed information 
			about selected media items.</li>
			
			<li>The theme thumbnail PNG image, named <b>thumbnail.png</b>. 
			This is optional, but recommended. This thumbnail should be designed 
			as a small icon for the theme, sized to about 160x120 pixels.</li>
			
			<li>The theme preview PNG image, named <b>preview.png</b>.
			This is optional, but recommended. This preview shold be designed 
			as a showcase for what the theme features are, sized to about 
			1024x768 pixels.</li>
			
			<li><div>The theme metadata properties file, named <b>theme.properties</b>.
			This is optional, but recommended. It allows the ThemePak to define 
			the following properties:</div>
			
				<dl>
					<dt>theme.name</dt>
					<dd>The name of the theme.</dd>
					
					<dt>theme.author</dt>
					<dd>The name of the theme's author(s).</dd>
					
					<dt>theme.authoremail</dt>
					<dd>The email address of the theme's author(s).</dd>
					
					<dt>theme.created</dt>
					<dd>The creation date of the theme, in the format 
					<code>yyyy-mm-dd</code> (eg. 2006-09-26).</dd>
				</dl>
			
			</li>
			
			<li>The theme messages resource bundle, named 
			<b>theme-messages<em>[_lang]</em>.properties</b>. These are optional. They can be 
			used to provide localized messages to make the theme work in different 
			languages. One of the bundles can omit the <code>_lang</code> portion of the file name
			to be used as the default message bundle.</li>
			
			<li>Any other resources required by the theme, such as CSS files, images, 
			JavaScript, etc.</li>
		</ol>
		
	</xsl:template>
	
</xsl:stylesheet>
