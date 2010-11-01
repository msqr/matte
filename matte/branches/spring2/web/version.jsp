<%--
  Copyright (c) 2002-2007 Matt Magoffin
  
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

  $Id: version.jsp,v 1.4 2007/01/02 23:40:58 matt Exp $   
--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
	<head>
		<title>@APP_NAME@ Version</title>
		<style type="text/css">
			pre			
			{
				white-space: pre;
				font-family: monospace;
			}
		</style>
	</head>
	<body>
		<pre>
App:     @APP_NAME@ 
Build:   @BUILD_VERSION@ 
Date:    @BUILD_DATE@
Target:  @BUILD_TARGET_ENV@
Host:    <%= java.net.InetAddress.getLocalHost().getHostName() %>
</pre>
	</body>
</html>
