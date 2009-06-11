package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.List;

/**
 * A widget which group widgets into a frame with a title
 * 
 * 
 * This is typically used in a form to group certain form elements together.
 * <p>
 * Usage example:
 * <p>
 * <code>
 enum Vote { Republican , Democrate , NoVote }; <br> 
  <br> 
 // use a group box as widget container for 3 radio buttons, with a title <br> 
 WGroupBox container = new WGroupBox(&quot;USA elections vote&quot;); <br> 
 		  <br> 
 // use a button group to logically group the 3 options <br> 
 WButtonGroup group = new WButtonGroup(this); <br> 
		  <br> 
 WRadioButton button; <br> 
 button = new WRadioButton(&quot;I voted Republican&quot;, container); <br> 
 new WBreak(container); <br> 
 group.addButton(button, Vote.Republican.ordinal()); <br> 
 <br> 
 button = new WRadioButton(&quot;I voted Democrat&quot;, container); <br> 
 new WBreak(container); <br> 
 group.addButton(button, Vote.Democrate.ordinal()); <br> 
 <br> 
 button = new WRadioButton(&quot;I didn&apos;t vote&quot;, container); <br> 
 new WBreak(container); <br> 
 group.addButton(button, Vote.NoVote.ordinal()); <br> 
		  <br> 
 group.setCheckedButton(group.button(Vote.NoVote.ordinal()));
</code>
 * <p>
 * The widget corresponds to the HTML <code>&lt;fieldset&gt;</code> tag, and the
 * title in a nested <code>&lt;legend&gt;</code> tag.
 * <p>
 * Like {@link WContainerWidget}, WGroupBox is by default a block level widget.
 * <p>
 */
public class WGroupBox extends WContainerWidget {
	/**
	 * Create a groupbox with empty title.
	 */
	public WGroupBox(WContainerWidget parent) {
		super(parent);
		this.title_ = new WString();
		this.titleChanged_ = false;
	}

	public WGroupBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a groupbox with given title message.
	 */
	public WGroupBox(CharSequence title, WContainerWidget parent) {
		super(parent);
		this.title_ = new WString(title);
		this.titleChanged_ = false;
	}

	public WGroupBox(CharSequence title) {
		this(title, (WContainerWidget) null);
	}

	/**
	 * Get the title.
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Set the title.
	 */
	public void setTitle(CharSequence title) {
		this.title_ = WString.toWString(title);
		this.titleChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	public void refresh() {
		if (this.title_.refresh()) {
			this.titleChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString title_;
	private boolean titleChanged_;

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_FIELDSET;
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			DomElement legend = DomElement
					.createNew(DomElementType.DomElement_LEGEND);
			legend.setId(this.getFormName() + "l");
			legend.setProperty(Property.PropertyInnerHTML, escapeText(
					this.title_).toString());
			element.addChild(legend);
			this.titleChanged_ = false;
		}
		super.updateDom(element, all);
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		this.updateDom(e, false);
		result.add(e);
		if (this.titleChanged_) {
			DomElement legend = DomElement.getForUpdate(this.getFormName()
					+ "l", DomElementType.DomElement_LEGEND);
			legend.setProperty(Property.PropertyInnerHTML, escapeText(
					this.title_).toString());
			this.titleChanged_ = false;
			result.add(legend);
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.titleChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected int getFirstChildIndex() {
		return 1;
	}
}