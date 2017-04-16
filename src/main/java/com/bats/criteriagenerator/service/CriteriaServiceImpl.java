package com.bats.criteriagenerator.service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CriteriaServiceImpl<T> implements CriteriaService
{
	@Resource(name="dateFormats")
	private String[] pattern;
	
	@Resource(name="entityList")
	private Map<String, Class<T>> entityList;
	
	@Autowired
	private EntityManager entitymanager;
		
	@Override
	public List<?> search(String entityName, String queryHash, String query)
	{
		List<T> resultsList = new ArrayList<>();
		if(validateQuery(query, queryHash)) {
			
			Class<T> clazz = entityList.get(entityName);
			
			if(clazz == null) throw new RuntimeException("ENTITY.NOT.FOUND");
			
			CriteriaBuilder criteriaBuilder = entitymanager.getCriteriaBuilder();
			CriteriaQuery<T> cq = criteriaBuilder.createQuery(clazz);
			Root<T> root = cq.from(clazz);			
			
			if(query!=null) {
				Predicate predicate = populatePredicates(root, query);
				cq.where(predicate);				
			}
			
            cq.select(root);
            TypedQuery<T> q = entitymanager.createQuery(cq);
            resultsList = q.getResultList();
            System.out.println(resultsList.size());
		} else {
			throw new RuntimeException("QUERY.HASH.DOESNT.MAP");
		}
		return resultsList;
	}
	
	/**
	 * input query = field:abcAND(field<bcdOR(field:defANDfield>=efg))
	 * return field:abc,field<bcd,field:def,field>=efg,AND,OR,AND
	 * @param root 
	 * @param query
	 * @param predicates 
	 * @return 
	 * @return
	 */
	private Predicate populatePredicates(Root<T> root, String query)
	{
		if(StringUtils.countOccurrencesOf(query, Conjunctions.SP.toString()) == StringUtils.countOccurrencesOf(query, Conjunctions.EP.toString())) {
			LinkedList<String> postfix = createPostfixExpression(query);
			boolean hasSingleSearchField = postfix.size() == 1;  
			
			Map<String, Predicate> predicateMap = new LinkedHashMap<>();
			
			for (int i = 0; i < postfix.size(); i++)
			{
				String attr = postfix.get(i);
				if(Conjunctions.isConjunction(attr)) {
					
					String key = postfix.get(i-1) + attr + postfix.get(i-2);
					
					Predicate rightPredicate = (predicateMap.containsKey(postfix.get(i-1)))? predicateMap.get(postfix.get(i-1)) : buildPredicate(root, new SearchField(postfix.get(i-1))); 
					
					Predicate leftPredicate = (predicateMap.containsKey(postfix.get(i-2)))? predicateMap.get(postfix.get(i-2)) : buildPredicate(root, new SearchField(postfix.get(i-2)));
					
					postfix.set(i-2, key);
					postfix.remove(i);
					postfix.remove(i-1);
					
					//reset loop
					i=0;
					
					List<Predicate> combinedPredicates = new ArrayList<>();
					combinedPredicates.add(leftPredicate);
					combinedPredicates.add(rightPredicate);
					
					Predicate combinedPredicate = buildPredicateWithOperator(root, Conjunctions.getEnum(attr), combinedPredicates);
					predicateMap.put(key, combinedPredicate);
				}
			}
			
			if(hasSingleSearchField) {
				SearchField field = new SearchField(postfix.get(0));
				predicateMap.put(postfix.get(0), buildPredicate(root, field));
			}
			
			return (Predicate) predicateMap.values().toArray()[predicateMap.size()-1];
		} else {
			throw new RuntimeException("MALFORMED.QUERY");
		}
	}
	
	protected Predicate buildPredicateWithOperator(Path<T> tt, Conjunctions conjunctions, List<Predicate> predicateList) {
		CriteriaBuilder criteriaBuilder = this.entitymanager.getCriteriaBuilder();
		Predicate predicate = null;
		if(conjunctions.equals(Conjunctions.AND)) {
			predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
		} else if(conjunctions.equals(Conjunctions.OR)) {
			predicate = criteriaBuilder.or(predicateList.toArray(new Predicate[predicateList.size()]));
		}
		return predicate;
	}
	
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	protected Predicate buildPredicate(Path<T> root, SearchField field)
	{
		Path<T> tt = (!field.getField().contains(".")) ? root.get(field.getField()) : fetchNestedPath(root, field.getField());
		CriteriaBuilder criteriaBuilder = this.entitymanager.getCriteriaBuilder();
		
		Class<?> javaType = tt.getJavaType();
		
		if (!classCompatibleWithOperator(javaType, field.getOperator()))
		{
			throw new RuntimeException("operator incompatible with field");
		}
		
		Object valueObject = convertStringValueToObject(field.getValue(), javaType);
		switch (field.getOperator())
		{
			case GE:
				return criteriaBuilder.greaterThan((Expression) tt, (Comparable) valueObject);
			case GTE:
				return criteriaBuilder.greaterThanOrEqualTo((Expression) tt, (Comparable) valueObject);
			case LE:
				return criteriaBuilder.lessThan((Expression) tt, (Comparable) valueObject);
			case LTE:
				return criteriaBuilder.lessThanOrEqualTo((Expression) tt, (Comparable) valueObject);
			case NE: 
                return criteriaBuilder.notEqual(tt, valueObject); 
			case EX:
				return criteriaBuilder.like((Expression) tt, "%"+field.getValue()+"%");
			default:
			{
				//EQ
				return criteriaBuilder.equal(tt, valueObject);
			}
		}
	}
	
	private Path<T> fetchNestedPath(Path<T> root, String fieldname) {
		String[] fields = fieldname.split("\\.");
		Path<T> result = null;
		for (String field : fields) {
			if(result == null) {
				result = root.get(field);
			} else {
				result = result.get(field);
			}
		}
		return result;
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	private Enum safeEnumValueOf(Class enumType, String name) {
        Enum enumValue = null;
        if (name != null) {
            try {
                enumValue = Enum.valueOf(enumType, name);
            } catch (Exception e) {
                enumValue = null;
            }
        }
        return enumValue;
    }
 
    private Object convertStringValueToObject(String value, Class<?> clazz) {
        Object convertedValue = null;
        if (clazz.isEnum()) {
            convertedValue = safeEnumValueOf(clazz, value);
        } else if (Date.class.isAssignableFrom(clazz)) {
            try {
                convertedValue = DateUtils.parseDateStrictly(value, pattern);
            } catch (ParseException ex) {
                convertedValue = null;
                convertedValue = new Date(Long.parseLong(value));
            }
        } else if ((clazz.isPrimitive() && !clazz.equals(boolean.class))
                    || (Number.class.isAssignableFrom(clazz))){
            try {
                convertedValue = NumberFormat.getInstance().parse(value);
            } catch (ParseException ex) {
                convertedValue = null;
            }
        } else {
            convertedValue = value;
        }
        if (convertedValue != null){
            return convertedValue;
        } else {
            throw new RuntimeException("Wrong format for value " + value);
        }
    }
    
    private boolean classCompatibleWithOperator(Class<?> clazz, Operators operator) {
        if (operator == null) {
            return true;
        } else {
            switch (operator) {
	            case NE:
                case EQ:
                    return true;
                case GE:
                case GTE:
                case LE:
                case LTE:
                    return (Date.class.isAssignableFrom(clazz)
                            || (clazz.isPrimitive() && !clazz.equals(boolean.class))
                            || Number.class.isAssignableFrom(clazz));
                case EX:
                    return String.class.equals(clazz);
                default:
                    return false;
            }
        }
    }
	
	private LinkedList<String> createPostfixExpression(String searchQuery) {
		String[] queryAttr = filterEmptyFields(searchQuery.split(Conjunctions.splitRegex()));
		LinkedList<String> postFixQueue = new LinkedList<>();
		Stack<String> operatorStack = new Stack<String>();
		for (String attr : queryAttr)
		{
			if(Conjunctions.isConjunction(attr)) {
				//operatorStack.push(attr);
				
				if(operatorStack.size()>0) {
					if(attr.equals(Conjunctions.SP.getValue())) {
						operatorStack.push(attr);
					} else if(attr.equals(Conjunctions.EP.getValue())) {
						do {
							if(!operatorStack.peek().equals(Conjunctions.EP.getValue()) && !operatorStack.peek().equals(Conjunctions.SP.getValue())) {
								postFixQueue.add(operatorStack.pop());							
							} 
						} while (!operatorStack.peek().equals(Conjunctions.SP.getValue()));
						operatorStack.pop();
					} else if(Conjunctions.getPreference(operatorStack.peek()) > Conjunctions.getPreference(attr)) {
						if(!operatorStack.peek().equals(Conjunctions.EP.getValue()) && !operatorStack.peek().equals(Conjunctions.SP.getValue())) {
							postFixQueue.add(operatorStack.pop());
						} 
						operatorStack.push(attr);
					}
				} else {
					operatorStack.push(attr);
				}
			} else {
				postFixQueue.add(attr);
			}
		}
		
		while (!operatorStack.empty()) {
			postFixQueue.add(operatorStack.pop());
		};
		
		System.out.println(postFixQueue);
		return postFixQueue;
	}
	
	private String[] filterEmptyFields(String[] fields) {
		List<String> result = new ArrayList<>();
		for (String field : fields)
		{
			if(field != "" && field.length() > 0) result.add(field);
		}
		return result.toArray(new String[result.size()]);
	}

	private boolean validateQuery(String query, String queryHash) {
		//TODO: write implementation of this method
		return true;
	}

}
