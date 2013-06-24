package com.ulyssecarion.pdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Element;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava3.structure.StructureIO;

public class PDBLooper {
	private static final int CLOSEST_DISTANCE = 17;

	public static void main(String[] args) throws IOException,
			StructureException {
		AtomCache cache = initializeCache();

		BufferedReader br = new BufferedReader(new FileReader("pdbids.txt"));
		String pdbId;
		int count = 0;

		Map<String, Long> na2c = new HashMap<>();

		while ((pdbId = br.readLine()) != null && count++ < 50) {
			System.out.println(pdbId);

			StructureIO.setAtomCache(cache);
			int bioAssemblyCount = StructureIO.getNrBiologicalAssemblies(pdbId);
			// System.out.println("... has " + bioAssemblyCount
			// + " bio-assemblies.");
			int bioAssemblyId = bioAssemblyCount > 0 ? 1 : 0;
			// System.out.println("... using bio assembly #" + bioAssemblyId);

			Structure structure = StructureIO.getBiologicalAssembly(pdbId,
					bioAssemblyId);
			Atom[] atoms = StructureTools.getAllAtomArray(structure);

			long result = 0L;

			for (Atom atom : atoms) {
				if (atom.getElement().isMetal()) {
					// System.out.println(atom.getElement()
					// + " is a metal. Its distances:");

					for (Atom other : atoms) {
						double distance = Calc.getDistance(atom, other);

						if (distance < 8 && distance > CLOSEST_DISTANCE / 10.0
								&& atom != other
								&& atom.getElement() == Element.Na
								&& other.getElement() == Element.C) {
							
							System.out.println("\t" + distance);

							int index = (int) (distance * 10)
									- CLOSEST_DISTANCE;
							// System.out.println("\tWhich is index: " + index);

							result |= (1 << index);
						}
					}
				}
			}

			na2c.put(pdbId, result);
		}

		br.close();

		for (String pdb : na2c.keySet())
			System.out.println(pdb + ": "
					+ String.format("%064x", na2c.get(pdb)));

	}

	private static AtomCache initializeCache() {
		AtomCache cache = new AtomCache();
		FileParsingParameters params = cache.getFileParsingParams();
		params.setStoreEmptySeqRes(true);
		params.setAlignSeqRes(true);
		params.setLoadChemCompInfo(true);

		return cache;
	}
}
