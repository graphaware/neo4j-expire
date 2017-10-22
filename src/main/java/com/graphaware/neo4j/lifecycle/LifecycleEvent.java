package com.graphaware.neo4j.lifecycle;

import java.util.Arrays;
import java.util.List;

public enum LifecycleEvent {
	EXPIRY,
	REVIVAL;

	public static List<LifecycleEvent> list() {
		return Arrays.asList(values());
	}
}

