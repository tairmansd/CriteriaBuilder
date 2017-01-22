package com.bats.criteriagenerator.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bats.criteriagenerator.service.CriteriaService;

@RestController
@RequestMapping("/criteria")
public class BuilderRestController
{
	@Autowired
	private CriteriaService service;
	
	/*@RequestMapping(value = "/search/{entityName}", method = RequestMethod.GET)
	public List<?> getMetaData(@PathVariable String entityName, @RequestBody String queryHash, @RequestParam("query") String query) {
		return service.search(entityName, queryHash, query);
	}*/
	
	@RequestMapping(value = "/search/{entityName}", method = RequestMethod.GET)
	public List<?> getMetaData(@PathVariable String entityName) {
		
		String queryHash = null;
		String query = "empNo:10002ANDfirstName:INDERJEET";
		return service.search(entityName, queryHash, query );
	}
}

