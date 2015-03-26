#!/bin/sh

## Constants
imgtag=klaemo/couchdb-ssl
craimgtag=slauber/crawcial

## Check for root and docker

if [[ $EUID -ne 0 ]]; then echo "This script must be run as root" 1>&2;exit 1;fi
command -v docker >/dev/null 2>&1 || { echo >&2 "This script requires docker. Please install it first."; exit 1; }

if [ "$#" -eq 5 ]; then
	## Non-interactive - 6 Params: dbuser dbpassword dbcontainername dbport crawcialcontainername crawcialport
	user="$1"
	password="$2"
	name="$3"
	port="$4"
	craname="$5"
	craport="$6"
	else
	
## Interactive
	echo "*** Crawcial with CouchDB for Docker setup ****"
	echo
	echo "for non-interactive, use ./crawcial_setup.sh [dbuser dbpassword dbcontainername dbport crawcialcontainername crawcialport]"
	echo
	echo "Do not use blanks or not URL-safe characters!"
	echo
	echo "Enter a CouchDB administrator username"
	read user
	echo "Enter a CouchDB administrator password (will not be shown)"
	read -s password
	echo "Confirm CouchDB administrator password (will not be shown)"
	read -s password_conf
	echo "Enter a Docker container name for CouchDB or press [ENTER] to use default (couchdb)"
	read customname
	[ ! -z "$customname" ] && name="$customname" || name="couchdb"
	echo "Enter a Docker container name for Crawcial or press [ENTER] to use default (crawcial)"
	read customcraname
	[ ! -z "$customcraname" ] && craname="$customcraname" || craname="crawcial"
	echo "Enter an avaiable TCP port for https access to CouchDB or press [ENTER] to use default (6984)"
	read customport
	[ ! -z "$customport" ] && port="$customport" || port="6984"
	echo "Enter an avaiable TCP port for https access to Crawcial or press [ENTER] to use default (443)"
	read customcraport
	[ ! -z "$customcraport" ] && craport="$customcraport" || craport="443"

fi
## Check for matching passwords

if [ "$password_conf" == "$password" ]; then	
	## Start containers	
		
	echo
	echo "**** Starting CouchDB container... ****"
    docker run -d -p "$port":6984 --name="$name" "$imgtag"
	echo
	echo "**** Starting Crawcial container... ****"
	docker run -d -p "$craport":8443 --link="$name":"$name" --name="$craname" "$craimgtag"
	
	## Set credentials and pass them to Crawcial
	echo
	echo "**** Setting CouchDB credentials ****"
	until $(curl -k --output /dev/null --silent --head --fail https://127.0.0.1:"$port"); do
		printf '.'
		sleep 2
	done
	curl -k --silent -XPUT https://127.0.0.1:"$port"/_config/admins/$user -d \""$password"\"
	echo
	echo "**** Preparing Crawcial ****"
	until $(curl -k --output /dev/null --silent --head --fail https://127.0.0.1:"$craport"); do
		printf '.'
		sleep 2
	done	
	curl -k --silent -XPOST https://127.0.0.1:"$craport"/updateconfig --data "port=5984&protocol=http&user=$user&password=$password&feedback=Accept&action=update&code=2&host=couchdb"
	echo
	echo "Your Crawcial instance is now available via HTTPS at port $craport"
else
	echo "Passwords did not match, exiting..."
	exit 1
fi