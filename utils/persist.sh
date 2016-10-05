#!/bin/bash
DATE=`date -d "yesterday 13:00" +%y-%m-%d`
DAY_OF_WEEK=`date -d "yesterday 13:00" +%a`
CURRENT_FILENAME="logs_"${DATE}"_"${DAY_OF_WEEK}".zip"
DAY_FILENAME="logs_"${DAY_OF_WEEK}".zip"

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

sshpass -p ganawese scp ${CURRENT_FILENAME} janusz@student.agh.edu.pl:/home/ietgrp/janusz/public_html/TrafficLogs/
sshpass -p ganawese scp ${DAY_FILENAME} janusz@student.agh.edu.pl:/home/ietgrp/janusz/public_html/TrafficLogs/aggregated/

mv ${CURRENT_FILENAME} ./archiveLogs/
mv ${DAY_FILENAME} ./archiveLogs/aggregated/
rm TrafficLog*___*.log
