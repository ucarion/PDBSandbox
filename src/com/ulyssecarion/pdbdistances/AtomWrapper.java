package com.ulyssecarion.pdbdistances;

import java.util.ArrayList;
import java.util.List;

public class AtomWrapper {
	public static class TargetAtom {
		private String name;
		private String groupName;
		
		public TargetAtom(String name, String groupName) {
			this.name = name;
			this.groupName = groupName;
		}
		
		public String toString() {
			return groupName + ":" + name;
		}
		
		public boolean isGroupedWith(TargetAtom t) {
			return groupName.equals(t.groupName);
		}
	}
	
	public static class LigandAtom {
		private String name;
		private String groupName;
		private List<TargetAtom> links;
		
		public LigandAtom(String name, String groupName) {
			this.name = name;
			this.groupName = groupName;
			
			links = new ArrayList<>();
		}
		
		public boolean isLinkedTo(TargetAtom t) {
			return links.contains(t);
		}
		
		public boolean isGroupedWith(LigandAtom l) {
			return groupName.equals(l.groupName);
		}
		
		public List<TargetAtom> getLinks() {
			return links;
		}
		
		public void addLink(TargetAtom t) {
			links.add(t);
		}
		
		public boolean equals(Object other) {
			LigandAtom l = (LigandAtom) other;
			return name.equals(l.name) && groupName.equals(l.groupName);
		}
		
		public String toString() {
			String s = groupName + ":" + name + "->\n";
			for (TargetAtom t : links)
				s += t + "\n";
			return s;
		}
	}
	
	public static List<LigandAtom> getLigands(DistanceResult dr) {
		List<LigandAtom> ligands = new ArrayList<>();
		
		for (MatchDatum md : dr.getMatchData()) {
			LigandAtom l = new LigandAtom(md.getLigandName(), md.getLigandGroupName());
			TargetAtom t = new TargetAtom(md.getTargetName(), md.getTargetGroupName());
			
			if (!ligands.contains(l)) {
				ligands.add(l);
			}
			
			ligands.get(ligands.size() - 1).addLink(t);
		}
		
		return ligands;
	}
}
