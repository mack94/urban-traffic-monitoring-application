cd archiveLogs/

for FILE in `ls`
do
	if [ -f $FILE ]; then
		PARTS=$(echo $FILE | awk -F_ '{print $1; print $2; print $3; print $8}')
		i=0
		for PART in $PARTS
		do
			i=$((i+1))
		done
		if [ $i -eq 2 ]; then
			j=0
			for PART in $PARTS
			do
				j=$((j+1))
				if [ $j -eq 2 ]; then
					DATE=$(echo $PART | awk -F. '{print "20"$1}')
					DAY_OF_WEEK=`date -d"${DATE}" +%a`
					DATE=$(echo $PART | awk -F. '{print $1}')
				fi
			done
			mv $FILE "logs_"$DATE"_"$DAY_OF_WEEK".zip"
		elif [ $i -eq 3 ]; then
			j=0
			for PART in $PARTS
			do
				j=$((j+1))
				if [ $j -eq 2 ]; then
					DAY_OF_WEEK=$PART
				elif [ $j -eq 3 ]; then
					DATE=$(echo $PART | awk -F. '{print $1}')
				fi
			done
			mv $FILE "logs_"$DATE"_"$DAY_OF_WEEK".zip"
		else
			j=0
			for PART in $PARTS
			do
				j=$((j+1))
				if [ $j -eq 2 ]; then
					DAY_OF_WEEK=$PART
				elif [ $j -eq 3 ]; then
					DATE=$(echo $PART | awk -F. '{print $1}')
				elif [ $j -eq 4 ]; then
					SUFFIX=$PART
					echo "   "$SUFFIX
				fi
			done
			mv $FILE "logs_"$DATE"_"$DAY_OF_WEEK"_____"$SUFFIX
		fi
	fi
done
