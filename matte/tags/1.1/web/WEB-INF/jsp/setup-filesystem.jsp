<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
  <head>
    <title><fmt:message key="setup.fs.title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="setup.fs.title"/></h1>
    <p><fmt:message key="setup.fs.intro"/></p>

    <spring:hasBindErrors name="cmd">
    	<div class="error-intro">
			<spring:bind path="cmd.settings">
				<c:choose>
					<c:when test="${status.errors.errorCount > 1}">
						<ul>
							<c:forEach items="${status.errors.allErrors}" var="error">
								<li>
									<spring:message code="${error.code}" arguments="${error.arguments}"/><br />
								</li>
							</c:forEach>
						</ul>
					</c:when>
					<c:when test="${status.errors.errorCount == 1}">
						<c:set property="error" value="${status.errors.allErrors[0]}"/>
						<spring:message code="${error.code}" arguments="${error.arguments}"/><br />
					</c:when>
				</c:choose>
			</spring:bind>
    	</div>
    </spring:hasBindErrors>
    
	<form method="post" action="<c:url value="/setupWizard.do"/>">
		
		<dl class="menu">
			
			<spring:nestedPath path="cmd">
			
				<dt><fmt:message key="setup.fs.index.dir.displayName"/></dt>
				<dd>
					<spring:bind path="settings['lucene.index.base.path']">
						<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
					</spring:bind>
					<fmt:message key="setup.fs.index.dir.caption"/>
				</dd>
				
				<dt><fmt:message key="setup.fs.collections.dir.displayName"/></dt>
				<dd>
					<spring:bind path="settings['collection.root.path']">
						<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
					</spring:bind>
					<fmt:message key="setup.fs.collections.dir.caption"/>
				</dd>
				
				<dt><fmt:message key="setup.fs.themes.dir.displayName"/></dt>
				<dd>
					<spring:bind path="settings['theme.path']">
						<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
					</spring:bind>
					<fmt:message key="setup.fs.themes.dir.caption"/>
				</dd>
				
				<dt><fmt:message key="setup.fs.cache.dir.displayName"/></dt>
				<dd>
					<spring:bind path="settings['cache.path']">
						<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
					</spring:bind>
					<fmt:message key="setup.fs.cache.dir.caption"/>
				</dd>
					
				<dt><fmt:message key="setup.fs.resource.dir.displayName"/></dt>
				<dd>
					<spring:bind path="settings['resource.path']">
						<input type="text" class="filepath" 
							name="${status.expression}" 
							value="${status.value}"/> <br />
					</spring:bind>
					<fmt:message key="setup.fs.resource.dir.caption"/>
				</dd>
					
			</spring:nestedPath>
			
		</dl>
		
		<div>
			<input type="submit" name="_target${currPage - 1}" value="<fmt:message key="back.displayName"/>"/>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
