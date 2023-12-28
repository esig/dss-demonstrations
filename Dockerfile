FROM maven:3.9.5-eclipse-temurin-17 as build

COPY pom.xml /usr/src/mymaven/dss-demonstrations/

COPY dss-standalone-app/pom.xml /usr/src/mymaven/dss-demonstrations/dss-standalone-app/
COPY dss-standalone-app/src /usr/src/mymaven/dss-demonstrations/dss-standalone-app/src

COPY dss-standalone-app-package/pom.xml /usr/src/mymaven/dss-demonstrations/dss-standalone-app-package/
COPY dss-standalone-app-package/src /usr/src/mymaven/dss-demonstrations/dss-standalone-app-package/src

COPY dss-demo-webapp/pom.xml /usr/src/mymaven/dss-demonstrations/dss-demo-webapp/
COPY dss-demo-webapp/src /usr/src/mymaven/dss-demonstrations/dss-demo-webapp/src

COPY dss-demo-bundle/pom.xml /usr/src/mymaven/dss-demonstrations/dss-demo-bundle/
COPY sscd-mocca-adapter/pom.xml /usr/src/mymaven/dss-demonstrations/sscd-mocca-adapter/
COPY dss-rest-doc-generation/pom.xml /usr/src/mymaven/dss-demonstrations/dss-rest-doc-generation/
COPY dss-esig-validation-tests/pom.xml /usr/src/mymaven/dss-demonstrations/dss-esig-validation-tests/

# OPTIONAL: provide documentation + javaDoc (change "pathTo")
#COPY pathTo/generated-docs /usr/src/mymaven/dss/dss-cookbook/target/generated-docs
#COPY pathTo/site/apidocs /usr/src/mymaven/dss/target/site/apidocs

WORKDIR /usr/src/mymaven/dss-demonstrations

RUN mvn package -pl dss-standalone-app,dss-standalone-app-package,dss-demo-webapp -P quick


FROM tomcat:10

COPY --from=build /usr/src/mymaven/dss-demonstrations/dss-demo-webapp/target/dss-demo-webapp-*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080