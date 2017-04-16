package com.bats.criteriagenerator.service;

public enum Operators
{
	EQ (":"),
	NE ("!"),
	LE ("<"),
	LTE ("<="),
	GE (">"),
	GTE (">="),
	EX ("*");
	
	private String value;

	private Operators(String value)
	{
		this.value = value;
	}
	
	public static String[] getAll() {
		String[] result = {EQ.value, LE.value, LTE.value, GE.value, GTE.value, EX.value, NE.value};
		return result;
	}
	
	public static Operators getEnum(String value) {
		Operators[] enums = {EQ, LE, LTE, GE, GTE, EX, NE};
		for (Operators a : enums)	
		{
			if(a.value.equals(value)) {
				return a;
			}
		}
		return null;
	}
	
	public static String splitRegex() {
		//except for LIKE %(.*?)% [\)]|\(([^\(]+)$
		return "[:|<=|<|>=|>|\\*|!]+";
	}
 }
 