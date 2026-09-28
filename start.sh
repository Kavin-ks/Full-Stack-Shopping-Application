#!/usr/bin/env bash
# Starts MongoDB check → 4 microservices → API gateway → Vue dev server.
# Usage: ./start.sh            (build if needed, then start everything)
#        ./start.sh --rebuild  (force a fresh Maven build)
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
LOGS="$ROOT/backend/logs"
mkdir -p "$LOGS"

if ! nc -z localhost 27017 2>/dev/null; then
  echo "MongoDB is not running on localhost:27017."
  echo "Start it with:  brew services start mongodb-community"
  exit 1
fi

SERVICES="user-service product-service cart-service order-service api-gateway"
need_build=false
for s in $SERVICES; do [ -f "$ROOT/backend/$s/target/$s-1.0.0.jar" ] || need_build=true; done
if $need_build || [ "$1" == "--rebuild" ]; then
  echo "Building microservices..."
  (cd "$ROOT/backend" && mvn -q -DskipTests package)
fi

"$ROOT/stop.sh" >/dev/null 2>&1 || true

for s in $SERVICES; do
  echo "Starting $s..."
  nohup java -jar "$ROOT/backend/$s/target/$s-1.0.0.jar" > "$LOGS/$s.log" 2>&1 &
  echo $! > "$LOGS/$s.pid"
done

echo -n "Waiting for the API gateway"
for i in $(seq 1 60); do
  if curl -s -o /dev/null http://localhost:8090/api/products/categories; then echo " ready"; break; fi
  echo -n "."; sleep 1
done

if [ ! -d "$ROOT/frontend/node_modules" ]; then
  (cd "$ROOT/frontend" && npm install)
fi
echo "Starting Vue frontend on http://localhost:5180 ..."
nohup npm --prefix "$ROOT/frontend" run dev > "$LOGS/frontend.log" 2>&1 &
echo $! > "$LOGS/frontend.pid"

cat <<MSG

  Flipkart clone is running
  ─────────────────────────
  Web app          http://localhost:5180
  API gateway      http://localhost:8090
  user-service     http://localhost:8084   (db: flipkart_users)
  product-service  http://localhost:8081   (db: flipkart_products)
  cart-service     http://localhost:8082   (db: flipkart_carts)
  order-service    http://localhost:8083   (db: flipkart_orders)

  Seller Hub login: mobile 9999999999 (OTP is shown on screen)
  Logs: backend/logs/      Stop: ./stop.sh
MSG
