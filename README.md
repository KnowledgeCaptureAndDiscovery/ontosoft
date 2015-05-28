# TurboSoft (version 2)
Software repository portal built on semantic technologies

The Turbosoft portal provides access to Geosoft's Turbosoft system (http://www.geosoft-earthcube.org/) by allowing you to browse, and add models to the repository.

Demo here:
http://www.geosoft-earthcube.org/portal/

Installation
=============
Requirements
------------
1. Java JDK 1.7+ (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. Maven 2/3 (http://maven.apache.org/)
3. Tomcat 7+ (http://tomcat.apache.org/)

Installation
-------------
1. Change KBConstants.java to make sure that the server url is set correctly.

2. $ mvn clean install
	- This will create a turbosoft-server-[version].war file in server/target
	- It will also create a turbosoft-client-[version].war file in client/target

3. Move the war files to a Servlet container (Tomcat)
	- $ mv /path/to/turbosoft-server-<version>.war /path/to/tomcat/webapps/turbosoft-server.war
	- $ mv /path/to/turbosoft-client-<version>.war /path/to/tomcat/webapps/turbosoft-client.war

4. Start tomcat
	- $ /path/to/tomcat/bin/startup.sh

5. Open http://[your-server-name]:8080/turbosoft-server/software/vocabulary to check that the local repository server is working fine. It might take a little while to open it for the first time as it downloads vocabularies from the internet.

6. Open http://[your-server-name]:8080/turbosoft-client to access the TurboSoft UI that connects with the local repository
