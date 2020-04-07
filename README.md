## Demonstrations for DSS : Digital Signature Service

This is the demonstration repository for project DSS : https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eSignature. 

# Issue Tracker

Please, use the new JIRA for project is on https://ec.europa.eu/cefdigital/tracker/projects/DSS/issues. 

# Maven repository

The release is published on CEF Digital repository : 

https://ec.europa.eu/cefdigital/artifact/#welcome

<pre>
&lt;repository&gt;
  &lt;id&gt;cefdigital&lt;/id&gt;
  &lt;name&gt;cefdigital&lt;/name&gt;
  &lt;url&gt;https://ec.europa.eu/cefdigital/artifact/content/repositories/esignaturedss/&lt;/url&gt;
&lt;/repository&gt;
</pre>

# Demonstration

The release is deployed on https://ec.europa.eu/cefdigital/DSS/webapp-demo

# DSS Standalone Application

In order to build the standalone application, the following modules are required:

 * dss-mock-tsa;
 * dss-standalone-app;
 * dss-standalone-package.
 
If the build is successfull, you will be able to find out the following containers in the directory `/dss-standalone-app-package/target/`:

 * dss-standalone-app-package-minimal.zip - contains the application code. Requires JDK ad JavaFX installed on a target machine in order to run the application;
 * dss-standalone-app-package-complete.zip - contains the application code, as well as JDK and JavaFX library code. Can be run on a machine whithout pre-installed libraries.

In order to launch the application, you will need to extract the archive and run the file `dss-run.bat`.

# DSS Web Application

To build the DSS Web Application the following modules are required:

 * dss-mock-tsa;
 * dss-demo-webapp;
 * dss-demo-bundle.
 
After a successfull build, in the directory `/dss-demo-bundle/target/` you will be able to find out two containers: `dss-demo-bundle.zip` and `dss-demo-bundle.tar.gz`. Despite the container type, the content of both files is the same. After extracting the content, you will need to run the file `Webapp-Startup.bat` in order to launch the server and the file `Webapp-Shutdown.bat` to stop the server. After running the server, the web-application will be availble at the address `http://localhost:8080/`.

# JavaDoc

The JavaDoc is available on https://ec.europa.eu/cefdigital/DSS/webapp-demo/apidocs/index.html

# Ready-to-use bundles

Bundles which contain the above demonstration can be downloaded from the [Maven repository](https://ec.europa.eu/cefdigital/artifact/content/repositories/esignaturedss/eu/europa/ec/joinup/sd-dss/dss-demo-bundle/).

The code of the demonstration can be found on https://ec.europa.eu/cefdigital/code/projects/ESIG/repos/dss-demos/browse

[![License (LGPL version 2.1)](https://img.shields.io/badge/license-GNU%20LGPL%20version%202.1-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-2.1.html)