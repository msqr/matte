<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<c:set var="errorMessage" scope="page">${exception}</c:set>
<html>
	<head>
		<title><fmt:message key="global.error.title"/></title>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<script type="text/javascript" src="<c:url value="/js/prototype.js"/>"></script>
		<link rel="stylesheet" type="text/css" href="<c:url value="/css/matte-global.css"/>" />
		<script type="text/javascript">
			window.onload = function() {
				var div = $('stacktrace');
				div.onclick = function() {
					if ( Element.hasClassName(div, 'hide') ) {
						Element.removeClassName(div, 'hide');
					} else {
						Element.addClassName(div, 'hide');
					}
				}
				Element.show(div);
			}
		</script>
	</head>
	<body>
		<%@ include file="error-message.jsp" %>
		<c:if test="${exception != null}">
			<div id="stacktrace" class="hide" style="display: none;"><%
				Exception exception = (Exception)request.getAttribute("exception");
				exception.printStackTrace(new java.io.PrintWriter(out));
				%></div>
		</c:if>
	</body>
</html>
