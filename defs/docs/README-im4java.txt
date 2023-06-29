USING ImageMagick or GraphicsMagick VIA im4java WITH MATTE

ImageMagick and GraphicsMagick can usually re-size images faster, and
with better quality, than the built-in Java Image-IO classes can. If
you want to use ImageMagick or GraphicsMagick Matte supports doing so
via im4java.

Note Matte also supports using ImageMagick via JMagick, see the
README-JMagick.txt for more information. Using JMagick can result
in faster image processing performance, but it can be more difficult
to configure correctly.

1) Install ImageMagick or GraphicsMagick

   You can get binary and source releases from ImageMagick's
   website at http://www.imagemagick.org/ and at GraphicMagick's
   website at http://www.graphicsmagick.org/.

3) Configure the JVM to run with im4java

  If you want to use GraphicsMagick instead of ImageMagick, you
  must specify the following system property on the JVM running
  Matte:
  
  im4java.useGM = true
  
  You normally do this by passing a -D flag to the JVM when
  starting Matte, like this:
  
  -Dim4java.useGM=true
   
  If ImageMagick or GraphicsMagic are not installed in a
  default location where Matte can find them, you must also set an
  environment variable for the JVM running Matte:
  
  IM4JAVA_TOOLPATH = /path/to/tools
  
  where /path/to/tools is the path to the directory with the 
  ImageMagick or GraphicsMagick applications in it. For example
  you might configure the variable like
  
  IM4JAVA_TOOLPATH = /usr/local/bin

4) Configure Matte to use im4java

  The binary distribution of Matte includes an example configuration
  context for using im4java with Matte: environmentContext-im4java.xml.
  Replace the contents of environmentContext.xml file included in the
  Matte WAR file, located in the WEB-INF/classes directory, with the
  contents of environmentContext-im4java.xml and then customize
  to suit your needs.


----------------------------------------------------------------------
$Id: README-JMagick.txt,v 1.2 2007/01/25 03:47:50 matt Exp $
