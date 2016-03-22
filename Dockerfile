FROM glassfish:4.1-jdk8

COPY target/clickCount.war /
COPY docker/deploy.sh /
COPY docker/start.sh /

RUN /deploy.sh
ENV REDIS_HOST redis
ENV REDIS_PORT 6379
ENV REDIS_CONNECTION_TIMEOUT 2000

EXPOSE 8080 4848
ENTRYPOINT ["/start.sh"]
