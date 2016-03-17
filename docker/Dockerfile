FROM glassfish:4.1-jdk8

COPY ../target/clickCount.war /
COPY start.sh /

ENV REDIS_HOST redis
ENV REDIS_PORT 6379
EXPOSE 8080

ENTRYPOINT ["/start.sh"]
