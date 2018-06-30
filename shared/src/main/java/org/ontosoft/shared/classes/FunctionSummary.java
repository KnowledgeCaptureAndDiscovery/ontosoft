package org.ontosoft.shared.classes;

import org.ontosoft.shared.classes.entities.SoftwareVersion;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FunctionSummary extends SoftwareVersionSummary {

	SoftwareVersionSummary softwareVersionSummary;

	public FunctionSummary() {
		super();
	}
	
	public FunctionSummary(SoftwareVersion sw) {
		super(sw);
	}

	public SoftwareVersionSummary getSoftwareVersionSummary() {
		return softwareVersionSummary;
	}

	public void setSoftwareVersionSummary(SoftwareVersionSummary softwareSummary) {
		this.softwareVersionSummary = softwareSummary;
	}
}
