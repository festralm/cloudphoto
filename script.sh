#!/bin/bash

sudo rm -rf /opt/cloudphoto-43
sudo mkdir -p /opt/cloudphoto-43

sudo apt update
sudo apt install default-jre
sudo apt install maven
sudo apt install zip

sudo wget https://github.com/festralm/cloudphoto/archive/refs/tags/v1.0.2.zip
sudo mv v1.0.2.zip /opt/cloudphoto-43
sudo unzip /opt/cloudphoto-43/v1.0.2.zip -d /opt/cloudphoto-43/ &> /dev/null
sudo rm /opt/cloudphoto-43/v1.0.2.zipx
cd /opt/cloudphoto-43/cloudphoto-1.0.2
echo 'Downloading libraries...'
sudo mvn clean install &> /dev/null
echo 'Finished downloading libraries'
sudo mv target/cloudphoto-0.0.1-SNAPSHOT.jar /opt/cloudphoto-43/

file="/usr/bincloudphoto"
sudo touch $file
sudo chmod o+w $file
sudo echo '#!/bin/bash' >> $file
sudo echo 'java -jar /opt/cloudphoto-43/cloudphoto-0.0.1-SNAPSHOT.jar "$@"' >> $file
sudo chmod o-w $file
sudo chmod +x $file