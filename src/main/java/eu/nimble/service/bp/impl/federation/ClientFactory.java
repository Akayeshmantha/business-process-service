package eu.nimble.service.bp.impl.federation;

import feign.Feign;
import feign.Response;
import feign.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class ClientFactory {

    @Autowired
    private CoreFunctions core;

    public BusinessProcessClient clientGenerator(String instanceid){
        String url=core.getEndpointFromInstanceId(instanceid);
        return createClient(BusinessProcessClient.class,url);
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
