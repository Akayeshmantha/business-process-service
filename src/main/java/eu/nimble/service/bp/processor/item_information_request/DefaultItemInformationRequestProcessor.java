package eu.nimble.service.bp.processor.item_information_request;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import eu.nimble.service.bp.application.IBusinessProcessApplication;
import eu.nimble.service.bp.impl.util.persistence.DocumentDAOUtility;
import eu.nimble.service.bp.swagger.model.ExecutionConfiguration;
import eu.nimble.service.bp.swagger.model.ProcessConfiguration;
import eu.nimble.service.model.ubl.iteminformationrequest.ItemInformationRequestType;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DefaultItemInformationRequestProcessor implements JavaDelegate {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @HystrixCommand
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info(" $$$ DefaultItemInformationRequestProcessor: {}", execution);
        final Map<String, Object> variables = execution.getVariables();
        // for debug purposes
        for (String key : variables.keySet()) {
            logger.debug(" $$$ Variable name {}, value {}", key, variables.get(key));
        }

        // get process instance id
        String processInstanceId = execution.getProcessInstance().getProcessInstanceId();

        // get input variables
        String buyer = variables.get("initiatorID").toString();
        String seller = variables.get("responderID").toString();
        String creatorUserID = variables.get("creatorUserID").toString();
        String processContextId = variables.get("processContextId").toString();
        List<String> relatedProducts = (List<String>) variables.get("relatedProducts");
        List<String> relatedProductCategories = (List<String>) variables.get("relatedProductCategories");
        ItemInformationRequestType itemInformationRequest = (ItemInformationRequestType) variables.get("itemInformationRequest");

        // get application execution configuration
        ExecutionConfiguration executionConfiguration = DocumentDAOUtility.getExecutionConfiguration(buyer, execution.getProcessInstance().getProcessDefinitionId(), ProcessConfiguration.RoleTypeEnum.BUYER, "ITEM_INFORMATION_REQUEST",
                ExecutionConfiguration.ApplicationTypeEnum.DATAPROCESSOR);
        String applicationURI = executionConfiguration.getExecutionUri();
        ExecutionConfiguration.ExecutionTypeEnum executionType = executionConfiguration.getExecutionType();

        // Call that configured application with the variables
        if(executionType == ExecutionConfiguration.ExecutionTypeEnum.JAVA) {
            Class applicationClass = Class.forName(applicationURI);
            Object instance = applicationClass.newInstance();

            IBusinessProcessApplication businessProcessApplication = (IBusinessProcessApplication) instance;

            businessProcessApplication.saveDocument(processContextId,processInstanceId, buyer, seller,creatorUserID, itemInformationRequest, relatedProducts, relatedProductCategories);
        } else if(executionType == ExecutionConfiguration.ExecutionTypeEnum.MICROSERVICE) {
            // TODO: How to call a microservice
        } else {
            // TODO: think other types of execution possibilities
        }
    }
}
