#!/bin/bash

sudo apt install default-jre

file="cloudphoto"
echo '#!/bin/bash' >> $file
echo 'java -jar /opt/cloudphoto/cloudphoto-0.0.1-SNAPSHOT.jar "$@"' >> $file

sudo chmod +x cloudphoto

sudo mkdir -p /opt/cloudphoto

sudo mv cloudphoto-0.0.1-SNAPSHOT.jar /opt/cloudphoto/

sudo mv cloudphoto /usr/bin