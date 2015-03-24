#!/bin/bash

source /couchdb.cfg

couchdb -b && sleep 3 && curl -X PUT http://127.0.0.1:5984/_config/admins/$couch_username -d '"$couch_password"' && couchdb -d
