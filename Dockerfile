FROM maven:3.9.12-eclipse-temurin-25 AS build

RUN useradd -m demouser -d /home/demouser

COPY . /home/demouser/dss-demonstrations/

# OPTIONAL: provide documentation + javaDoc (change "pathTo")
#COPY pathTo/generated-docs /home/demouser/dss/dss-cookbook/target/generated-docs
#COPY pathTo/site/apidocs /home/demouser/dss/target/site/apidocs

WORKDIR /home/demouser/dss-demonstrations

RUN mvn package -pl dss-standalone-app,dss-standalone-app-package,dss-demo-webapp -P quick,linux

USER demouser

FROM tomcat:11.0.18-jdk25-temurin

COPY --from=build /home/demouser/dss-demonstrations/dss-demo-webapp/target/dss-demo-webapp-*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080