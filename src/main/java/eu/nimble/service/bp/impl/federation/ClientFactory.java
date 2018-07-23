package eu.nimble.service.bp.impl.federation;

import feign.Feign;
import org.springframework.stereotype.Component;

@Component
public class ClientFactory {

    public  <T> T createClient(Class<T> clientClass ,String url) {
        T result = Feign.builder()
                .decoder(new GsonDecoder())
                .target(clientClass, url);
        return result;
    }
}
