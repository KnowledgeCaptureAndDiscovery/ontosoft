package org.ontosoft.shared.classes;

import org.ontosoft.shared.classes.entities.SoftwareVersion;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SoftwareVersionSummary extends SoftwareSummary {

	SoftwareSummary softwareSummary;

	public SoftwareVersionSummary() {
		super();
	}
	
	public SoftwareVersionSummary(SoftwareVersion sw) {
		super(sw);
	}

	public SoftwareSummary getSoftwareSummary() {
		return softwareSummary;
	}

	public void setSoftwareSummary(SoftwareSummary softwareSummary) {
		this.softwareSummary = softwareSummary;
	}
}
