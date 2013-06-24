package com.ulyssecarion.pdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.Element;
import org.junit.Test;

public class DistanceTablesBuilderTest {
	@Test
	public void test4ACL() {
		Map<String, Long> result = DistanceTablesBuilder.buildTableFor("4ACL");

		assertTrue(DistanceTablesBuilder.matches(result, Element.Au, Element.N,
				4.0, 4.1));
		assertTrue(DistanceTablesBuilder.matches(result, Element.Au, Element.C,
				5.2, 5.3));
		assertTrue(DistanceTablesBuilder.matches(result, Element.Au, Element.O,
				7.0, 7.1));
	}

	@Test
	public void testMatchesNonmetallicLigands() {
		Map<String, Long> result = DistanceTablesBuilder.buildTableFor("3TWY");

		// the O is in a SO4 ligand
		assertTrue(DistanceTablesBuilder.matches(result, Element.O, Element.N,
				2.0, 3.0));
	}

	@Test
	public void testWorksWithLigandlessMolecules() {
		// let's test this on DNA that doesn't have any ligands
		assertEquals(DistanceTablesBuilder.buildTableFor("1T2K").keySet()
				.size(), 0);
	}

	@Test
	public void testCrOnFullPDB() {
		List<String> expected = new ArrayList<>();
		expected.add("1HUZ");
		expected.add("2Z68");
		expected.add("1ZQE");
		expected.add("9ICC");
		expected.add("1LM2");
		expected.add("1SM8");
		expected.add("1J3F");
		expected.add("1HUO");
		expected.add("2A01");

		Collections.sort(expected);

		List<String> crN = DistanceTablesBuilder.getMatchesInRange(
				DistanceTableSerializer.deserialize(Element.Cr, Element.N),
				1.7, 8);

		Collections.sort(crN);

		assertEquals(expected, crN);
	}

	@Test
	public void testSpeedOnFullPDB() {
		// we really need this to work within under a second -- Na-N is probably
		// one of the queries that will get the most hits.
		long start = System.currentTimeMillis();
		DistanceTablesBuilder.getMatchesInRange(
				DistanceTableSerializer.deserialize(Element.Na, Element.N),
				1.7, 8);
		long stop = System.currentTimeMillis();

		assertTrue(stop - start < 1000);
	}

	@Test
	public void test1J3F() {
		Map<String, Long> map = DistanceTablesBuilder.buildTableFor("1J3F");

		// each of these are hits
		double[] hits = { 1.8, 6.8, 7.7 };
		for (double hit : hits) {
			assertTrue(DistanceTablesBuilder.matches(map, Element.Cr,
					Element.O, hit, hit));
		}

		// none of these are hits
		double[] misses = { 1.7, 3.0, 3.5, 4.0, 4.5, 5.0 };
		for (double miss : misses) {
			assertFalse(DistanceTablesBuilder.matches(map, Element.Cr,
					Element.O, miss, miss));
		}
	}

	@Test
	public void testTable() {
		List<String> ids = new ArrayList<>();
		ids.add("4ACL");
		ids.add("3QL0");
		ids.add("3QL1");

		List<String> results1 = new ArrayList<>();
		results1.add("4ACL");
		results1.add("3QL0");

		Map<String, Map<String, Long>> table = DistanceTablesBuilder
				.buildTable(ids);

		assertEquals(results1, DistanceTablesBuilder.getMatchesInRange(table,
				Element.Na, Element.N, 1.7, 8.0));

		List<String> results2 = new ArrayList<>();
		results2.add("3QL0");

		assertEquals(results2, DistanceTablesBuilder.getMatchesInRange(table,
				Element.Na, Element.N, 1.7, 4.6));
	}
}
