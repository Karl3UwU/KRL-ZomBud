package io.github.karl3uwu.zombud.patches.experimental.http;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.karl3uwu.zombud.patches.experimental.HttpServer;
import io.github.karl3uwu.zombud.api.ZomBud;

public class VersionHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpServer.logRequest(exchange);
        String response = ZomBud.getFullVersionString() + "\n";
        HttpServer.sendResponse(exchange, 200, response);
    }
}
