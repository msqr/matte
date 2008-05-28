<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
  <head>
    <title><fmt:message key="setup.fs.title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="setup.confirm.title"/></h1>
    <p><fmt:message key="setup.confirm.intro"/></p>

	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<dl class="menu" id="setup-confirm">
		
			<dt><fmt:message key="setup.features.heading"/></dt>
			<dd>

				<table>
					<tr>
						<th><fmt:message key="setup.feature.registration.displayName"/></th>
						<td>
							<c:choose>
								<c:when test="${cmd.settings['feature.registration'] == 'true'}">
									<fmt:message key="enabled"/>
								</c:when>
								<c:otherwise>
									<fmt:message key="disabled"/>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th><fmt:message key="setup.feature.upload.applet.displayName"/></th>
						<td>
							<c:choose>
								<c:when test="${cmd.settings['feature.upload.applet'] == 'true'}">
									<fmt:message key="enabled"/>
								</c:when>
								<c:otherwise>
									<fmt:message key="disabled"/>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</table>
			
			</dd>
			
			<dt><fmt:message key="setup.db.heading"/></dt>
			<dd>

				<table>
					<tr>
						<th><fmt:message key="setup.db.hibernate.dialect.displayName"/></th>
						<td>
							<c:choose>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.DB2Dialect'}">
									DB2
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.DerbyDialect'}">
									Derby
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.MySQLDialect'}">
									MySQL
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.PostgreSQLDialect'}">
									PostgreSQL
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.OracleDialect'}">
									Oracle 8
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.Oracle9Dialect'}">
									Oracle 9
								</c:when>
								<c:when test="${cmd.settings['hibernate.dialect'] 
									== 'org.hibernate.dialect.SQLServerDialect'}">
									SQL Server
								</c:when>
								<c:otherwise>
									<c:out value="${cmd.settings['hibernate.dialect']}"/>
								</c:otherwise>
							</c:choose>
							<c:if test="${cmd.changedHibernateDialect}">
								<div class="error">
									<fmt:message key="setup.db.chagned.hibernateDialect"/>
								</div>
							</c:if>
						</td>
					</tr>
				</table>
			
			</dd>
			
			<dt><fmt:message key="setup.fs.heading"/></dt>
			<dd>

				<table>
					<tr>
						<th><fmt:message key="setup.fs.index.dir.displayName"/></th>
						<td><c:out value="${cmd.settings['lucene.index.base.path']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.fs.collections.dir.displayName"/></th>
						<td><c:out value="${cmd.settings['collection.root.path']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.fs.themes.dir.displayName"/></th>
						<td><c:out value="${cmd.settings['theme.path']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.fs.cache.dir.displayName"/></th>
						<td><c:out value="${cmd.settings['cache.path']}"/></td>
					</tr>
				</table>
			
			</dd>
			
			<dt><fmt:message key="setup.other.heading"/></dt>
			<dd>

				<table>
					<tr>
						<th><fmt:message key="setup.mail.from.displayName"/></th>
						<td><c:out value="${cmd.settings['mail.from']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.crypto.key.displayName"/></th>
						<td><c:out value="${cmd.settings['crypto.key']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.crypto.pbe.salt.displayName"/></th>
						<td><c:out value="${cmd.settings['crypto.pbe.salt']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.crypto.salt.displayName"/></th>
						<td><c:out value="${cmd.settings['crypto.salt']}"/></td>
					</tr>
					<tr>
						<th><fmt:message key="setup.xslt.cache.displayName"/></th>
						<td>
							<c:choose>
								<c:when test="${cmd.settings['xslt.cache'] == 'true'}">
									<fmt:message key="yes"/>
								</c:when>
								<c:otherwise>
									<fmt:message key="no"/>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</table>
			
			</dd>
			
		</dl>
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_finish" value="<fmt:message key="save.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
