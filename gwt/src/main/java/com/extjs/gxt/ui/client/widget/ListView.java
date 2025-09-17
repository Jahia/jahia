/*
 * Sencha GXT 2.3.1a - Sencha for GWT
 * Copyright(c) 2007-2013, Sencha, Inc.
 * licensing@sencha.com
 *
 * http://www.sencha.com/products/gxt/license/
 */
package com.extjs.gxt.ui.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.aria.FocusFrame;
import com.extjs.gxt.ui.client.core.CompositeElement;
import com.extjs.gxt.ui.client.core.DomQuery;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * A mechanism for displaying data using custom layout templates. ListView uses
 * an {@link XTemplate} as its internal templating mechanism.
 *
 * <p />
 * <b>In order to use these features, an {@link #setItemSelector(String)} must
 * be provided for the ListView to determine what nodes it will be working
 * with.</b>
 *
 * <dl>
 * <dt><b>Events:</b></dt>
 *
 * <dd><b>Select</b> : ListViewEvent(listView, event)<br>
 * <div>Fires when a template node is clicked.</div>
 * <ul>
 * <li>listView : this</li>
 * <li>event : the dom event</li>
 * <li>index : the index of the target node</li>
 * </ul>
 * </dd>
 *
 * <dd><b>DoubleClick</b> : ListViewEvent(listView, index, element, event)<br>
 * <div>Fires when a template node is double clicked.</div>
 * <ul>
 * <li>listView : this</li>
 * <li>index : the index of the target node</li>
 * <li>element : the target node</li>
 * <li>event : the dom event</li>
 * </ul>
 * </dd>
 *
 * <dd><b>ContextMenu</b> : ListViewEvent(listView, index, element, event)<br>
 * <div>Fires when a template node is right clicked.</div>
 * <ul>
 * <li>listView : this</li>
 * <li>index : the index of the target node</li>
 * <li>element : the target node</li>
 * <li>event : the dom event</li>
 * </ul>
 * </dd>
 * </dl>
 *
 * <dl>
 * <dt>Inherited Events:</dt>
 * <dd>BoxComponent Move</dd>
 * <dd>BoxComponent Resize</dd>
 * <dd>Component Enable</dd>
 * <dd>Component Disable</dd>
 * <dd>Component BeforeHide</dd>
 * <dd>Component Hide</dd>
 * <dd>Component BeforeShow</dd>
 * <dd>Component Show</dd>
 * <dd>Component Attach</dd>
 * <dd>Component Detach</dd>
 * <dd>Component BeforeRender</dd>
 * <dd>Component Render</dd>
 * <dd>Component BrowserEvent</dd>
 * <dd>Component BeforeStateRestore</dd>
 * <dd>Component StateRestore</dd>
 * <dd>Component BeforeStateSave</dd>
 * <dd>Component SaveState</dd>
 * </dl>
 */
public class ListView<M extends ModelData> extends BoxComponent {

    protected int rowSelectorDepth = 5;

    protected ListStore<M> store;
    private CompositeElement all;
    private String displayProperty = "text";
    private boolean enableQuickTip = true;
    private String itemSelector = ".x-view-item";
    private String loadingText;
    private ModelProcessor<M> modelProcessor;
    private Element overElement;
    private String overStyle = "x-view-item-over";
    private QuickTip quickTip;
    private boolean selectOnHover;
    private String selectStyle = "x-view-item-sel";
    private ListViewSelectionModel<M> sm;
    private StoreListener<M> storeListener;

    private XTemplate template;

    /**
     * Creates a new view.
     */
    public ListView() {
        initComponent();
        setSelectionModel(new ListViewSelectionModel<M>());
        all = new CompositeElement();
        baseStyle = "x-view";
        disableTextSelection(true);
    }

    /**
     * Creates a new view.
     */
    public ListView(ListStore<M> store) {
        this();
        setStore(store);
    }

    /**
     * Creates a new template list.
     *
     * @param template the template
     */
    public ListView(ListStore<M> store, XTemplate template) {
        this(store);
        this.template = template;
    }

    /**
     * Returns the matching element.
     *
     * @param element the element or any child element
     * @return the parent element
     */
    public Element findElement(Element element) {
        return fly(element).findParentElement(itemSelector, rowSelectorDepth);
    }

    /**
     * Returns the element's index.
     *
     * @param element the element or any child element
     * @return the element index or -1 if no match
     */
    public int findElementIndex(Element element) {
        Element elem = findElement(element);
        if (elem != null) {
            return indexOf(elem);
        }
        return -1;
    }

    /**
     * Returns the display property.
     *
     * @return the display property
     */
    public String getDisplayProperty() {
        return displayProperty;
    }

    /**
     * Returns the element at the given index.
     *
     * @param index the index
     * @return the element
     */
    public Element getElement(int index) {
        return all.getElement(index);
    }

    /**
     * Returns all of the child elements.
     *
     * @return the elements
     */
    public List<Element> getElements() {
        return all.getElements();
    }

    /**
     * Returns the number of models in the view.
     *
     * @return the count
     */
    public int getItemCount() {
        return store == null ? 0 : store.getCount();
    }

    /**
     * Returns the item selector.
     *
     * @return the selector
     */
    public String getItemSelector() {
        return itemSelector;
    }

    /**
     * Returns the view's loading text.
     *
     * @return the loading text
     */
    public String getLoadingText() {
        return loadingText;
    }

    /**
     * Returns the model processor.
     *
     * @return the model processor
     */
    public ModelProcessor<M> getModelProcessor() {
        return modelProcessor;
    }

    /**
     * Returns the over style.
     *
     * @return the over style
     */
    public String getOverStyle() {
        return overStyle;
    }

    /**
     * Returns the view's quick tip instance.
     *
     * @return the quicktip instance or null if not enabled
     */
    public QuickTip getQuickTip() {
        return quickTip;
    }

    /**
     * Returns the view's selection model.
     *
     * @return the selection model
     */
    public ListViewSelectionModel<M> getSelectionModel() {
        return sm;
    }

    /**
     * Returns true if select on hover is enabled.
     *
     * @return the select on hover state
     */
    public boolean getSelectOnOver() {
        return selectOnHover;
    }

    /**
     * Returns the select style.
     *
     * @return the select style
     */
    public String getSelectStyle() {
        return selectStyle;
    }

    /**
     * Returns the combo's store.
     *
     * @return the store
     */
    public ListStore<M> getStore() {
        return store;
    }

    /**
     * Returns the list's template.
     *
     * @return the template
     */
    public XTemplate getTemplate() {
        return template;
    }

    /**
     * Returns the index of the element.
     *
     * @param element the element
     * @return the index
     */
    public int indexOf(Element element) {
        if (element.getPropertyString("viewIndex") != null) {
            return element.getPropertyInt("viewIndex");
        }
        return all.indexOf(element);
    }

    /**
     * Returns true if quicktips are enabled.
     *
     * @return true for enabled
     */
    public boolean isEnableQuickTips() {
        return enableQuickTip;
    }

    /**
     * Moves the current selections down one level.
     */
    public void moveSelectedDown() {
        List<M> sel = getSelectionModel().getSelectedItems();

        Collections.sort(sel, new Comparator<M>() {
            public int compare(M o1, M o2) {
                return store.indexOf(o1) < store.indexOf(o2) ? 1 : 0;
            }
        });

        for (M m : sel) {
            int idx = store.indexOf(m);
            if (idx < (store.getCount() - 1)) {
                store.remove(m);
                store.insert(m, idx + 1);
            }
        }
        getSelectionModel().select(sel, false);
    }

    /**
     * Moves the current selections up one level.
     */
    public void moveSelectedUp() {
        List<M> sel = getSelectionModel().getSelectedItems();

        Collections.sort(sel, new Comparator<M>() {
            public int compare(M o1, M o2) {
                return store.indexOf(o1) > store.indexOf(o2) ? 1 : 0;
            }
        });

        for (M m : sel) {
            int idx = store.indexOf(m);
            if (idx > 0) {
                store.remove(m);
                store.insert(m, idx - 1);
            }
        }
        getSelectionModel().select(sel, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onComponentEvent(ComponentEvent ce) {
        super.onComponentEvent(ce);
        ListViewEvent le = (ListViewEvent<?>) ce;
        switch (ce.getEventTypeInt()) {
            case Event.ONMOUSEOVER:
                onMouseOver(le);
                break;
            case Event.ONMOUSEOUT:
                onMouseOut(le);
                break;
            case Event.ONMOUSEDOWN:
                onMouseDown(le);
                break;
            case Event.ONDBLCLICK:
                if (le.getIndex() != -1) {
                    onDoubleClick(le);
                }
                break;
            case Event.ONCLICK:
                if (le.getIndex() != -1) {
                    onClick(le);
                }
                break;
            case Event.ONFOCUS:
                onFocus(ce);
                break;
        }
    }

    /**
     * Refreshes the view by reloading the data from the store and re-rendering
     * the template.
     */
    public void refresh() {
        if (!rendered) {
            return;
        }
        el().setInnerHtml("");
        repaint();
        List<M> models = store == null ? new ArrayList<M>() : store.getModels();
        all.removeAll();
        template.overwrite(getElement(), Util.getJsObjects(collectData(models, 0), template.getMaxDepth()));
        all = new CompositeElement(Util.toElementArray(el().select(itemSelector)));
        if (GXT.isAriaEnabled()) {
            for (int i = 0; i < all.getCount(); i++) {
                all.getElement(i).setId(XDOM.getUniqueId());
            }
        }
        updateIndexes(0, -1);
        fireEvent(Events.Refresh);
    }

    /**
     * Refreshes an individual node's data from the store.
     *
     * @param index the items data index in the store
     */
    public void refreshNode(int index) {
        onUpdate(store.getAt(index), index);
    }

    /**
     * Sets the display property. Applies when using the default template for each
     * item's text.
     *
     * @param displayProperty the display property
     */
    public void setDisplayProperty(String displayProperty) {
        this.displayProperty = displayProperty;
    }

    /**
     * True to enable quicktips (defaults to true, pre-render).
     *
     * @param enableQuickTip true to enable quicktips
     */
    public void setEnableQuickTips(boolean enableQuickTip) {
        assertPreRender();
        this.enableQuickTip = enableQuickTip;
    }

    /**
     * This is a required setting. A simple CSS selector (e.g. div.some-class or
     * span:first-child) that will be used to determine what nodes this DataView
     * will be working with (defaults to 'x-view-item').
     *
     * @param itemSelector the item selector
     */
    public void setItemSelector(String itemSelector) {
        this.itemSelector = itemSelector;
    }

    /**
     * Sets the text loading text to be displayed during a load request.
     *
     * @param loadingText the loading text
     */
    public void setLoadingText(String loadingText) {
        this.loadingText = loadingText;
    }

    /**
     * Sets the view's model processor. The model processor can be used to provide
     * "formatted" properties to the XTemplate used to render the view.
     *
     * @see ModelProcessor
     * @param modelProcessor
     */
    public void setModelProcessor(ModelProcessor<M> modelProcessor) {
        this.modelProcessor = modelProcessor;
    }

    /**
     * Sets the style name to apply on mouse over.
     *
     * @param overStyle the over style
     */
    public void setOverStyle(String overStyle) {
        this.overStyle = overStyle;
    }

    /**
     * Sets the selection model.
     *
     * @param sm the selection model
     */
    public void setSelectionModel(ListViewSelectionModel<M> sm) {
        if (this.sm != null) {
            this.sm.bindList(null);
        }
        this.sm = sm;
        if (sm != null) {
            sm.bindList(this);
        }
    }

    /**
     * True to select the item when mousing over a element (defaults to false).
     *
     * @param selectOnHover true to select on mouse over
     */
    public void setSelectOnOver(boolean selectOnHover) {
        this.selectOnHover = selectOnHover;
    }

    /**
     * The style to be applied to each selected item (defaults to
     * 'x-view-item-sel').
     *
     * @param selectStyle the select style
     */
    public void setSelectStyle(String selectStyle) {
        this.selectStyle = selectStyle;
    }

    /**
     * Sets the template fragment to be used for the text of each listview item.
     *
     * <pre>
     * &lt;code&gt;
     * listview.setSimpleTemplate(&quot;{abbr} {name}&quot;);
     * &lt;/code&gt;
     * </pre>
     *
     * @param html the html used only for the text of each item in the list
     */
    public void setSimpleTemplate(String html) {
        assertPreRender();
        html = "<tpl for=\".\"><div class=x-view-item>" + html + "</div></tpl>";
        template = XTemplate.create(html);
    }

    /**
     * Changes the data store bound to this view and refreshes it.
     *
     * @param store the store to bind this view
     */
    public void setStore(ListStore<M> store) {
        if (this.store != null) {
            this.store.removeStoreListener(storeListener);
        }
        if (store != null) {
            store.addStoreListener(storeListener);
        }
        this.store = store;
        sm.bindList(this);

        if (store != null && isRendered()) {
            refresh();
        }
    }

    /**
     * Sets the view's template.
     *
     * @param html the HTML fragment
     */
    public void setTemplate(String html) {
        setTemplate(XTemplate.create(html));
    }

    /**
     * Sets the view's template.
     *
     * @param template the template
     */
    public void setTemplate(XTemplate template) {
        this.template = template;
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        refresh();
    }

    protected List<M> collectData(List<M> models, int startIndex) {
        List<M> list = new ArrayList<M>();
        for (int i = 0, len = models.size(); i < len; i++) {
            list.add(prepareData(models.get(i)));
        }
        return list;
    }

    @Override
    protected ComponentEvent createComponentEvent(Event event) {
        return new ListViewEvent<M>(this, event);
    }

    protected void focusItem(int index) {
        Element elem = getElement(index);
        if (elem != null) {
            fly(elem).scrollIntoView(getElement(), false);
        }
        focus();
    }

    protected void initComponent() {
        storeListener = new StoreListener<M>() {
            @Override
            public void storeAdd(StoreEvent<M> se) {
                onAdd(se.getModels(), se.getIndex());
            }

            @Override
            public void storeBeforeDataChanged(StoreEvent<M> se) {
                onBeforeLoad();
            }

            @Override
            public void storeClear(StoreEvent<M> se) {
                refresh();
            }

            @Override
            public void storeDataChanged(StoreEvent<M> se) {
                refresh();
            }

            @Override
            public void storeFilter(StoreEvent<M> se) {
                refresh();
            }

            @Override
            public void storeRemove(StoreEvent<M> se) {
                onRemove(se.getModel(), se.getIndex());
            }

            @Override
            public void storeSort(StoreEvent<M> se) {
                refresh();
            }

            @Override
            public void storeUpdate(StoreEvent<M> se) {
                int index = store.indexOf(se.getModel());
                onUpdate(se.getModel(), index);
            }

        };
    }

    protected void onAdd(List<M> models, int index) {
        if (rendered) {
            if (all.getCount() == 0) {
                refresh();
                return;
            }
            NodeList<Element> nodes = bufferRender(models);
            Element[] elements = Util.toElementArray(nodes);
            all.insert(elements, index);

            Element ref = index == 0 ? all.getElement(elements.length) : all.getElement(index - 1);

            for (int i = elements.length - 1; i >= 0; i--) {
                Node n = ref.getParentNode();
                if (index == 0) {
                    n.insertBefore(elements[i], n.getFirstChild());
                } else {
                    Node next = ref == null ? null : ref.getNextSibling();
                    if (next == null) {
                        n.appendChild(elements[i]);
                    } else {
                        n.insertBefore(elements[i], next);
                    }
                }
                if (GXT.isAriaEnabled()) {
                    elements[i].setId(XDOM.getUniqueId());
                }
            }

            updateIndexes(index, -1);
        }
    }

    protected void onBeforeLoad() {
        if (loadingText != null) {
            if (rendered) {
                el().setInnerHtml("<div class='loading-indicator'>" + loadingText + "</div>");
            }
            all.removeAll();
        }
    }

    protected void onClick(ListViewEvent<M> e) {

    }

    protected void onDoubleClick(ListViewEvent<M> e) {
        fireEvent(Events.DoubleClick, e);
    }

    protected void onFocus(ComponentEvent ce) {
        FocusFrame.get().frame(this);
    }

    protected void onHighlightRow(int index, boolean highLight) {
        Element e = getElement(index);
        if (e != null) {
            fly(e).setStyleName("x-view-highlightrow", highLight);
            if (highLight && GXT.isAriaEnabled()) {
                setAriaState("aria-activedescendant", e.getId());
            }
        }
    }

    protected void onMouseDown(ListViewEvent<M> e) {
        if (e.getIndex() != -1) {
            fireEvent(Events.Select, e);
        }
    }

    protected void onMouseOut(ListViewEvent<M> ce) {
        if (overElement != null) {
            if (!ce.within(overElement, true)) {
                fly(overElement).removeStyleName(overStyle);
                overElement = null;
            }
        }
    }

    protected void onMouseOver(ListViewEvent<M> ce) {
        if (ce.getIndex() != -1) {
            if (selectOnHover) {
                sm.select(ce.getIndex(), false);
            } else {
                Element e = all.getElement(ce.getIndex());
                if (e != null && e != overElement) {
                    fly(e).addStyleName(overStyle);
                    overElement = e;
                }
            }
        }
    }

    protected void onRemove(M data, int index) {
        if (all != null) {
            Element e = getElement(index);
            if (e != null) {
                fly(e).removeStyleName(overStyle);
                if (overElement == e) {
                    overElement = null;
                }

                getSelectionModel().deselect(index);

                fly(e).removeFromParent();
                all.remove(index);
                updateIndexes(index, -1);
            }
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        setElement(DOM.createDiv(), target, index);

        el().setTabIndex(0);
        el().setElementAttribute("hideFocus", "true");

        String aria = GXT.isAriaEnabled() ? " role='option' aria-selected='false' " : "";

        if (template == null) {
            template = XTemplate.create("<tpl for=\".\"><div class='x-view-item' " + aria + ">{" + displayProperty
                    + "}</div></tpl>");
        }

        if (enableQuickTip) {
            quickTip = new QuickTip(this);
        }

        if (GXT.isAriaEnabled()) {
            setAriaRole("listbox");
            SelectionMode mode = getSelectionModel().getSelectionMode();
            setAriaState("aria-multiselectable", mode != SelectionMode.SINGLE ? "true" : "false");
            setAriaRole("listbox");
        }

        sinkEvents(Event.ONCLICK | Event.ONDBLCLICK | Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONKEYDOWN);
    }

    protected void onSelectChange(M model, boolean select) {
        if (rendered && all != null) {
            int index = store.indexOf(model);
            if (index != -1 && index < all.getCount()) {
                Element e = all.getElement(index);
                fly(e).setStyleName(selectStyle, select);
                fly(e).removeStyleName(overStyle);
                if (GXT.isAriaEnabled()) {
                    e.setAttribute("aria-selected", "" + select);
                    if (select) {
                        setAriaState("aria-activedescendant", e.getId());
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void onUpdate(M model, int index) {
        if (rendered) {
            Element original = all.getElement(index);
            if (original != null) {
                List list = Util.createList(model);
                Element node = (Element) bufferRender(list).getItem(0);
                all.replaceElement(original, node);
                original.getParentElement().replaceChild(node, original);
            }
            ListViewEvent<M> evt = new ListViewEvent<M>(this);
            evt.setModel(model);
            evt.setIndex(index);
            fireEvent(Events.RowUpdated, evt);
        }
    }

    protected M prepareData(M model) {
        if (modelProcessor != null) {
            boolean silent = false;
            if (model instanceof BaseModel) {
                silent = ((BaseModel) model).isSilent();
                ((BaseModel) model).setSilent(true);
            }

            M m = modelProcessor.prepareData(model);

            if (model instanceof BaseModel) {
                ((BaseModel) model).setSilent(silent);
            }

            return m;
        }
        return model;
    }

    private NodeList<Element> bufferRender(List<M> models) {
        Element div = DOM.createDiv();
        template.overwrite(div, Util.getJsObjects(collectData(models, 0), template.getMaxDepth()));
        return DomQuery.select(itemSelector, div);
    }

    private void updateIndexes(int startIndex, int endIndex) {
        List<Element> elems = all.getElements();
        endIndex = endIndex == -1 ? elems.size() - 1 : endIndex;
        for (int i = startIndex; i <= endIndex; i++) {
            elems.get(i).setPropertyInt("viewIndex", i);
        }
    }

}
