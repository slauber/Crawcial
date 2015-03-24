#!/bin/bash
cd /data/crawcial && git pull
if [ ! -e /data/crawcial/store.keystore ]; then
        mvn keytool:generateKeyPair
fi
mvn compiler:compile resources:resources jetty:run
