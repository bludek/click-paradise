package fr.xebia.clickcount;

import fr.xebia.clickcount.repository.ClickRepository;
import fr.xebia.clickcount.web.resource.ClickResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {

    public Application() {
        register(new ApplicationBinder());
        this.register(ClickResource.class);
    }

    private static class ApplicationBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(Configuration.class).to(Configuration.class);
            bind(ClickRepository.class).to(ClickRepository.class);
        }
    }

}



