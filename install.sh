#!/bin/bash
# Installscript for Crawcial - Requires Maven
git clone https://github.com/slauber/crawcial.git
cd crawcial
mvn compiler:compile resources:resources war:war jetty:run-exploded