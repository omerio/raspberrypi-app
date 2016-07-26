# Raspberry Pi App

A Java based, Raspberry Pi App that sends data to authenticated Google App Engine Cloud Endpoint [application](https://github.com/omerio/raspberrypi-appengine-portal). The data is then displayed on a real time dashboard.


![Alt text](http://omerio.com/wp-content/uploads/2016/01/raspberrypid1.png "Sensor Dashboard")

## Live Demo
- [https://raspberrypi-dash.appspot.com/](https://raspberrypi-dash.appspot.com/)

## Overall Architecture
![Alt text](http://omerio.com/wp-content/uploads/2016/01/pi_appengine_architecture.png "Architecture")


## Documentation
- [Real Time Sensor Dashboard Using Google App Engine and Raspberry Pi Zero](http://omerio.com/2016/01/16/real-time-sensor-dashboard-using-google-app-engine-and-raspberry-pi-zero/)

## Hardware Prerequisites
- A Raspberry Pi Zero or Model B.
- Assemble the hardware components on a breadboard by following the [blog post](http://omerio.com/2016/01/16/real-time-sensor-dashboard-using-google-app-engine-and-raspberry-pi-zero/).
- Alternatively my friend Lez has created populated and bare boards for the hardware which you can purchase on his [Website](http://www.neptunenavigation.co.uk/pi/pcbfordashboard.htm).

![Alt text](http://omerio.com/wp-content/uploads/2016/01/raspberry_pi_board.jpg "Boards")


## Software Prerequisites
- (Optional) Install [Apache Maven](https://www.xianic.net/post/installing-maven-on-the-raspberry-pi/) on the Raspberry Pi if you want to build the project on the Pi itself.
- Make sure git and Java are installed on the Pi.
- Install [Pi4J](http://pi4j.com/) library on the Pi.
- Ensure the [SPI interface](http://www.raspberrypi-spy.co.uk/2014/08/enabling-the-spi-interface-on-the-raspberry-pi/) is enabled.


## Setup Instructions
1. Start a shell on the Pi.
1. Checkout the project `git clone https://github.com/omerio/raspberrypi-app.git`.
2. Update the [client_secret.json](https://github.com/omerio/raspberrypi-app/blob/master/src/main/resources/client_secret.json) file with your client_id and client_secret. 
1. Compile the project `cd raspberrypi-app && mvn clean package`, once built the RaspberryPiApp.jar will be available inside the dist folder
2. Run the application `cd dist` then `sudo java -jar RaspberryPiApp.jar`. On first start the app will prompt you to copy and paste a link in your browser. Navigate to the URL and accept the Google Accounts permission request. It will give you a code that you can copy and paste into the command line prompt.
3. On subsequent starts of the application you can use the `runapp.sh` script.
4. To kill the app do `ps -ef | grep java` and note the pid id then do a 'sudo kill pid-id'
5. Optionally you can get the RaspberryPiApp.jar to run by default whenever the Pi starts:
```bash
sudo cp runapp.sh /etc/init.d/piapp
sudo update-rc.d piapp defaults
```

