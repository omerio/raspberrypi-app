#!/bin/sh
cd dist
sudo java -jar RaspberryPiApp.jar > RaspberryPiApp.log 2>&1 &
exit 0