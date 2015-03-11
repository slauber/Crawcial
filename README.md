# Crawcial
Crawcial - Die freie Komponente zur automatischen Extraktion von Inhalten aus sozialen Netzwerken

Crawcial ist die leichtgewichtige und schnelle Alternative zu mächtigen Frameworks wie Spring XE. Es setzt lediglich eine Java Web Profile Umgebung und eine CouchDB Instanz voraus.
Die Steuerung der Extraktion der Daten erfolgt bequem über ein Webinterface, in dem sich die Stichworte der zu speichernden Beiträge eingeben lassen. Crawcial dient lediglich zur Extraktion der Daten, 
es beinhaltet keine Analyse- oder Visualisierungsfunktionen.

## Aktueller Stand
- Crawcial ist zur Zeit in der Lage, Realtime Streams über die Twitter Streaming API abzufragen und zu verarbeiten.
- Der Nutzer wird bei der Einrichtung der Software durch einen Assistenten unterstützt.
- Crawcial kann neben den reinen JSON Daten auch die Mediendaten der Tweets herunterladen (dies setzt eine höhere Bandbreite und deutlich mehr freien Speicherplatz voraus).
