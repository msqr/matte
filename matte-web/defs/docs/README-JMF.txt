USING JAVA MEDIA FRAMEWORK (JMF) WITH MATTE

If you want to enable support for several video formats you can use 
the Java Media Framework (JMF) with Media Album to do so. Visit 

  http://java.sun.com/products/java-media/jmf/
  
to download the latest version (2.1.1 at the time of this writing). 
Then insall it according to the Set Up documentation for your 
platform, available at under

  http://java.sun.com/products/java-media/jmf/reference/docs/
  
If you install the native library version of JMF (called the 
Performance Pack) then Tomcat will need access to the JMF native 
libraries that come with JMF. This is most easily accomplished by
creating a shell script to start Tomcat, setting the LD_LIBRARY_PATH
environment variable (or whatever appropriate for your OS), 
something like this:

    -----
    #!/bin/sh
    export JAVA_HOME=/opt/java/jdk
    export JMFHOME=/opt/java/JMF-2.1.1e
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JMFHOME/lib
    /opt/apache-tomcat-5.5.17/bin/catalina.sh start
    -----

TODO: explain Matte configuration settings

----------------------------------------------------------------------
$Id: README-JMF.txt,v 1.2 2007/01/25 03:47:50 matt Exp $
