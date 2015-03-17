# Baseimage by Dmytro Aleksandrov
# https://raw.githubusercontent.com/dockerfile/java/master/oracle-java8/Dockerfile
# --------------------------------------------------------------------------------
FROM dockerfile/java:oracle-java8

# Maintainer
# ----------
MAINTAINER Sebastian Lauber <mail@crawcial.de>

# Install Maven, clone Crawcial repository and add start up / update script
# -------------------------------------------------------------------------
WORKDIR /data
RUN apt-get update && apt-get upgrade -y && apt-get install maven -y --no-install-recommends && apt-get autoclean -y
ADD start.sh /start.sh
RUN chmod 555 /start.sh && cd /data && git clone https://github.com/slauber/crawcial

# Default command to run on container boot
CMD ["/start.sh"]

EXPOSE 8443