FROM jenkins:1.642.2

USER root
# Install Docker from Docker Inc. repositories.
RUN curl -sSL https://get.docker.com/ | sh

COPY plugins.txt /usr/share/jenkins/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt
