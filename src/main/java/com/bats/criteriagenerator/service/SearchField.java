package com.bats.criteriagenerator.service;

public class SearchField
{

	private String field;
	private String value;
	private Operators operator;
	private String expression;
	
	public SearchField(String expression)
	{
		String operatorValue = new String();
		String[] keyValue = expression.split(Operators.splitRegex());
		
		if(keyValue.length != 2) throw new RuntimeException("MALFORMED.EXPRESSION:"+expression);
		
		for (String op : Operators.getAll())
		{
			if(expression.contains(op)) {
				operatorValue = op;
			}
		}
		this.field = keyValue[0];
		this.value = keyValue[1];
		this.operator = Operators.getEnum(operatorValue);
		this.expression = expression;
	}
	
	public String getField()
	{
		return field;
	}
	public void setField(String field)
	{
		this.field = field;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	public Operators getOperator()
	{
		return operator;
	}
	public void setOperator(Operators operator)
	{
		this.operator = operator;
	}
	
	@Override
	public String toString()
	{
		return this.expression;
	}
}
