"# import-osm-mongo" 

This is a simple app that's used to read an OpenStreetMap export file (OSM, in XML format) and insert into a 
MongoDB collection. 

It's part of a Coffee Shop application that originated as a presentation shown here:

https://www.infoq.com/presentations/demo-java-javascript-mongodb

I've been adapting that presentation to use Spring Boot (see elsewhere on my github). In order
to get anything to run, you need sample data - coffee shops & cafes, with their locations. This is extracted
from OpenStreetMap - an example file is included. 

 I've created a separate project as the Spring Boot-managed MongoDB drivers are out-of-date (v2 vs. v3.x), and do
 not support the Document or Point types. 

