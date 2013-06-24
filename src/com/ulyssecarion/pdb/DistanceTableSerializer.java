package com.ulyssecarion.pdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.biojava.bio.structure.Element;

public class DistanceTableSerializer {
	private static final String PATH_ELEMENT_PAIRS = "serialized_pairs"
			+ File.separator;
	private static final String PATH_SAVE = "saved_tables" + File.separator;
	private static final String EXTENSION = ".ser";

	/**
	 * For temporary saving of generated tables for individual PDB entries. This
	 * is for when you need to save your tables as you go so you don't lose them
	 * all.
	 * 
	 * @param table
	 *            the table to save
	 * @return true if everything worked without any errors, false otherwise
	 */
	public static boolean saveTables(Map<String, Map<String, Long>> table) {
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

	/**
	 * Save a distances table (generated from
	 * {@link DistanceTablesBuilder#buildTable(java.util.List)}) by serializing
	 * it. You can get back the map for any element pair by using the
	 * {@link #deserialize(String)} method.
	 * 
	 * @param distanceTable
	 *            the distance table to serialize
	 * @return true if the table serialized without any errors, false otherwise
	 */
	public static boolean serialize(Map<String, Map<String, Long>> distanceTable) {
		boolean allSerializedWithoutError = true;

		if (DistanceTablesBuilder.VERBOSE)
			System.out.println("Serializing...");

		for (String elementPair : distanceTable.keySet()) {
			if (DistanceTablesBuilder.VERBOSE)
				System.out.println("Serializing pair: " + elementPair);

			Map<String, Long> distances = distanceTable.get(elementPair);

			if (!serialize(elementPair, distances)) {
				System.err
						.println("Serializing encountered exception when saving: "
								+ elementPair);
				allSerializedWithoutError = false;
			}
		}

		return allSerializedWithoutError;
	}
	
	// use this to rebuild the element-pairs, assuming you still have saved maps
	public static void main(String[] args) {
		serialize(DistanceTablesBuilder.reformatMap(deserializeSavedTables()));
	}

	/**
	 * Loads the saved tables from {@value #PATH_SAVE} and combines them together.
	 * 
	 * @return the combined saved tables
	 */
	public static Map<String, Map<String, Long>> deserializeSavedTables() {
		File folder = new File(PATH_SAVE);
		File[] listOfFiles = folder.listFiles();
		
		Map<String, Map<String, Long>> pdb2Elem = new HashMap<>();

		for (File file : listOfFiles) {
			System.out.println(file.getName());
			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(file));
				
				@SuppressWarnings("unchecked")
				Map<String, Map<String, Long>> m = (Map<String, Map<String, Long>>) in.readObject();
				
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

	private static boolean serialize(String elementPair,
			Map<String, Long> distances) {
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

	/**
	 * Deserializes the desired element pair. This will only work if
	 * {@link #serialize(Map)} has already been called.<br />
	 * 
	 * This method is a utility one; it is exactly identical to simply using<br />
	 * 
	 * <pre>
	 * deserialize(a + &quot;&quot; + b);
	 * </pre>
	 * 
	 * <b>Note:</b> This method is sensitive to the order of its arguments. Use
	 * responsibly.
	 * 
	 * @param a
	 *            the first element of the pair
	 * @param b
	 *            the second element of the pair
	 * @return the map corresponding to the passed pair
	 */
	public static Map<String, Long> deserialize(Element a, Element b) {
		return deserialize(a + "" + b);
	}

	/**
	 * Deserializes the desired element pair. This will of course only work if
	 * {@link #serialize(Map)} has already been called, of course.
	 * 
	 * @param elementPair
	 *            the strning representation of the element pair; this should
	 *            just be the concatenation of the two elements (i.e. Na + C
	 *            becomes "NaC").
	 * @return the map if the deserializing worked, null if not
	 */
	public static Map<String, Long> deserialize(String elementPair) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					getFileFor(elementPair)));

			// nothing can be done about this warning
			@SuppressWarnings("unchecked")
			Map<String, Long> map = (Map<String, Long>) in.readObject();

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
