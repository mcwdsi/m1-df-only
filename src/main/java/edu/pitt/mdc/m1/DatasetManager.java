package edu.pitt.mdc.m1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class DatasetManager {
	
	ArrayList<DatasetNode> ds;
	HashMap<Integer, ArrayList<DatasetNode>> formatIdToDatasetNodes;
	HashMap<Integer, ArrayList<Dataset>> formatIdToDatasets;
	HashMap<Integer, Dataset> idToDataset;

	public DatasetManager(ArrayList<DatasetNode> ds) {
		this.ds = new ArrayList<DatasetNode>();
		for (DatasetNode dn : ds) {
			if (dn != null)
				this.ds.add(dn);
		}
		buildFormatMapToDatasetNodes();
		buildFormatMapToDatasets();
	}

	protected void buildFormatMapToDatasetNodes() {
		formatIdToDatasetNodes = new HashMap<Integer, ArrayList<DatasetNode>>();
		//System.out.println("DEBUG: number of datasets is " + this.ds.size());
		for (DatasetNode d : this.ds) {
			//System.out.println("\tDEBUG: dataset node d is " + d);
			int formatId = d.getFormatId();
			int key = Integer.valueOf(formatId);
			ArrayList<DatasetNode> dsForFormat;
			if (!formatIdToDatasetNodes.containsKey(key)) {
				dsForFormat = new ArrayList<DatasetNode>();
				formatIdToDatasetNodes.put(key, dsForFormat);
			} else {
				dsForFormat = formatIdToDatasetNodes.get(key);
			}
			dsForFormat.add(d);
		}
	}

	protected void buildFormatMapToDatasets() {
		formatIdToDatasets = new HashMap<Integer, ArrayList<Dataset>>();
		idToDataset = new HashMap<Integer, Dataset>();

		for (DatasetNode dn : this.ds) {
			//System.out.println("\tDEBUG: dataset node d is " + d);
			int formatId = dn.getFormatId();
			int key = Integer.valueOf(formatId);
			ArrayList<Dataset> dsForFormat;
			if (!formatIdToDatasets.containsKey(key)) {
				dsForFormat = new ArrayList<Dataset>();
				formatIdToDatasets.put(key, dsForFormat);
			} else {
				dsForFormat = formatIdToDatasets.get(key);
			}
			Dataset d = new Dataset(dn.title, dn.id, dn.formatId);
			dsForFormat.add(d);
			idToDataset.put(dn.id, d);
		}
	}

	public Iterator<DatasetNode> getDatasetNodesForFormatId(Integer formatId) {
		ArrayList<DatasetNode> datasets = formatIdToDatasetNodes.get(formatId);
		if (datasets != null)
			return datasets.iterator();
		else 
			return null;
	}

	public Iterator<Dataset> getDatasetsForFormatId(Integer formatId) {
		ArrayList<Dataset> datasets = formatIdToDatasets.get(formatId);
		if (datasets != null)
			return datasets.iterator();
		else 
			return null;
	}

	public Set<Integer> getUniqueDataFormats() {
		return formatIdToDatasets.keySet();
	}

	public Dataset getDatasetById(int datasetId) {
		return this.idToDataset.get(datasetId);
	}

	public String getTitleForDataset(int datasetId) {
		return this.getDatasetById(datasetId).title;
	}

}