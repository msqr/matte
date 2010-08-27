=======================================================================
Matte: share your photos, movies, and more
Version @VERSION@ @BUILD_DATE@
=======================================================================

This is the binary distribution of Matte. Matte is a personal photo,
movie, and audio organization and publishing tool. It allows you to 
upload media items to your web server and then organize these items 
into albums, which you can then publish and share with friends and
family. Matte is a web-based application which means you need an 
Internet-accessible server machine you can deploy it to (i.e. a 
hosting provider or your own personal server).


INSTALLATION ==========================================================

Matte is a Java web application, and requires a Java 5 runtime and a
J2EE servlet container to run in, such as Tomcat or JBoss. Matte also 
requires a relational database to store the data in. Matte has been 
tested with PostgreSQL, MySQL, and Apache Derby.

***** Note on Java 6 *****
  
  A bug in the Java 6 JAXP XSLT implementation prevents Matte from
  running. This is Sun bug 6537167:

  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6537167

  Until this bug is fixed, Matte will only work in a Java 5 runtime.


FIRST-TIME DATABASE CREATION ==========================================

  You must initialize your database for first-time use. Currently 
  PostgreSQL 7.4 or later is directly supported:
  
  
  POSTGRES ------------------------------------------------------------
  
  1) Create a database user for Matte
    
    As a Postgres super-user (e.g. "psql -U postgres template1"), 
    execute:
    
    $ createuser -ADEP matte
    
    You will be prompted to enter the matte user's password. If you 
    get a permissions error when you attempt to run this command, 
    make sure you are executing it as a Postgres user with sufficient
    privileges. You can explicity run as the Postgres super-user with
    
    $ createuser -U postgres -ADEP matte
    
    in which you may be prompted for the postgres user's password.
    
  2) Create a database for Matte
  
    As a Postgres super-user, execute:
    
    $ createdb -E UNICODE -O matte matte
    
    Again, you may need to pass the -U flag to specify a Postgres
    super-user account to execute the command as. The database
    must be created with the Unicode encoding!
    
  See the setup/sql/postgres/newdb.sql script for reference, or you
  can directly execute this with psql:
  
  $ psql -U postgres -d template1 -f setup/sql/postgres/newdb.sql
  

  MYSQL ---------------------------------------------------------------
  
  Matte has been tested against MySQL 4.1. It should work with any 
  greater version, too. The process is similar as above: create a 
  database and user for Matte using the mysql shell (or GUI tool):
  
  As a MySQL super-user (e.g. "mysql -u root mysql"), execute:
    
  mysql> create database matte character set utf8;
  mysql> grant all privileges on matte.* to 'matte'@'localhost' 
    -> identified by 'matte';
  mysql> flush privileges;
  

FIRST-TIME DATABASE SETUP =============================================

  After creating the database for the first time, you must run
  some SQL scripts to create the Matte database tables.
  
  
  POSTGRES ------------------------------------------------------------
  
  Execute:
  
  $ psql -U matte -d matte -f setup/sql/postgres/create-tables.sql
  $ psql -U matte -d matte -f setup/sql/postgres/create-system.sql
  
  Ignore any warnings about "table X does not exist".
  

  MYSQL ---------------------------------------------------------------
  
  Execute:
  
  $ mysql -u matte -p matte -f <setup/sql/mysql/create-tables.sql
  $ mysql -u matte -p matte -f <setup/sql/mysql/create-system.sql
  
  Ignore any warnings about "Table 'X' doesn't exist".


