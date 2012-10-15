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
 * An model that manages a list of strings.
 * <p>
 * 
 * This model only manages a unidimensional list of strings. It is used as the
 * default model for view classes that show a list.
 * <p>
 * The model only presents {@link ItemDataRole#DisplayRole DisplayRole} data of
 * a single column of data, but otherwise provides support for all standard
 * features of a model, including editing and addition and removal of data rows.
 * <p>
 * You can populate the model by passing a list of strings to its consructor, or
 * by using the {@link WStringListModel#setStringList(List strings)
 * setStringList()} method. You can set or retrieve data using the
 * {@link WStringListModel#setData(WModelIndex index, Object value, int role)
 * setData()} and {@link WStringListModel#getData(WModelIndex index, int role)
 * getData()} methods, and add or remove data using the
 * {@link WStringListModel#insertRows(int row, int count, WModelIndex parent)
 * insertRows()} and
 * {@link WStringListModel#removeRows(int row, int count, WModelIndex parent)
 * removeRows()} methods.
 * <p>
 * 
 * @see WComboBox
 * @see WSelectionBox
 */
public class WStringListModel extends WAbstractListModel {
	private static Logger logger = LoggerFactory
			.getLogger(WStringListModel.class);

	/**
	 * Creates a new empty string list model.
	 */
	public WStringListModel(WObject parent) {
		super(parent);
		this.strings_ = new ArrayList<WString>();
	}

	/**
	 * Creates a new empty string list model.
	 * <p>
	 * Calls {@link #WStringListModel(WObject parent) this((WObject)null)}
	 */
	public WStringListModel() {
		this((WObject) null);
	}

	/**
	 * Creates a new string list model.
	 */
	public WStringListModel(List<WString> strings, WObject parent) {
		super(parent);
		this.strings_ = strings;
	}

	/**
	 * Creates a new string list model.
	 * <p>
	 * Calls {@link #WStringListModel(List strings, WObject parent)
	 * this(strings, (WObject)null)}
	 */
	public WStringListModel(List<WString> strings) {
		this(strings, (WObject) null);
	}

	/**
	 * Sets a new string list.
	 * <p>
	 * Replaces the current string list with a new list.
	 * <p>
	 * 
	 * @see WAbstractItemModel#dataChanged()
	 * @see WStringListModel#addString(CharSequence string)
	 */
	public void setStringList(List<WString> strings) {
		int currentSize = this.strings_.size();
		int newSize = strings.size();
		if (newSize > currentSize) {
			this.beginInsertRows(null, currentSize, newSize - 1);
		} else {
			if (newSize < currentSize) {
				this.beginRemoveRows(null, newSize, currentSize - 1);
			}
		}
		Utils.copyList(strings, this.strings_);
		if (newSize > currentSize) {
			this.endInsertRows();
		} else {
			if (newSize < currentSize) {
				this.endRemoveRows();
			}
		}
		int numChanged = Math.min(currentSize, newSize);
		if (numChanged != 0) {
			this.dataChanged().trigger(this.getIndex(0, 0),
					this.getIndex(numChanged - 1, 0));
		}
	}

	/**
	 * Inserts a string.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public void insertString(int row, CharSequence string) {
		this.insertRows(row, 1);
		this.setData(row, 0, string);
	}

	/**
	 * Adds a string.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public void addString(CharSequence string) {
		this.insertString(this.getRowCount(), string);
	}

	/**
	 * Returns the string list.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public List<WString> getStringList() {
		return this.strings_;
	}

	/**
	 * Returns the flags for an item.
	 * <p>
	 * This method is reimplemented to return {@link ItemFlag#ItemIsSelectable
	 * ItemIsSelectable} | {@link ItemFlag#ItemIsEditable ItemIsEditable}.
	 * <p>
	 * 
	 * @see ItemFlag
	 */
	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		return EnumSet.of(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEditable);
	}

	public boolean setData(WModelIndex index, Object value, int role) {
		if (role == ItemDataRole.EditRole) {
			role = ItemDataRole.DisplayRole;
		}
		if (role == ItemDataRole.DisplayRole) {
			this.strings_.set(index.getRow(), StringUtils.asString(value));
			this.dataChanged().trigger(index, index);
			return true;
		} else {
			return false;
		}
	}

	public Object getData(WModelIndex index, int role) {
		return role == ItemDataRole.DisplayRole ? this.strings_.get(index
				.getRow()) : null;
	}

	public int getRowCount(WModelIndex parent) {
		return (parent != null) ? 0 : this.strings_.size();
	}

	public boolean insertRows(int row, int count, WModelIndex parent) {
		if (!(parent != null)) {
			this.beginInsertRows(parent, row, row + count - 1);
			{
				int insertPos = 0 + row;
				for (int ii = 0; ii < count; ++ii)
					this.strings_.add(insertPos + ii, new WString());
			}
			;
			this.endInsertRows();
			return true;
		} else {
			return false;
		}
	}

	public boolean removeRows(int row, int count, WModelIndex parent) {
		if (!(parent != null)) {
			this.beginRemoveRows(parent, row, row + count - 1);
			for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii)
				this.strings_.remove(0 + row);
			;
			this.endRemoveRows();
			return true;
		} else {
			return false;
		}
	}

	public void sort(int column, SortOrder order) {
		this.layoutAboutToBeChanged().trigger();
		if (order == SortOrder.AscendingOrder) {
			Collections.sort(this.strings_);
		} else {
			Collections.sort(this.strings_, new ReverseOrder<WString>());
		}
		this.layoutChanged().trigger();
	}

	private List<WString> strings_;
}
