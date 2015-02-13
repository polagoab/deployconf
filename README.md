Deployconf
==========

This repository contains the source code for deployconf, a Command Line Tool
used by Deployers to configure java artifacts for the actual deployment
environment.

System requirements for development
-----------------------------------

* Java SE JDK 1.7(7.0) or later (http://java.sun.com/)
* Maven 3.1 or later (http://maven.apache.org/)

Installation instructions
-------------------------

If you haven't downloaded Java SE, this is the time since it's
needed to run Maven. Make sure that you can execute java -version from
a command prompt.

Download Maven and unpack the distribution. 

Make sure that the mvn executable is available in your PATH. 

Start a command line shell and change directory to the same directory
where this file is located.

Build the tool by running:

    mvn package

Project documentation
---------------------

This projects documentation is located in the src/site directory. 
To read the documentation, you need to generate the HTML web
site by running:

    mvn site
