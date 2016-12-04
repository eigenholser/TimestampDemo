=====================================
Scala Slick PostgreSQL Timestamp Demo
=====================================

Demo program for helping to understand working with PostgreSQL
`timestamp without time zone` and `timestamp with time zone` data types.


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
* [Difference between timestamps with/without time zone in PostgreSQL][]
* [How to get a java.time object from a java.sql.Timestamp][]
* [Get `Instant` from `ZonedDateTime` in java.time][]
* [Current Unix Timestamp UTC][]
* [Scala UTC timestamp in seconds since January 1st, 1970][]


[Timestamps and Time Zones in PostgreSQL]: http://phili.pe/posts/timestamps-and-time-zones-in-postgresql/
[8.5.3. Time Zones]: https://www.postgresql.org/docs/9.1/static/datatype-datetime.html#DATATYPE-TIMEZONES
[Difference between timestamps with/without time zone in PostgreSQL]: http://stackoverflow.com/a/5876276
[Get date in current timezone in java]: http://stackoverflow.com/a/24808474
[How to get a java.time object from a java.sql.Timestamp]: http://stackoverflow.com/a/22470650
[Get `Instant` from `ZonedDateTime` in java.time]: http://stackoverflow.com/a/31936813
[Current Unix Timestamp UTC]: http://www.unixtimestamp.com/
[Scala UTC timestamp in seconds since January 1st, 1970]: http://stackoverflow.com/a/11352208
