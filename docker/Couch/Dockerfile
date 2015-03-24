FROM klaemo/couchdb-ssl:latest

MAINTAINER Sebastian Lauber mail@crawcial.de

ADD couchdb.cfg /couchdb.cfg
ADD setpassword.sh /setpassword.sh

RUN chmod 777 /setpassword.sh && /setpassword.sh && rm /couchdb.cfg && rm /setpassword.sh

EXPOSE 6984

ENTRYPOINT ["/entrypoint-stud.sh"]
