#!/bin/bash
DATE=`date -d "yesterday 13:00" +%y-%m-%d`
DAY_OF_WEEK=`date -d "yesterday 13:00" +%a`
CURRENT_FILENAME="logs_"${DATE}"_"${DAY_OF_WEEK}".zip"
DAY_FILENAME="logs_"${DAY_OF_WEEK}".zip"
ROUTES_1_8_FILENAME="logs_1_8.zip"
ROUTES_9_16_FILENAME="logs_9_16.zip"
ROUTES_17_24_FILENAME="logs_17_24.zip"
ROUTES_25_32_FILENAME="logs_25_32.zip"
ROUTES_33_40_FILENAME="logs_33_40.zip"
ROUTES_41_48_FILENAME="logs_41_48.zip"
ROUTES_49_56_FILENAME="logs_49_56.zip"

for FILE in TrafficLog*
do
	NAME=`echo "${FILE}" | cut -d'.' -f1`
	EXTENSION=`echo "${FILE}" | cut -d'.' -f2`
	mv ${FILE} ${NAME}___${DAY_OF_WEEK}_${DATE}.${EXTENSION}
done
sleep 15
zip ${CURRENT_FILENAME} TrafficLog*___*.log

for ZIP in `ls archiveLogs/*${DAY_OF_WEEK}*`
do
	unzip ${ZIP} -d .
done
zip ${DAY_FILENAME} TrafficLog*___*.log

rm TrafficLog*___*.log
unzip ${CURRENT_FILENAME} -d .

unzip ./archiveLogs/aggregated/${ROUTES_1_8_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_9_16_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_17_24_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_25_32_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_33_40_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_41_48_FILENAME} -d .
unzip ./archiveLogs/aggregated/${ROUTES_49_56_FILENAME} -d .

zip ${ROUTES_1_8_FILENAME} TrafficLog_1_8_*.log
zip ${ROUTES_9_16_FILENAME} TrafficLog_9_16_*.log
zip ${ROUTES_17_24_FILENAME} TrafficLog_17_24_*.log
zip ${ROUTES_25_32_FILENAME} TrafficLog_25_32_*.log
zip ${ROUTES_33_40_FILENAME} TrafficLog_33_40_*.log
zip ${ROUTES_41_48_FILENAME} TrafficLog_41_48_*.log
zip ${ROUTES_49_56_FILENAME} TrafficLog_49_56_*.log

sshpass -p ganawese scp ${CURRENT_FILENAME} janusz@student.agh.edu.pl:/home/ietgrp/janusz/public_html/TrafficLogs/
mv ${CURRENT_FILENAME} ./archiveLogs/

sshpass -p ganawese scp logs_*.zip janusz@student.agh.edu.pl:/home/ietgrp/janusz/public_html/TrafficLogs/aggregated/

mv logs_*.zip ./archiveLogs/aggregated/
rm TrafficLog*___*.log
