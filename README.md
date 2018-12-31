Scala Slick PostgreSQL Timestamp Demo
=====================================

Demo program for helping to understand working with PostgreSQL
`timestamp without time zone` and `timestamp with time zone` data types.

Here is an example of the output:

    +-----+-----------------------------------------------+-----------------------------------------------+--------------------------------------------------------------------+
    | Id  | Timestamp Without Time Zone                   | Timestamp With Time Zone                      | Wall Clock Time On Insert                                          |
    +-----+-----------------------------------------------+-----------------------------------------------+--------------------------------------------------------------------+
    | 170 | 2016-12-04T16:23:47.407-07:00[America/Denver] | 2016-12-04T16:23:47.407-07:00[America/Denver] | 2016-12-04-16:23:47 Mountain Standard Time (America/Denver)        |
    | 171 | 2016-12-04T16:23:47.411-07:00[America/Denver] | 2016-12-04T16:23:47.411-07:00[America/Denver] | 2016-12-04-16:23:47 Mountain Standard Time (America/Denver)        |
    | 172 | 2016-12-05T02:24:21.885-07:00[America/Denver] | 2016-12-04T16:24:21.885-07:00[America/Denver] | 2016-12-05-02:24:21 Eastern African Time (Africa/Addis_Ababa)      |
    | 173 | 2016-12-05T02:24:21.889-07:00[America/Denver] | 2016-12-04T16:24:21.889-07:00[America/Denver] | 2016-12-05-02:24:21 Eastern African Time (Africa/Addis_Ababa)      |
    | 174 | 2016-12-05T11:24:34.792-07:00[America/Denver] | 2016-12-04T16:24:34.792-07:00[America/Denver] | 2016-12-05-11:24:34 Petropavlovsk-Kamchatski Time (Asia/Kamchatka) |
    | 175 | 2016-12-05T11:24:34.795-07:00[America/Denver] | 2016-12-04T16:24:34.795-07:00[America/Denver] | 2016-12-05-11:24:34 Petropavlovsk-Kamchatski Time (Asia/Kamchatka) |
    | 176 | 2016-12-05T00:24:43.122-07:00[America/Denver] | 2016-12-04T16:24:43.122-07:00[America/Denver] | 2016-12-05-00:24:43 Western African Time (Africa/Ndjamena)         |
    | 177 | 2016-12-05T00:24:43.126-07:00[America/Denver] | 2016-12-04T16:24:43.126-07:00[America/Denver] | 2016-12-05-00:24:43 Western African Time (Africa/Ndjamena)         |
    | 178 | 2016-12-04T15:24:53.943-07:00[America/Denver] | 2016-12-04T16:24:53.943-07:00[America/Denver] | 2016-12-04-15:24:53 Pacific Standard Time (America/Los_Angeles)    |
    | 179 | 2016-12-04T15:24:53.946-07:00[America/Denver] | 2016-12-04T16:24:53.946-07:00[America/Denver] | 2016-12-04-15:24:53 Pacific Standard Time (America/Los_Angeles)    |
    | 180 | 2016-12-04T23:25:40.489-07:00[America/Denver] | 2016-12-04T16:25:40.489-07:00[America/Denver] | 2016-12-04-23:25:40 Greenwich Mean Time (Europe/London)            |
    | 181 | 2016-12-04T23:25:40.492-07:00[America/Denver] | 2016-12-04T16:25:40.492-07:00[America/Denver] | 2016-12-04-23:25:40 Greenwich Mean Time (Europe/London)            |
    +-----+-----------------------------------------------+-----------------------------------------------+--------------------------------------------------------------------+


-----
Setup
-----


Create a user from your `bash` shell, not your DB shell:

    $ createuser -U postgres -P -s -e dev

Assign the password `password` unless you also change the one in
`application.conf`. Note that the last password requested is for the `postgres`
user, not the `dev` user.

Create a database called `tztest` and grant privileges:

    CREATE DATABASE tztest;
    GRANT ALL PRIVILEGES ON database tztest TO dev;

Create this table:

    CREATE TABLE tztest (
      id SERIAL PRIMARY KEY,
      timestamp TIMESTAMP WITHOUT TIME ZONE,
      timestamptz TIMESTAMP WITH TIME ZONE,
      description VARCHAR(1024)
    );

You may play with time zone settings:

    SHOW TIME ZONE;
    SET TIME ZONE 'Japan';

Also this query is useful to understand how the timestamps are stored:

    SELECT id, extract(epoch from timestamp) as "Time",
           extract(epoch from timestamptz) as "Time TZ",
           (extract(epoch from timestamp) -
            extract(epoch from timestamptz))/3600 as "Offset",
           description as "Client Wall Clock Time"
        FROM tztest ORDER BY id;


-------------
Build and Run
-------------

Build the jar file:

    sbt assembly

Run it:

    java -jar target/scala-2.11/timestamp_demo-assembly-0.2.0.jar


---------------------
Command Line Switches
---------------------

`--insert`

Insert two new rows into the `tztest` table. One row features timestamps
from `java.util.Calendar` library while the other row features timestamps from
the newer `java.time` library. The switch is boolean:

    --insert


----------
References
----------

* [Timestamps and Time Zones in PostgreSQL][]
* [8.5.3. Time Zones][]
* [Get date in current timezone in java][]
* [PostgreSQL 9.2 JDBC driver uses client time zone?][]
* [Difference between timestamps with/without time zone in PostgreSQL][]
* [How to get a java.time object from a java.sql.Timestamp][]
* [Get `Instant` from `ZonedDateTime` in java.time][]
* [Current Unix Timestamp UTC][]
* [Scala UTC timestamp in seconds since January 1st, 1970][]
* [Database handling for TimeZones][]


[Timestamps and Time Zones in PostgreSQL]: http://phili.pe/posts/timestamps-and-time-zones-in-postgresql/
[8.5.3. Time Zones]: https://www.postgresql.org/docs/9.1/static/datatype-datetime.html#DATATYPE-TIMEZONES
[Difference between timestamps with/without time zone in PostgreSQL]: http://stackoverflow.com/a/5876276
[Get date in current timezone in java]: http://stackoverflow.com/a/24808474
[PostgreSQL 9.2 JDBC driver uses client time zone?]: http://stackoverflow.com/a/18449597
[How to get a java.time object from a java.sql.Timestamp]: http://stackoverflow.com/a/22470650
[Get `Instant` from `ZonedDateTime` in java.time]: http://stackoverflow.com/a/31936813
[Current Unix Timestamp UTC]: http://www.unixtimestamp.com/
[Scala UTC timestamp in seconds since January 1st, 1970]: http://stackoverflow.com/a/11352208
[Database handling for TimeZones]: http://brian.pontarelli.com/2011/08/16/database-handling-for-timezones/
