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

class WWidgetCanvasPainter extends WWidgetPainter {
	private static Logger logger = LoggerFactory
			.getLogger(WWidgetCanvasPainter.class);

	public WWidgetCanvasPainter(WPaintedWidget widget) {
		super(widget);
	}

	public WCanvasPaintDevice createPaintDevice(boolean paintUpdate) {
		return new WCanvasPaintDevice(new WLength(this.widget_.renderWidth_),
				new WLength(this.widget_.renderHeight_), (WObject) null,
				paintUpdate);
	}

	public WPaintDevice getPaintDevice(boolean paintUpdate) {
		return this.createPaintDevice(paintUpdate);
	}

	public void createContents(DomElement result, WPaintDevice device) {
		String wstr = String.valueOf(this.widget_.renderWidth_);
		String hstr = String.valueOf(this.widget_.renderHeight_);
		result.setProperty(Property.PropertyStylePosition, "relative");
		result.setProperty(Property.PropertyStyleOverflowX, "hidden");
		result.setProperty(Property.PropertyStyleOverflowY, "hidden");
		DomElement canvas = DomElement
				.createNew(DomElementType.DomElement_CANVAS);
		canvas.setId('c' + this.widget_.getId());
		canvas.setProperty(Property.PropertyStyleDisplay, "block");
		canvas.setAttribute("width", wstr);
		canvas.setAttribute("height", hstr);
		result.addChild(canvas);
		this.widget_.sizeChanged_ = false;
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		DomElement text = null;
		if (canvasDevice.getTextMethod() == WCanvasPaintDevice.TextMethod.DomText) {
			text = DomElement.createNew(DomElementType.DomElement_DIV);
			text.setId('t' + this.widget_.getId());
			text.setProperty(Property.PropertyStylePosition, "absolute");
			text.setProperty(Property.PropertyStyleZIndex, "1");
			text.setProperty(Property.PropertyStyleTop, "0px");
			text.setProperty(Property.PropertyStyleLeft, "0px");
		}
		DomElement el = text != null ? text : result;
		boolean hasJsObjects = this.widget_.jsObjects_.size() > 0;
		if (hasJsObjects) {
			StringBuilder ss = new StringBuilder();
			WApplication app = WApplication.getInstance();
			ss.append("new Wt3_3_5.WPaintedWidget(")
					.append(app.getJavaScriptClass()).append(",")
					.append(this.widget_.getJsRef()).append(");");
			this.widget_.jsObjects_.updateJs(ss);
			el.callJavaScript(ss.toString());
		}
		canvasDevice.render('c' + this.widget_.getId(), el);
		if (hasJsObjects) {
			StringBuilder ss = new StringBuilder();
			ss.append(this.widget_.getObjJsRef())
					.append(".repaint=function(){");
			ss.append(canvasDevice.recordedJs_.toString());
			if (this.widget_.areaImage_ != null) {
				this.widget_.areaImage_.setTargetJS(this.widget_.getObjJsRef());
				ss.append(this.widget_.areaImage_.getUpdateAreasJS());
			}
			ss.append("};");
			ss.append(this.widget_.getObjJsRef()).append(".repaint();");
			el.callJavaScript(ss.toString());
		} else {
			StringBuilder ss = new StringBuilder();
			ss.append(canvasDevice.recordedJs_.toString());
			el.callJavaScript(ss.toString());
		}
		if (text != null) {
			result.addChild(text);
		}
		;
	}

	public void updateContents(final List<DomElement> result,
			WPaintDevice device) {
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		if (this.widget_.sizeChanged_) {
			DomElement canvas = DomElement.getForUpdate(
					'c' + this.widget_.getId(),
					DomElementType.DomElement_CANVAS);
			canvas.setAttribute("width",
					String.valueOf(this.widget_.renderWidth_));
			canvas.setAttribute("height",
					String.valueOf(this.widget_.renderHeight_));
			result.add(canvas);
			this.widget_.sizeChanged_ = false;
		}
		boolean domText = canvasDevice.getTextMethod() == WCanvasPaintDevice.TextMethod.DomText;
		DomElement el = DomElement.getForUpdate(
				domText ? 't' + this.widget_.getId() : this.widget_.getId(),
				DomElementType.DomElement_DIV);
		if (domText) {
			el.removeAllChildren();
		}
		boolean hasJsObjects = this.widget_.jsObjects_.size() > 0;
		if (hasJsObjects) {
			StringBuilder ss = new StringBuilder();
			this.widget_.jsObjects_.updateJs(ss);
			el.callJavaScript(ss.toString());
		}
		canvasDevice.render('c' + this.widget_.getId(), el);
		if (hasJsObjects) {
			StringBuilder ss = new StringBuilder();
			ss.append(this.widget_.getObjJsRef())
					.append(".repaint=function(){");
			ss.append(canvasDevice.recordedJs_.toString());
			if (this.widget_.areaImage_ != null) {
				this.widget_.areaImage_.setTargetJS(this.widget_.getObjJsRef());
				ss.append(this.widget_.areaImage_.getUpdateAreasJS());
			}
			ss.append("};");
			ss.append(this.widget_.getObjJsRef()).append(".repaint();");
			el.callJavaScript(ss.toString());
		} else {
			StringBuilder ss = new StringBuilder();
			ss.append(canvasDevice.recordedJs_.toString());
			el.callJavaScript(ss.toString());
		}
		result.add(el);
		;
	}

	public WWidgetPainter.RenderType getRenderType() {
		return WWidgetPainter.RenderType.HtmlCanvas;
	}
}
