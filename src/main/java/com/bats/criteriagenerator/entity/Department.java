package com.bats.criteriagenerator.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the departments database table.
 * 
 */
@Entity(name="department")
@Table(name="departments")
@NamedQuery(name="Department.findAll", query="SELECT d FROM department d")
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="dept_no")
	private String deptNo;

	@Column(name="dept_name")
	private String deptName;

	public Department() {
	}

	public String getDeptNo() {
		return this.deptNo;
	}

	public void setDeptNo(String deptNo) {
		this.deptNo = deptNo;
	}

	public String getDeptName() {
		return this.deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
}