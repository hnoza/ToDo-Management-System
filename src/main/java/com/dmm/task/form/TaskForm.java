package com.dmm.task.form;

import lombok.Data;

@Data
public class TaskForm {

	private String title;
	private String date;
	private String text;
	private Boolean done;
}
