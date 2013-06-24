package com.ulyssecarion.pdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import org.biojava.bio.structure.align.util.HTTPConnectionTools;

public class GetRepresentatives {
	private static String allUrl = "http://www.rcsb.org/pdb/rest/getCurrent/";

	/**
	 * Returns the current list of all PDB IDs.
	 * 
	 * @return PdbChainKey set of all PDB IDs.
	 */
	public static SortedSet<String> getAll() {
		SortedSet<String> representatives = new TreeSet<String>();

		try {

			URL u = new URL(allUrl);

			InputStream stream = HTTPConnectionTools.getInputStream(u, 60000);

			if (stream != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream));

				String line = null;

				while ((line = reader.readLine()) != null) {
					int index = line.lastIndexOf("structureId=");
					if (index > 0) {
						representatives.add(line.substring(index + 13,
								index + 17));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return representatives;
	}

	public static void main(String[] args) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter("pdbids.txt"));

		for (String s : getAll()) {
			out.write(s);
			out.newLine();
		}
		
		out.close();
	}
}
