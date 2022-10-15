#!/bin/bash

sudo rm -rf /opt/cloudphoto
sudo mkdir -p /opt/cloudphoto

sudo apt update
sudo apt install default-jre
sudo apt install maven
sudo apt install zip

sudo wget https://github.com/festralm/cloudphoto/archive/refs/tags/v1.0.0.zip
sudo mv v1.0.0 /opt/cloudphoto
sudo /opt/cloudphoto/unzip v8.1.1.zip -d /opt/cloudphoto/
cd /opt/cloudphoto/
sudo mv target/cloudphoto-0.0.1-SNAPSHOT.jar /opt/cloudphoto/


file="cloudphoto"
echo '#!/bin/bash' >> $file
echo 'java -jar /opt/cloudphoto/cloudphoto-0.0.1-SNAPSHOT.jar "$@"' >> $file
sudo chmod +x cloudphoto
sudo mv cloudphoto /usr/bin