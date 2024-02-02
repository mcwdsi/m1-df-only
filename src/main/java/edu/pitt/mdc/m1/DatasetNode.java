package edu.pitt.mdc.m1;

public class DatasetNode extends Node {
	int id;
	int formatId;
	String name;

	public DatasetNode(int id, int formatId, String name) {
		this.id = id;
		this.name = name;
		this.formatId = formatId;
	}

	public int getId() {
		return id;
	}

	public int getFormatId() {
		return formatId;
	}

	public String getName() {
		return name;
	}

} // For future use. No need to represent datasets as nodes until software are enacted and producing output datasets
 