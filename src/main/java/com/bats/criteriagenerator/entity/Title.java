package com.bats.criteriagenerator.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the titles database table.
 * 
 */
@Entity(name="title")
@Table(name="titles")
@NamedQuery(name="Title.findAll", query="SELECT t FROM title t")
public class Title implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private TitlePK id;

	@Temporal(TemporalType.DATE)
	@Column(name="to_date")
	private Date toDate;

	//bi-directional many-to-one association to Employee
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="emp_no", insertable=false, updatable=false)
	private Employee employee;

	public Title() {
	}

	public TitlePK getId() {
		return this.id;
	}

	public void setId(TitlePK id) {
		this.id = id;
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

}