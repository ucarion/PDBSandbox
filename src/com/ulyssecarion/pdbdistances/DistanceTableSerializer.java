package com.ulyssecarion.pdbdistances;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DistanceTableSerializer {
	private static final String PATH_ELEMENT_PAIRS = "serialized_pairs"
			+ File.separator;
	private static final String PATH_SAVE = "saved_tables" + File.separator;
	private static final String EXTENSION = ".ser";

	public static boolean saveTables(
			Map<String, Map<String, DistanceResult>> table) {
		Object[] keys = table.keySet().toArray();

		String fileName = PATH_SAVE + keys[0] + "-" + keys[keys.length - 1]
				+ EXTENSION;

		if (DistanceTablesBuilder.VERBOSE)
			System.out.println("Saving to " + fileName);

		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(fileName));
			out.writeObject(table);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static void main(String[] args) {
		serialize(DistanceTablesBuilder.buildTable(Arrays.asList(new String[] { "100D", "101D", "101M" })));
		System.out.println(deserialize("MgBr"));
	}

	public static boolean serialize(Map<String, Map<String, DistanceResult>> distanceTable) {
		boolean allSerializedWithoutError = true;

		if (DistanceTablesBuilder.VERBOSE)
			System.out.println("Serializing...");

		for (String elementPair : distanceTable.keySet()) {
			if (DistanceTablesBuilder.VERBOSE)
				System.out.println("Serializing pair: " + elementPair);

			Map<String, DistanceResult> distances = distanceTable.get(elementPair);

			if (!serialize(elementPair, distances)) {
				System.err
						.println("Serializing encountered exception when saving: "
								+ elementPair);
				allSerializedWithoutError = false;
			}
		}

		return allSerializedWithoutError;
	}

	private static boolean serialize(String elementPair,
			Map<String, DistanceResult> distances) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(getFileFor(elementPair)));
			out.writeObject(distances);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static Map<String, Map<String, DistanceResult>> deserializeSavedTables() {
		File folder = new File(PATH_SAVE);
		File[] listOfFiles = folder.listFiles();

		Map<String, Map<String, DistanceResult>> pdb2Elem = new HashMap<>();

		for (File file : listOfFiles) {
			System.out.println(file.getName());
			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(file));

				@SuppressWarnings("unchecked")
				Map<String, Map<String, DistanceResult>> m = (Map<String, Map<String, DistanceResult>>) in
						.readObject();

				for (String pdbId : m.keySet()) {
					pdb2Elem.put(pdbId, m.get(pdbId));
				}

				in.close();
			} catch (Exception e) {
				System.err.println("Error when reading from file "
						+ file.getName());
				e.printStackTrace();
			}
		}

		return pdb2Elem;
	}

	public static Map<String, DistanceResult> deserialize(String elementPair) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					getFileFor(elementPair)));

			// nothing can be done about this warning
			@SuppressWarnings("unchecked")
			Map<String, DistanceResult> map = (Map<String, DistanceResult>) in.readObject();

			in.close();
			return map;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getFileFor(String elementPair) {
		return PATH_ELEMENT_PAIRS + elementPair + EXTENSION;
	}
}
