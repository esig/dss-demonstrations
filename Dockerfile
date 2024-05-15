FROM maven:3.9.6-eclipse-temurin-17 as build

RUN useradd -m demouser -d /home/demouser

COPY . /home/demouser/dss-demonstrations/

# OPTIONAL: provide documentation + javaDoc (change "pathTo")
#COPY pathTo/generated-docs /home/demouser/dss/dss-cookbook/target/generated-docs
#COPY pathTo/site/apidocs /home/demouser/dss/target/site/apidocs

WORKDIR /home/demouser/dss-demonstrations

RUN mvn package -pl dss-standalone-app,dss-standalone-app-package,dss-demo-webapp -P quick

USER demouser

FROM tomcat:10.1.24

COPY --from=build /home/demouser/dss-demonstrations/dss-demo-webapp/target/dss-demo-webapp-*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080