<%--
  Copyright (c) 2002-2006 Matt Magoffin
  
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

  $Id: index.jsp,v 1.9 2007/08/20 09:31:20 matt Exp $   
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:choose>
	<c:when test="${param['refresh'] == 'true'}">
		<c:redirect url="/home.do"/>
	</c:when>
	<c:otherwise>
		<c:redirect url="/index.jsp">
			<c:param name="refresh">
				true
			</c:param>
		</c:redirect>
	</c:otherwise>
</c:choose>
