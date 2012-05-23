/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class NonModalDialog extends WDialog {
	private static Logger logger = LoggerFactory
			.getLogger(NonModalDialog.class);

	public NonModalDialog(CharSequence title, EventDisplayer ed) {
		super(title);
		this.setModal(false);
		this.setClosable(true);
		new WText(
				"You can freely format the contents of a WDialog by adding any widget you want to it.<br/>Here, we added WText, WLineEdit and WPushButton to a dialog",
				this.getContents());
		new WBreak(this.getContents());
		new WText("Enter your name: ", this.getContents());
		this.edit_ = new WLineEdit(this.getContents());
		new WBreak(this.getContents());
		this.ok_ = new WPushButton("Ok", this.getContents());
		this.edit_.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				NonModalDialog.this.welcome();
			}
		});
		this.ok_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						NonModalDialog.this.welcome();
					}
				});
		this.ed_ = ed;
	}

	private WLineEdit edit_;
	private WPushButton ok_;
	private EventDisplayer ed_;

	private void welcome() {
		this.ed_.setStatus("Welcome, " + this.edit_.getText());
		this.setHidden(true);
		if (this != null)
			this.remove();
	}
}
