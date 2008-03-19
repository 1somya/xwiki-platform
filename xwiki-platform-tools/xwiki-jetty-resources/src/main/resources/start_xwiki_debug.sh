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
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# In order to avoid getting a "java.lang.IllegalStateException: Form too large" error
# when editing large page in XWiki we need to tell Jetty to allow for large content
# since by default it only allows for 20K. We do this by passing the
# org.mortbay.http.HttpRequest.maxFormContentSize property.
# Note that setting this value too high can leave your server vulnerable to denial of
# service attacks.
JAVA_OPTS="$JAVA_OPTS -Dorg.mortbay.http.HttpRequest.maxFormContentSize=1000000"

# The port on which to start Jetty can be passed to this script as the first argument
if [ -n "$1" ]; then
  JETTY_PORT=$1
else
  JETTY_PORT=8080
fi

# For enabling YourKit Profiling.
# $3 must the path where Yourkit can find the agent.
# For example: "/Applications/YourKit Java Profiler 7.0.11.app/bin/mac"
# Note: you must also pass the port as $1 for now till we use getopts.
if [ "$2" = "profiler" ]; then
  JAVA_OPTS="$JAVA_OPTS -agentlib:yjpagent"
  export DYLD_LIBRARY_PATH="$3"
fi

echo Starting Jetty on port $JETTY_PORT ...
echo Logs are in the $PRGDIR/xwiki.log file

# Ensure the logs directory exists as otherwise Jetty reports an error
mkdir -p $JETTY_HOME/logs 2>/dev/null

LANG=fr_FR.ISO8859-1 java $JAVA_OPTS -Dfile.encoding=iso-8859-1 -Djetty.port=$JETTY_PORT -Djetty.home=$JETTY_HOME -jar $JETTY_HOME/start.jar
