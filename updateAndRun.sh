#!/bin/bash
cd $CRAWCIAL
git pull
mvn compiler:compile resources:resources war:war jetty:run-exploded