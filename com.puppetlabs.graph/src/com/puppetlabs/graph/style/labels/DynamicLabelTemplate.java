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

import com.google.common.base.Function;
import com.puppetlabs.graph.IGraphElement;

/**
 * A dynamic/indirect way to obtain a label template. Useful when not knowing until data is seen if it
 * should be represented as a simple string or a table.
 * 
 */
public class DynamicLabelTemplate implements ILabelTemplate {
	private Function<IGraphElement, ILabelTemplate> templateFunc;

	public DynamicLabelTemplate(Function<IGraphElement, ILabelTemplate> templateString) {
		this.templateFunc = templateString;
	}

	public ILabelTemplate getTemplate(IGraphElement ge) {
		return templateFunc.apply(ge);
	}
}
