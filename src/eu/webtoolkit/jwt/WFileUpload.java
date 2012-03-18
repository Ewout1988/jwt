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
 * A widget that allows a file to be uploaded.
 * <p>
 * 
 * This widget is displayed as a box in which a filename can be entered and a
 * browse button.
 * <p>
 * Depending on availability of JavaScript, the behaviour of the widget is
 * different, but the API is designed in a way which facilitates a portable use.
 * <p>
 * When JavaScript is available, the file will not be uploaded until
 * {@link WFileUpload#upload() upload()} is called. This will start an
 * asynchronous upload (and thus return immediately).
 * <p>
 * When no JavaScript is available, the file will be uploaded with the next
 * click event. Thus, {@link WFileUpload#upload() upload()} has no effect -- the
 * file will already be uploaded, and the corresponding signals will already be
 * emitted. To test if {@link WFileUpload#upload() upload()} will start an
 * upload, you may check using the {@link WFileUpload#canUpload() canUpload()}
 * call.
 * <p>
 * Thus, to properly use the widget, one needs to follow these rules:
 * <ul>
 * <li>
 * Be prepared to handle the {@link WFileUpload#uploaded() uploaded()} or
 * {@link WFileUpload#fileTooLarge() fileTooLarge()} signals also when
 * {@link WFileUpload#upload() upload()} was not called.</li>
 * <li>
 * Check using {@link WFileUpload#canUpload() canUpload()} if
 * {@link WFileUpload#upload() upload()} will schedule a new upload. if
 * (!canUpload()) then {@link WFileUpload#upload() upload()} will not have any
 * effect. if ({@link WFileUpload#canUpload() canUpload()}),
 * {@link WFileUpload#upload() upload()} will start a new file upload, which
 * completes succesfully using an {@link WFileUpload#uploaded() uploaded()}
 * signal or a {@link WFileUpload#fileTooLarge() fileTooLarge()} signals gets
 * emitted.</li>
 * </ul>
 * <p>
 * The WFileUpload widget must be hidden or deleted when a file is received. In
 * addition it is wise to prevent the user from uploading the file twice as in
 * the example below.
 * <p>
 * The uploaded file is automatically spooled to a local temporary file which
 * will be deleted together with the {@link WFileUpload} widget, unless
 * {@link WFileUpload#stealSpooledFile() stealSpooledFile()} is called.
 * <p>
 * WFileUpload is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The file upload itself corresponds to a
 * <code>&lt;input type=&quot;file&quot;&gt;</code> tag, but may be wrapped in a
 * <code>&lt;form&gt;</code> tag. This widget does not provide styling, and
 * styling through CSS is not well supported across browsers.
 */
public class WFileUpload extends WWebWidget {
	private static Logger logger = LoggerFactory.getLogger(WFileUpload.class);

	/**
	 * Creates a file upload widget.
	 */
	public WFileUpload(WContainerWidget parent) {
		super(parent);
		this.flags_ = new BitSet();
		this.textSize_ = 20;
		this.uploadedFiles_ = new ArrayList<UploadedFile>();
		this.fileTooLarge_ = new Signal1<Long>(this);
		this.dataReceived_ = new Signal2<Long, Long>(this);
		this.progressBar_ = null;
		this.tooLargeSize_ = 0;
		this.setInline(true);
		this.fileTooLargeImpl().addListener(this, new Signal.Listener() {
			public void trigger() {
				WFileUpload.this.handleFileTooLargeImpl();
			}
		});
		this.create();
	}

	/**
	 * Creates a file upload widget.
	 * <p>
	 * Calls {@link #WFileUpload(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WFileUpload() {
		this((WContainerWidget) null);
	}

	public void remove() {
		if (this.flags_.get(BIT_UPLOADING)) {
			WApplication.getInstance().enableUpdates(false);
		}
		super.remove();
	}

	/**
	 * Sets whether the file upload accepts multiple files.
	 * <p>
	 * In browsers which support the &quot;multiple&quot; attribute for the file
	 * upload (to be part of HTML5) control, this will allow the user to select
	 * multiple files at once.
	 * <p>
	 * All uploaded files are available from
	 * {@link WFileUpload#getUploadedFiles() getUploadedFiles()}. The
	 * single-file API will return only information on the first uploaded file.
	 * <p>
	 * The default value is <code>false</code>.
	 */
	public void setMultiple(boolean multiple) {
		this.flags_.set(BIT_MULTIPLE, multiple);
	}

	/**
	 * Returns whether multiple files can be uploaded.
	 * <p>
	 * 
	 * @see WFileUpload#setMultiple(boolean multiple)
	 */
	public boolean isMultiple() {
		return this.flags_.get(BIT_MULTIPLE);
	}

	/**
	 * Sets the size of the file input.
	 */
	public void setFileTextSize(int chars) {
		this.textSize_ = chars;
	}

	/**
	 * Returns the size of the file input.
	 */
	public int getFileTextSize() {
		return this.textSize_;
	}

	/**
	 * Returns the spooled location of the uploaded file.
	 * <p>
	 * Returns the temporary filename in which the uploaded file was spooled.
	 * The file is guaranteed to exist as long as the {@link WFileUpload} widget
	 * is not deleted, or a new file is not uploaded.
	 * <p>
	 * When multiple files were uploaded, this returns the information from the
	 * first file.
	 * <p>
	 * 
	 * @see WFileUpload#stealSpooledFile()
	 * @see WFileUpload#uploaded()
	 */
	public String getSpoolFileName() {
		if (!this.isEmpty()) {
			return this.uploadedFiles_.get(0).getSpoolFileName();
		} else {
			return "";
		}
	}

	/**
	 * Returns the client filename.
	 * <p>
	 * When multiple files were uploaded, this returns the information from the
	 * first file.
	 */
	public String getClientFileName() {
		if (!this.isEmpty()) {
			return this.uploadedFiles_.get(0).getClientFileName();
		} else {
			return "";
		}
	}

	/**
	 * Returns the client content description.
	 * <p>
	 * When multiple files were uploaded, this returns the information from the
	 * first file.
	 */
	public String getContentDescription() {
		if (!this.isEmpty()) {
			return this.uploadedFiles_.get(0).getContentType();
		} else {
			return "";
		}
	}

	/**
	 * Steals the spooled file.
	 * <p>
	 * By stealing the file, the spooled file will no longer be deleted together
	 * with this widget, which means you need to take care of managing that.
	 * <p>
	 * When multiple files were uploaded, this returns the information from the
	 * first file.
	 */
	public void stealSpooledFile() {
		if (!this.isEmpty()) {
			this.uploadedFiles_.get(0).stealSpoolFile();
		}
	}

	/**
	 * Returns whether one or more files have been uploaded.
	 */
	public boolean isEmpty() {
		return this.uploadedFiles_.isEmpty();
	}

	/**
	 * Checks if no filename was given and thus no file uploaded.
	 * (<b>Deprecated</b>).
	 * <p>
	 * Return whether a non-empty filename was given.
	 * <p>
	 * 
	 * @deprecated This method was renamed to {@link WFileUpload#isEmpty()
	 *             isEmpty()}
	 */
	public boolean isEmptyFileName() {
		return this.isEmpty();
	}

	/**
	 * Returns the uploaded files.
	 */
	public List<UploadedFile> getUploadedFiles() {
		return this.uploadedFiles_;
	}

	/**
	 * Returns whether {@link WFileUpload#upload() upload()} will start a new
	 * file upload.
	 * <p>
	 * A call to {@link WFileUpload#upload() upload()} will only start a new
	 * file upload if there is no JavaScript support. Otherwise, the most recent
	 * file will already be uploaded.
	 */
	public boolean canUpload() {
		return this.fileUploadTarget_ != null;
	}

	/**
	 * Signal emitted when a new file was uploaded.
	 * <p>
	 * This signal is emitted when file upload has been completed. It is good
	 * practice to hide or delete the {@link WFileUpload} widget when a file has
	 * been uploaded succesfully.
	 * <p>
	 * 
	 * @see WFileUpload#upload()
	 * @see WFileUpload#fileTooLarge()
	 */
	public EventSignal uploaded() {
		return this.voidEventSignal(UPLOADED_SIGNAL, true);
	}

	/**
	 * Signal emitted when the user tried to upload a too large file.
	 * <p>
	 * The parameter is the (approximate) size of the file the user tried to
	 * upload.
	 * <p>
	 * The maximum file size is determined by the maximum request size, which
	 * may be configured in the configuration file (&lt;max-request-size&gt;).
	 * <p>
	 * 
	 * @see WFileUpload#uploaded()
	 * @see WApplication#requestTooLarge()
	 */
	public Signal1<Long> fileTooLarge() {
		return this.fileTooLarge_;
	}

	/**
	 * Signal emitted when the user selected a new file.
	 * <p>
	 * One could react on the user selecting a (new) file, by uploading the file
	 * immediately.
	 * <p>
	 * Caveat: this signal is not emitted with konqueror and possibly other
	 * browsers. Thus, in the above scenario you should still provide an
	 * alternative way to call the {@link WFileUpload#upload() upload()} method.
	 */
	public EventSignal changed() {
		return this.voidEventSignal(CHANGE_SIGNAL, true);
	}

	/**
	 * Starts the file upload.
	 * <p>
	 * The {@link WFileUpload#uploaded() uploaded()} signal is emitted when a
	 * file is uploaded, or the {@link WFileUpload#fileTooLarge()
	 * fileTooLarge()} signal is emitted when the file size exceeded the maximum
	 * request size.
	 * <p>
	 * 
	 * @see WFileUpload#uploaded()
	 * @see WFileUpload#canUpload()
	 */
	public void upload() {
		if (this.fileUploadTarget_ != null && !this.flags_.get(BIT_UPLOADING)) {
			this.flags_.set(BIT_DO_UPLOAD);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
			if (this.progressBar_ != null) {
				if (this.progressBar_.getParent() != this) {
					this.hide();
				} else {
					this.progressBar_.show();
				}
			}
			WApplication.getInstance().enableUpdates();
			this.flags_.set(BIT_UPLOADING);
		}
	}

	/**
	 * Sets a progress bar to indicate upload progress.
	 * <p>
	 * When the file is being uploaded, upload progress is indicated using the
	 * provided progress bar. Both the progress bar range and values are
	 * configured when the upload starts.
	 * <p>
	 * If the provided progress bar already has a parent, then the file upload
	 * itself is hidden as soon as the upload starts. If the provided progress
	 * bar does not yet have a parent, then the bar becomes part of the file
	 * upload, and replaces the file prompt when the upload is started.
	 * <p>
	 * The default progress bar is 0 (no upload progress is indicated).
	 * <p>
	 * To update the progess bar server push is used, you should only use this
	 * functionality when using a Servlet 3.0 compatible servlet container.
	 * <p>
	 * 
	 * @see WFileUpload#dataReceived()
	 */
	public void setProgressBar(WProgressBar bar) {
		if (this.progressBar_ != null)
			this.progressBar_.remove();
		this.progressBar_ = bar;
		if (this.progressBar_ != null) {
			if (!(this.progressBar_.getParent() != null)) {
				this.progressBar_.setParentWidget(this);
				this.progressBar_.hide();
			}
		}
	}

	/**
	 * Returns the progress bar.
	 * <p>
	 * 
	 * @see WFileUpload#setProgressBar(WProgressBar bar)
	 */
	public WProgressBar getProgressBar() {
		return this.progressBar_;
	}

	/**
	 * Signal emitted while a file is being uploaded.
	 * <p>
	 * When supported by the connector library, you can track the progress of
	 * the file upload by listening to this signal.
	 */
	public Signal2<Long, Long> dataReceived() {
		return this.dataReceived_;
	}

	public void enableAjax() {
		this.create();
		this.flags_.set(BIT_ENABLE_AJAX);
		this.repaint();
		super.enableAjax();
	}

	private static final int BIT_DO_UPLOAD = 0;
	private static final int BIT_ENABLE_AJAX = 1;
	private static final int BIT_UPLOADING = 2;
	private static final int BIT_MULTIPLE = 3;
	private static final int BIT_ENABLED_CHANGED = 4;
	BitSet flags_;
	private int textSize_;
	private List<UploadedFile> uploadedFiles_;
	private Signal1<Long> fileTooLarge_;
	private Signal2<Long, Long> dataReceived_;
	private WResource fileUploadTarget_;
	private WProgressBar progressBar_;

	private void create() {
		boolean methodIframe = WApplication.getInstance().getEnvironment()
				.hasAjax();
		if (methodIframe) {
			this.fileUploadTarget_ = new WFileUploadResource(this);
			this.fileUploadTarget_.setUploadProgress(true);
			this.fileUploadTarget_.dataReceived().addListener(this,
					new Signal2.Listener<Long, Long>() {
						public void trigger(Long e1, Long e2) {
							WFileUpload.this.onData(e1, e2);
						}
					});
		} else {
			this.fileUploadTarget_ = null;
		}
		this.setFormObject(!(this.fileUploadTarget_ != null));
		this.uploaded().addListener(this, new Signal.Listener() {
			public void trigger() {
				WFileUpload.this.onUploaded();
			}
		});
		this.fileTooLarge().addListener(this, new Signal1.Listener<Long>() {
			public void trigger(Long e1) {
				WFileUpload.this.onUploaded();
			}
		});
	}

	private void onData(long current, long total) {
		this.dataReceived_.trigger(current, total);
		WebSession.Handler h = WebSession.Handler.getInstance();
		long dataExceeded = 0L;
		h.setRequest((WebRequest) null, (WebResponse) null);
		if (dataExceeded != 0) {
			if (this.flags_.get(BIT_UPLOADING)) {
				this.flags_.clear(BIT_UPLOADING);
				this.tooLargeSize_ = dataExceeded;
				this.handleFileTooLargeImpl();
				WApplication app = WApplication.getInstance();
				app.triggerUpdate();
				app.enableUpdates(false);
			}
			return;
		}
		if (this.progressBar_ != null && this.flags_.get(BIT_UPLOADING)) {
			this.progressBar_.setRange(0, (double) total);
			this.progressBar_.setValue((double) current);
			WApplication app = WApplication.getInstance();
			app.triggerUpdate();
		}
	}

	void setRequestTooLarge(long size) {
		this.fileTooLarge().trigger(size);
	}

	void updateDom(DomElement element, boolean all) {
		boolean containsProgress = this.progressBar_ != null
				&& this.progressBar_.getParent() == this;
		DomElement inputE = null;
		if (element.getType() != DomElementType.DomElement_INPUT
				&& this.flags_.get(BIT_DO_UPLOAD) && containsProgress
				&& !this.progressBar_.isRendered()) {
			element.addChild(this.progressBar_.createSDomElement(WApplication
					.getInstance()));
		}
		if (this.fileUploadTarget_ != null && this.flags_.get(BIT_DO_UPLOAD)) {
			element.callMethod("submit()");
			this.flags_.clear(BIT_DO_UPLOAD);
			if (containsProgress) {
				inputE = DomElement.getForUpdate("in" + this.getId(),
						DomElementType.DomElement_INPUT);
				inputE.setProperty(Property.PropertyStyleDisplay, "none");
			}
		}
		if (this.flags_.get(BIT_ENABLED_CHANGED)) {
			if (!(inputE != null)) {
				inputE = DomElement.getForUpdate("in" + this.getId(),
						DomElementType.DomElement_INPUT);
			}
			inputE.callMethod("disabled=true");
			this.flags_.clear(BIT_ENABLED_CHANGED);
		}
		EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
		if (change != null && change.needsUpdate(all)) {
			if (!(inputE != null)) {
				inputE = DomElement.getForUpdate("in" + this.getId(),
						DomElementType.DomElement_INPUT);
			}
			this.updateSignalConnection(inputE, change, "change", all);
		}
		if (inputE != null) {
			element.addChild(inputE);
		}
		super.updateDom(element, all);
	}

	DomElement createDomElement(WApplication app) {
		DomElement result = DomElement.createNew(this.getDomElementType());
		if (result.getType() == DomElementType.DomElement_FORM) {
			result.setId(this.getId());
		} else {
			result.setName(this.getId());
		}
		EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
		if (this.fileUploadTarget_ != null) {
			DomElement i = DomElement
					.createNew(DomElementType.DomElement_IFRAME);
			i.setProperty(Property.PropertyClass, "Wt-resource");
			i
					.setProperty(Property.PropertySrc, this.fileUploadTarget_
							.getUrl());
			i.setName("if" + this.getId());
			DomElement form = result;
			form.setAttribute("method", "post");
			form.setAttribute("action", this.fileUploadTarget_.getUrl());
			form.setAttribute("enctype", "multipart/form-data");
			form.setProperty(Property.PropertyStyle,
					"margin:0;padding:0;display:inline");
			form.setProperty(Property.PropertyTarget, "if" + this.getId());
			DomElement d = DomElement.createNew(DomElementType.DomElement_SPAN);
			d.addChild(i);
			form.addChild(d);
			DomElement input = DomElement
					.createNew(DomElementType.DomElement_INPUT);
			input.setAttribute("type", "file");
			if (this.flags_.get(BIT_MULTIPLE)) {
				input.setAttribute("multiple", "multiple");
			}
			input.setAttribute("name", "data");
			input.setAttribute("size", String.valueOf(this.textSize_));
			input.setId("in" + this.getId());
			if (!this.isEnabled()) {
				input.setProperty(Property.PropertyDisabled, "true");
			}
			if (change != null) {
				this.updateSignalConnection(input, change, "change", true);
			}
			form.addChild(input);
		} else {
			result.setAttribute("type", "file");
			if (this.flags_.get(BIT_MULTIPLE)) {
				result.setAttribute("multiple", "multiple");
			}
			result.setAttribute("size", String.valueOf(this.textSize_));
			if (!this.isEnabled()) {
				result.setProperty(Property.PropertyDisabled, "true");
			}
			if (change != null) {
				this.updateSignalConnection(result, change, "change", true);
			}
		}
		this.updateDom(result, true);
		this.flags_.clear(BIT_ENABLE_AJAX);
		return result;
	}

	DomElementType getDomElementType() {
		return this.fileUploadTarget_ != null ? DomElementType.DomElement_FORM
				: DomElementType.DomElement_INPUT;
	}

	void propagateRenderOk(boolean deep) {
		super.propagateRenderOk(deep);
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.flags_.get(BIT_ENABLE_AJAX)) {
			DomElement plainE = DomElement.getForUpdate(this,
					DomElementType.DomElement_INPUT);
			DomElement ajaxE = this.createDomElement(app);
			plainE.replaceWith(ajaxE);
			result.add(plainE);
		} else {
			super.getDomChanges(result, app);
		}
	}

	protected void propagateSetEnabled(boolean enabled) {
		this.flags_.set(BIT_ENABLED_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		super.propagateSetEnabled(enabled);
	}

	EventSignal fileTooLargeImpl() {
		return this.voidEventSignal(FILETOOLARGE_SIGNAL, true);
	}

	private void handleFileTooLargeImpl() {
		this.fileTooLarge().trigger(this.tooLargeSize_);
	}

	private void onUploaded() {
		if (this.flags_.get(BIT_UPLOADING)) {
			WApplication.getInstance().enableUpdates(false);
			this.flags_.clear(BIT_UPLOADING);
		}
	}

	long tooLargeSize_;

	void setFormData(WObject.FormData formData) {
		this.setFiles(formData.files);
		if (!formData.files.isEmpty()) {
			this.uploaded().trigger();
		}
	}

	void setFiles(List<UploadedFile> files) {
		this.uploadedFiles_.clear();
		for (int i = 0; i < files.size(); ++i) {
			if (files.get(i).getClientFileName().length() != 0) {
				this.uploadedFiles_.add(files.get(i));
			}
		}
	}

	private static String CHANGE_SIGNAL = "M_change";
	private static String UPLOADED_SIGNAL = "M_uploaded";
	private static String FILETOOLARGE_SIGNAL = "M_filetoolarge";
}
