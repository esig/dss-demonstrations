#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/java/bin/java" \
  --module-path "$DIR/fx-sdk/lib" \
  --add-modules=javafx.fxml,javafx.controls \
  -jar "$DIR/dss-app.jar"