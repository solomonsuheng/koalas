#!/bin/bash
#
# ---------------------------------------------------------------------
# Koalas startup script.
# ---------------------------------------------------------------------
clear
DIRNAME=`which dirname`

# ---------------------------------------------------------------------
# Get Current Working Directory of the Script(default:Scirpt in bin/)
# ---------------------------------------------------------------------
CURRENT_WORKING_HOME="$(cd `"$DIRNAME" $0`;pwd)"
MAIN_HOME="$CURRENT_WORKING_HOME"
MAIN_CLASS_JAR="$MAIN_HOME/target/koalas-1.0.jar"
MAIN_CLASS_NAME="io.transwarp.maintenance.koalas.Koalas"
# ---------------------------------------------------------------------
# Inspect JAVA Environment or SCALA Environment
# ---------------------------------------------------------------------
RUNNER=""
if [ `command -v java` ]; then
  RUNNER="java"
elif [ `command -v scala` ]; then
  RUNNER="scala"
else
  echo "No Java or Scala is installed, PLease install Java or Scala."
  exit 1
fi

# ---------------------------------------------------------------------
# Set ClassPath(default:jars in lib)
# ---------------------------------------------------------------------
CLASSPATH="$MAIN_CLASS_JAR"
if [ -e "$MAIN_HOME/target/lib" ]; then
  CLASSPATH+=":$MAIN_HOME/target/lib/*"
fi

# ---------------------------------------------------------------------
# Load .so File
# ---------------------------------------------------------------------
JNI_PATH="-Djava.library.path="
JNI_PATH+="$CURRENT_WORKING_HOME/*.so"

# ---------------------------------------------------------------------
# Execute Java
# ---------------------------------------------------------------------
if [ "$RUNNER" = "java" ]; then
  exec "$RUNNER" "$JNI_PATH" -cp "$CLASSPATH" "$MAIN_CLASS_NAME" "$@"
else
  exec "$RUNNER" "$JNI_PATH" -cp "$CLASSPATH" "$MAIN_CLASS_NAME" "$@"
fi



