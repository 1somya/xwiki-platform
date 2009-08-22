#!/bin/sh

# Ensure that the commands below are always started in the directory where this script is
# located. To do this we compute the location of the current script.

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
cd "$PRGDIR"

JETTY_HOME=jetty
JAVA_OPTS=-Xmx300m

# The port on which to start Jetty can be passed to this script as the first argument
if [ -n "$1" ]; then
  JETTY_PORT=$1
else
  JETTY_PORT=8080
fi

# The port on which to stop Jetty can be passed to this script as the second argument
if [ -n "$2" ]; then
  JETTY_STOPPORT=$2
else
  JETTY_STOPPORT=8079
fi

echo Starting Jetty on port $JETTY_PORT ...
echo Logs are in the $PRGDIR/xwiki.log file

# Ensure the logs directory exists as otherwise Jetty reports an error
mkdir -p $JETTY_HOME/logs 2>/dev/null

# Ensure the work directory exists so that Jetty uses it for its temporary files.
mkdir -p $JETTY_HOME/work 2>/dev/null

# Specify port and key to stop a running Jetty instance
JAVA_OPTS="$JAVA_OPTS -DSTOP.KEY=xwiki -DSTOP.PORT=$JETTY_STOPPORT"

java $JAVA_OPTS -Dfile.encoding=UTF8 -Djetty.port=$JETTY_PORT -Djetty.home=$JETTY_HOME -jar $JETTY_HOME/start.jar
