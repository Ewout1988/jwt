/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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
 * Represents a selection of a bar.
 */
public class WBarSelection extends WSelection {
	private static Logger logger = LoggerFactory.getLogger(WBarSelection.class);

	/**
	 * The index that corresponds to the selected bar in the WAbstractDataModel.
	 */
	public WModelIndex index;

	public WBarSelection() {
		super();
		this.index = null;
	}

	public WBarSelection(double distance, WModelIndex index) {
		super(distance);
		this.index = index;
	}
}
