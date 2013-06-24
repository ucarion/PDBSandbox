package com.ulyssecarion.pdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceTablesDriver {
	private static final int START_AT = 91000;
	private static final int STORE_EVERY = 1000;

	public static void main(String[] args) throws Exception {
		buildAndSaveTables();
	}
	
	/**
	 * Goes through a list of PDBs and generates distance tables for each of
	 * them, serializing in groups of {@value #STORE_EVERY}.
	 * 
	 * @throws Exception because I'm too lazy to catch these properly
	 */
	public static void buildAndSaveTables() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("pdbids.txt"));
		String pdbId;
		List<String> pdbIds = new ArrayList<>();

		while ((pdbId = br.readLine()) != null) {
			pdbIds.add(pdbId);
		}

		br.close();

		if (DistanceTablesBuilder.VERBOSE)
			System.out.println("There are " + pdbIds.size()
					+ " PDB IDs to work on.");

		for (int i = START_AT; i < pdbIds.size(); i += STORE_EVERY) {
			long start = System.currentTimeMillis();

			List<String> toBuild = new ArrayList<>();

			for (int j = i; j < i + STORE_EVERY && j < pdbIds.size(); j++) {
				toBuild.add(pdbIds.get(j));
			}

			if (DistanceTablesBuilder.VERBOSE) {
				System.out.println("Building table for " + toBuild.get(0)
						+ " to " + toBuild.get(toBuild.size() - 1));
				System.out.println("Last save was around " + i);
			}

			Map<String, Map<String, Long>> tables = new HashMap<>();
			int count = 0;
			for (String pdb : toBuild) {
				try {
					tables.put(pdb, DistanceTablesBuilder.buildTableFor(pdb));
					if (DistanceTablesBuilder.VERBOSE)
						System.out.println("Building " + pdb + " (#"
								+ (i + count++) + ")");
				} catch (Exception e) {
					System.err.println("Error encountered on: " + pdb);
				}
			}

			DistanceTableSerializer.saveTables(tables);

			long stop = System.currentTimeMillis();

			if (DistanceTablesBuilder.VERBOSE)
				System.out.println("Took " + (stop - start) / 1000
						+ " seconds to get through that section.");
		}
	}
}
