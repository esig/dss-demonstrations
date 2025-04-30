[ ! -d /usr/share/openjfx/lib ] && echo "JavaFX is not installed. Please install openjfx package with sudo apt install openjfx" && exit 1
java --module-path /usr/share/openjfx/lib --add-modules javafx.fxml,javafx.controls -jar dss-app.jar

