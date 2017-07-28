# OntoSoft
Software repository portal built on semantic technologies

The OntoSoft portal provides access to the OntoSoft system (http://www.ontosoft.org/) by allowing you to browse, and add models to the repository.

Demo here:
http://www.ontosoft.org/portal/

Installation
=============
Requirements
------------
1. Java JDK 1.7+ (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. Maven 2/3 (http://maven.apache.org/)
3. Tomcat 7+ (http://tomcat.apache.org/)

Installation
-------------
1. Clone OntoSoft Repository

2. In terminal, move to general ontosoft folder and run the following commands:
    $ mvn clean
    $ mvn install
  This will create a ontosoft-server-[version].war file in server/target
  It will also create a ontosoft-client-[version].war file in client/target

3. Move the war files to a Servlet container (Tomcat)
	- $ mv /path/to/ontosoft-server-<version>.war /path/to/tomcat/webapps/ontosoft-server.war
	- $ mv /path/to/ontosoft-client-<version>.war /path/to/tomcat/webapps/ontosoft-client.war

4. Start tomcat
	- $ /path/to/tomcat/bin/startup.sh

5. Open http://[your-server-name]:8080/ontosoft-server/vocabulary to check that the local repository server is working fine. It might take a little while to open it for the first time as it downloads vocabularies from the internet.

6. Check $HOME/.ontosoft/server.properties file to see that server name is correctly identified

7. Open http://[your-server-name]:8080/ontosoft-client to access the OntoSoft UI that connects with the local repository

8. Customize the client by changing /path/to/tomcat/webapps/ontosoft-client/customise/config.js

9. The default userid is "admin" with password "changeme!". Remember to change it :)


*** 
If the server does not work then try these:
Check the title of the war files to ensure they match “ontosoft-server” and “ontosoft-client”. Change if necessary
If the server name in “server.properties” does not match the name of the server war file, then change it.
Make sure the url in the “config.js” file matches http://[your-server-name]:8080/ontosoft-server
***


  