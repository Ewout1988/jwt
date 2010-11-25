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

/**
 * Enumeration that indicates a role for a data item.
 * <p>
 * A single data item can have data associated with it corresponding to
 * different roles. Each role may be used by the corresponding view class in a
 * different way.
 * <p>
 * 
 * @see WModelIndex#getData(int role)
 */
public class ItemDataRole {
	/**
	 * Role for textual representation.
	 */
	public final static int DisplayRole = 0;
	/**
	 * Role for the url of an icon.
	 */
	public final static int DecorationRole = 1;
	/**
	 * Role for the edited value.
	 */
	public final static int EditRole = 2;
	/**
	 * Role for the style class.
	 */
	public final static int StyleClassRole = 3;
	/**
	 * <p>
	 * Role that indicates the check state. Data for this role should be a
	 * <code>bool</code>. When the {@link ItemFlag#ItemIsTristate} flag is set
	 * for the item, data for this role should be of type {@link CheckState}.
	 */
	public final static int CheckStateRole = 4;
	/**
	 * Role for a tooltip.
	 */
	public final static int ToolTipRole = 5;
	/**
	 * Role for an internal path activated when clicked.
	 */
	public final static int InternalPathRole = 6;
	/**
	 * Role for a url activated when clicked.
	 */
	public final static int UrlRole = 7;
	/**
	 * Level in aggregation, for header data.
	 */
	public final static int LevelRole = 8;
	/**
	 * Marker pen color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}).
	 */
	public final static int MarkerPenColorRole = 16;
	/**
	 * Marker brush color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}).
	 */
	public final static int MarkerBrushColorRole = 17;
	/**
	 * First role reserved for user purposes.
	 */
	public final static int UserRole = 32;
}
