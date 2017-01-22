package com.bats.criteriagenerator.service;

import java.util.List;

public interface CriteriaService
{

	List<?> search(String entityName, String queryHash, String query);
	
}
