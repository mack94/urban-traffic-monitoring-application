# urban-traffic-monitoring-application
Application for monitoring urban traffic. 

## Compile

Change the Api Key in config.propersties, set up right routes in routes.json and then run the command

`gradle jar` 

The *.jar should appear in the build/lib directory. 


## Run on jagullar 

Open your crontab file

`crontab -e`

and add following line

`0,15,30,45 * * * * java -jar /home/stud/USERNAME/YOUR_PATH_TO_JAR_FILE`

Close vi saving your crontab file. After the first run of the program the log file should appear in your home directory.

## Info:

The Google Distance Matrix API updates every 30 second (during a day).
