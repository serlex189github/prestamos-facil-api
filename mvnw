#!/bin/sh

set -eu

project_directory=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
wrapper_jar="$project_directory/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$wrapper_jar" ]; then
  echo "Maven Wrapper JAR not found: $wrapper_jar" >&2
  exit 1
fi

exec java ${MAVEN_OPTS:-} \
  -classpath "$wrapper_jar" \
  "-Dmaven.multiModuleProjectDirectory=$project_directory" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

