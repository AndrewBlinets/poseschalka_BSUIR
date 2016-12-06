package com.urban.basicsample.model;

public class Schedule {

	private String lessonTime;
	private int numSubgroup;
	private String studentGroup;
	private String subject;
	private String weekNumber = "";
	private String day;

	public String getLessonTime() {
		return lessonTime;
	}

	public void setLessonTime(String lessonTime) {
		this.lessonTime = lessonTime;
	}

	public int getNumSubgroup() {
		return numSubgroup;
	}

	public void setNumSubgroup(int numSubgroup) {
		this.numSubgroup = numSubgroup;
	}

	public String getStudentGroup() {
		return studentGroup;
	}

	public void setStudentGroup(String studentGroup) {
		this.studentGroup = studentGroup;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getWeekNumber() {
		return weekNumber;
	}

	public void addWeekNumber(String weekNumber) {
		this.weekNumber += weekNumber;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

}
