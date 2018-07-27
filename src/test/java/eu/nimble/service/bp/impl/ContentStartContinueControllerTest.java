package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.swagger.model.*;
import eu.nimble.service.bp.swagger.model.Process;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * API tests for DefaultApi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Ignore
public class ContentStartContinueControllerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    //TODO: Rewrite this test

    /**
     * Add a new business process
     */
    @Test
    public void t1_addProcessDefinitionTest() {
        Process body = TestObjectFactory.createOrderProcess();
        String url = "http://localhost:" + port + "/content";

        ResponseEntity<ModelApiResponse> response = restTemplate.postForEntity(url, body, ModelApiResponse.class);

        logger.info(" $$$ Test response {} ", response.toString());

        assertEquals(200, response.getBody().getCode().intValue());
    }

    /**
     * Get the business process definitions
     */
    @Test
    public void t2_getProcessDefinitionTest() {
        String processID = TestObjectFactory.getProcessID();
        String url = "http://localhost:" + port + "/content/{processID}";

        Map<String, String> params = new HashMap<String, String>();
        params.put("processID", processID);

        ResponseEntity<Process> response = restTemplate.getForEntity(url, Process.class, params);

        logger.info(" $$$ Test response {} ", response.toString());

        assertNotNull(response);
    }

    /**
     * Update a business process
     */
    @Test
    public void t4_updateProcessDefinitionTest() {
        Process body = TestObjectFactory.updateProcess();

        restTemplate.put("http://localhost:" + port + "/content", body);
    }

    static String processInstanceID = "";

    /**
     * Start an instance of a business process
     */
    @Test
    public void t50_startBusinessProcessInstanceTest() {
        ProcessInstanceInputMessage body = TestObjectFactory.createStartProcessInstanceInputMessage();
        String url = "http://localhost:" + port + "/start";

        ResponseEntity<ProcessInstance> response = restTemplate.postForEntity(url, body, ProcessInstance.class);

        logger.info(" $$$ Test response {} ", response.toString());

        processInstanceID = response.getBody().getProcessInstanceID();

        ProcessDocumentMetadata document = TestObjectFactory.createBusinessDocumentMetadata(processInstanceID);
        url = "http://localhost:" + port +"/document";

        restTemplate.postForEntity(url, document, ModelApiResponse.class);

        assertNotNull(response);
    }

    static ProcessInstanceGroup group = null;

    /**
     * Creates a business process instance group
     */
    @Test
    public void t51_createBusinessProcessInstanceGroupTest() {
        group = TestObjectFactory.createProcessInstanceGroupMessage();
        //group.addProcessInstanceIDsItem(processInstanceID);

        String url = "http://localhost:" + port + "/group";
        logger.info(" $$$ Sending process instance group {} ", group.toString());

        ResponseEntity<Boolean> response = restTemplate.postForEntity(url, group, Boolean.class);

        logger.info(" $$$ Create Process Instance Group {} ", response.toString());

        assertNotNull(response);
    }

    /**
     * Send input to a waiting process instance (because of a human task)
     */
    @Test
    public void t6_continueBusinessProcessInstanceTest() {
        ProcessInstanceInputMessage body = TestObjectFactory.createContinueProcessInstanceInputMessage();

        body.setProcessInstanceID(processInstanceID);

        String url = "http://localhost:" + port + "/continue";

        ResponseEntity<ProcessInstance> response = restTemplate.postForEntity(url, body, ProcessInstance.class);

        logger.info(" $$$ Test response {} ", response.toString());

        assertNotNull(response);
    }

    /**
     * Add a new business process
     */
    @Test
    public void t8_addProcessDefinitionTest() {
        Process body = TestObjectFactory.createNegotiationProcess();
        String url = "http://localhost:" + port + "/content";

        ResponseEntity<ModelApiResponse> response = restTemplate.postForEntity(url, body, ModelApiResponse.class);

        logger.info(" $$$ Test response {} ", response.toString());

        assertEquals(200, response.getBody().getCode().intValue());
    }

    /**
     * Get the business process definitions
     */
    @Test
    public void t90_getProcessDefinitionTest() {
        String processID = "NegotiationTest";
        String url = "http://localhost:" + port + "/content/{processID}";

        Map<String, String> params = new HashMap<String, String>();
        params.put("processID", processID);

        ResponseEntity<Process> response = restTemplate.getForEntity(url, Process.class, params);

        logger.info(" $$$ Test response {} ", response.toString());

        assertNotNull(response);
    }

    /**
     * Get the business process definitions
     */
    @Test
    public void t91_getProcessDefinitionsTest() {
        ResponseEntity<List> response = restTemplate.getForEntity("http://localhost:" + port + "/content", List.class);

        logger.info(" $$$ Test response {} ", response.toString());
        assertNotNull(response);
    }

    /**
     * Start an instance of a business process
     */
    @Test
    public void t920_startBusinessProcessInstanceTest() {
        ProcessInstanceInputMessage body = TestObjectFactory.createStartProcessInstanceInputMessageForNegotiation();
        String url = "http://localhost:" + port + "/start";

        ResponseEntity<ProcessInstance> response = restTemplate.postForEntity(url, body, ProcessInstance.class);

        logger.info(" $$$ Test response {} ", response.toString());

        processInstanceID = response.getBody().getProcessInstanceID();

        ProcessDocumentMetadata document = TestObjectFactory.createBusinessDocumentMetadata(processInstanceID);
        url = "http://localhost:" + port +"/document";

        restTemplate.postForEntity(url, document, ModelApiResponse.class);

        assertNotNull(response);
    }

    /**
     * Creates a business process instance group
     */
    @Test
    public void t921_createBusinessProcessInstanceGroupTest() {
        String url = "http://localhost:" + port + "/group/" + group.getID() + "/process-instance";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("processInstanceID", processInstanceID);

        ResponseEntity<ProcessInstanceGroup> response = restTemplate.postForEntity(url, form, ProcessInstanceGroup.class);

        logger.info(" $$$ Create Process Instance Group {} {}", url, response.toString());

        assertNotNull(response);
    }

    /**
     * Search for a business process instance group
     */
    @Test
    public void t922_searchBusinessProcessInstanceGroupTest() {
        String url = "http://localhost:" + port + "/group";

        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        logger.info(" $$$ Process instance groups {} {}", url, response.toString());

        assertNotNull(response);
    }

    /**
     * Search for a business process instance group
     */
    @Test
    public void t923_searchBusinessProcessInstanceGroupTest() {
        String url = "http://localhost:" + port + "/group";

        List<String> tradingPartners = new ArrayList<>();
        tradingPartners.add("seller1387");
        tradingPartners.add("seller1388");

        List<String> products = new ArrayList<>();
        products.add("Chair");
        products.add("Laptop");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                // Add query parameter
                //.queryParam("initiationDateRange", "2018-03-01T15:45:37"+"_"+"2018-03-03T15:45:37")
                //.queryParam("tradingPartnerIDs", String.join(",", tradingPartners))
                .queryParam("relatedProducts", String.join(",", products))
                .queryParam("partyID", TestObjectFactory.getPartnerID());


        ResponseEntity<List> response = restTemplate.getForEntity(builder.toUriString(), List.class);

        logger.info(" $$$ Process instance groups {} {}", builder.toUriString(), response.toString());

        assertNotNull(response);
    }

    /**
     * Send input to a waiting process instance (because of a human task)
     */
    @Test
    public void t93_continueBusinessProcessInstanceTest() {
        ProcessInstanceInputMessage body = TestObjectFactory.createContinueProcessInstanceInputMessageForNegotiation();

        body.setProcessInstanceID(processInstanceID);

        String url = "http://localhost:" + port + "/continue";

        ResponseEntity<ProcessInstance> response = restTemplate.postForEntity(url, body, ProcessInstance.class);

        logger.info(" $$$ Test response {} ", response.toString());

        assertNotNull(response);
    }

    /**
     * Deletes a business process definition
     */
    @Test
    public void t94_removeProcessDefinitionTest() {
        String processID = TestObjectFactory.getProcessID();
        String url = "http://localhost:" + port + "/content/{processID}";

        Map<String, String> params = new HashMap<String, String>();
        params.put("processID", processID);

        restTemplate.delete(url, params);
    }


    /**
     * Deletes a business process definition
     */
    @Test
    public void t95_removeProcessDefinitionTest() {
        String processID = "NegotiationTest";
        String url = "http://localhost:" + port + "/content/{processID}";

        Map<String, String> params = new HashMap<String, String>();
        params.put("processID", processID);

        restTemplate.delete(url, params);
    }

    /**
     * Deletes a business process instance group
     */
    @Test
    public void t96_deleteBusinessProcessInstanceGroupTest() {
        String url = "http://localhost:" + port + "/group/{ID}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ID", group.getID());

        restTemplate.delete(url, params);

    }
}
