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

package com.graphaware.neo4j.expire.strategy;

import java.util.Map;

import org.neo4j.graphdb.PropertyContainer;

/**
 * A strategy for expiring {@link PropertyContainer}s.
 *
 * @param <P> type of container this strategy is for.
 */
public abstract class ExpirationStrategy<P extends PropertyContainer> {

	private Map<String, String> config;

	public ExpirationStrategy(Map<String, String> config) {
		this.setConfig(config);
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	/**
	 * Evaluate necessity of, and execute expiry of a container.
	 *
	 * @param pc to expire.
	 */
	public abstract boolean expireIfNeeded(P pc);

	/**
	 * Determines if the expired PropertyContainer is removed from the index on expiry.
	 *
	 * @return
	 */
	public boolean removesFromIndex() {
		return true;
	}
}
