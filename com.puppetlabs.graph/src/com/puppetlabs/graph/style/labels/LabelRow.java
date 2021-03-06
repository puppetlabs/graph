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
package com.puppetlabs.graph.style.labels;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.Function;
import com.puppetlabs.graph.IGraphElement;

/**
 * Data for label format
 * 
 */
public class LabelRow {
	public static class Separator extends LabelRow {
		public Separator(Function<IGraphElement, Set<String>> styleClass) {
			super(styleClass);
		}

		@Override
		public boolean isSeparator() {
			return true;
		}
	}

	LabelCell[] cells;

	Function<IGraphElement, Set<String>> styleClass;

	public LabelRow(final Function<IGraphElement, Set<String>> styleClass, LabelCell... cells) {
		this.styleClass = styleClass;
		this.cells = cells;
	}

	public LabelRow(LabelCell... cells) {
		this("", cells);
	}

	public LabelRow(final String styleClass, LabelCell... cells) {
		this.styleClass = new Function<IGraphElement, Set<String>>() {
			public Set<String> apply(IGraphElement ge) {
				return Collections.singleton(styleClass);
			}
		};
		this.cells = cells;
	}

	public LabelCell[] getCells() {
		return cells;
	}

	public Set<String> getStyleClasses(IGraphElement ge) {
		return styleClass.apply(ge);
	}

	public boolean isSeparator() {
		return false;
	}
}
