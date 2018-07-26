package eu.nimble.service.bp.impl.federation;

import feign.Feign;
import feign.Response;
import feign.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

public class ClientFactory {


    private static ClientFactory clientFactoryInstance;

    public static ClientFactory getClientFactoryInstance() {
        if(clientFactoryInstance == null){
            clientFactoryInstance = new ClientFactory();
        }
        return clientFactoryInstance;
    }

    public  <T> T createClient(Class<T> clientClass ,String url) {
        T result = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(clientClass, url);
        return result;
    }

    public ResponseEntity createResponseEntity(Response response) throws Exception{
        String s = Util.toString(response.body().asReader());
        return  ResponseEntity.status(HttpStatus.valueOf(response.status())).body(s);
    }
}
