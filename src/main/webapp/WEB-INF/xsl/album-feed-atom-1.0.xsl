<?xml version="1.0" encoding="UTF-8"?>
<!--
	XSLT to transform album feed into Atom 1.0 XML.
	===================================================================
	Copyright (c) 2006 Matt Magoffin
	
	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License as 
	published by the Free Software Foundation; either version 2 of 
	the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
	General Public License for more details.
	
	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
	02111-1307 USA
	===================================================================
	$Id: album-feed-atom-1.0.xsl,v 1.10 2007/07/03 07:36:02 matt Exp $   
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:x="http://msqr.us/xsd/jaxb-web"
	exclude-result-prefixes="m x">
	
	<xsl:import href="../themes/theme-util.xsl"/>
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>
	
	<xsl:variable name="author" select="x:x-data/x:x-model[1]/m:model[1]/m:user[1]"/>
	<xsl:variable name="date.format" select="'[D01] [MNn,*-3] [Y0001]'"/>
	
	<xsl:template match="x:x-data">
		<!--?xml-stylesheet href="first-x.css" type="text/css" ?-->
		<xsl:processing-instruction name="xml-stylesheet">
			<xsl:text>type="text/css" href="</xsl:text>
			<xsl:call-template name="get-resource-url">
				<xsl:with-param name="theme" select="$theme"/>
				<xsl:with-param name="resource" select="'woosh-browse.css'"/>
				<xsl:with-param name="web-context" select="$web-context"/>
			</xsl:call-template>
			<xsl:text>"</xsl:text>
		</xsl:processing-instruction>
		<feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en-us">
			<title>
				<xsl:value-of select="$author/@name"/>
				<xsl:value-of select="key('i18n','feed.author.posessive.suffix')"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="key('i18n','feed.album.title')"/>
			</title>
			<xsl:variable name="port" select="$ctx/x:server-port"/>
			<link rel="alternate">
				<xsl:attribute name="href">
					<xsl:call-template name="server-url"/>
					<xsl:value-of select="$web-context"/>
					<xsl:text>/browse.do?userKey=</xsl:text>
					<xsl:value-of select="$author/@anonymous-key"/>
				</xsl:attribute>
			</link>
			<link rel="self">
				<xsl:attribute name="href">
					<xsl:call-template name="server-url"/>
					<xsl:value-of select="$web-context"/>
					<xsl:value-of select="$ctx/x:path"/>
					<xsl:if test="$req">
						<xsl:text>?</xsl:text>
						<xsl:for-each select="$req">
							<xsl:value-of select="@key"/>
							<xsl:text>=</xsl:text>
							<xsl:value-of select="."/>
						</xsl:for-each>
					</xsl:if>
				</xsl:attribute>
			</link>
			<!--icon>http://www.finmodler.com/favicon.ico</icon-->
			<xsl:variable name="update-date">
				<xsl:for-each select="x:x-model[1]/m:model[1]/m:search-results[1]/m:album
					| x:x-model[1]/m:model[1]/m:search-results[1]/descendant::m:search-album">
					<xsl:sort select="substring-before(concat(@modify-date,@album-date,@creation-date),'T')" 
						order="descending"/>
					<xsl:if test="position() = 1">
						<xsl:choose>
							<xsl:when test="@modify-date">
								<xsl:value-of select="@modify-date"/>
							</xsl:when>
							<xsl:when test="@album-date">
								<xsl:value-of select="@album-date"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@creation-date"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<updated>
				<xsl:value-of select="$update-date"/>
			</updated>
			<author>
				<name>
					<xsl:value-of select="$author/@name"/>
				</name>
			</author>
			<!--subtitle>FinModler Update Blog</subtitle>
			<id>http://www.finmodler.com/updateblog</id-->
			<generator>
				<xsl:value-of select="key('i18n','title')"/>
			</generator>
			
			<xsl:apply-templates select="x:x-model[1]/m:model[1]/m:search-results[1]/m:album"/>
			
		</feed>		
	</xsl:template>
	
	<xsl:template match="m:album">
		<xsl:variable name="album.date">
			<xsl:choose>
				<xsl:when test="@album-date">
					<xsl:value-of select="@album-date"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@creation-date"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="total-item-count" select="sum(.//@item-count)"/>
		<xsl:variable name="min-date">
			<xsl:for-each select=".//@item-min-date">
				<xsl:sort select="." order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="max-date">
			<xsl:for-each select=".//@item-max-date">
				<xsl:sort select="." order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="total-album-count" select="count(m:search-album) + 1"/>
		<entry xmlns="http://www.w3.org/2005/Atom">
			<title>
				<xsl:value-of select="@name"/>
			</title>
			<link>
				<xsl:attribute name="href">
					<xsl:apply-templates select="." mode="view.album.absolute.url"/>
				</xsl:attribute>
			</link>
			<id>
				<xsl:value-of select="@anonymous-key"/>
			</id>
			<published>
				<xsl:value-of select="$album.date"/>
			</published>
			<updated>
				<xsl:choose>
					<xsl:when test="@modify-date">
						<xsl:value-of select="@modify-date"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@creation-date"/>
					</xsl:otherwise>
				</xsl:choose>
			</updated>
			<!--category scheme="http://www.finmodler.com/categories" term="Software/Patches"
				label="Software/Patches"/-->
			<content type="xhtml">
				<div xmlns="http://www.w3.org/1999/xhtml">
					<xsl:apply-templates select="m:search-poster"/>
					<div>
						<xsl:value-of select="m:comment"/>
					</div>
					<div style="font-size: 60%;">
						<br/>
						<xsl:value-of select="format-date(xs:date(substring-before($album.date,'T')),$date.format)"/>
						<xsl:text> - </xsl:text>
						<xsl:value-of select="$total-item-count"/>
						<xsl:text> </xsl:text>
						<xsl:choose>
							<xsl:when test="@total-item-count = 1">
								<xsl:value-of select="key('i18n','browse.items.count.single')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="key('i18n','browse.items.count')"/>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:if test="$total-album-count &gt; 1">
							<xsl:text> </xsl:text>
							<xsl:value-of select="key('i18n','in')"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="$total-album-count"/>
							<xsl:text> </xsl:text>
							<span style="text-decoration: lowercase;">
								<xsl:value-of select="key('i18n','albums.displayName')"/>
							</span>
						</xsl:if>
						<xsl:if test="@modify-date">
							<xsl:text> - </xsl:text>
							<xsl:value-of select="key('i18n','browse.album.lastupdated')"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="format-date(xs:date(substring-before(@modify-date,'T')),$date.format)"/>
						</xsl:if>
						<xsl:if test="@item-count &gt; 0 and $min-date != $max-date">
							<div class="browse-album-info">
								<xsl:value-of select="key('i18n', 'browse.items.itemrange')"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="format-date(xs:date(substring-before($min-date,'T')),$date.format)"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="key('i18n', 'to')"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="format-date(xs:date(substring-before($max-date,'T')),$date.format)"/>
							</div>
						</xsl:if>
					</div>
				</div>
			</content>
		</entry>		
	</xsl:template>
	
	<xsl:template match="m:search-poster">
		<img xmlns="http://www.w3.org/1999/xhtml"
			style="float:left; margin-right: 10px; margin-bottom: 10px; border: 0" 
			alt="{@name}">
			<xsl:attribute name="src">
				<xsl:call-template name="server-url"/>
				<xsl:value-of select="$web-context"/>
				<xsl:text>/media.do?id=</xsl:text>
				<xsl:value-of select="@item-id"/>
				<xsl:text>&amp;size=THUMB_BIGGER</xsl:text>
			</xsl:attribute>
		</img>
	</xsl:template>
	
</xsl:stylesheet>
		