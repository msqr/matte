USING ImageMagick VIA JMagick WITH MATTE

ImageMagick can usually re-size images faster, and with better quality,
than the built-in Java Image-IO classes can. If you want to use 
ImageMagick Matte supports doing so via JMagick.

1) Install ImageMagick

   You can get binary and source releases from ImageMagick's
	website at http://www.imagemagick.org/.

2) Install JMagick

  You can get binary and source releases from JMagick's website at
  http://www.yeo.id.au/jmagick/. Note you need to have a version
  of JMagick that matches the version of ImageMagick you have.

3) Configure Tomcat to run with JMagick

  JMagick uses JNI, and as such only wants to be loaded once during 
  the JVM's life. For this to happen it relies on being loaded by the 
  system class loader so that it can be certain not to be loaded more 
  than once.

  TODO: update this documentation

4) Configure Matte to use JMagick

  TODO: update this documentation

=====================================================================
SPECIAL NOTE IF JMAGICK IS INSTALLED IN A NON-STANDARD LOCATION

If JMagick is not installed in a normal "system" location such as 
/usr/lib, you need to tell Tomcat where to find the JMagick 
shared libraries. To do this you can pass the 'java.library.path'
system property to Tomcat when it starts, setting the path to 
your JMagick 'lib' directory. An easy way to do this is to make
a shell script called 'start-tomcat' like the following:

 -----
 #!/bin/sh
 export JAVA_HOME=/opt/java/jdk
 export CATALINA_OPTS="-Djava.library.path=/opt/JMagick/lib"
 /opt/apache-tomcat-5.5.17/bin/catalina.sh start
 -----

----------------------------------------------------------------------
$Id: README-JMagick.txt,v 1.2 2007/01/25 03:47:50 matt Exp $
