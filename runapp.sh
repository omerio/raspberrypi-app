#!/bin/sh
cd dist
java -jar RaspberryPiApp.jar > RaspberryPiApp.log 2>&1 &
exit 0