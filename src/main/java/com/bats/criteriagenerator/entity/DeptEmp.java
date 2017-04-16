package com.bats.criteriagenerator.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the dept_emp database table.
 * 
 */
@Entity(name="deptEmp")
@Table(name="dept_emp")
@NamedQuery(name="DeptEmp.findAll", query="SELECT d FROM deptEmp d")
public class DeptEmp implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private DeptEmpPK id;

	@Temporal(TemporalType.DATE)
	@Column(name="from_date")
	private Date fromDate;

	@Temporal(TemporalType.DATE)
	@Column(name="to_date")
	private Date toDate;

	//bi-directional many-to-one association to Employee
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="emp_no", insertable=false, updatable=false)
	private Employee employee;

	//bi-directional many-to-one association to Department
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="dept_no", insertable=false, updatable=false)
	private Department department;

	public DeptEmp() {
	}

	public DeptEmpPK getId() {
		return this.id;
	}

	public void setId(DeptEmpPK id) {
		this.id = id;
	}

	public Date getFromDate() {
		return this.fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return this.toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Employee getEmployee() {
		return this.employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Department getDepartment() {
		return this.department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

}