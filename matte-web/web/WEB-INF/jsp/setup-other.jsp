<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
  <head>
    <title><fmt:message key="setup.other.title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="setup.other.title"/></h1>
    <p><fmt:message key="setup.other.intro"/></p>
    
	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<dl class="menu">
			
			<dt><fmt:message key="setup.mail.from.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['mail.from']">
					<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
				</spring:bind>
				<fmt:message key="setup.mail.from.caption"/>
			</dd>
			
			<dt><fmt:message key="setup.crypto.key.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['crypto.key']">
					<input type="text" 
							name="${status.expression}" 
							value="${status.value}"
							maxlength="20"/> <br />
				</spring:bind>
				<fmt:message key="setup.crypto.key.caption"/>
			</dd>
			
			<dt><fmt:message key="setup.crypto.pbe.salt.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['crypto.pbe.salt']">
					<input type="text"  
							name="${status.expression}" 
							value="${status.value}"
							maxlength="8"/> <br />
				</spring:bind>
				<fmt:message key="setup.crypto.pbe.salt.caption"/>
			</dd>
			
			<dt><fmt:message key="setup.crypto.salt.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['crypto.salt']">
					<input type="text" 
							name="${status.expression}" 
							value="${status.value}"
							maxlength="20"/> <br />
				</spring:bind>
				<fmt:message key="setup.crypto.salt.caption"/>
			</dd>
			
			<dt><fmt:message key="setup.xslt.cache.displayName"/></dt>
			<dd>
				<spring:bind path="cmd.settings['xslt.cache']">
					<input type="radio"
							name="${status.expression}" 
							value="true"
							<c:if test="${cmd.settings['xslt.cache'] == 'true' }">
								checked="checked"
							</c:if>
							/> <fmt:message key="yes"/><br />
					<input type="radio"
							name="${status.expression}" 
							value="false"
							<c:if test="${cmd.settings['xslt.cache'] != 'true'}">
								checked="checked"
							</c:if>
							/> <fmt:message key="no"/><br />
				</spring:bind>
				<fmt:message key="setup.xslt.cache.caption"/>
			</dd>
			
		</dl>
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
