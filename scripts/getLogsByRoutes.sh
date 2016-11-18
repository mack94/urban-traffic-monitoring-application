#!/bin/bash
# RUN THIS IN SOME DIRECTORY OF HOME DIRECTORY !!!
# OTHERWISE CURRENT LOGS WILL BE DELETED.
ROUTES_1_8_FILENAME="logs_1_8.zip"
ROUTES_9_16_FILENAME="logs_9_16.zip"
ROUTES_17_24_FILENAME="logs_17_24.zip"
ROUTES_25_32_FILENAME="logs_25_32.zip"
ROUTES_33_40_FILENAME="logs_33_40.zip"
ROUTES_41_48_FILENAME="logs_41_48.zip"
ROUTES_49_56_FILENAME="logs_49_56.zip"

for ZIP in `ls ../archiveLogs/aggregated/*`
do
	unzip ${ZIP} -d .
done

zip ${ROUTES_1_8_FILENAME} TrafficLog_1_8_*.log
zip ${ROUTES_9_16_FILENAME} TrafficLog_9_16_*.log
zip ${ROUTES_17_24_FILENAME} TrafficLog_17_24_*.log
zip ${ROUTES_25_32_FILENAME} TrafficLog_25_32_*.log
zip ${ROUTES_33_40_FILENAME} TrafficLog_33_40_*.log
zip ${ROUTES_41_48_FILENAME} TrafficLog_41_48_*.log
zip ${ROUTES_49_56_FILENAME} TrafficLog_49_56_*.log

mv logs_* ../archiveLogs/aggregated/
rm TrafficLog*.log
