/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A user control that represents a check box.
 * <p>
 * 
 * By default, a checkbox can have two states: {@link CheckState#Checked} or
 * {@link CheckState#Unchecked}, which can be inspected using
 * {@link WAbstractToggleButton#isChecked() WAbstractToggleButton#isChecked()},
 * and set using {@link WAbstractToggleButton#setChecked()
 * WAbstractToggleButton#setChecked()}.
 * <p>
 * A checkbox may also provide a third state,
 * {@link CheckState#PartiallyChecked}, which is useful to indicate that it is
 * neither checked or unchecked. JWt will use native browser support for this
 * HTML5 extension when available (Safari and MS IE), and use an image-based
 * workaround otherwise. You may enable support for the third state using
 * {@link WCheckBox#setTristate(boolean tristate) setTristate()}, and use
 * {@link WCheckBox#setCheckState(CheckState state) setCheckState()} and
 * {@link WCheckBox#getCheckState() getCheckState()} to read all three states.
 * <p>
 * A label is added as a sibling of the checkbox to the same parent.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WGroupBox box = new WGroupBox(&quot;In-flight options&quot;);
 * 
 * 	WCheckBox w1 = new WCheckBox(&quot;Vegetarian diet&quot;, box);
 * 	box.addWidget(new WBreak());
 * 	WCheckBox w2 = new WCheckBox(&quot;WIFI access&quot;, box);
 * 	box.addWidget(new WBreak());
 * 	WCheckBox w3 = new WCheckBox(&quot;AC plug&quot;, box);
 * 
 * 	w1.setChecked(false);
 * 	w2.setChecked(true);
 * 	w3.setChecked(true);
 * }
 * </pre>
 * <p>
 * WCheckBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget corresponds to the HTML
 * <code>&lt;input type=&quot;checkbox&quot;&gt;</code> tag. Depending on
 * whether a text is included, it may be nested in a <code>&lt;span&gt;</code>
 * tag which also includes a rendered {@link WLabel}. This widget does not
 * provide styling, and can be styled using inline or external CSS as
 * appropriate.
 * <p>
 * 
 * @see WAbstractToggleButton
 */
public class WCheckBox extends WAbstractToggleButton {
	private static Logger logger = LoggerFactory.getLogger(WCheckBox.class);

	/**
	 * Creates a checkbox with empty label.
	 */
	public WCheckBox(WContainerWidget parent) {
		super(parent);
		this.triState_ = false;
		this.safariWorkaround_ = false;
		this.setFormObject(true);
	}

	/**
	 * Creates a checkbox with empty label.
	 * <p>
	 * Calls {@link #WCheckBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCheckBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a checkbox with given label.
	 */
	public WCheckBox(CharSequence text, WContainerWidget parent) {
		super(text, parent);
		this.triState_ = false;
		this.safariWorkaround_ = false;
		this.setFormObject(true);
	}

	/**
	 * Creates a checkbox with given label.
	 * <p>
	 * Calls {@link #WCheckBox(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WCheckBox(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Makes a tristate checkbox.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>You should enable tristate functionality right after
	 * construction and this cannot be modified later. </i>
	 * </p>
	 */
	public void setTristate(boolean tristate) {
		this.triState_ = tristate;
		if (this.triState_) {
			if (!this.supportsIndeterminate(WApplication.getInstance()
					.getEnvironment())) {
				this.changed().addListener(clearOpacityJS);
			} else {
				if (WApplication.getInstance().getEnvironment().agentIsSafari()
						&& !this.safariWorkaround_) {
					this.clicked().addListener(safariWorkaroundJS);
					this.safariWorkaround_ = true;
				}
			}
		}
	}

	/**
	 * Makes a tristate checkbox.
	 * <p>
	 * Calls {@link #setTristate(boolean tristate) setTristate(true)}
	 */
	public final void setTristate() {
		setTristate(true);
	}

	/**
	 * Returns whether the checkbox is tristate.
	 * <p>
	 * 
	 * @see WCheckBox#setTristate(boolean tristate)
	 */
	public boolean isTristate() {
		return this.triState_;
	}

	/**
	 * Sets the check state.
	 * <p>
	 * Unless it is a tri-state checkbox, only {@link CheckState#Checked} and
	 * {@link CheckState#Unchecked} are valid states.
	 */
	public void setCheckState(CheckState state) {
		super.setCheckState(state);
	}

	/**
	 * Returns the check state.
	 * <p>
	 * 
	 * @see WCheckBox#setCheckState(CheckState state)
	 * @see WAbstractToggleButton#isChecked()
	 */
	public CheckState getCheckState() {
		return this.state_;
	}

	void updateInput(DomElement input, boolean all) {
		if (all) {
			input.setAttribute("type", "checkbox");
		}
	}

	private boolean triState_;
	private boolean safariWorkaround_;
	static JSlot safariWorkaroundJS = new JSlot(
			"function(obj, e) { obj.onchange(); }");
	static JSlot clearOpacityJS = new JSlot(
			"function(obj, e) { obj.style.opacity=''; }");
}
