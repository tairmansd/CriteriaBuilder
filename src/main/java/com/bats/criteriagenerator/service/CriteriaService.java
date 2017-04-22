package com.bats.criteriagenerator.service;

import java.util.List;

public interface CriteriaService
{

	List<?> search(String entityName, String query, int limit, int pagenumber);
	
}
