#! /bin/bash

# G1 Collector latency optimized
# - MaxGCPauseMillis - 200 ms (250 default)
# - AlwaysPreTouch - for latency touch all the pages
# - InitiatingHeapOccupancyPercent - during young collection; threshold of old gen before kicking of 'concurrent start'
#     70% (45% default)
# - MaxRAMPercentage - size of Heap relative to the available memory in the container/vm 80%
# - OmitStackTraceInFastThrow - Repeat intrinsic JVM exceptions (default no-repeat)
# - StartFlightRecording - Starts the blackbox recorder which provides continuous recording, dumps at exit
# - PrintFinalFlag - Dump JVM config struct
JAVA_OPTS="-Ddb.config.file.template=$DB_CONFIG_FILE_TEMPLATE \
            -XX:+UseZGC \
            -XX:MaxRAMPercentage=80 \
            -XX:-OmitStackTraceInFastThrow \
            -XX:StartFlightRecording=disk=false \
            -Dfile.encoding=UTF8 \
            -XX:+PrintFlagsFinal"

 exec java $DEBUG_OPTS $JAVA_OPTS -jar /service/bin/jlg-dw4-otk-server.jar server /service/conf/service.yaml
