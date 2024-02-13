package edu.pitt.mdc.m1;

public class Software extends DigitalResearchObject
{
    Integer dataFormat;
    PortType portType;
	int portNumber;          // DF-matching port (the goodies!)
	
	Software(String title, Integer softwareId, PortType portType, int portNumber, int formatId) {
		super(softwareId);
		this.title = title;
		this.portType = portType;
		this.portNumber = portNumber;
		this.dataFormat = formatId;
	}

	public Integer getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(Integer dataFormat) {
		this.dataFormat = dataFormat;
	}

	public PortType getPortType() {
		return portType;
	}

	public void setPortType(PortType portType) {
		this.portType = portType;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
}