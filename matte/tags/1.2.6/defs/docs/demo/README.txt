=======================================================================
Matte: share your photos, movies, and more
Version @VERSION@ @BUILD_DATE@
=======================================================================

This is the Live Demo distribution of Matte. Matte is a personal photo,
movie, and audio organization and publishing tool. It allows you to 
upload media items to your web server and then organize these items 
into albums, which you can then publish and share with friends and
family. This Live Demo is a complete stand-alone Matte application 
pre-configured to run without any modification so you can see how 
Matte works. The only thing you need besides the Live Demo (which you 
have if you're reading this) is a Java 5 Runtime Environment (i.e. 
version 1.5) or higher. You can get a JRE from Sun at 
http://java.sun.com/. Other vendors make JREs, any one of them should
work as long as they are version 1.5 or higher.

***** Note on Java 6 *****
  
  A bug in the Java 6 JAXP XSLT implementation prevents Matte from
  running. This is Sun bug 6537167:

  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6537167

  Until this bug is fixed, Matte will only work in a Java 5 runtime.


Starting the Live Demo  -----------------------------------------------

To start Matte simply run './start.sh' or '.\start.bat' script (as 
appropriate for your operating system). This will start the Matte 
application running with the included servlet container. After a brief 
moment you should be able to go to the following URL in a browser:

  http://localhost:8080/matte

If you get an error that the JAVA_HOME environment variable is not 
defined, you need to set this environment variable, which varies 
depending on the operating system you're using. For Unix-like OSes 
using sh-derived shells you can run

  $ JAVA_HOME=/path/to/jdk ./start.sh
  
For Windows users you can run

  > set JAVA_HOME=\path\to\jdk
  > .\start.bat
  

Stopping the Live Demo  -----------------------------------------------

To stop Matte simply run './stop.sh' or '.\stop.bat' script (as 
appropriate for your operating system).


Logging into the Live Demo --------------------------------------------

The Live Demo comes configured with a normal user so you can log into
the application and browse or add media, etc. Log in with the 
following information:

  Username: demo
  Password: demo
  
In addition the Live Demo comes configured with an admin user so you 
can perform administration functions. To log in as an admin user use:

  Username: matte-admin
  Password: password


Troubleshooting the Live Demo -----------------------------------------

If you run into problems getting the Live Demo running, look at the 
<LIVE DEMO HOME>/apache-tomcat/logs directory for the application log 
files. They may provide clues as to the cause of the problem.
