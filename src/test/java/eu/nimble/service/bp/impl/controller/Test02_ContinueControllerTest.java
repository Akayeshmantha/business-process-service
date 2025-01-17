package eu.nimble.service.bp.impl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.nimble.service.bp.impl.model.dashboard.CollaborationGroupResponse;
import eu.nimble.service.bp.swagger.model.ProcessInstance;
import eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage;
import eu.nimble.utility.JsonSerializationUtility;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
public class Test02_ContinueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String orderResponseJSON1 = "/controller/orderResponseJSON1.txt";

    @Test
    public void continueProcessInstance() throws Exception {
        ObjectMapper objectMapper = JsonSerializationUtility.getObjectMapper();
        String inputMessageAsString = IOUtils.toString(ProcessInstanceInputMessage.class.getResourceAsStream(orderResponseJSON1));
        inputMessageAsString = inputMessageAsString.replace("pid", Test01_StartControllerTest.processInstanceIdOrder1);

        // get collaboration group and process instance group ids for seller
        MockHttpServletRequestBuilder request = get("/collaboration-groups")
                .header("Authorization", TestConfig.initiatorPersonId)
                .param("partyID","706")
                .param("collaborationRole","SELLER")
                .param("offset", "0")
                .param("limit", "10");
        MvcResult mvcResult = this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();
        CollaborationGroupResponse collaborationGroupResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CollaborationGroupResponse.class);
        String sellerCollaborationGroupID = collaborationGroupResponse.getCollaborationGroups().get(0).getID();
        String sellerProcessInstanceGroupID = collaborationGroupResponse.getCollaborationGroups().get(0).getAssociatedProcessInstanceGroups().get(0).getID();

        // continue the process
        request = post("/continue")
                .header("Authorization", TestConfig.responderPersonId)
                .param("gid", sellerProcessInstanceGroupID)
                .param("collaborationGID", sellerCollaborationGroupID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(inputMessageAsString);
        mvcResult = this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();

        ProcessInstance processInstance = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProcessInstance.class);
        Assert.assertEquals(processInstance.getStatus(), ProcessInstance.StatusEnum.COMPLETED);
        Assert.assertEquals(processInstance.getProcessInstanceID(), Test01_StartControllerTest.processInstanceIdOrder1);
    }

}
