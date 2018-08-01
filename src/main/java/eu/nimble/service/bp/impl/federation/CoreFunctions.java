package eu.nimble.service.bp.impl.federation;
import eu.nimble.service.bp.config.GenericConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreFunctions {

    @Autowired
    private GenericConfig config;

    public String getEndpointFromInstanceId(String instanceId){

        String coreUrl=config.getCoreEndpointUrl();

        /*
            TODO:
            A consumer in order to get the instance endpoint from the core url.
        */
        if(instanceId.equals(config.getInstanceid())){
            return "http://localhost:8081";
        }
        else if(instanceId.equals("11")){
            return "http://localhost:8082";
        }
        else {
            return "http://localhost:8083";
        }
    }


    public boolean isValidToken(String instanceId, String bearerToken){

        String endpoint=this.getEndpointFromInstanceId(instanceId);
        /*
            TODO:
            A consumer in order to validate the token from instance endpoint.
        */
        return true;
    }

}
