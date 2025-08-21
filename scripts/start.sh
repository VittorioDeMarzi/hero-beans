#!/bin/bash
set -euo pipefail


JAR_FILE=$(ls /home/ubuntu/app/build/libs/*.jar | head -n 1 || true)

PROFILE="${1:-${SPRING_PROFILES_ACTIVE:-prod}}"

SPRING_OPTS="--spring.profiles.active=${PROFILE} --logging.config=classpath:logback-spring.xml"

if [ -z "$JAR_FILE" ]; then
  echo ">>> [ApplicationStart] No JAR file found!"
  exit 1
fi

echo ">>> [ApplicationStart] Starting: $JAR_FILE (profile=${PROFILE})"
nohup java $JAVA_OPTS -jar "$JAR_FILE" $SPRING_OPTS > /dev/null 2>&1 &

