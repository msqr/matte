#!/bin/sh

DEBUG=""

while getopts ":d" opt; do
	case $opt in
		d)
			DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8081,server=y,suspend=n"
			;;
	esac
done

#shift $(($OPTIND - 1))

# find our CATALINA_HOME (copied from catalina.sh)
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CATALINA_HOME if not already set
[ -z "$CATALINA_HOME" ] && CATALINA_HOME=`cd "$PRGDIR/apache-tomcat" ; pwd`

export CATALINA_OPTS="-Dderby.system.home=$PRGDIR/matte/db $DEBUG"

echo "Using CATALINA_OPTS:   ${CATALINA_OPTS}"

sh $CATALINA_HOME/bin/catalina.sh start
