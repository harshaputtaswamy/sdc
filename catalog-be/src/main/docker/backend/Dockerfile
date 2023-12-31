FROM onap/integration-java11:10.0.0

USER root
ARG JETTY_FOLDER=/app/jetty

# Install Chef
RUN set -ex && \
    apk update && \
    apk add --no-cache \
        wget \
        build-base \
        ruby \
        ruby-dev \
        libffi-dev \
        libxml2-dev && \
    gem update --system --no-document && \
    gem install --no-update-sources public_suffix:4.0.7 multipart-post:2.2.0 etc:1.3.0 bundler:2.3.26 chef:13.8.5 berkshelf:6.3.1 io-console:0.4.6 webrick --no-document && \
    gem cleanup && \
    apk update

ENV JETTY_HOME=$JETTY_FOLDER
ENV JETTY_BASE=$JETTY_FOLDER
ENV JETTY_USER=onap
ENV JETTY_GROUP=onap

RUN mkdir $JETTY_FOLDER && chown onap:onap $JETTY_FOLDER

USER onap

#Download jetty
RUN wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${jetty-distribution.version}/jetty-distribution-${jetty-distribution.version}.tar.gz --tries=3 -O $JETTY_FOLDER/jetty.tar.gz && \
        tar xz -C $JETTY_FOLDER -f $JETTY_FOLDER/jetty.tar.gz --strip 1 && \
        rm -rf $JETTY_FOLDER/jetty.tar.gz

RUN sed -i 's/"jetty"/"onap"/g' $JETTY_FOLDER/etc/jetty-setuid.xml

COPY --chown=onap:onap chef-solo $JETTY_FOLDER/chef-solo/
COPY --chown=onap:onap chef-repo/cookbooks $JETTY_FOLDER/chef-solo/cookbooks/
COPY --chown=onap:onap onap-sdc-backend/catalog-be.war       $JETTY_FOLDER/webappwar/
COPY --chown=onap:onap context.xml       $JETTY_FOLDER/webapps/
COPY --chown=onap:onap startup.sh $JETTY_FOLDER/

RUN chmod 770 $JETTY_FOLDER/startup.sh

ENTRYPOINT [ "sh", "-c", "${JETTY_HOME}/startup.sh"]
