RUNNING MATTE UNDER X WINDOWS

If you're running Matte on a Unix-like environment (Linux, BSD, 
etc), it might require an X server to operate correctly. In order 
for the application to resize images using the default configuration, 
Matte uses the Java Image IO API, which uses the AWT, which 
usually requires an X server to connect to. To get around this you can 
tell AWT to run in "headless" mode, meaning it does not have an X
environment to attach to. To do this create a simple shell script to
pass the following to the catalina.sh script:

  -----
  #!/bin/sh
  export JAVA_HOME=/opt/java/jdk
  export CATALINA_OPTS="-Djava.awt.headless=true"
  export SHELL=/bin/sh
  /opt/apache-tomcat-5.5.17/bin/catalina.sh start
  -----

If you name this script 'start-tomcat' and place it somewhere on your
PATH you can easily start up Tomcat without X. Of course you must
adjust the paths to your environment.

You can also run Tomcat normally as long as it can connect to your X
server. If you see error messages like the following:

java.lang.InternalError: Can't connect to X11 window server using ':0' 
  as the value of the DISPLAY variable.
	at sun.awt.X11GraphicsEnvironment.initDisplay(Native Method)
	at sun.awt.X11GraphicsEnvironment.(X11GraphicsEnvironment.java:134)

or

java.lang.NoClassDefFoundError
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:141)
	at java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(
	  GraphicsEnvironment.java:62)
	
then Tomcat can't connect to to X. You should update your X access 
control to allow the user Tomcat runs as to connect to your X server. 
This can usually be accomplished by (running while logged into X):

  xhost +localhost
  
or

  xhost +
  
Note that 'xhost +' completely disables access control to the X server,
so anybody could display anything. This may or may not be a concern for
you.

Another alternative is to run Xvfb to act as a virtual X server. With 
this approach you probably need to specify the DISPLAY environment 
variable if it is not already set, like this:

    -----
    #!/bin/sh
	export DISPLAY=:0
    export JAVA_HOME=/opt/java/jdk
    /opt/apache-tomcat-5.5.17/bin/catalina.sh start
    -----

----------------------------------------------------------------------
$Id: README-X.txt,v 1.2 2007/01/25 03:47:50 matt Exp $
