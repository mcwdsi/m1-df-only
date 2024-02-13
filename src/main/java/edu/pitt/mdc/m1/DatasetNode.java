package edu.pitt.mdc.m1;

public class DatasetNode extends Node {

	int formatId;
	String title;

	public DatasetNode(int id, int formatId, String title) {
		super(id);
		this.title = title;
		this.formatId = formatId;
	}

	public int getFormatId() {
		return this.formatId;
	}

	public String getTitle() {
		return this.title;
	}

} // For future use. No need to represent datasets as nodes until software are enacted and producing output datasets
 