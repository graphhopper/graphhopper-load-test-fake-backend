const http = require('http');
const url = require('url');
const args = process.argv.slice(2);
const port = args.length > 0 ? parseInt(args[0]) : 8000;
var jobId = 1;

async function requestHandler(request, response) {
    const timeout = Math.floor(20.0 + Math.random() * 130)
    await sleep(timeout);
    const urlInfo = url.parse(request.url);
    if (urlInfo.pathname == "/favicon.ico")
        return;
    console.log(`GET ${urlInfo.pathname}`);
    response.writeHead(200, {"Content-Type": "application/json"});
    response.end('{"status": "finished", "job_id": "' + jobId++ + '"}');
};
const server = http.createServer(requestHandler);

server.listen(port, (err) => {
    console.log(`Server listening on http://localhost:${port}`);
});

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
