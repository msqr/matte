<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
  <head>
    <title><fmt:message key="setup.features.title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="setup.features.title"/></h1>
    <p><fmt:message key="setup.features.intro"/></p>

	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<dl class="menu">
			
			<spring:nestedPath path="cmd">
			
				<dt><fmt:message key="setup.feature.registration.displayName"/></dt>
				<dd>
					<spring:bind path="settings['feature.registration']">
						<input type="hidden" 
							name="_<c:out value="${status.expression}"/>" 
							value="visible"/>
						<input class="setting" type="checkbox" 
							name="<c:out value="${status.expression}"/>"
							value="true" <c:if test="${status.value == 'true'}">checked="checked"</c:if>/>
					</spring:bind>
					<fmt:message key="setup.feature.registration.caption"/>
				</dd>
			
				<dt><fmt:message key="setup.feature.upload.applet.displayName"/></dt>
				<dd>
					<spring:bind path="settings['feature.upload.applet']">
						<input type="hidden" 
							name="_<c:out value="${status.expression}"/>" 
							value="visible"/>
						<input class="setting" type="checkbox" 
							name="<c:out value="${status.expression}"/>"
							value="true" <c:if test="${status.value == 'true'}">checked="checked"</c:if>/>
					</spring:bind>
					<fmt:message key="setup.feature.upload.applet.caption"/>
				</dd>
			
			</spring:nestedPath>
			
		</dl>
		
		<div>
			<input type="submit" name="_target${currPage + 1}" 
				value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
