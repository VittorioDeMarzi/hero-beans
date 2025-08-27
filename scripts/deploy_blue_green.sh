#!/bin/bash
set -e

APP_NAME="herobeans-backend"
APP_DIR="/home/ubuntu/app"

PORT_BLUE=8081
PORT_GREEN=8082
ACTIVE_UPSTREAM_CONFIG="/etc/nginx/snippets/active_upstream.conf"
HEALTH_CHECK_ENDPOINT="/actuator/health"

PROFILE="${1:-${SPRING_PROFILES_ACTIVE:-prod}}"


if [ ! -f $ACTIVE_UPSTREAM_CONFIG ] || grep -q "blue" $ACTIVE_UPSTREAM_CONFIG; then
    CURRENT_COLOR="blue"
    CURRENT_PORT=$PORT_BLUE
    NEXT_COLOR="green"
    NEXT_PORT=$PORT_GREEN
else
    CURRENT_COLOR="green"
    CURRENT_PORT=$PORT_GREEN
    NEXT_COLOR="blue"
    NEXT_PORT=$PORT_BLUE
fi

if [[ -f "$APP_DIR/.env" ]]; then
  set -a
  source "$APP_DIR/.env"
  set +a
fi

JAR_FILE=$(ls /home/ubuntu/app/build/libs/*.jar | head -n 1 || true)
SPRING_OPTS="--spring.profiles.active=${PROFILE} --logging.config=classpath:logback-spring.xml"

if [ -z "$JAR_FILE" ]; then
  echo ">>> [ApplicationStart] No JAR file found!"
  exit 1
fi

JVM_OPTS="-Dserver.port=${NEXT_PORT}"

echo "[deploy] current=$CURRENT_COLOR:$CURRENT_PORT  next=$NEXT_COLOR:$NEXT_PORT"
echo "[deploy] jar=${JAR_FILE}  profile=${PROFILE}"

nohup java ${JVM_OPTS} -jar "${JAR_FILE}" ${SPRING_OPTS}> "${APP_DIR}/app-${NEXT_COLOR}.log" 2>&1 &
NEW_PID=$!

sleep 60

HEALTH_CHECK_URL="http://127.0.0.1:$NEXT_PORT$HEALTH_CHECK_ENDPOINT"

for i in {1..10}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)
    if [ "$HTTP_CODE" == "200" ]; then

        echo "set \$active_upstream ${NEXT_COLOR};" | sudo tee "${ACTIVE_UPSTREAM_CONFIG}" >/dev/null
        sudo nginx -t && sudo systemctl reload nginx

        OLD_PID=$(lsof -t -i:$CURRENT_PORT)
        if [ -n "$OLD_PID" ]; then
            kill $OLD_PID
        fi
        exit 0
    fi
    echo "[deploy] health not ready on ${NEXT_PORT} (attempt ${i}/10); retrying..."
    sleep 10
done

kill "${NEW_PID}" || true
echo "[deploy] ERROR: health checks failed on ${NEXT_PORT}"
exit 1
