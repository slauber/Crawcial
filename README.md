# Crawcial
Crawcial - Die freie Komponente zur automatischen Extraktion von Inhalten aus sozialen Netzwerken

Crawcial ist die leichtgewichtige und schnelle Alternative zu mächtigen Frameworks wie Spring XE. Es setzt lediglich eine Java Web Profile Umgebung und eine CouchDB Instanz voraus.
Die Steuerung der Extraktion der Daten erfolgt bequem über ein Webinterface, in dem sich die Stichworte der zu speichernden Beiträge eingeben lassen. Crawcial dient lediglich zur Extraktion der Daten, 
es beinhaltet keine Analyse- oder Visualisierungsfunktionen.

## Aktueller Stand
- Crawcial ist zur Zeit in der Lage, Realtime Streams über die Twitter Streaming API abzufragen und zu verarbeiten.
- Der Nutzer wird bei der Einrichtung der Software durch einen Assistenten unterstützt.
- Crawcial kann neben den reinen JSON Daten auch die Mediendaten der Tweets herunterladen (dies setzt eine höhere Bandbreite und deutlich mehr freien Speicherplatz voraus).
- Crawcial kann als Callback-Server für Facebook Real Time Updates genutzt werden (die Verarbeitung der Statusnachrichten wird noch nicht vorgenommen, siehe [de.crawcial.facebook.FacebookStreamer Klasse](https://github.com/slauber/Crawcial/blob/master/src/main/java/de/crawcial/facebook/FacebookStreamer.java))
- Die vorhandenen Inhalte von Seiten, die im Besitz des angemeldeten Facebooknutzers sind, können gespeichert werden (mit Einschränkungen, erfordert weitere Arbeit, siehe [de.crawcial.facebook.FacebookStaticLoader Klasse](https://github.com/slauber/Crawcial/blob/master/src/main/java/de/crawcial/facebook/FacebookStaticLoader.java)).

## Installation mit Docker
Crawcial auf einem System mit Docker zu installieren ist ganz einfach:
  1. [Setup Skript](https://github.com/slauber/Crawcial/blob/master/docker/crawcial_setup.sh) herunterladen (wget https://raw.githubusercontent.com/slauber/Crawcial/master/docker/crawcial_setup.sh)
  2. Rechte korrekt vergeben (chmod 555 crawcial_setup.sh)
  3. Setup als root ausführen (sudo ./crawcial_setup.sh)

Während der Installation wird automatisch eine CouchDB Docker Instanz angelegt und Crawcial mit HTTP und HTTPS Zugang vorbereitet.
