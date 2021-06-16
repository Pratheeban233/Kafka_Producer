package gov.nic.eap.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import gov.nic.eap.service.implementation.RestIngester;

@RestController
public class RrsController {

	@Autowired
	private RestIngester restIngester;

	@RequestMapping(value = "/{action}/**", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> getRichRestServiceResponse(@PathVariable String action, 
			@RequestParam Map<String,String> allRequestParams,HttpServletRequest httpRequest) throws Exception {
		return restIngester.mTaskImplementation (action);
	}

}
