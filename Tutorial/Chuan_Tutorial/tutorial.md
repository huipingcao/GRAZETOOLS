Tools to pre-process GPS data representing cattle movement
==========================================================
***NOTE*** This document is written for SQL version of the code that is programmed by Chuan. Moreover, chuan's code follows another student. 

Tools for cow speed calculation
-------------------------------

This tutorial presents how to calculate distance traveled, path sinuosity, and woodland preference index for each cow.
There are 4 time periods considered: 24 hours; daytime hours; pre-sunrise night hours (from midnight to sunrise); and post-sunset night hours (from sunset to midnight). 

Refer to [link](range_speed.html)


Tools for extracting grazing pattern
--------------------------------------

This tutorial presents how to extract presumed grazing locations using animal velocity between two consecutive points, then these extracted grazing points were used to calculate percent grazed pixels, and pixel residence time, revisit rate (visits on different days), and return interval (interval between visits when cows visited the same pixel for more than once) for each animal. 

Refer to [link](range_pixel.html)

Tools for cow data partitioning
---------------------------------

This tutorial presents how to partition a GPS data collection of cows into 3 period by utilizing MySQL database management system.

The GPS data contains GPS coordinates for each cow over a whole day.  One day can be divided into 3 period: pre-sunrise, day time and post sunset.
This tutorial shows the instructions of using MySQL database management software to partition the data into 3 periods: pre-sunrise, day time and post sunset.
The results can benefit other range research that works on a certain period of the GPS data. 

Refer to [link](range_partition.html)
