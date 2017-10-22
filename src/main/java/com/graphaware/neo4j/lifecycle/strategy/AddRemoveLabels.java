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

package com.graphaware.neo4j.lifecycle.strategy;

import static com.graphaware.neo4j.lifecycle.LifecycleEvent.*;

import java.util.*;
import java.util.stream.Collectors;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.neo4j.lifecycle.LifecycleEvent;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;


public final class AddRemoveLabels extends LifecycleStrategy<Node> {

	private EnumMap<LifecycleEvent, List<String>> labelsToAdd = new EnumMap<>(LifecycleEvent.class);
	private EnumMap<LifecycleEvent, List<String>> labelsToRemove = new EnumMap<>(LifecycleEvent.class);

	static {
		Serializer.register(AddRemoveLabels.class, new SingletonSerializer());
	}

	private static final AddRemoveLabels INSTANCE = new AddRemoveLabels();

	public static AddRemoveLabels getInstance() {
		return INSTANCE;
	}

	private AddRemoveLabels() {
	}

	public EnumMap<LifecycleEvent, List<String>> getLabelsToAdd() {
		return labelsToAdd;
	}

	public EnumMap<LifecycleEvent, List<String>> getLabelsToRemove() {
		return labelsToRemove;
	}

	@Override
	public void setConfig(Map<String, String> config) {
		super.setConfig(config);
		labelsToAdd.put(EXPIRY, toList(config, "nodeExpirationStrategy.labelsToAdd"));
		labelsToRemove.put(EXPIRY, toList(config, "nodeExpirationStrategy.labelsToRemove"));
		labelsToAdd.put(REVIVAL, toList(config, "nodeRevivalStrategy.labelsToAdd"));
		labelsToRemove.put(REVIVAL, toList(config, "nodeRevivalStrategy.labelsToRemove"));
	}

	@Override
	public boolean applyIfNeeded(Node node, LifecycleEvent event) {
		System.out.println("Event: " + event);
		for (String label : this.labelsToRemove.get(event)) {
			node.removeLabel(Label.label(label));
		}
		for (String label : this.labelsToAdd.get(event)) {
			node.addLabel(Label.label(label));
		}
		return true;
	}

	private List<String> toList(Map<String, String> config, String propertyName) {
		String labelsToRemove = config.get(propertyName);
		if (labelsToRemove != null) {
			labelsToRemove = labelsToRemove.replaceAll("^\\[|]$", "");
			return Arrays.stream(labelsToRemove.split(",")).map(String::trim).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