FIRST-TIME APPSERVER SETUP ============================================

  Matte depends on the application server it is running in to provide 
  a JDBC DataSource to connect to the database with and a JavaMail 
  Session for sending mail with. Thus you must configure the DataSource
  and Session the first time you install Matte.
  
  For Tomcat 5.5, create the DataSource first by creating a deployment
  context file named <TOMCAT HOME>/conf/Catalina/localhost/matte.xml.
  Add the below XML (adjust the parameter values as necessary for your 
  environment, but if you are following these directions from the start, 
  these should work for you).
  
  Note that Tomcat does not ship with the JavaMail binaries, you must
  download both JavaMail and the required Java Activation Framework for
  for the javax.mail.Session support required by Matte. See the 
  
  http://tomcat.apache.org/tomcat-5.5-doc/jndi-resources-howto.html
  
  page (the JavaMail Sessions section) for more information and links
  to the associated download pages. Place the JARs in the 
  <TOMCAT HOME>/common/lib directory.
  
  
  POSTGRES ------------------------------------------------------------
  
  <Context path="/matte" 
    className="org.apache.catalina.core.StandardContext" 
    crossContext="false" reloadable="false" 
    mapperClass="org.apache.catalina.core.StandardContextMapper" 
    useNaming="true" debug="0" swallowOutput="false" privileged="false" 
    displayName="Matte" 
    wrapperClass="org.apache.catalina.core.StandardWrapper" 
    docBase="/path/to/matte.war"  cookies="true"
    cachingAllowed="true" 
    charsetMapperClass="org.apache.catalina.util.CharsetMapper">
    
    <Resource name="jdbc/matte" 
      type="javax.sql.DataSource" scope="Shareable"
      driverClassName="org.postgresql.Driver" 
      url="jdbc:postgresql://localhost:5432/matte"
      username="matte" password="matte" maxWait="5000"
      maxActive="4" maxIdle="2" removeAbandoned="true"
      removeAbandonedTimeout="60" logAbandoned="true"
      validationQuery="select CURRENT_TIMESTAMP"
    />
    
    <Resource name="mail/matte" 
      type="javax.mail.Session" scope="Shareable"
      mail.smtp.host="localhost" 
    />
  </Context>
    
  Then, if you don't already have the Postgres JDBC driver added to 
  Tomcat, copy setup/lib/postgresql-8.1-407.jdbc3.jar to the 
  <TOMCAT HOME>/common/lib directory.


  MYSQL ---------------------------------------------------------------
  
  The setup is similar to that shown above, but change the DataSource
  configuration to use MySQL settings. For example:
  
  <Context path="/matte" ...>
    <Resource name="jdbc/matte" 
      type="javax.sql.DataSource" scope="Shareable"
      driverClassName="com.mysql.jdbc.Driver" 
      url="jdbc:mysql://localhost:3306/matte"
      username="matte" password="matte" maxWait="5000"
      maxActive="4" maxIdle="2" removeAbandoned="true"
      removeAbandonedTimeout="60" logAbandoned="true"
    />
    <Resource path="mail/matte" .../>
  </Context>

  Then, if you don't already have the MySQL JDBC driver added to
  Tomcat, copy setup/lib/mysql-connector-java-5.0.5-bin.jar to the
  <TOMCAT HOME>/common/lib directory.
  
  
APPLICATION SETUP =====================================================

  To install, copy the WAR file included in this package your 
  application server's deployment directory. For Tomcat this defaults
  to <TOMCAT HOME>/webapps.
  
  Finally, you might want to adjust the application logging, which 
  by default will attempt to log via Log4J to the console. You 
  can adjust the verbosity and location of this log by unpacking the 
  WAR and editing the <MATTE WAR>/WEB-INF/classes/log4j.properties 
  Log4J configuration. Then either change your application server 
  configuration to point to the unpacked WAR directory or zip up the 
  unpacked WAR directory back to the original file name.

  
FIRST-TIME USE ========================================================

  Start up your application server. Once started, visit 
  
  http://<your server>:<port>/matte
  
  where <your server> is the machine Matte is running on (i.e. 
  localhost) and <port> is the port the applicaiton server is 
  listening on. For Tomcat this would default to:
  
  http://localhost:8080/matte
  
  You should see the Matte Setup Wizard page. The Setup Wizard will 
  guide you through configuring the remaining Matte options.

