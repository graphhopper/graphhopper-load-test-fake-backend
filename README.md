# Fake backend server

This repository is for supplying a fake routing or VRP server for load testing purposes.

## Java

To be added...

## Node.js

To run the server in Node.js, run the following:

    docker run -d --rm --network host --name vrp-fake -v "$PWD"/node:/usr/src/app -w /usr/src/app node:8 node server.js [port]

This will run a Node 8 Docker container, pass in the `node/server.js` file and run that on the selected port.
