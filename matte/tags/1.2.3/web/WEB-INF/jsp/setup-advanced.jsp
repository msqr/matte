<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
	<head>
		<title><fmt:message key="setup.advanced.title"/></title>
		<link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
		<script type="text/javascript" src="<c:url value="/js/prototype.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/matte-setup-advanced.js"/>"></script>
	</head>
	<body>
    <h1><fmt:message key="setup.advanced.title"/></h1>
    <p><fmt:message key="setup.advanced.intro"/></p>
    
	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
		
		<table class="setup" id="advanced-setup-table">
			<c:forEach items="${advancedProps}" var="prop" varStatus="status">
				<tr>
					<th>${prop.key}</th>
					<td <c:if test="${settings[prop.key] != null}">
						class="modified"
						</c:if>>
						<c:choose>
							<c:when test="${settings[prop.key] != null}">
								<c:choose>
									<c:when test="${fn:length(settings[prop.key]) > 100}">
										<textarea name="settings['${prop.key}']">${settings[prop.key]}</textarea>
									</c:when>
									<c:otherwise>
										<input type="text" name="settings['${prop.key}']"
											value="${settings[prop.key]}"/>																			
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								${prop.value}
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:forEach>
		</table>
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
