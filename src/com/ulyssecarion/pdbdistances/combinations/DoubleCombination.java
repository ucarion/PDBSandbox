package com.ulyssecarion.pdbdistances.combinations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.Element;

import com.ulyssecarion.pdbdistances.AtomWrapper;
import com.ulyssecarion.pdbdistances.AtomWrapper.LigandAtom;
import com.ulyssecarion.pdbdistances.AtomWrapper.TargetAtom;
import com.ulyssecarion.pdbdistances.DistanceQuery;
import com.ulyssecarion.pdbdistances.DistanceResult;
import com.ulyssecarion.pdbdistances.DistanceTablesBuilder;

/**
 * Combinations that look like this:
 * 
 * <pre>
 * 	a) -- (b-c) -- (d
 * </pre>
 * 
 * In other words, combinations such that:
 * 
 * <pre>
 * 	a is linked to b
 * 	b and c share a group
 * 	c is linked to d
 * 	a and d do not share a group
 * </pre>
 * 
 * @author Ulysse Carion
 * 
 */
public class DoubleCombination {
	/**
	 * Finds all PDBs with the given double combination.
	 * 
	 * @param ab
	 *            the link between atoms 'a' and 'b'
	 * @param cd
	 *            the link between atoms 'c' and 'd'
	 * @return a list of PBB IDs matching the query
	 */
	public static List<String> find(Map<String, Map<String, DistanceResult>> m,
			DistanceQuery ab, DistanceQuery cd) {
		List<String> matches = new ArrayList<>();

		String keyAB = ab.toKey();
		String keyCD = cd.toKey();

		Map<String, DistanceResult> candidatesAB = m.get(keyAB);
		Map<String, DistanceResult> candidatesCD = m.get(keyCD);

		for (String pdbId : candidatesAB.keySet()) {
			if (!candidatesCD.containsKey(pdbId))
				continue;

			List<LigandAtom> candidatesB = AtomWrapper.getLigands(candidatesAB
					.get(pdbId));
			List<LigandAtom> candidatesC = AtomWrapper.getLigands(candidatesCD
					.get(pdbId));

			for (LigandAtom b : candidatesB) {
				for (LigandAtom c : candidatesC) {
					if (!b.isGroupedWith(c))
						continue;
					
					for (TargetAtom a : b.getLinks()) {
						for (TargetAtom d : c.getLinks()) {
							
						}
					}
				}
			}
		}

		return matches;
	}

	public static void main(String[] args) {
		DistanceQuery ab = new DistanceQuery(Element.N, null, null, Element.N,
				null, null, 0, 0);
		DistanceQuery cd = new DistanceQuery(Element.C, null, null, Element.O,
				null, null, 0, 0);

		Map<String, Map<String, DistanceResult>> m = DistanceTablesBuilder
				.buildTable(Arrays.asList(new String[] { "101D" }));
		find(m, ab, cd);
	}
}
