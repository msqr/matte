<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
  <head>
    <title><fmt:message key="title"/></title>
    <link media="screen" href="<c:url value="/css/matte-global.css"/>" type="text/css" rel="stylesheet" />
  </head>
  <body>
    <h1><fmt:message key="title"/></h1>
    <p><fmt:message key="setup.welcome.intro"/></p>

	<form method="post" action="<c:url value="/setupWizard.do"/>">
		<div>
			<input type="submit" name="_target${currPage + 1}" value="<fmt:message key="continue.displayName"/>"/>
		</div>
	</form>

  </body>
</html>
