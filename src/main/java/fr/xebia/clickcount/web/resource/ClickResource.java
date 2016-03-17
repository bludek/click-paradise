package fr.xebia.clickcount.web.resource;

import fr.xebia.clickcount.repository.ClickRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/")
@Singleton
public class ClickResource {

    @Inject
    private ClickRepository clickRepository;

    @GET
    @Path("click")
    @Produces(MediaType.TEXT_PLAIN)
    public long getCount() {
        return clickRepository.getCount();
    }

    @POST
    @Path("click")
    @Produces(MediaType.TEXT_PLAIN)
    public long incrementCount() {
        return clickRepository.incrementAndGet();
    }

    @GET
    @Path("healthcheck")
    @Produces(MediaType.TEXT_PLAIN)
    public String healthcheck() {
        String result = clickRepository.ping();
        if ("PONG".equals(result)) {
            return "ok";
        }
        return "ko : " + result;
    }

}
