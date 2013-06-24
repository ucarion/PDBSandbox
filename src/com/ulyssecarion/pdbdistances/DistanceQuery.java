package com.ulyssecarion.pdbdistances;

import org.biojava.bio.structure.Element;

public class DistanceQuery {
	public static final String WILDCARD = null;
	
	private Element ligandElement;
	private String ligandName;
	private String ligandGroupName;

	private Element targetElement;
	private String targetName;
	private String targetGroupName;

	private int minDistance;
	private int maxDistance;
	
	public DistanceQuery(Element ligandElement, String ligandName, String ligandGroupName,
			Element targetElement, String targetName, String targetGroupName, int minDistance,
			int maxDistance) {
		this.ligandElement = ligandElement;
		this.ligandName = ligandName;
		this.ligandGroupName = ligandGroupName;
		this.targetElement = targetElement;
		this.targetName = targetName;
		this.targetGroupName = targetGroupName;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
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

	public Element getLigandElement() {
		return ligandElement;
	}

	public Element getTargetElement() {
		return targetElement;
	}

	public String getTargetGroupName() {
		return targetGroupName;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}
	
	public long getBitmap() {
		long bitmap = 0L;
		
		for (int i = minDistance; i <= maxDistance; i++) {
			bitmap |= 1L << i;
		}
		
		return bitmap;
	}
	
	public String toKey() {
		return ligandElement + "" + targetElement;
	}
}
