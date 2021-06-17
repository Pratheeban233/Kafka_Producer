package gov.nic.eap.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import gov.nic.eap.service.implementation.RestIngester;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@Autowired
	private RestIngester restIngester;

	@RequestMapping(value = "/{action}/**", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> getRichRestServiceResponse(@PathVariable String action, 
			@RequestParam Map<String,String> allRequestParams,HttpServletRequest httpRequest) throws Exception {
		return restIngester.mTaskImplementation (action,null,null,null);
	}

}
