package com.graphhopper.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BugTest {
    public static final int PORT = 8989;
    public static final MediaType MT_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MT_GPX = MediaType.parse("application/gpx+xml");

    public static void main(String[] args) throws Exception {
        String gpxStr = toString(BugTest.class.getResourceAsStream("/gpx.xml"));
        String matrix4 = toString(BugTest.class.getResourceAsStream("/matrix4x4.json"));
        String matrix10 = toString(BugTest.class.getResourceAsStream("/matrix10x10.json"));
        OkHttpClient mClient = new OkHttpClient.Builder().
                connectTimeout(6, TimeUnit.SECONDS).
                readTimeout(12, TimeUnit.SECONDS).
                writeTimeout(20, TimeUnit.SECONDS).
                // default is 5 connections and 5min
//                        connectionPool(new ConnectionPool(100, 15, TimeUnit.MINUTES)).
        build().newBuilder().addInterceptor(new GzipRequestInterceptor()).
                        build();
        Logger logger = LoggerFactory.getLogger(BugTest.class);

        Thread t1 = new Thread(() -> {
            try {
                logger.info("4 -> started");
                for (int i = 0; i < 10_000; i++) {
                    query(mClient, matrix4, 4);
                }
                logger.info("4 -> all fine");
            } catch (Exception ex) {
                logger.error("4 -> stopped", ex);
            }
        });
        t1.start();

        Thread t2 = new Thread(() -> {
            logger.info("10 -> started");
            try {
                for (int i = 0; i < 10_000; i++) {
                    query(mClient, matrix10, 10);
                }
                logger.info("10 -> all fine");
            } catch (Exception ex) {
                logger.error("10 -> stopped", ex);
            }
        });
        t2.start();

        for (int i = 0; i < 100; i++) {
            Response rsp = null;
            try {
                Thread.sleep(3 * 1000);
//                Request.Builder reqBuilder = new Request.Builder().url("http://localhost:" + PORT + "/matrix");
//                reqBuilder.post(RequestBody.create("{ \"blup\":"+matrix10, MT_JSON));
                Request.Builder reqBuilder = new Request.Builder().url("http://localhost:" + PORT + "/match");
                reqBuilder.post(RequestBody.create(gpxStr, MT_GPX));
                rsp = mClient.newCall(reqBuilder.build()).execute();
                Map json = getJson(rsp.body().string());
                logger.info("GPX rsp: " + json);
            } finally {
                if (rsp != null) rsp.body().close();
            }
        }

        t1.join();
        t2.join();
        logger.info("finished");
    }

    static ObjectMapper mapper = new ObjectMapper();

    static Map<String, Object> getJson(String str) {
        try {
            return mapper.readValue(str, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (Exception ex) {
            // if it is empty or none-POST or similar
            return new HashMap<>(5);
        }
    }

    private static void query(OkHttpClient client, String string, int count) throws IOException {
        Response rsp = null;
        try {
            try {
                Thread.sleep(System.currentTimeMillis() % 37);
            } catch (Exception ex) {
            }
            Request.Builder reqBuilder = new Request.Builder().url("http://localhost:" + PORT + "/route");
            reqBuilder.post(RequestBody.create(string, MT_JSON));
            rsp = client.newCall(reqBuilder.build()).execute();
            Map<String, Object> json = getJson(rsp.body().string());
            if (json.get("paths") == null)
                throw new IllegalStateException(count + " -> mismatch " + json);
            int rspCount = ((List) ((Map) ((Map) ((List) json.get("paths")).get(0)).get("snapped_waypoints")).get("coordinates")).size();
//            if (json.getJsonArray("distances") == null || json.getJsonArray("distances").size() != count)
            if (rspCount != count)
                throw new IllegalStateException(count + " -> mismatch " + rspCount + ", " + json);
        } finally {
            if (rsp != null) rsp.body().close();
        }
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