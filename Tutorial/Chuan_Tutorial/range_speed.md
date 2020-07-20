Tools for cow speed calculation
=============================
Tool description
-------------------

This tutorial presents how to calculate distance traveled, path sinuosity, and woodland preference index for each cow.
There are 4 time periods considered: 24 hours; daytime hours; pre-sunrise night hours (from midnight to sunrise); and post-sunset night hours (from sunset to midnight). 

Program
-------

We design a Java program that can automatically do this work.  The program can be downloaded at [link](./files/range_speed/AnimalDataInitialProcessing/Calculations.jar)

Date format
-----------

The input of this the Java program are 3 files:

1.  DataWithPosition.csv.  It contains 6 columns:
    1.  CowID
    2.  Date (mm/dd/yyyy)
    3.  Time (hh:mm:ss AM/PM)
    4.  Northing and easting (GPS coordinate)
    5.  Woodlan (1 if the cow is in woodlang; -9999 else)

2.  Time.csv.  It contains 3 columns:  
    1.  Date (yyyy-mm-dd)
    2.  Sunrise and sunset (hh-mm-dd)

3. Weather.csv.  It contains 7 columns:
    1.  Date (mm/dd/yyyy) 
    2.  Cum\_PPT\_in (float number)
    3.  Act\_PPT\_in (float number)
    4.  Temp_c (float number)
    5.  Wind_degree (float number)
    6.  Wind_mph (float number)
    7.  Lunar (float number)

Here are the examples of these 3 files: [DataWithPosition.csv](./files/range_speed/AnimalDataInitialProcessing/DataWithPosition.csv)  [Time.csv](./files/range_speed/AnimalDataInitialProcessing/Time.csv)  [Weather.csv](./files/range_speed/AnimalDataInitialProcessing/Weather.csv)

Run program
-----------

Put your data and the Java program under the same folder, then run

```
java -cp Calculations.jar
```

Then the program will output 2 files DistanceandSiniosity.csv, CompleteProcessedData.csv.
Here are the examples of the output files: [DistanceandSiniosity.csv](./files/range_speed/AnimalDataInitialProcessing/DistanceandSiniosity.csv) [CompleteProcessedData.csv](./files/range_speed/AnimalDataInitialProcessing/CompleteProcessedData.csv)


