package eu.europa.esig.dss.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DssDemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DssDemoApplication.class, args);
    }

}
