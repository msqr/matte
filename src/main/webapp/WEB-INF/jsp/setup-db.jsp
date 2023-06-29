<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
  <head>
    <title><fmt:message key="setup.db.title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="setup.db.title"/></h1>
    <p><fmt:message key="setup.db.intro"/></p>
    
	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<dl class="menu">
			
			<dt><fmt:message key="setup.db.hibernate.dialect.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['hibernate.dialect']">
					<select name="<c:out value="${status.expression}"/>">
						<option value="org.hibernate.dialect.DB2Dialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.DB2Dialect'}">selected="selected"</c:if>>DB2</option>
						<option value="org.hibernate.dialect.DerbyDialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.DerbyDialect'}">selected="selected"</c:if>>Derby</option>
						<option value="org.hibernate.dialect.MySQLDialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.MySQLDialect'}">selected="selected"</c:if>>MySQL</option>
						<option value="org.hibernate.dialect.PostgreSQLDialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.PostgreSQLDialect'}">selected="selected"</c:if>>PostgreSQL</option>
						<option value="org.hibernate.dialect.OracleDialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.OracleDialect'}">selected="selected"</c:if>>Oracle 8</option>
						<option value="org.hibernate.dialect.Oracle9Dialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.Oracle9Dialect'}">selected="selected"</c:if>>Oracle 9</option>
						<option value="org.hibernate.dialect.SQLServerDialect" <c:if 
							test="${status.value == 
								'org.hibernate.dialect.SQLServerDialect'}">selected="selected"</c:if>>SQL Server</option>
					</select> <br />
				</spring:bind>
				<fmt:message key="setup.db.hibernate.dialect.caption"/>
			</dd>
			
			
		</dl>
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
