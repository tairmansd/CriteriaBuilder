package com.bats.criteriagenerator.service;

import java.util.Arrays;

public enum Conjunctions
{
	AND ("AND"),
	OR ("OR"),
	SP ("("),
	EP (")");
	
	private String value;

	public String getValue()
	{
		return value;
	}

	private Conjunctions(String lit)
	{
		this.value = lit;
	}
	
	public static String[] getAll() {
		String[] result = {AND.value,  OR.value, SP.value, EP.value};
		return result;
	}
	
	public static int getPreference(String op) {
		if(SP.value.equalsIgnoreCase(op) ||  EP.value.equalsIgnoreCase(op)) {
			return 3;
		} else if(AND.value.equalsIgnoreCase(op)) {
			return 2;
		} else if(OR.value.equalsIgnoreCase(op)) {
			return 1;
		}
		return 0;
	}

	public static String splitRegex()
	{
		return "((?<=AND)|(?=AND))|((?<=OR)|(?=OR))|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))";
	}

	public static boolean isConjunction(String expAttr)
	{
		return Arrays.asList(getAll()).contains(expAttr);
	}
	
	public static Conjunctions getEnum(String value) {
		Conjunctions[] enums = {AND, OR, SP, EP};
		for (Conjunctions a : enums)	
		{
			if(a.value.equals(value)) {
				return a;
			}
		}
		return null;
	}
}
