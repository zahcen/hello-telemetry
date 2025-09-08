FROM node:20-slim

# Update system packages to reduce vulnerabilities
USER root
RUN apt-get update && apt-get upgrade -y && apt-get clean && apt-get install -y vim

RUN mkdir -p /home/node/app/node_modules && chown -R node:node /home/node/app

RUN npm install @opentelemetry/api @opentelemetry/sdk-node @opentelemetry/instrumentation @opentelemetry/instrumentation-http @opentelemetry/instrumentation-express

WORKDIR /home/node/app

COPY package.json ./

COPY server.js ./
USER node
RUN npm install
COPY --chown=node:node . .
EXPOSE 3000
CMD [ "node", "server.js" ]
