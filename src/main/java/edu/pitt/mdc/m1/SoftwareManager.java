package edu.pitt.mdc.m1;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

public class SoftwareManager {

	ArrayList<SoftwareNode> softwareNodes = new ArrayList<SoftwareNode>();
	HashMap<Integer, ArrayList<SoftwareNode>> softwareByInputFormatId =
		new HashMap<Integer, ArrayList<SoftwareNode>>();
	HashMap<Integer, ArrayList<SoftwareNode>> softwareByOutputFormatId =
		new HashMap<Integer, ArrayList<SoftwareNode>>();
	
	public void loadFromJsonFile(File f) {
		try {
			FileReader fr = new FileReader(f);
			JsonReader jr = new JsonReader(fr);
			JsonElement je = JsonParser.parseReader(jr);
			Gson gs = new Gson();
			if (je.isJsonArray()) {
				JsonArray ja = je.getAsJsonArray();
				Iterator<JsonElement> i = ja.iterator();
				while (i.hasNext()) {
					JsonElement jei = i.next();
					if (!jei.isJsonNull()) {
						JsonObject jo = jei.getAsJsonObject();

						JsonPrimitive idJp = jo.getAsJsonPrimitive("id");
						JsonPrimitive titleJp = jo.getAsJsonPrimitive("title");
						JsonArray inputsJa = jo.getAsJsonArray("inputs");
						JsonArray outputsJa = jo.getAsJsonArray("outputs");

						int id = idJp.getAsInt();
						String title = titleJp.getAsString();

						SoftwareNode sn = new SoftwareNode(id);
						sn.title = title;

						ArrayList<SoftwarePort> inputPorts = parseInputs(inputsJa, id);
						ArrayList<SoftwarePort> outputPorts = parseOutputs(outputsJa, id);
						sn.inputPorts = inputPorts;
						sn.outputPorts = outputPorts;
						softwareNodes.add(sn);
					}
				}
			}
			buildHashMaps();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<SoftwarePort> parseInputs(JsonArray ja, int softwareId) {
		ArrayList<SoftwarePort> inputPorts = new ArrayList<SoftwarePort>();
		Iterator<JsonElement> i = ja.iterator();
		while (i.hasNext()) {
			JsonElement inputJe = i.next();
			JsonObject inputJo = inputJe.getAsJsonObject();
			JsonPrimitive numAsJp = inputJo.getAsJsonPrimitive("number");
			JsonArray formatsAsJa = inputJo.getAsJsonArray("formatIds");
			Iterator<JsonElement> j = formatsAsJa.iterator();
			JsonElement firstFormatAsJe = j.next();
			int formatId = firstFormatAsJe.getAsInt();
			SoftwarePort sp = new SoftwarePort(softwareId, PortType.INPUT, formatId);
			inputPorts.add(sp);
		}
		return inputPorts;
	}

	public ArrayList<SoftwarePort> parseOutputs(JsonArray ja, int softwareId) {
		ArrayList<SoftwarePort> outputPorts = new ArrayList<SoftwarePort>();
		Iterator<JsonElement> i = ja.iterator();
		while (i.hasNext()) {
			JsonElement inputJe = i.next();
			JsonObject inputJo = inputJe.getAsJsonObject();
			JsonPrimitive numAsJp = inputJo.getAsJsonPrimitive("number");
			JsonArray formatsAsJa = inputJo.getAsJsonArray("formatIds");
			Iterator<JsonElement> j = formatsAsJa.iterator();
			JsonElement firstFormatAsJe = j.next();
			int formatId = firstFormatAsJe.getAsInt();
			SoftwarePort sp = new SoftwarePort(softwareId, PortType.OUTPUT, formatId);
			outputPorts.add(sp);
		}
		return outputPorts;
	}

	protected void buildHashMaps() {
		for (SoftwareNode sn : softwareNodes) {
			for (SoftwarePort sp : sn.inputPorts) {
				Integer formatId = sp.dataFormatID;
				ArrayList<SoftwareNode> sForId;
				if (!softwareByInputFormatId.containsKey(formatId)) {
					sForId = new ArrayList<SoftwareNode>();
					softwareByInputFormatId.put(formatId, sForId);
				} else {
					sForId = softwareByInputFormatId.get(formatId);
				}
				sForId.add(sn);
			}

			for (SoftwarePort sp : sn.outputPorts) {
				Integer formatId = sp.dataFormatID;
				ArrayList<SoftwareNode> sForId;
				if (!softwareByOutputFormatId.containsKey(formatId)) {
					sForId = new ArrayList<SoftwareNode>();
					softwareByOutputFormatId.put(formatId, sForId);
				} else {
					sForId = softwareByOutputFormatId.get(formatId);
				}
				sForId.add(sn);
			}
		}
	}
}