package com.hehehey.ghost;

import com.hehehey.ghost.resource.DataResource;
import com.hehehey.ghost.resource.TaskResource;
import com.hehehey.ghost.resource.UrlResource;
import com.hehehey.ghost.schedule.DatabaseConnection;
import com.hehehey.ghost.schedule.MasterConfig;
import com.hehehey.ghost.schedule.RedisConnection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class ScheduleServer {

    private static final Logger logger = Logger.getLogger(ScheduleServer.class.getName());

    public static void main(String[] args) throws IOException, URISyntaxException {
        MasterConfig.reload();
        logger.log(Level.INFO, "Configuration load.");

        RedisConnection.newPool();
        DatabaseConnection.newConnection();
        logger.log(Level.INFO, "Backend ready.");

        URI baseUri = UriBuilder.fromUri(MasterConfig.INSTANCE.getBaseUrl())
                .port(MasterConfig.INSTANCE.getPort())
                .build();
        ResourceConfig config = new ResourceConfig(TaskResource.class, UrlResource.class, DataResource.class,
                CORSFilter.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Thread.sleep(1000);
                server.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Server down! Will retry later.", e);
            }
        }
    }

    @Provider
    public static class CORSFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext,
                           final ContainerResponseContext cres) throws IOException {
            cres.getHeaders().add("Access-Control-Allow-Origin", "*");
            cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
            cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            cres.getHeaders().add("Access-Control-Max-Age", "1209600");
        }

    }
}
