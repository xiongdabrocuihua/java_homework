package com.thegame.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // 静态文件处理器
        server.createContext("/", new StaticFileHandler());
        
        // API 处理器
        server.createContext("/api", new ApiHandler());

        server.setExecutor(null); // default executor
        System.out.println("服务器已启动: http://localhost:" + port + "/index.html");
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String uri = t.getRequestURI().getPath();
            if (uri.equals("/")) uri = "/index.html";
            
            // 简单的安全检查，防止目录遍历
            if (uri.contains("..")) {
                sendResponse(t, 403, "Forbidden");
                return;
            }

            // 假设静态文件在 src/com/thegame/web/static 下，或者直接在项目根目录
            // 为了方便，我们把 index.html 放在项目根目录，或者 src/com/thegame/web/ 下
            // 这里假设放在 src/com/thegame/web/ 下
            File file = new File("src/com/thegame/web" + uri);
            if (!file.exists()) {
                // 尝试直接在根目录找（如果用户放在那里）
                file = new File("." + uri);
            }

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "File not found: " + uri;
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // 设置 CORS 头，方便本地调试（虽然同源不需要，但好习惯）
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            String path = t.getRequestURI().getPath();
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            
            WebGameManager gameManager = WebGameManager.getInstance();
            String response = "{}";

            try {
                if (path.endsWith("/start")) {
                    gameManager.initGame();
                    response = gameManager.getStatus();
                } else if (path.endsWith("/move")) {
                    String dir = params.get("dir");
                    if (dir != null) {
                        response = gameManager.handleMove(dir);
                    } else {
                        response = "{\"error\": \"Missing dir parameter\"}";
                    }
                } else if (path.endsWith("/battle")) {
                    String action = params.get("action");
                    String param = params.get("param");
                    if (action != null) {
                        response = gameManager.handleBattleAction(action, param);
                    } else {
                        response = "{\"error\": \"Missing action parameter\"}";
                    }
                } else if (path.endsWith("/useItem")) {
                    String index = params.get("index");
                    if (index != null) {
                        response = gameManager.handleUseItem(index);
                    } else {
                        response = "{\"error\": \"Missing index parameter\"}";
                    }
                } else if (path.endsWith("/status")) {
                    response = gameManager.getStatus();
                } else {
                    response = "{\"error\": \"Unknown API endpoint\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "{\"error\": \"" + e.getMessage() + "\"}";
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null) return result;
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }
    }
    
    private static void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        t.sendResponseHeaders(statusCode, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
