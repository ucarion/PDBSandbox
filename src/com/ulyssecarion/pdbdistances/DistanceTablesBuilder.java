package com.ulyssecarion.pdbdistances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Element;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava3.structure.StructureIO;

public class DistanceTablesBuilder {
	protected static final boolean VERBOSE = true;

	private static final double MIN_DISTANCE = 1.7; // angstroms

	// there are 64 bits to store into a long; each bit represents 0.1 A
	private static final double MAX_DISTANCE = MIN_DISTANCE + 6.3;

	private static final List<String> WATERNAMES = Arrays.asList(new String[] {
			"HOH", "DOD", "WAT" });

	private static AtomCache cache;

	static {
		cache = getCache();
	}

	public static void main(String[] args) {
		DistanceQuery q = new DistanceQuery(Element.N, "N1", null, Element.C, null, null,
				asDistanceIndex(MIN_DISTANCE), asDistanceIndex(MAX_DISTANCE));

		Map<String, DistanceResult> m = buildTable(
				Arrays.asList(new String[] { "100D", "101D", "101M" })).get(
				"NC");

		System.out.println(m.get("101D"));
		System.out.println(AtomWrapper.getLigands(m.get("101D")));
	}

	public static List<String> getMatchesFor(
			Map<String, DistanceResult> pdbToResultMap, DistanceQuery query) {
		List<String> results = new ArrayList<>();

		for (String pdbId : pdbToResultMap.keySet()) {
			if (pdbToResultMap.get(pdbId).hasPotentialMatch(query)) {
				for (MatchDatum matchDatum : pdbToResultMap.get(pdbId)
						.getMatchData()) {
					if (matchDatum.matches(query) && !results.contains(pdbId)) {
						results.add(pdbId);
					}
				}
			}
		}

		return results;
	}

	public static Map<String, Map<String, DistanceResult>> buildTable(
			List<String> pdbIds) {
		Map<String, Map<String, DistanceResult>> pdbToPair = new HashMap<>();

		for (String pdbId : pdbIds) {
			pdbToPair.put(pdbId, buildTableFor(pdbId));
		}

		return reformatMap(pdbToPair);
	}

	public static Map<String, Map<String, DistanceResult>> reformatMap(
			Map<String, Map<String, DistanceResult>> pdbToPair) {
		Map<String, Map<String, DistanceResult>> result = new HashMap<>();

		for (String pdbId : pdbToPair.keySet()) {
			Map<String, DistanceResult> elementToBitmap = pdbToPair.get(pdbId);

			for (String elementPair : elementToBitmap.keySet()) {
				DistanceResult distanceResult = elementToBitmap
						.get(elementPair);

				if (result.containsKey(elementPair)) {
					result.get(elementPair).put(pdbId, distanceResult);
				} else {
					result.put(elementPair,
							new HashMap<String, DistanceResult>());
					result.get(elementPair).put(pdbId, distanceResult);
				}
			}
		}

		return result;
	}

	public static Map<String, DistanceResult> buildTableFor(String pdbId) {
		StructureIO.setAtomCache(cache);

		Map<String, DistanceResult> results = new HashMap<>();

		int bioAssemblyCount = StructureIO.getNrBiologicalAssemblies(pdbId);
		int bioAssemblyId = bioAssemblyCount > 0 ? 1 : 0;

		Structure structure = null;
		try {
			structure = StructureIO.getBiologicalAssembly(pdbId, bioAssemblyId);
		} catch (IOException | StructureException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		List<Atom> atoms = structureToAtomArray(structure);
		List<Group> ligands = getLigands(structure);

		try {
			for (Atom a : atoms) {
				if (ligands.contains(a.getGroup())) { // it's a ligand
					for (Atom b : atoms) {
						if (a.getGroup() != b.getGroup()) {
							double distance = Calc.getDistance(a, b);

							if (distance >= MIN_DISTANCE
									&& distance <= MAX_DISTANCE) {
								updateMap(results, a, b,
										asDistanceIndex(distance));
							}
						}
					}
				}
			}
		} catch (StructureException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		return results;
	}

	private static void updateMap(Map<String, DistanceResult> map, Atom a,
			Atom b, int distanceIndex) {
		String key = a.getElement() + "" + b.getElement();
		MatchDatum matchDatum = new MatchDatum(a, b, distanceIndex);

		if (map.containsKey(key)) {
			map.get(key).add(matchDatum);
		} else {
			map.put(key, new DistanceResult(matchDatum));
		}
	}

	private static List<Group> getLigands(Structure structure) {
		List<Group> ligands = new ArrayList<>();

		for (int i = 0; i < structure.nrModels(); i++) {
			List<Chain> model = structure.getModel(i);

			for (Chain chain : model) {
				List<Group> allGroups = chain.getAtomGroups();
				List<Group> seqResGroups = chain.getSeqResGroups();

				for (Group group : allGroups) {
					if (!seqResGroups.contains(group)
							&& !WATERNAMES.contains(group.getPDBName()))
						ligands.add(group);
				}
			}
		}

		return ligands;
	}

	private static List<Atom> structureToAtomArray(Structure structure) {
		ArrayList<Atom> atoms = new ArrayList<Atom>();

		for (int i = 0; i < structure.nrModels(); i++) {
			List<Chain> model = structure.getModel(i);

			for (Chain chain : model) {
				for (Group group : chain.getAtomGroups()) {
					for (Atom a : group.getAtoms()) {
						atoms.add(a);
					}
				}
			}
		}

		return atoms;
	}

	private static int asDistanceIndex(double distance) {
		if (distance < MIN_DISTANCE)
			throw new RuntimeException("Out of bounds: " + distance
					+ " must be greater than or equal to " + MIN_DISTANCE);
		if (distance > MAX_DISTANCE)
			throw new RuntimeException("Out of bounds: " + distance
					+ " must be less than or equal to " + MAX_DISTANCE);

		return (int) ((distance - MIN_DISTANCE) * 10);
	}

	private static AtomCache getCache() {
		AtomCache cache = new AtomCache();
		FileParsingParameters params = cache.getFileParsingParams();
		params.setStoreEmptySeqRes(true);
		params.setAlignSeqRes(true);
		params.setLoadChemCompInfo(true);

		return cache;
	}
}
