package com.hehehey.ghost;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.hehehey.ghost.resource.Task;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class ScheduleServer {

    private static final Logger logger = Logger.getLogger(ScheduleServer.class.getName());

    public static void main(String[] args) {
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(666).build();
        ResourceConfig config = new ResourceConfig(Task.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Thread.sleep(1000);
                server.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Server down!", e);
            }
        }
    }
}
