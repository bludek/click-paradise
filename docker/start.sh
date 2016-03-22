#!/bin/sh

asadmin create-jvm-options -Dredis_host=${REDIS_HOST}:-Dredis_port=${REDIS_PORT}
asadmin start-domain --verbose
