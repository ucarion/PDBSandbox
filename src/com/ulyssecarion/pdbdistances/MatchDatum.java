package com.ulyssecarion.pdbdistances;

import java.io.Serializable;

import org.biojava.bio.structure.Atom;

public class MatchDatum implements Serializable {
	private static final long serialVersionUID = 8613395175599083493L;
	
	private String ligandName;
	private String ligandGroupName;

	private String targetName;
	private String targetGroupName;

	private int location;

	public MatchDatum(Atom ligand, Atom target, int location) {
		ligandName = ligand.getName();
		ligandGroupName = ligand.getGroup().getPDBName();

		targetName = target.getName();
		targetGroupName = target.getGroup().getPDBName();

		this.location = location;
	}

	public int getLocation() {
		return location;
	}

	public boolean matches(DistanceQuery query) {
		return matchWithWildcard(ligandName, query.getLigandName())
				&& matchWithWildcard(ligandGroupName,
						query.getLigandGroupName())
				&& matchWithWildcard(targetName, query.getTargetName())
				&& matchWithWildcard(targetGroupName,
						query.getTargetGroupName())
				&& location >= query.getMinDistance()
				&& location <= query.getMaxDistance();
	}

	private static boolean matchWithWildcard(String myProperty,
			String queryProperty) {
		// WILDCARD is null; only == works correctly here
		if (queryProperty == DistanceQuery.WILDCARD)
			return true;
		return myProperty.equals(queryProperty);
	}

	@Override
	public String toString() {
		return ligandGroupName + ":" + ligandName + " -> " + targetGroupName
				+ ":" + targetName + " @ " + location;
	}

	public String getLigandName() {
		return ligandName;
	}

	public String getLigandGroupName() {
		return ligandGroupName;
	}

	public String getTargetName() {
		return targetName;
	}

	public String getTargetGroupName() {
		return targetGroupName;
	}
}