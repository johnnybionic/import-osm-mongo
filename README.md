# import-osm-mongo 

This is a simple app that's used to read an OpenStreetMap export file (OSM, in XML format) and insert into a 
MongoDB collection. 

It's the first step in adapting a Coffee Shop application that originated as a presentation shown here:

https://www.infoq.com/presentations/demo-java-javascript-mongodb

I've been adapting that presentation to use Spring Boot (see elsewhere on my github). In order
to get anything to run, you need sample data - coffee shops & cafes, with their locations. This is extracted
from OpenStreetMap - an example file is included. 

 I've created a separate project as the Spring Boot-managed MongoDB drivers are out-of-date (v2 vs. v3.x), and do
 not support the Document or Point types. 
 
 This project makes use of BasicOSMParser here:
 
 https://github.com/PanierAvide/BasicOSMParser
 
 For information on how to extract OSM data refer to my blog entry:
 
 http://www.johnnybionic.me.uk/wordpress/2016/10/26/revisiting-the-coffee-shop-apps/

