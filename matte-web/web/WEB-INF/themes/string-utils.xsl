<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<!-- Function: js-string
		
		Replace occurances of " in a string with \", and embed the result in ".
		
		Parameters:
		str - The string to search/replace on.
	-->
	<xsl:function name="m:js-string" as="xs:string">
		<xsl:param name="str" as="xs:string"/>
		<xsl:value-of select="concat('&quot;', replace($str, '&quot;', '\\&quot;'), '&quot;')"/>
	</xsl:function>
	
	<!--
		Function: javascript-string
		
		Replace occurances of " in a string with \".
		
		Parameters:
		str	- the text to seach/replace in
	-->
	<xsl:function name="m:javascript-string" as="xs:string">
		<xsl:param name="str" as="xs:string"/>
		<xsl:value-of select="replace($str, '&quot;', '\\&quot;')"/>
	</xsl:function>
	
	<!--
		Named template: javascript-string
		
		Replace occurances of " in a string with \".
		
		Parameters:
		str	- the text to seach/replace in
	-->
	<xsl:template name="javascript-string" as="xs:string">
		<xsl:param name="output-string" as="xs:string"/>
		<xsl:value-of select="replace($output-string, '&quot;', '\\&quot;')"/>
	</xsl:template>
	
	<!--
		Function: single-quote-string
		
		Replace occurances of ' in a string with \'.
		
		Parameters:
		str	- the text to seach/replace in
	-->
	<xsl:function name="m:single-quote-string" as="xs:string">
		<xsl:param name="str" as="xs:string"/>
		<xsl:value-of select="replace($str, '''', '\\''')"/>
	</xsl:function>
	
	<!--
		Function: escape-string
		
		Replace occurances of a string with that string preceeded by a '\' 
		character.
		
		Parameters:
		output-string	- the text to seach/replace in
		target			- the text to search for
	-->
	<xsl:function name="m:escape-string" as="xs:string">
		<xsl:param name="str" as="xs:string"/>
		<xsl:param name="target" as="xs:string"/>
		<xsl:value-of select="replace($str, $target, concat('\\', $target))"/>
	</xsl:function>
	
	<!--
		Named Template: truncate-at-word
		
		Truncate a string at a word break (space). If the input text
		is shorter than max-length the text is returned unchanged.
		Otherwise the text is truncated at the max-length plus any 
		characters up to the next space, and a ellipsis character is
		appended.
		
		Parameters:
		text       - the text to truncate
		max-length - the maximum number of characters to allow
	-->
	<xsl:template name="truncate-at-word" as="xs:string">
		<xsl:param name="text" as="xs:string"/>
		<xsl:param name="max-length" as="xs:integer">350</xsl:param>
		<xsl:choose>
			<xsl:when test="string-length($text) &lt; $max-length">
				<xsl:value-of select="$text" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="start" select="substring($text,1,$max-length)"/>
				<xsl:variable name="after" select="substring($text,($max-length+1))"/>
				<xsl:variable name="word" select="substring-before($after,' ')"/>
				<xsl:variable name="truncated">
					<xsl:value-of select="$start" disable-output-escaping="yes"/>
					<xsl:value-of select="$word" disable-output-escaping="yes"/>
				</xsl:variable>
				<xsl:value-of select="concat($truncated, '&#x2026;')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		Named Template: render-file-size
		
		Generate text representation of the size of a file. For example:
		
		render-file-size(size = 14875) => 14.53 KB
		
		Parameters:
		size - an integer, assumed to be the number of bytes of the file
	-->
	<xsl:template name="render-file-size" as="xs:string">
		<xsl:param name="size" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="$size &gt; 1048576">
				<xsl:value-of select="concat(format-number($size div 1048576,'#,##0.##'), ' MB')"/>
			</xsl:when>
			<xsl:when test="$size &gt; 1024">
				<xsl:value-of select="concat(format-number($size div 1024,'#,##0.##'), ' KB')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat(format-number($size div 1024,'#,##0'), ' bytes')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
