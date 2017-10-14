/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.neo4j.lifecycle.expire.strategy;

import java.util.*;
import java.util.stream.Collectors;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;


public final class AddRemoveLabels extends ExpirationStrategy<Node> {

	private List<String> labelsToAdd;
	private List<String> labelsToRemove;

	static {
		Serializer.register(AddRemoveLabels.class, new SingletonSerializer());
	}

	private static final AddRemoveLabels INSTANCE = new AddRemoveLabels();

	public static AddRemoveLabels getInstance() {
		return INSTANCE;
	}

	private AddRemoveLabels() { }

	public List<String> getLabelsToAdd() {
		return labelsToAdd;
	}

	public List<String> getLabelsToRemove() {
		return labelsToRemove;
	}

	@Override
	public void setConfig(Map<String, String> config) {
		super.setConfig(config);

		String labelsToAdd = config.get("nodeExpirationStrategy.labelsToAdd");
		if (labelsToAdd != null) {
			labelsToAdd = labelsToAdd.replaceAll("^\\[|]$", ""); //Replace leading and trailing brackets, if exist
			this.labelsToAdd = Arrays.stream(labelsToAdd.split(","))
					.map(String::trim).collect(Collectors.toList());
		}
		else {
			this.labelsToAdd = Collections.emptyList();
		}

		String labelsToRemove = config.get("nodeExpirationStrategy.labelsToRemove");
		if (labelsToRemove != null) {
			labelsToRemove = labelsToRemove.replaceAll("^\\[|]$", "");
			this.labelsToRemove = Arrays.stream(labelsToRemove.split(","))
					.map(String::trim).collect(Collectors.toList());
		}
		else {
			this.labelsToRemove = Collections.emptyList();
		}

	}

	@Override
	public boolean expireIfNeeded(Node node) {
		for (String label : this.labelsToRemove) {
			node.removeLabel(Label.label(label));
		}
		for (String label : this.labelsToAdd) {
			node.addLabel(Label.label(label));
		}

		return true;
	}
}
