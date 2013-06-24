package com.ulyssecarion.pdbdistances;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DistanceResult implements Serializable {
	private static final long serialVersionUID = -6155530470021111481L;
	
	private long bitmap;
	private List<MatchDatum> matchData;

	public DistanceResult(MatchDatum matchDatum) {
		matchData = new ArrayList<>();
		matchData.add(matchDatum);
		bitmap = 1L << matchDatum.getLocation();
	}

	public void add(MatchDatum matchDatum) {
		matchData.add(matchDatum);

		bitmap |= 1L << matchDatum.getLocation();
	}
	
	public List<MatchDatum> getMatchData() {
		return matchData;
	}
	
	public boolean hasPotentialMatch(DistanceQuery query) {
		return (bitmap & query.getBitmap()) != 0L;
	}

	@Override
	public String toString() {
		String result = "DistanceResult\n";
		for (MatchDatum matchDatum : matchData)
			result += "\t" + matchDatum + "\n";
		return result;
	}
}
