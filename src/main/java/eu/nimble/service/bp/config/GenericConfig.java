package eu.nimble.service.bp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * Created by suat on 17-May-18.
 */
@Component
public class GenericConfig {

    @Value("${nimble.data-channel.url}")
    private String dataChannelServiceUrl;

    @Value("${nimble.federation.core-endpoint}")
    private String coreEndpointUrl;

    @Value("${nimble.instance.instance_id}")
    private String instanceId;

    public String getInstanceid() {
        return instanceId;
    }

    public String getDataChannelServiceUrl() {
        return dataChannelServiceUrl;
    }

    public String getCoreEndpointUrl(){return coreEndpointUrl;}
}
