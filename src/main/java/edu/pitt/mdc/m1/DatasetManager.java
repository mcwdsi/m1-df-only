package edu.pitt.mdc.m1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DatasetManager {
	
	ArrayList<DatasetNode> ds;
	HashMap<Integer, ArrayList<DatasetNode>> formatIdToDatasets;

	public DatasetManager(ArrayList<DatasetNode> ds) {
		this.ds = (ArrayList<DatasetNode>)ds.clone();
		buildFormatMap();
	}

	protected void buildFormatMap() {
		formatIdToDatasets = new HashMap<Integer, ArrayList<DatasetNode>>();
		for (DatasetNode d : ds) {
			int formatId = d.getFormatId();
			int key = Integer.valueOf(formatId);
			ArrayList<DatasetNode> dsForFormat;
			if (!formatIdToDatasets.containsKey(key)) {
				dsForFormat = new ArrayList<DatasetNode>();
				formatIdToDatasets.put(key, dsForFormat);
			} else {
				dsForFormat = formatIdToDatasets.get(key);
			}
			dsForFormat.add(d);
		}
	}

	public Iterator<DatasetNode> getDatasetsForFormatId(Integer formatId) {
		ArrayList<DatasetNode> datasets = formatIdToDatasets.get(formatId);
		if (datasets != null)
			return datasets.iterator();
		else 
			return null;
	}

}