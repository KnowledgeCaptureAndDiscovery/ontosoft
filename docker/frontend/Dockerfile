# syntax=docker/dockerfile:1
FROM nginx
RUN apt-get update \
    && apt-get install -yq unzip
COPY build/ontosoft-client-*.war /tmp/ontosoft-client.war
WORKDIR /usr/share/nginx/html
RUN mkdir portal \
    && cd portal \
    && unzip /tmp/ontosoft-client.war
COPY docker/frontend/default.conf /etc/nginx/conf.d/default.conf