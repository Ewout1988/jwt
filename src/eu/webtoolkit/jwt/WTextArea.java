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
 * A widget that provides a multi-line edit.
 * <p>
 * 
 * To act upon text changes, connect a slot to the {@link WFormWidget#changed()
 * WFormWidget#changed()} signal. This signal is emitted when the user changed
 * the content, and subsequently removes the focus from the line edit.
 * <p>
 * To act upon editing, connect a slot to the
 * {@link WInteractWidget#keyWentUp() WInteractWidget#keyWentUp()} signal.
 * <p>
 * At all times, the current content may be accessed with the
 * {@link WTextArea#getText() getText()} method.
 * <p>
 * WTextArea is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to an HTML <code>&lt;textarea&gt;</code> tag can be
 * styled using inline or external CSS as appropriate. The emptyText style can
 * be configured via .Wt-edit-emptyText.
 * <p>
 * 
 * @see WLineEdit
 */
public class WTextArea extends WFormWidget {
	private static Logger logger = LoggerFactory.getLogger(WTextArea.class);

	/**
	 * Creates a text area with empty content and optional parent.
	 */
	public WTextArea(WContainerWidget parent) {
		super(parent);
		this.content_ = "";
		this.cols_ = 20;
		this.rows_ = 5;
		this.contentChanged_ = false;
		this.attributesChanged_ = false;
		this.setInline(true);
		this.setFormObject(true);
	}

	/**
	 * Creates a text area with empty content and optional parent.
	 * <p>
	 * Calls {@link #WTextArea(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTextArea() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a text area with given content and optional parent.
	 */
	public WTextArea(final String text, WContainerWidget parent) {
		super(parent);
		this.content_ = text;
		this.cols_ = 20;
		this.rows_ = 5;
		this.contentChanged_ = false;
		this.attributesChanged_ = false;
		this.setInline(true);
		this.setFormObject(true);
	}

	/**
	 * Creates a text area with given content and optional parent.
	 * <p>
	 * Calls {@link #WTextArea(String text, WContainerWidget parent) this(text,
	 * (WContainerWidget)null)}
	 */
	public WTextArea(final String text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Sets the number of columns.
	 * <p>
	 * The default value is 20.
	 */
	public void setColumns(int columns) {
		this.cols_ = columns;
		this.attributesChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Sets the number of rows.
	 * <p>
	 * The default value is 5.
	 */
	public void setRows(int rows) {
		this.rows_ = rows;
		this.attributesChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Returns the number of columns.
	 * <p>
	 * 
	 * @see WTextArea#setColumns(int columns)
	 */
	public int getColumns() {
		return this.cols_;
	}

	/**
	 * Returns the number of rows.
	 * <p>
	 * 
	 * @see WTextArea#setRows(int rows)
	 */
	public int getRows() {
		return this.rows_;
	}

	/**
	 * Returns the current content.
	 */
	public String getText() {
		return this.content_;
	}

	/**
	 * Sets the content of the text area.
	 * <p>
	 * The default text is &quot;&quot;.
	 */
	public void setText(final String text) {
		this.content_ = text;
		this.contentChanged_ = true;
		this.repaint();
		this.validate();
		this.applyEmptyText();
	}

	/**
	 * Returns the current selection start.
	 * <p>
	 * Returns -1 if there is no selected text.
	 * <p>
	 * 
	 * @see WTextArea#hasSelectedText()
	 * @see WTextArea#getSelectedText()
	 */
	public int getSelectionStart() {
		WApplication app = WApplication.getInstance();
		if (app.getFocus().equals(this.getId())) {
			if (app.getSelectionStart() != -1
					&& app.getSelectionEnd() != app.getSelectionStart()) {
				return app.getSelectionStart();
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Returns the currently selected text.
	 * <p>
	 * Returns an empty string if there is currently no selected text.
	 * <p>
	 * 
	 * @see WTextArea#hasSelectedText()
	 */
	public String getSelectedText() {
		if (this.getSelectionStart() != -1) {
			WApplication app = WApplication.getInstance();
			return this.getText().substring(app.getSelectionStart(),
					app.getSelectionEnd() - app.getSelectionStart());
		} else {
			return WString.Empty.toString();
		}
	}

	/**
	 * Returns whether there is selected text.
	 * <p>
	 */
	public boolean hasSelectedText() {
		return this.getSelectionStart() != -1;
	}

	/**
	 * Returns the current cursor position.
	 * <p>
	 * Returns -1 if the widget does not have the focus.
	 */
	public int getCursorPosition() {
		WApplication app = WApplication.getInstance();
		if (app.getFocus().equals(this.getId())) {
			return app.getSelectionEnd();
		} else {
			return -1;
		}
	}

	/**
	 * Returns the current value.
	 * <p>
	 * Returns {@link WTextArea#getText() getText()}.
	 */
	public String getValueText() {
		return this.getText();
	}

	/**
	 * Sets the current value.
	 * <p>
	 * Calls {@link WTextArea#setText(String text) setText()}.
	 */
	public void setValueText(final String value) {
		this.setText(value);
	}

	/**
	 * Event signal emitted when the text in the input field changed.
	 * <p>
	 * This signal is emitted whenever the text contents has changed. Unlike the
	 * {@link WFormWidget#changed() WFormWidget#changed()} signal, the signal is
	 * fired on every change, not only when the focus is lost. Unlike the
	 * {@link WInteractWidget#keyPressed() WInteractWidget#keyPressed()} signal,
	 * this signal is fired also for other events that change the text, such as
	 * paste actions.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 * @see WFormWidget#changed()
	 */
	public EventSignal textInput() {
		return this.voidEventSignal(INPUT_SIGNAL, true);
	}

	private static String INPUT_SIGNAL = "input";
	private String content_;
	private int cols_;
	private int rows_;
	private boolean contentChanged_;
	private boolean attributesChanged_;

	void updateDom(final DomElement element, boolean all) {
		if (element.getType() == DomElementType.DomElement_TEXTAREA) {
			if (this.contentChanged_ || all) {
				element.setProperty(Property.PropertyValue, this.content_);
				this.contentChanged_ = false;
			}
		}
		if (this.attributesChanged_ || all) {
			element.setAttribute("cols", String.valueOf(this.cols_));
			element.setAttribute("rows", String.valueOf(this.rows_));
			this.attributesChanged_ = false;
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_TEXTAREA;
	}

	void propagateRenderOk(boolean deep) {
		this.attributesChanged_ = false;
		this.contentChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected void setFormData(final WObject.FormData formData) {
		if (this.contentChanged_ || this.isReadOnly()) {
			return;
		}
		if (!(formData.values.length == 0)) {
			String value = formData.values[0];
			StringUtils.replace(value, '\r', "");
			this.content_ = value;
		}
	}

	protected int boxPadding(Orientation orientation) {
		final WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.agentIsIE() || env.agentIsOpera()) {
			return 1;
		} else {
			if (env.agentIsChrome()) {
				return 2;
			} else {
				if (env.getUserAgent().indexOf("Mac OS X") != -1) {
					return 0;
				} else {
					if (env.getUserAgent().indexOf("Windows") != -1) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		}
	}

	protected int boxBorder(Orientation orientation) {
		final WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.agentIsIE() || env.agentIsOpera()) {
			return 2;
		} else {
			if (env.agentIsChrome()) {
				return 1;
			} else {
				if (env.getUserAgent().indexOf("Mac OS X") != -1) {
					return 1;
				} else {
					if (env.getUserAgent().indexOf("Windows") != -1) {
						return 2;
					} else {
						return 2;
					}
				}
			}
		}
	}

	void resetContentChanged() {
		this.contentChanged_ = false;
	}
}
