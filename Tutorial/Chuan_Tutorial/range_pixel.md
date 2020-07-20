Tools for extracting grazing pattern
======================================

Tool description
-------------------
This tutorial presents how to extract presumed grazing locations using animal velocity between two consecutive points, then these extracted grazing points were used to calculate percent grazed pixels, and pixel residence time, revisit rate (visits on different days), and return interval (interval between visits when cows visited the same pixel for more than once) for each animal. 

Pixels in this project are 30m x 30m. 

Download and install MySQL and MySQL workbench
----------------------------------------------

1.  Download and install MySQL from [link](http://dev.mysql.com/downloads/mysql/)

2.  Download and install MySQL workbench from [link](http://dev.mysql.com/downloads/workbench/)

Create a new connection to MySQL database
-----------------------------------------

1. Open MySQL workbench

   [![mysql-workbench-1.png](fig/mysql-workbench-1-small.png)](fig/mysql-workbench-1.png)
   click to zoom in

2. Click new connection button

   [![mysql-workbench-2.png](fig/mysql-workbench-2-small.png)](fig/mysql-workbench-2.png)
   click to zoom in

3. Name the new connection

   [![mysql-workbench-3.png](fig/mysql-workbench-3-small.png)](fig/mysql-workbench-3.png)
   click to zoom in

4. Double click new connection, you will see a new window opened

   [![mysql-workbench-4.png](fig/mysql-workbench-4-small.png)](fig/mysql-workbench-4.png)
   click to zoom in

Create a new database for your cow data
---------------------------------------

Setup password for root user (optional)
in the new opened SQL File window, type

```
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('newpwd');
```

[![mysql-workbench-5.png](fig/mysql-workbench-5-small.png)](fig/mysql-workbench-5.png)
click to zoom in

Then click execute button to execute the script.


Preprocessing the data
----------------------

1. Convert your GPS data into csv format and use comma as delimiter.
   In total the csv file should only have 5 columns.  They are:
   id,CowID,Date,northing,easting

   *  id is the unique record of each GPS data record. 
   *  CowID is an integer.  
   *  The format of Date is yyyy-mm-dd.  
   *  Northing and easting are float numbers.

   [Here](files/range_pixel/cordinate_points.csv) is an example of GPS data.

2. Convert your pixel data into csv format and use comma as delimiter.
   In total the csv file should only have 3 columns.  They are: pid, y, x
   *  id is the unique id for each pixel
   *  y,x are northing and easting coordinate respectively.  They are float numbers.

   [Here](files/range_pixel/pixel.csv) is an example of pixel data.

3. Open [range_db.sql](./files/range_pixel/range_db.sql) file from MySQL workbench.

   [![mysql-workbench-7.png](fig/mysql-workbench-7-small.png)](fig/mysql-workbench-7.png)
   click to zoom in

   Execute range_db.sql.  A database will be created for your application.

Extract grazing pattern
-----------------------

1. Open [grazing_pattern.sql](./files/range_pixel/grazing_pattern.sql) file from MySQL workbench. 
   Specify the path you want to export your result.  Then execute the script. 

   [![mysql-workbench-8.png](fig/mysql-workbench-8-small.png)](fig/mysql-workbench-8.png)
   click to zoom in
