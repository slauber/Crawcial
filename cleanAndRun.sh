#!/bin/bash
cd $CRAWCIAL
git pull
mvn clean compiler:compile resources:resources war:war jetty:run-exploded