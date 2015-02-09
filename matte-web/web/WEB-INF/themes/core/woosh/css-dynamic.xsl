<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m x date"
	extension-element-prefixes="date">
	
	<xsl:import href="../../theme-util.xsl"/>

	<xsl:output method="text"/>
	
	<xsl:variable name="theme" select="x:x-data/x:x-model[1]/m:model[1]/m:theme[1]"/>
	
	<xsl:template match="/x:x-data">
		.frame-t {
			background: #fff url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=img/tb_05.gif') repeat-x left top;
		}
		
		.frame-ml {
			background: #fff url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=img/tb_18.gif');
		}
		
		.frame-mr {
			background: #fff url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=img/tb_23.gif') repeat-y;
		}
		
		.frame-b {
			background: #fff url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=img/tb_27.gif') repeat-x left bottom;
		}
		
		.yui-calcontainer .calnavleft {
			background: url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=yui/callt.gif') no-repeat;
		}
		
		.yui-calcontainer .calnavright {
			background: url('<xsl:value-of 
				select="$web-context"/>/themeResource.do?themeId=<xsl:value-of 
					select="$theme/@theme-id"/>&amp;resource=yui/calrt.gif') no-repeat;
		}
	</xsl:template>
	
</xsl:stylesheet>
	