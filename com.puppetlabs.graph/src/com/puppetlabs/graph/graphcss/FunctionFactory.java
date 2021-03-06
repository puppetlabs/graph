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
package com.puppetlabs.graph.graphcss;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.puppetlabs.graph.IGraphElement;
import com.puppetlabs.graph.ILabeledGraphElement;
import com.puppetlabs.graph.style.labels.ILabelTemplate;
import com.puppetlabs.graph.style.labels.LabelStringTemplate;
import com.puppetlabs.graph.style.labels.LabelTable;
import com.puppetlabs.graph.utils.Base64;

/**
 * A FunctionFactory producing values that are dynamically produced when applying a style to
 * a graph element.
 * 
 */
@Singleton
public class FunctionFactory implements IFunctionFactory {

	private static class EmptyLabel implements Function<IGraphElement, Boolean> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public Boolean apply(IGraphElement ge) {
			if(!(ge instanceof ILabeledGraphElement))
				return false;
			String item = ((ILabeledGraphElement) ge).getLabel();
			return item == null || item.length() == 0;
		}

	}

	private static class EmptyLabelData implements Function<IGraphElement, Boolean> {

		private Object key;

		public EmptyLabelData(Object key) {
			this.key = key;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public Boolean apply(IGraphElement ge) {
			if(!(ge instanceof ILabeledGraphElement))
				return false;
			String item = ((ILabeledGraphElement) ge).getUserData().get(key);
			return item == null || item.length() == 0;
		}

	}

	/**
	 * A Function that translates a IGraphElement into a base64 encoded string containing
	 * id="ID" class="CLASS" where:
	 * <ul>
	 * <li>ID is the user data {@link #ID_KEY} value from the element, or if missing the fully qualified id of the given element.</li>
	 * <li>CLASS</li> is the type of element and the style class of the given element</li>
	 * </ul>
	 * The intention is to combine the user of this function to set the id style of
	 * an element and use a SVG post processor that replaces the generated id="..." class="..."
	 * sequence with that encoded in what this function produces.
	 * 
	 * @author henrik
	 * 
	 */
	public static class IdClassReplacerFunction implements Function<IGraphElement, String> {

		@Override
		public String apply(IGraphElement from) {

			String idString = from.getUserData(ID_KEY);
			if(idString == null)
				idString = computeID(from);
			String allStyleClasses = from.getAllStyleClasses();
			StringBuilder builder = new StringBuilder(idString.length() + allStyleClasses.length() + 20);
			builder.append("id=\"");
			builder.append(idString);
			builder.append("\" class=\"");
			builder.append(from.getElementType()); // e.g. "vertex", "edge", etc.
			builder.append(" ");
			builder.append(allStyleClasses);
			builder.append("\"");
			try {
				return "base64:" + Base64.byteArrayToBase64(builder.toString().getBytes("UTF8"));
			}
			catch(UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}

		private String computeID(IGraphElement element) {
			IGraphElement[] parents = Iterators.toArray(element.getContext(), IGraphElement.class);
			int plength = (parents == null)
					? 0
					: parents.length;
			StringBuilder buf = new StringBuilder(10 + 5 * plength);
			// add each parents id separated by - start with root (last in array)
			if(parents != null)
				for(int i = parents.length - 1; i >= 0; i--)
					buf.append((i == (plength - 1)
							? ""
							: "-") + parents[i].getId());
			buf.append("-");
			buf.append(element.getId());
			return buf.toString();
		}
	}

	private static class Label implements Function<IGraphElement, ILabelTemplate> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public ILabelTemplate apply(IGraphElement ge) {
			String text = "";
			if(ge instanceof ILabeledGraphElement)
				text = ((ILabeledGraphElement) ge).getLabel();
			return new LabelStringTemplate(text);
		}

	}

	private static class LabelData implements Function<IGraphElement, String> {

		private Object key;

		public LabelData(Object key) {
			this.key = key;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public String apply(IGraphElement ge) {
			String text = "";
			if(ge instanceof ILabeledGraphElement)
				text = ((ILabeledGraphElement) ge).getUserData().get(key);
			return text;
		}
	}

	private static class LiteralLabelTemplate implements Function<IGraphElement, ILabelTemplate> {
		final private ILabelTemplate value;

		public LiteralLabelTemplate(LabelTable value) {
			this.value = value;
		}

		public LiteralLabelTemplate(String value) {
			this.value = new LabelStringTemplate(value);
		}

		@Override
		public ILabelTemplate apply(IGraphElement from) {
			return value;
		}
	}

	private static class LiteralString implements Function<IGraphElement, String> {

		private String value;

		public LiteralString(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public String apply(IGraphElement ge) {
			return value;
		}
	}

	private static class LiteralStringSet implements Function<IGraphElement, Set<String>> {

		private Set<String> value;

		public LiteralStringSet(Set<String> value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public Set<String> apply(IGraphElement ge) {
			return value;
		}
	}

	public static class Not implements Function<IGraphElement, Boolean> {

		Function<IGraphElement, Boolean> function;

		public Not(Function<IGraphElement, Boolean> function) {
			this.function = function;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.common.base.Function#apply(java.lang.Object)
		 */
		@Override
		public Boolean apply(IGraphElement ge) {
			return !function.apply(ge);
		}

	}

	private static final EmptyLabel theEmptyLabelFunction = new EmptyLabel();

	private static final Not theNotEmptyLabelFunction = new Not(theEmptyLabelFunction);

	private static final Label theLabelFunction = new Label();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#emptyLabel()
	 */
	@Override
	public Function<IGraphElement, Boolean> emptyLabel() {
		return theEmptyLabelFunction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#emptyLabelData(java.lang.Object)
	 */
	@Override
	public Function<IGraphElement, Boolean> emptyLabelData(Object key) {
		return new EmptyLabelData(key);
	}

	@Override
	public Function<IGraphElement, String> idClassReplacer() {
		return new IdClassReplacerFunction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#label()
	 */
	@Override
	public Function<IGraphElement, ILabelTemplate> label() {
		return theLabelFunction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#labelData(java.lang.Object)
	 */
	@Override
	public Function<IGraphElement, String> labelData(Object key) {
		return new LabelData(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#labelTemplate(com.google.common.base.Function)
	 */
	@Override
	public Function<IGraphElement, ILabelTemplate> labelTemplate(final Function<IGraphElement, String> stringFunc) {
		return new Function<IGraphElement, ILabelTemplate>() {
			@Override
			public ILabelTemplate apply(IGraphElement from) {
				return new LiteralLabelTemplate(stringFunc.apply(from)).apply(from);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#tableStringTemplate(com.puppetlabs.graph.style.labels.LabelTable)
	 */
	@Override
	public Function<IGraphElement, ILabelTemplate> literalLabelTemplate(LabelTable t) {
		return new LiteralLabelTemplate(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#literalStringTemplate(java.lang.String)
	 */
	@Override
	public Function<IGraphElement, ILabelTemplate> literalLabelTemplate(String s) {
		return new LiteralLabelTemplate(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#literalString(java.lang.String)
	 */
	@Override
	public Function<IGraphElement, String> literalString(String s) {
		return new LiteralString(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#literalStringSet(java.util.Collection)
	 */
	@Override
	public Function<IGraphElement, Set<String>> literalStringSet(Collection<String> s) {
		return new LiteralStringSet(Sets.newHashSet(s));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#literalStringSet(java.lang.String)
	 */
	@Override
	public Function<IGraphElement, Set<String>> literalStringSet(String s) {
		return new LiteralStringSet(Collections.singleton(s));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#notEmptyLabel()
	 */
	@Override
	public Function<IGraphElement, Boolean> notEmptyLabel() {
		return theNotEmptyLabelFunction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.puppetlabs.graph.graphcss.IFunctionFactory#notEmptyLabelData(java.lang.Object)
	 */
	@Override
	public Function<IGraphElement, Boolean> notEmptyLabelData(Object key) {
		return new Not(emptyLabelData(key));
	}
}
