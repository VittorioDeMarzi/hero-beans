#!/bin/bash
set -euo pipefail

# Load environment variables from .env
if [ -f /home/ubuntu/app/.env ]; then
  export $(grep -v '^#' /home/ubuntu/app/.env | xargs)
fi

# Ensure logs folder exists
mkdir -p /home/ubuntu/app/logs

# Find JAR
JAR_FILE=$(ls /home/ubuntu/app/build/libs/*.jar | head -n 1 || true)

PROFILE="${1:-${SPRING_PROFILES_ACTIVE:-prod}}"

SPRING_OPTS="--spring.profiles.active=${PROFILE} --logging.config=classpath:logback-spring.xml"

if [ -z "$JAR_FILE" ]; then
  echo ">>> [ApplicationStart] No JAR file found!"
  exit 1
fi

echo ">>> [ApplicationStart] Starting: $JAR_FILE (profile=${PROFILE})"
nohup java $JAVA_OPTS -jar "$JAR_FILE" $SPRING_OPTS > /home/ubuntu/app/logs/app.log 2>&1 &

# Save PID
echo $! > /home/ubuntu/app/app.pid

echo ">>> [ApplicationStart] Application started with PID $(cat /home/ubuntu/app/app.pid)"
