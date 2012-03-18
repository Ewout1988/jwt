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
 * An application event.
 * <p>
 * 
 * The application is notified of an event (like a user interaction, a sesion
 * timeout, an internal keep-alive event, or other event) using
 * {@link WApplication#notify(WEvent e) WApplication#notify()}.
 * <p>
 * You can check for a particular event type using {@link WEvent#getEventType()
 * getEventType()}.
 */
public class WEvent {
	private static Logger logger = LoggerFactory.getLogger(WEvent.class);

	/**
	 * Returns the event type.
	 */
	public EventType getEventType() {
		if (!(this.impl_.handler != null)) {
			return EventType.OtherEvent;
		}
		return this.impl_.handler.getSession().getEventType(this);
	}

	WEvent(WEvent.Impl impl) {
		this.impl_ = impl;
	}

	WEvent.Impl impl_;

	static class Impl {
		private static Logger logger = LoggerFactory.getLogger(Impl.class);

		WebSession.Handler handler;
		boolean renderOnly;

		Impl(WebSession.Handler aHandler, boolean doRenderOnly) {
			this.handler = aHandler;
			this.renderOnly = doRenderOnly;
		}

		public Impl(WebSession.Handler aHandler) {
			this(aHandler, false);
		}

		Impl() {
			this.handler = null;
		}
	}

	static int asInt(String v) {
		return Integer.parseInt(v);
	}

	static int asUInt(String v) {
		return Integer.parseInt(v);
	}

	static int parseIntParameter(WebRequest request, String name, int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return asInt(p);
			} catch (NumberFormatException ee) {
				logger.error(new StringWriter().append(
						"Could not cast event property '").append(name).append(
						": ").append(p).append("' to int").toString());
				return ifMissing;
			}
		} else {
			return ifMissing;
		}
	}

	static String getStringParameter(WebRequest request, String name) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			return p;
		} else {
			return "";
		}
	}

	static void decodeTouches(String str, List<Touch> result) {
		if (str.length() == 0) {
			return;
		}
		List<String> s = new ArrayList<String>();
		s = new ArrayList<String>(Arrays.asList(str.split(";")));
		if (s.size() % 9 != 0) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
		try {
			for (int i = 0; i < s.size(); i += 9) {
				result.add(new Touch(asUInt(s.get(i + 0)), asInt(s.get(i + 1)),
						asInt(s.get(i + 2)), asInt(s.get(i + 3)), asInt(s
								.get(i + 4)), asInt(s.get(i + 5)), asInt(s
								.get(i + 6)), asInt(s.get(i + 7)), asInt(s
								.get(i + 8))));
			}
		} catch (NumberFormatException ee) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
	}
}
