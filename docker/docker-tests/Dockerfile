FROM cypress/base:13.8.0

RUN adduser --disabled-password jahians
USER jahians

COPY --chown=jahians:jahians ./package.json ./yarn.lock /tmp/
WORKDIR /tmp

#CI=true reduces the verbosity of the installation logs
RUN CI=true yarn install
RUN CI=true /tmp/node_modules/.bin/cypress install

COPY --chown=jahians:jahians . /tmp/

RUN mkdir /tmp/run-artifacts
RUN mkdir -p /tmp/results

CMD ["/bin/bash", "-c", "/tmp/env.run.sh"]
