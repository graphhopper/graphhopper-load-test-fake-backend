package com.graphhopper.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

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
        String jsonStr = toString(Server.class.getResourceAsStream("/response.json"));

        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end("<html><body><h2>Fake Server</h2></body></html>");
        });
        router.route("/optimize").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "application/json").end("{\"job_id\":\"" + UUID.randomUUID().toString() + "\"}");
        });

        router.route("/solution").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "application/json").end(jsonStr);
        });

        int port = config().getInteger("http.port", 8080);
        vertx.createHttpServer().requestHandler(router).listen(port);
        System.out.println("started on " + port);
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