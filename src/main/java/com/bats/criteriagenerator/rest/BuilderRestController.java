package com.bats.criteriagenerator.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bats.criteriagenerator.service.CriteriaService;

@RestController
@RequestMapping("/criteria")
public class BuilderRestController
{
	@Autowired
	private CriteriaService service;
	
	@RequestMapping(value = "/search/{entityName}", method = RequestMethod.GET)
	public List<?> getMetaData( @PathVariable String entityName,
								@RequestParam(value="query", required=false) String query,
								@RequestParam(value="limit", required=false, defaultValue="10") int limit,
								@RequestParam(value="page", required=false, defaultValue="1") int pagenumber) {
		return service.search(entityName, query, limit, pagenumber);
	}
}

