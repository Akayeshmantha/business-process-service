package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.impl.util.persistence.ProcessInstanceDAOUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dogukan on 28.07.2018.
 */
@Controller
public class ProcessInstanceController {

    @RequestMapping(value = "/processInstance/exists",
            method = RequestMethod.GET)
    public ResponseEntity processInstanceExists(@RequestParam(value = "relatedProducts", required = false) List<String> relatedProducts,
                                                @RequestParam(value = "relatedProductCategories", required = false) List<String> relatedProductCategories,
                                                @RequestParam(value = "tradingPartnerIDs", required = false) List<String> tradingPartnerIDs,
                                                @RequestParam(value = "initiationDateRange", required = false) String initiationDateRange,
                                                @RequestParam(value = "lastActivityDateRange", required = false) String lastActivityDateRange,
                                                @RequestParam(value = "processInstanceId", required = false) String processInstanceId) {
        return ResponseEntity.ok().body(ProcessInstanceDAOUtility.checkExistence(relatedProducts,relatedProductCategories,tradingPartnerIDs,initiationDateRange,lastActivityDateRange,processInstanceId));
    }

}
