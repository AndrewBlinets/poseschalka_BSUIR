package com.urban.basicsample.model;

public class Attendance {

	private String firstName;
	private String lastName;
	private String subjectName;
	private String group;
	private String date;
	private int at1 = 0;
	private int at2 = 0;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getAt1() {
		return at1;
	}

	public void setAt1(int at1) {
		this.at1 = at1;
	}

	public int getAt2() {
		return at2;
	}

	public void setAt2(int at2) {
		this.at2 = at2;
	}

}
