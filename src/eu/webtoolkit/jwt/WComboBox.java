package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A widget that provides a drop-down combo-box control.
 * 
 * 
 * A combo box provides the user with a set of options, from which one option
 * may be selected.
 * <p>
 * WComboBox is an MVC view class, using a simple string list model by default.
 * The model may be populated using addItem(const {@link WString}&amp;) or
 * {@link WComboBox#insertItem(int index, CharSequence text)} and the contents
 * can be cleared through {@link WComboBox#clear()}. These methods manipulate
 * the underlying {@link WComboBox#getModel()}.
 * <p>
 * To use the combo box with a custom model instead of the default
 * {@link WStringListModel}, use
 * {@link WComboBox#setModel(WAbstractItemModel model)}.
 * <p>
 * To react to selection events, connect to the {@link WFormWidget#changed()},
 * {@link WComboBox#activated()} or {@link WComboBox#sactivated()} signals.
 * <p>
 * At all times, the current selection index is available through
 * {@link WComboBox#getCurrentIndex()} and the current selection text using
 * {@link WComboBox#getCurrentText()}.
 * <p>
 * The widget corresponds to the HTML <code>&lt;select&gt;</code> tag.
 * <p>
 * WComboBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 */
public class WComboBox extends WFormWidget {
	/**
	 * Create an empty combo-box with optional <i>parent</i>.
	 */
	public WComboBox(WContainerWidget parent) {
		super(parent);
		this.model_ = null;
		this.modelColumn_ = 0;
		this.currentIndex_ = -1;
		this.itemsChanged_ = false;
		this.selectionChanged_ = false;
		this.currentlyConnected_ = false;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.activated_ = new Signal1<Integer>(this);
		this.sactivated_ = new Signal1<WString>(this);
		this.setInline(true);
		this.setFormObject(true);
		this.setModel(new WStringListModel(this));
	}

	public WComboBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Add an option item.
	 * 
	 * Equivalent to {@link WComboBox#insertItem(int index, CharSequence text)
	 * insertItem} ({@link WComboBox#getCount()}, <i>text</i>).
	 */
	public void addItem(CharSequence text) {
		this.insertItem(this.getCount(), text);
	}

	/**
	 * Returns the number of items.
	 */
	public int getCount() {
		return this.model_.getRowCount();
	}

	/**
	 * Returns the currently selected item.
	 * 
	 * If no item is currently selected, the method returns -1.
	 * <p>
	 * The default value is 0, unless the combo box is empty.
	 */
	public int getCurrentIndex() {
		return this.currentIndex_;
	}

	/**
	 * Insert an item at the specified position.
	 * 
	 * The item is inserted in the underlying model at position <i>index</i>.
	 * This requires that the {@link WComboBox#getModel()} is editable.
	 * <p>
	 * 
	 * @see WComboBox#addItem(CharSequence text)
	 * @see WComboBox#removeItem(int index)
	 */
	public void insertItem(int index, CharSequence text) {
		if (this.model_.insertRow(index)) {
			this.setItemText(index, text);
		}
	}

	/**
	 * Remove the item at the specified position.
	 * 
	 * The item is removed from the underlying model. This requires that the
	 * {@link WComboBox#getModel()} is editable.
	 * <p>
	 * 
	 * @see WComboBox#insertItem(int index, CharSequence text)
	 * @see WComboBox#clear()
	 */
	public void removeItem(int index) {
		this.model_.removeRow(index);
		this.setCurrentIndex(this.currentIndex_);
	}

	/**
	 * Changes the current selection.
	 * 
	 * Specify a value of -1 for <i>index</i> to clear the selection.
	 */
	public void setCurrentIndex(int index) {
		int newIndex = Math.min(index, this.getCount() - 1);
		if (this.currentIndex_ != newIndex) {
			this.currentIndex_ = newIndex;
			this.selectionChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
	}

	/**
	 * Changes the text for a specified option.
	 * 
	 * The text for the item at position <i>index</i> is changed. This requires
	 * that the {@link WComboBox#getModel()} is editable.
	 */
	public void setItemText(int index, CharSequence text) {
		this.model_.setData(index, this.modelColumn_, text);
	}

	/**
	 * Returns the text of the currently selected item.
	 * 
	 * @see WComboBox#getCurrentIndex()
	 * @see WComboBox#getItemText(int index)
	 */
	public WString getCurrentText() {
		if (this.currentIndex_ != -1) {
			return StringUtils.asString(this.model_.getData(this.currentIndex_,
					this.modelColumn_));
		} else {
			return new WString();
		}
	}

	/**
	 * Returns the text of a particular item.
	 * 
	 * @see WComboBox#setItemText(int index, CharSequence text)
	 * @see WComboBox#getCurrentText()
	 */
	public WString getItemText(int index) {
		return StringUtils.asString(this.model_.getData(index,
				this.modelColumn_));
	}

	/**
	 * Sets the model to be used for the items.
	 * 
	 * The <i>model</i> may not be 0, and ownership of the model is not
	 * transferred.
	 * <p>
	 * The default value is a {@link WStringListModel} that is owned by the
	 * combo box.
	 * <p>
	 * 
	 * @see WComboBox#setModelColumn(int index)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		this.modelConnections_.add(this.model_.columnsInserted().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.columnsRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WComboBox.this.itemsChanged();
					}
				}));
	}

	/**
	 * Sets the column in the model to be used for the items.
	 * 
	 * The column <i>index</i> in the model will be used to retrieve data.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WComboBox#setModel(WAbstractItemModel model)
	 */
	public void setModelColumn(int index) {
		this.modelColumn_ = 0;
	}

	/**
	 * Returns the data model.
	 * 
	 * @see WComboBox#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Returns the index of the first item that matches a text.
	 */
	public int findText(CharSequence text, MatchOptions flags) {
		List<WModelIndex> list = this.model_.match(this.model_.getIndex(0,
				this.modelColumn_), ItemDataRole.DisplayRole, text, 1, flags);
		if (list.isEmpty()) {
			return -1;
		} else {
			return list.get(0).getRow();
		}
	}

	public WValidator.State validate() {
		if (this.getValidator() != null) {
			String text = this.getCurrentText().toString();
			return this.getValidator().validate(text);
		} else {
			return WValidator.State.Valid;
		}
	}

	public void refresh() {
		this.itemsChanged();
		super.refresh();
	}

	/**
	 * Clear all items.
	 * 
	 * Removes all items from the underlying model. This requires that the
	 * {@link WComboBox#getModel()} is editable.
	 */
	public void clear() {
		this.model_.removeRows(0, this.getCount());
		this.setCurrentIndex(this.currentIndex_);
	}

	/**
	 * Signal emitted when the selection changed.
	 * 
	 * The newly selected item is passed as an argument.
	 * <p>
	 * 
	 * @see WComboBox#sactivated()
	 * @see WComboBox#getCurrentIndex()
	 */
	public Signal1<Integer> activated() {
		return this.activated_;
	}

	/**
	 * Signal emitted when the selection changed.
	 * 
	 * The newly selected text is passed as an argument.
	 * <p>
	 * 
	 * @see WComboBox#activated()
	 * @see WComboBox#getCurrentText()
	 */
	public Signal1<WString> sactivated() {
		return this.sactivated_;
	}

	private WAbstractItemModel model_;
	private int modelColumn_;
	private int currentIndex_;
	private boolean itemsChanged_;
	boolean selectionChanged_;
	private boolean currentlyConnected_;
	private List<AbstractSignal.Connection> modelConnections_;
	private Signal1<Integer> activated_;
	private Signal1<WString> sactivated_;

	private void itemsChanged() {
		this.itemsChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	private void propagateChange() {
		int myCurrentIndex = this.currentIndex_;
		WString myCurrentValue = new WString();
		if (this.currentIndex_ != -1) {
			myCurrentValue = this.getCurrentText();
		}
		this.activated_.trigger(this.currentIndex_);
		if (myCurrentIndex != -1) {
			this.sactivated_.trigger(myCurrentValue);
		}
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.itemsChanged_ || all) {
			if (all && this.getCount() > 0 && this.currentIndex_ == -1) {
				this.currentIndex_ = 0;
			}
			if (!all) {
				element.removeAllChildren();
			}
			for (int i = 0; i < this.getCount(); ++i) {
				DomElement item = DomElement
						.createNew(DomElementType.DomElement_OPTION);
				item.setAttribute("value", String.valueOf(i));
				item.setProperty(Property.PropertyInnerHTML, escapeText(
						StringUtils.asString(this.model_.getData(i,
								this.modelColumn_))).toString());
				if (this.isSelected(i)) {
					item.setProperty(Property.PropertySelected, "true");
				}
				WString sc = StringUtils.asString(this.model_.getData(i,
						this.modelColumn_, ItemDataRole.StyleClassRole));
				if (!(sc.length() == 0)) {
					item.setAttribute("class", sc.toString());
				}
				element.addChild(item);
			}
			this.itemsChanged_ = false;
		}
		if (this.selectionChanged_) {
			element.setProperty(Property.PropertySelectedIndex, String
					.valueOf(this.currentIndex_));
			this.selectionChanged_ = false;
		}
		if (!this.currentlyConnected_
				&& (this.activated_.isConnected() || this.sactivated_
						.isConnected())) {
			this.currentlyConnected_ = true;
			this.changed().addListener(this, new Signal.Listener() {
				public void trigger() {
					WComboBox.this.propagateChange();
				}
			});
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_SELECT;
	}

	protected void propagateRenderOk(boolean deep) {
		this.itemsChanged_ = false;
		this.selectionChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.selectionChanged_) {
			return;
		}
		if (!formData.values.isEmpty()) {
			String value = formData.values.get(0);
			if (value.length() != 0) {
				try {
					this.currentIndex_ = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					WApplication.getInstance().log("error").append(
							"WComboBox received illegal form value: '").append(
							value).append("'");
				}
			} else {
				this.currentIndex_ = -1;
			}
		}
	}

	protected boolean isSelected(int index) {
		return index == this.currentIndex_;
	}

	protected void dummy() {
	}
}