package com.graphhopper.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class Server extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Consumer<Vertx> runner = vertx -> {
            try {
                vertx.deployVerticle(Server.class.getName());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        Vertx vertx = Vertx.vertx(new VertxOptions());
        runner.accept(vertx);
    }

    @Override
    public void start() throws IOException {
        String routeOptSolution = toString(Server.class.getResourceAsStream("/routeopt.json"));
        String matrix4 = toString(Server.class.getResourceAsStream("/matrix4x4.json"));
        String matrix10 = toString(Server.class.getResourceAsStream("/matrix10x10.json"));

        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            routingContext.response().
                    putHeader("content-type", "text/html").
                    end("<html><body><h2>Fake Server</h2></body></html>");
        });
        Handler<RoutingContext> lambda1 = routingContext -> {
            // to avoid parsing the JSON put the jobId in the header
            String jobId = UUID.randomUUID().toString();
            routingContext.response().
                    putHeader("content-type", "application/json").
                    putHeader("X-GH-JobId", jobId).
                    end("{\"job_id\":\"" + jobId + "\"}");
        };
        // handle request as "back end"
        router.route("/optimize*").handler(lambda1);
        // handle requests directly
        router.route("/api/1/vrp/optimize*").handler(lambda1);

        Handler<RoutingContext> lambda2 = routingContext -> {
            routingContext.response().
                    putHeader("content-type", "application/json").
                    end(routeOptSolution);
        };
        router.route("/solution*").handler(lambda2);
        router.route("/api/1/vrp/solution*").handler(lambda2);
        router.post("/match").handler(routingContext -> {
//            routingContext.response().putHeader("content-type", "application/gpx+xml").
//                    end("{\"message\": \"Unable to process GPX!\"}");
            routingContext.response().putHeader("content-type", "application/json").
                    end("{\"message\": \"Unable to process GPX!\"}");
        });

        router.route("/matrix").handler(BodyHandler.create());
        router.post("/matrix").handler(routingContext -> {
            try {
                JsonObject obj = ungzip(routingContext);
                if (obj == null) {
                    routingContext.response().end("The Json body is null. Please recheck.." + System.lineSeparator());
                    return;
                }
                int size = obj.getJsonArray("points").size();
                routingContext.response().putHeader("content-type", "application/json").
                        end(size == 4 ? matrix4 : matrix10);
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).warn("problem happened " + ex.getMessage(), ex);
            }
        });

        int port = config().getInteger("http.port", 8900);
        vertx.createHttpServer().requestHandler(router).listen(port);
        System.out.println("started on " + port);
    }

    private JsonObject ungzip(RoutingContext routingContext) throws IOException {
        if (!"gzip".equals(routingContext.request().getHeader("Content-Encoding")))
            return routingContext.getBodyAsJson();

        // not safe against zip bomb!
        byte[] bytes = routingContext.getBody().getBytes();

        // IMPORTANT: use the following curl command as otherwise it won't be able to ungzip it properly
        // curl -s -XPOST -H "Content-Type: application/json" -H "Content-Encoding: gzip" 'http://localhost:8900/matrix' --data-binary @$file
        final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
        StringBuilder outStr = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            outStr.append(line);
        }
        return (JsonObject) Json.decodeValue(outStr.toString());
    }

    public static String toString(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("stream is null!");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufReader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}