package com.electronicstore.springboot.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CommonContext {

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    private WebServer webServer;
    private int port;
    private UriComponentsBuilder baseUriBuilder;

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        webServer = event.getWebServer();
        port = webServer.getPort();
        baseUriBuilder = UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host("localhost")
                        .port(port);
    }

    public int serverPort(){
        return port;
    }

    public UriComponentsBuilder getBaseUriBuilder(){
        return baseUriBuilder.cloneBuilder();
    }

    public WebServer getWebServer() {
        return webServer;
    }

}
