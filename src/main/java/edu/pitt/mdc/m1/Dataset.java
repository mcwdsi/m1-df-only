package edu.pitt.mdc.m1;

public class Dataset extends DigitalResearchObject {
   Integer dataFormat;
   
   Dataset(String title, Integer datasetId, Integer dataFormat){
	   super();
	   this.title = title;
	   this.rdoId = datasetId;
	   this.dataFormat = dataFormat;
   }
}
