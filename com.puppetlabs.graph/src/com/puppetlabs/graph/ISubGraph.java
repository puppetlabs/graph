/**
 * Copyright (c) 2013 Puppet Labs, Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Puppet Labs
 */
package com.puppetlabs.graph;

/**
 * Marker interface for a graph having a non root role.
 * 
 */
public interface ISubGraph extends IGraph {

	/**
	 * Create a new ClusterGraph with the same content as this subgraph graph plus the given label.
	 * 
	 * @return an IClusterGraph being a copy of this sub graph's content.
	 */
	public abstract IClusterGraph asClusterGraph(String label);

	/**
	 * Create a new RootGraph with the same content as this subgraph graph plus the given label.
	 * 
	 * @return an IRootGraph being a copy of this sub graph's content.
	 */
	public abstract IRootGraph asRootGraph(String label);

}
