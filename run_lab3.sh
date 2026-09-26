#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Laboratory work No. 3 — runs every experiment and saves the real outputs
# into ./lab3-results so they can be copied into the report.
#
# Usage (from the project root):   ./run_lab3.sh
# Requirements: JDK 25 (or 21 with java.version=21 in pom.xml), curl.
# ---------------------------------------------------------------------------
set -u
cd "$(dirname "$0")"

OUT=lab3-results
JAR=target/spring-lab-01-0.0.1-SNAPSHOT.jar
YML=src/main/resources/application.yml
mkdir -p "$OUT"
unset APP_MAIL_RETRYCOUNT SPRING_PROFILES_ACTIVE

pretty() { if command -v python3 >/dev/null 2>&1; then python3 -m json.tool; else cat; fi; }

build() {
    echo ">>> Building the jar"
    ./mvnw -q clean package -DskipTests || { echo "Build failed"; exit 1; }
}

# start <port> <logfile> <java args...>  — starts the jar in the background
start() {
    local port=$1 log=$2; shift 2
    java "$@" > "$log" 2>&1 &
    PID=$!
    for _ in $(seq 1 60); do
        if curl -s "http://localhost:$port/actuator/health" >/dev/null 2>&1; then return 0; fi
        if ! kill -0 $PID 2>/dev/null; then return 1; fi
        sleep 1
    done
    return 1
}
stop() { kill $PID 2>/dev/null; wait $PID 2>/dev/null; sleep 1; }

retry_count() { curl -s "http://localhost:$1/api/lab3/config" | sed -n 's/.*"mailRetryCount":\([0-9]*\).*/\1/p'; }

build

# --- Task 3: three profiles --------------------------------------------------
echo ">>> Task 3: no profile / dev / prod"
start 8080 "$OUT/run-default.log" -jar "$JAR" \
    && curl -s http://localhost:8080/api/lab3/config | pretty > "$OUT/config-default.json"; stop
start 8081 "$OUT/run-dev.log" -jar "$JAR" --spring.profiles.active=dev \
    && curl -s http://localhost:8081/api/lab3/config | pretty > "$OUT/config-dev.json"; stop
start 8080 "$OUT/run-prod.log" -jar "$JAR" --spring.profiles.active=prod \
    && curl -s http://localhost:8080/api/lab3/config | pretty > "$OUT/config-prod.json"; stop

# --- Task 4: precedence table ------------------------------------------------
echo ">>> Task 4: precedence of property sources"
T="$OUT/precedence-table.txt"
printf "%-45s | %-8s | %s\n" "What is set" "Expected" "Actual" > "$T"
printf -- "---------------------------------------------+----------+-------\n" >> "$T"

start 8080 /dev/null -jar "$JAR"
printf "%-45s | %-8s | %s\n" "application.yml only" 3 "$(retry_count 8080)" >> "$T"; stop

export APP_MAIL_RETRYCOUNT=7
start 8080 /dev/null -jar "$JAR"
printf "%-45s | %-8s | %s\n" "yml + environment variable" 7 "$(retry_count 8080)" >> "$T"; stop

start 8080 /dev/null -Dapp.mail.retry-count=5 -jar "$JAR"
printf "%-45s | %-8s | %s\n" "yml + variable + system property" 5 "$(retry_count 8080)" >> "$T"
curl -s http://localhost:8080/actuator/env/app.mail.retry-count | pretty > "$OUT/actuator-env-sysprop.json"; stop

start 8080 /dev/null -jar "$JAR" --app.mail.retry-count=9
printf "%-45s | %-8s | %s\n" "yml + variable + command line argument" 9 "$(retry_count 8080)" >> "$T"
curl -s http://localhost:8080/actuator/env/app.mail.retry-count | pretty > "$OUT/actuator-env-cmdline.json"; stop

start 8081 /dev/null -jar "$JAR" --spring.profiles.active=dev --app.mail.retry-count=9
printf "%-45s | %-8s | %s\n" "The dev profile + command line argument" 9 "$(retry_count 8081)" >> "$T"; stop
unset APP_MAIL_RETRYCOUNT
cat "$T"

# --- Task 5: actuator ---------------------------------------------------------
echo ">>> Task 5: actuator endpoints and --debug report"
start 8080 "$OUT/run-debug.log" -jar "$JAR" --debug
curl -s http://localhost:8080/actuator/env/app.mail.retry-count | pretty > "$OUT/actuator-env-retry-count.json"
curl -s http://localhost:8080/actuator/configprops | pretty > "$OUT/actuator-configprops.json"
curl -s http://localhost:8080/actuator/conditions | pretty > "$OUT/actuator-conditions.json"
stop
{
  echo "=== Positive match example ==="
  grep -A3 "DispatcherServletAutoConfiguration matched" "$OUT/run-debug.log" | head -4
  echo
  echo "=== Negative match example ==="
  grep -A3 "ProblemDetailsErrorHandlingConfiguration:" "$OUT/run-debug.log" | head -4
} > "$OUT/debug-report-examples.txt"
cat "$OUT/debug-report-examples.txt"

# --- Task 2: validation experiment (retry-count: 99) -----------------------
echo ">>> Task 2: validation experiment with retry-count: 99"
cp "$YML" "$YML.bak"
sed -i.tmp 's/retry-count: 3/retry-count: 99/' "$YML" && rm -f "$YML.tmp"
./mvnw -q clean package -DskipTests
java -jar "$JAR" > "$OUT/validation-error.log" 2>&1 &
PID=$!; sleep 15; kill $PID 2>/dev/null; wait $PID 2>/dev/null
mv "$YML.bak" "$YML"
sed -n '/APPLICATION FAILED TO START/,/Update your application/p' "$OUT/validation-error.log" \
    > "$OUT/validation-error.txt"
cat "$OUT/validation-error.txt"
build   # rebuild with the correct value

# --- Git ------------------------------------------------------------------------
git log --oneline > "$OUT/git-log.txt" 2>/dev/null
echo
echo "All results are saved in $OUT/"
