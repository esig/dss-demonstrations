package eu.europa.esig.dss.standalone.source;

public class SystemPropertyReader {

    private static final String USER_HOME = "user.home";

    public static String getUserHome() {
        return System.getProperty(USER_HOME);
    }

}
