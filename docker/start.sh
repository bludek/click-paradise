#!/bin/sh

/usr/local/glassfish4/bin/asadmin start-domain
/usr/local/glassfish4/bin/asadmin -u admin deploy /clickCount.war
/usr/local/glassfish4/bin/asadmin create-jvm-options -Dredis_host=${REDIS_HOST}:-Dredis_port=${REDIS_PORT}
/usr/local/glassfish4/bin/asadmin stop-domain
/usr/local/glassfish4/bin/asadmin start-domain --verbose
