<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:m="http://msqr.us/xsd/matte"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	xmlns:date="http://exslt.org/dates-and-times"
	exclude-result-prefixes="m xweb date">
	
	<xsl:import href="global-variables.xsl"/>

	<xsl:template match="m:user" mode="validate">
		<xsl:apply-templates select="." mode="display-internal"/>
	</xsl:template>

	<xsl:template match="m:user" mode="display-internal">
		<div>
			<div class="label"><xsl:value-of select="key('i18n','name.displayName')"/></div>
			<div><xsl:value-of select="@name"/></div>
		</div>
		<div>
			<div class="label"><xsl:value-of select="key('i18n','email.displayName')"/></div>
			<div><xsl:value-of select="@email"/></div>
		</div>
		<div>
			<div class="label"><xsl:value-of select="key('i18n','login.displayName')"/></div>
			<div><xsl:value-of select="@login"/></div>
		</div>
		<div>
			<div class="label"><xsl:value-of select="key('i18n','password.displayName')"/></div>
			<div>*****</div>
		</div>
		<xsl:if test="@createdDate">
			<div>
				<div class="label"><xsl:value-of select="key('i18n','createdDate.displayName')"/></div>
				<div><xsl:value-of select="date:format-date(string(@creation-date),'d MMM yyyy')"/></div>
			</div>
		</xsl:if>
		<xsl:if test="string(m:tz/@name)">
			<div>
				<div class="label"><xsl:value-of select="key('i18n','timeZone.displayName')"/></div>
				<div><xsl:value-of select="m:tz/@name"/></div>
			</div>
		</xsl:if>
		<!--xsl:if test="@lastLoginDate">
			<tr>
			<th><xsl:value-of select="key('i18n','lastLoginDate.displayName')"/>:</th>
			<td><xsl:value-of select="date:format-date(string(@lastLoginDate),'d MMM yyyy H:mm z')"/></td>
			</tr>
		</xsl:if-->
	</xsl:template>
	
	<xsl:template match="m:user" mode="edit">
		<div>
			<label for="user.name">
				<xsl:if test="$err[@field='user.name']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','name.displayName')"/>
			</label>
			<div>
				<input type="text" name="user.name" value="{@name}"
					maxlength="64"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','name.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="user.email">
				<xsl:if test="$err[@field='user.email']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','email.displayName')"/>
			</label>
			<div>
				<input type="text" name="user.email" value="{@email}"
					maxlength="128"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','email.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="user.login">
				<xsl:if test="$err[@field='user.login']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','login.displayName')"/>
			</label>
			<div>
				<input type="text" name="user.login" value="{@login}"
					maxlength="32"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','login.caption')"/>
				</div>
			</div>
		</div>
		<div>
			<label for="user.password">
				<xsl:if test="$err[@field='user.password']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','password.displayName')"/>
			</label>
			<div>
				<input type="password" name="user.password" value="{@password}"
					maxlength="64"/>
				<div class="caption">
					<xsl:value-of select="key('i18n','password.caption')"/>
					<xsl:if test="@userId &gt; 0">
						<xsl:text> </xsl:text>
						<xsl:value-of select="key('i18n','password.edit.caption')"/>
					</xsl:if>
				</div>
			</div>
		</div>
		<div>
			<label for="user.tz.code">
				<xsl:if test="$err[@field='user.timeZone']">
					<xsl:attribute name="class">error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="key('i18n','timeZone.displayName')"/>
			</label>
			<div>
				<select name="user.tz.code">
					<xsl:variable name="curr.tz" select="m:tz/@code"/>
					<xsl:for-each select="$aux/m:model/m:time-zone">
						<option value="{@code}">
							<xsl:if test="$curr.tz = @code">
								<xsl:attribute name="selected">
									<xsl:text>selected</xsl:text>
								</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
				<div class="caption">
					<xsl:value-of select="key('i18n','timeZone.caption')"/>
				</div>
			</div>
		</div>
	</xsl:template>

</xsl:stylesheet>