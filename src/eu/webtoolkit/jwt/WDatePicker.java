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
 * A date picker.
 * <p>
 * 
 * A date picker shows a line edit and an icon which when clicked popups a
 * {@link WCalendar} for editing the date. Any date entered in the line edit is
 * reflected in the calendar, and vice-versa.
 * <p>
 * Each of these widgets may be accessed individually (
 * {@link WDatePicker#getLineEdit() getLineEdit()},
 * {@link WDatePicker#getCalendar() getCalendar()}, and
 * {@link WDatePicker#getDisplayWidget() getDisplayWidget()}) and there is a
 * constructor that allows you to specify an existing line edit and display
 * widget.
 * <p>
 * The date format used by default is <code>&quot;dd/MM/yyyy&quot;</code> and
 * can be changed using {@link WDatePicker#setFormat(String format) setFormat()}
 * . At any time, the date set may be read using {@link WDatePicker#getDate()
 * getDate()}, or can be changed using {@link WDatePicker#setDate(WDate date)
 * setDate()}.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * Internationalization of {@link WDatePicker} is mostly handled through the
 * internationalization mechanism of {@link eu.webtoolkit.jwt.WDate}. The
 * &apos;Close&apos; button can be internationalized by overriding the default
 * value for the localization key Wt.DatePicker.Close.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The date picker is styled by the current CSS theme. The look can be
 * overridden using the <code>Wt-datepicker</code> and <code>Wt-outset</code>
 * CSS class; the calendar itself can be styled as documented in
 * {@link WCalendar}.
 * <p>
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WDatePicker-default-1.png"
 * alt="Example of a WDatePicker (default theme)">
 * <p>
 * <strong>Example of a WDatePicker (default theme)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img src="doc-files//WDatePicker-polished-1.png"
 * alt="Example of a WDatePicker (polished theme)">
 * <p>
 * <strong>Example of a WDatePicker (polished theme)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 */
public class WDatePicker extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WDatePicker.class);

	/**
	 * Create a new date picker.
	 * <p>
	 * This constructor creates a line edit with an icon that leads to a popup
	 * calendar. A {@link WDateValidator} is configured for the line edit.
	 */
	public WDatePicker(WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.popupClosed_ = new Signal();
		this.changed_ = new Signal();
		this.positionJS_ = new JSlot();
		this.createDefault();
	}

	/**
	 * Create a new date picker.
	 * <p>
	 * Calls {@link #WDatePicker(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WDatePicker() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new date picker for existing line edit and with custom display
	 * widget.
	 * <p>
	 * The <code>displayWidget</code> is a button or image which much be clicked
	 * to open the date picker. This widget will become owned by the picker.
	 * <p>
	 * The <code>forEdit</code> argument is the lineEdit that works in
	 * conjunction with the date picker. This widget does not become part of the
	 * date picker, and may be located anywhere else.
	 */
	public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit,
			WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.popupClosed_ = new Signal();
		this.changed_ = new Signal();
		this.positionJS_ = new JSlot();
		this.create(displayWidget, forEdit);
	}

	/**
	 * Create a new date picker for existing line edit and with custom display
	 * widget.
	 * <p>
	 * Calls
	 * {@link #WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit, WContainerWidget parent)
	 * this(displayWidget, forEdit, (WContainerWidget)null)}
	 */
	public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit) {
		this(displayWidget, forEdit, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		WApplication.getInstance().doJavaScript(
				"Wt3_2_3.remove('" + this.popup_.getId() + "');");
		super.remove();
	}

	/**
	 * Sets the format used for parsing or writing the date in the line edit.
	 * <p>
	 * Sets the format used for representing the date in the line edit. If the
	 * line edit has a {@link WDateValidator} configured for it, then also there
	 * the format is updated.
	 * <p>
	 * The default format is <code>&apos;dd/MM/yyyy&apos;</code>.
	 * <p>
	 * 
	 * @see WDatePicker#getFormat()
	 * @see WDate#toString()
	 */
	public void setFormat(String format) {
		this.format_ = format;
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			dv.setFormat(format);
		}
	}

	/**
	 * Returns the format.
	 * <p>
	 * 
	 * @see WDatePicker#setFormat(String format)
	 */
	public String getFormat() {
		return this.format_;
	}

	/**
	 * The calendar widget.
	 * <p>
	 * Returns the calendar widget.
	 */
	public WCalendar getCalendar() {
		return this.calendar_;
	}

	/**
	 * The line edit.
	 * <p>
	 * Returns the line edit which works in conjunction with this date picker.
	 */
	public WLineEdit getLineEdit() {
		return this.forEdit_;
	}

	/**
	 * The display widget.
	 * <p>
	 * Returns the widget which is displayed to activate the calendar.
	 */
	public WInteractWidget getDisplayWidget() {
		return this.displayWidget_;
	}

	/**
	 * The current date.
	 * <p>
	 * Reads the current date from the {@link WDatePicker#getLineEdit()
	 * getLineEdit()}.
	 * <p>
	 * Returns <code>null</code> if the date could not be parsed using the
	 * current {@link WDatePicker#getFormat() getFormat()}. <br>
	 * <p>
	 * 
	 * @see WDatePicker#setDate(WDate date)
	 * @see WDate#fromString(String s)
	 * @see WLineEdit#getText()
	 */
	public WDate getDate() {
		return WDate.fromString(this.forEdit_.getText(), this.format_);
	}

	/**
	 * Sets the current date.
	 * <p>
	 * Does nothing if the current date is <code>Null</code>.
	 * <p>
	 * 
	 * @see WDatePicker#getDate()
	 */
	public void setDate(WDate date) {
		if (!(date == null)) {
			this.forEdit_.setText(date.toString(this.format_));
			this.calendar_.select(date);
			this.calendar_.browseTo(date);
		}
	}

	/**
	 * Sets whether the widget is enabled.
	 * <p>
	 * This is the oppositie of {@link WDatePicker#setDisabled(boolean disabled)
	 * setDisabled()}.
	 */
	public void setEnabled(boolean enabled) {
		this.setDisabled(!enabled);
	}

	public void setDisabled(boolean disabled) {
		this.forEdit_.setDisabled(disabled);
		this.displayWidget_.setHidden(disabled);
	}

	/**
	 * Hide/unhide the widget.
	 */
	public void setHidden(boolean hidden, WAnimation animation) {
		super.setHidden(hidden, animation);
		this.forEdit_.setHidden(hidden, animation);
		this.displayWidget_.setHidden(hidden, animation);
	}

	/**
	 * Sets the bottom of the valid date range.
	 */
	public void setBottom(WDate bottom) {
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			dv.setBottom(bottom);
			this.calendar_.setBottom(bottom);
		}
	}

	/**
	 * Returns the bottom date of the valid range.
	 */
	public WDate getBottom() {
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			return dv.getBottom();
		} else {
			return null;
		}
	}

	/**
	 * Sets the top of the valid date range.
	 */
	public void setTop(WDate top) {
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			dv.setTop(top);
			this.calendar_.setTop(top);
		}
	}

	/**
	 * Returns the top date of the valid range.
	 */
	public WDate getTop() {
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			return dv.getTop();
		} else {
			return null;
		}
	}

	/**
	 * Signal emitted when the value has changed.
	 * <p>
	 * This signal is emitted when a new date has been entered (either through
	 * the line edit, or through the calendar popup).
	 */
	public Signal changed() {
		return this.changed_;
	}

	/**
	 * Controls how the calendar popup is positioned.
	 * <p>
	 * When <code>global</code> is <code>true</code>, then the popup will
	 * position itself globally. This avoids that the popup is affected by
	 * enclosing parents with overflow settings that clip the popup. This makes
	 * the popup however no longer follow the popup button when this button
	 * moves.
	 * <p>
	 * The default is <code>false</code>.
	 */
	public void setGlobalPopup(boolean global) {
		this.positionJS_.setJavaScript("function() { Wt3_2_3.getElement('"
				+ this.popup_.getId()
				+ "').style.display = '';Wt3_2_3.positionAtWidget('"
				+ this.popup_.getId() + "','" + this.displayWidget_.getId()
				+ "', Wt3_2_3.Horizontal, " + (global ? "true" : "false")
				+ ");}");
	}

	/**
	 * Shows or hides the popup.
	 */
	public void setPopupVisible(boolean visible) {
		this.popup_.setHidden(!visible);
	}

	/**
	 * A signal which indicates that the popup has been closed.
	 * <p>
	 * The signal is only fired when the popup has been closed by the user.
	 */
	public Signal popupClosed() {
		return this.popupClosed_;
	}

	private String format_;
	private WInteractWidget displayWidget_;
	private WLineEdit forEdit_;
	private WContainerWidget layout_;
	private WTemplate popup_;
	private WCalendar calendar_;
	private Signal popupClosed_;
	private Signal changed_;
	private JSlot positionJS_;

	private void createDefault() {
		WImage icon = new WImage(WApplication.getResourcesUrl()
				+ "calendar_edit.png");
		icon.resize(new WLength(16), new WLength(16));
		icon.setVerticalAlignment(AlignmentFlag.AlignMiddle);
		WLineEdit lineEdit = new WLineEdit();
		this.create(icon, lineEdit);
		this.layout_.insertWidget(0, lineEdit);
		lineEdit.setValidator(new WDateValidator(this.format_, this));
	}

	private void create(WInteractWidget displayWidget, WLineEdit forEdit) {
		this.setImplementation(this.layout_ = new WContainerWidget());
		this.displayWidget_ = displayWidget;
		this.forEdit_ = forEdit;
		this.forEdit_.setVerticalAlignment(AlignmentFlag.AlignMiddle);
		this.forEdit_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WDatePicker.this.setFromLineEdit();
			}
		});
		this.format_ = "dd/MM/yyyy";
		this.layout_.setInline(true);
		this.layout_.addWidget(displayWidget);
		this.layout_.setAttributeValue("style", "white-space: nowrap");
		String TEMPLATE = "${shadow-x1-x2}${calendar}<div style=\"text-align:center; margin-top:3px\">${close}</div>";
		this.layout_.addWidget(this.popup_ = new WTemplate(
				new WString(TEMPLATE)));
		this.calendar_ = new WCalendar();
		this.calendar_.activated().addListener(this.popup_,
				new Signal1.Listener<WDate>() {
					public void trigger(WDate e1) {
						WDatePicker.this.popup_.hide();
					}
				});
		this.calendar_.activated().addListener(this,
				new Signal1.Listener<WDate>() {
					public void trigger(WDate e1) {
						WDatePicker.this.onPopupHidden();
					}
				});
		this.calendar_.selectionChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WDatePicker.this.setFromCalendar();
					}
				});
		WPushButton closeButton = new WPushButton(tr("Wt.WDatePicker.Close"));
		closeButton.clicked().addListener(this.popup_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.popup_.hide();
					}
				});
		closeButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.onPopupHidden();
					}
				});
		this.popup_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.popup_.bindWidget("calendar", this.calendar_);
		this.popup_.bindWidget("close", closeButton);
		this.popup_.hide();
		this.popup_.setPopup(true);
		this.popup_.setPositionScheme(PositionScheme.Absolute);
		this.popup_.setStyleClass("Wt-outset Wt-datepicker");
		this.popup_.escapePressed().addListener(this.popup_,
				new Signal.Listener() {
					public void trigger() {
						WDatePicker.this.popup_.hide();
					}
				});
		displayWidget.clicked().addListener(this.popup_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.popup_.show();
					}
				});
		displayWidget.clicked().addListener(this.positionJS_);
		displayWidget.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.setFromLineEdit();
					}
				});
		this.setGlobalPopup(false);
	}

	private void setFromCalendar() {
		if (!this.calendar_.getSelection().isEmpty()) {
			WDate calDate = this.calendar_.getSelection().iterator().next();
			this.forEdit_.setText(calDate.toString(this.format_));
			this.forEdit_.changed().trigger();
		}
		this.changed_.trigger();
	}

	private void setFromLineEdit() {
		WDate d = WDate.fromString(this.forEdit_.getText(), this.format_);
		if ((d != null)) {
			if (this.calendar_.getSelection().isEmpty()) {
				this.calendar_.select(d);
				this.calendar_.selectionChanged().trigger();
			} else {
				WDate j = this.calendar_.getSelection().iterator().next();
				if (!(j == d || (j != null && j.equals(d)))) {
					this.calendar_.select(d);
					this.calendar_.selectionChanged().trigger();
				}
			}
			this.calendar_.browseTo(d);
		}
	}

	private void onPopupHidden() {
		this.forEdit_.setFocus();
		this.popupClosed();
	}
}
