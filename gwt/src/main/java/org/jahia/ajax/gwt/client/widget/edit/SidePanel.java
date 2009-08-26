package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ActionIconsImageBundle;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: romain
 * Date: Aug 19, 2009
 * Time: 11:56:07 AM
 * <p/>
 * This is the content browser / editor side panel.
 */
public class SidePanel extends ContentPanel {

    public static final ActionIconsImageBundle ACTION_ICONS = GWT.create(ActionIconsImageBundle.class);

    private boolean init = false;
    private ListStore<GWTJahiaNode> displayStore;
    private ListStore<GWTJahiaNodeType> displayTypesStore;
    private final PreviewTabItem previewTabItem;
    private final TabItem propertiesTabItem;
    private ContentPanel contentList;
    private Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> definitions;
    ListStore<GWTJahiaBasicDataBean> templateListStore;
    private Grid<GWTJahiaNode> displayGrid;
    private Grid<GWTJahiaNodeType> displayTypesGrid;
    private final EditManager editManager;

    public SidePanel(final EditManager editManager) {
        super();
        this.editManager = editManager;
        final EditLinker editLinker = editManager.getEditLinker();
        setHeaderVisible(true);
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(layout);
        ToolBar topToolbar = new ToolBar();
        Button createPage = new Button();
        createPage.setIcon(ACTION_ICONS.createPage());
        createPage.setToolTip("create New Page");
        createPage.addSelectionListener(editManager.getEditLinker().getCreatePageButtonListener(createPage));
        createPage.setEnabled(false);
        topToolbar.add(createPage);
        VBoxLayout vBoxLayout = new VBoxLayout();
        vBoxLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
//        ContentPanel panel = new ContentPanel(vBoxLayout);
//        panel.add(topToolbar);
        ContentPanel repository = getRepositoryPanel(editManager, editLinker);
        getContentListPanel(editManager, editLinker);

        ToolBar toolbar = getToolbar();
        // displayPanel panel
        ContentPanel displayPanel = new ContentPanel();
        displayPanel.setHeading("Display");
        vBoxLayout = new VBoxLayout();
        vBoxLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        displayPanel.setLayout(vBoxLayout);
        displayPanel.setCollapsible(true);
        displayPanel.add(toolbar);
        TabPanel displayTabs = new TabPanel();

        previewTabItem = new PreviewTabItem("Preview");
        previewTabItem.setLayout(new FitLayout());
        final PreviewDragSource source = new PreviewDragSource(previewTabItem);
        source.addDNDListener(editManager.getDndListener());
        editLinker.setPreviewTabItem(previewTabItem);
        displayTabs.add(previewTabItem);
        propertiesTabItem = new TabItem("Properties");
        propertiesTabItem.setLayout(new FitLayout());
        displayTabs.add(previewTabItem);
        displayTabs.add(propertiesTabItem);
        editLinker.setPropertiesTabItem(propertiesTabItem);
        displayPanel.add(displayTabs);

        // add to side panel
        VBoxLayoutData vBoxData = new VBoxLayoutData(5, 5, 5, 5);
        vBoxData.setFlex(1);
        add(topToolbar);
        add(repository, vBoxData);
        add(contentList, vBoxData);
        add(displayPanel, vBoxData);

    }

    private void getContentListPanel(EditManager editManager, final EditLinker editLinker) {
        // content list panel
        contentList = new ContentPanel();
        contentList.setHeading("Content list");
        contentList.setLayout(new FitLayout());
        contentList.setCollapsible(true);
        displayStore = new ListStore<GWTJahiaNode>();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("ext", Messages.getResource("fm_column_type"), 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("name", "Name", 250));
//        displayColumns.add(new ColumnConfig("path", "Path", 200));
        displayGrid = new Grid<GWTJahiaNode>(displayStore, new ColumnModel(displayColumns));
        displayGrid.setBorders(false);
        displayGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        displayGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                editLinker.onDisplayGridSelection(gwtJahiaNodeSelectionChangedEvent.getSelectedItem());
            }
        });
        GridDragSource displayGridSource = new DisplayGridDragSource(displayGrid);
        displayGridSource.addDNDListener(editManager.getDndListener());
        editLinker.setDisplayGrid(displayGrid);
        contentList.add(displayGrid);


        displayTypesStore = new ListStore<GWTJahiaNodeType>();

        displayColumns = new ArrayList<ColumnConfig>();
        col = new ColumnConfig("ext", Messages.getResource("fm_column_type"), 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNodeType>() {
            public Object render(GWTJahiaNodeType model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaNodeType> gwtJahiaNodeTypeListStore, Grid<GWTJahiaNodeType> gwtJahiaNodeTypeGrid) {
                return ContentModelIconProvider.getInstance().getIcon(model.getIcon()).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("name", "Name", 250));

        displayTypesGrid = new Grid<GWTJahiaNodeType>(displayTypesStore, new ColumnModel(displayColumns));
        displayTypesGrid.setBorders(false);
        displayTypesGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        GridDragSource createGridSource = new CreateGridDragSource(displayTypesGrid);
        createGridSource.addDNDListener(editManager.getDndListener());
        editLinker.setDisplayTypesGrid(displayTypesGrid);
    }

    private ContentPanel getRepositoryPanel(EditManager editManager, final EditLinker editLinker) {
        // repository panel
        ContentPanel repository = new ContentPanel();
        repository.setHeading("Repository");
        repository.setLayout(new FitLayout());
        repository.setBorders(false);
        repository.setBodyBorder(false);
        repository.getHeader().setBorders(false);
        repository.setCollapsible(true);

        TabPanel repositoryTabs = new TabPanel();

        // creating
        TabItem create = new TabItem("Create");
        create.setLayout(new FitLayout());
        final ListStore<GWTJahiaNodeType> createStore = new ListStore<GWTJahiaNodeType>();
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
            public void onFailure(Throwable caught) {
                MessageBox.alert("Alert", "Unable to load content definitions. Cause: "
                                          + caught.getLocalizedMessage(), null);
            }

            public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                definitions = result;
                List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>(result.keySet());
                createStore.add(list);
            }

        });
        List<ColumnConfig> createColumns = new ArrayList<ColumnConfig>();
        createColumns.add(new ColumnConfig("name", "Name", 150));
        createColumns.add(new ColumnConfig("label", "Label", 150));

        ListView<GWTJahiaNodeType> createView = new ListView<GWTJahiaNodeType>();
        createView.setTemplate(getTemplate());
        createView.setStore(createStore);
        createView.setItemSelector("div.thumb-wrap");
        createView.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        createView.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeType>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeType> gwtJahiaNodeSelectionChangedEvent) {
                GWTJahiaNodeType selected = gwtJahiaNodeSelectionChangedEvent.getSelectedItem();
                if (selected != null) {
                    List<GWTJahiaNodeType> list = definitions.get(selected);
                    setDisplayTypesContent(list);
                } else {
                    setDisplayTypesContent(null);
                }
                editLinker.onCreateGridSelection(selected);
            }
        });
        editLinker.setCreateView(createView);
        create.add(createView);

        // browsing
        TabItem browse = new TabItem("Browse");
        browse.setLayout(new FitLayout());
        // data proxy
        RpcProxy<List<GWTJahiaNode>> privateProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    JahiaContentManagementService.App.getInstance().getRoot(JCRClientUtils.GLOBAL_REPOSITORY, "", "", "", null, listAsyncCallback);
                } else {
                    JahiaContentManagementService.App.getInstance().ls((GWTJahiaNode) gwtJahiaFolder, "", "", "", null, false, listAsyncCallback);
                }
            }
        };
        TreeLoader<GWTJahiaNode> loader = new BaseTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren();
            }

            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false");
                    init = false;
                }
            }
        };
        TreeStore<GWTJahiaNode> treeStore = new TreeStore<GWTJahiaNode>(loader);
        TreePanel<GWTJahiaNode> m_tree = new TreePanel<GWTJahiaNode>(treeStore);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        m_tree.setDisplayProperty("displayName");
        m_tree.setBorders(false);
        editLinker.setBrowseTree(m_tree);
        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                GWTJahiaNode selected = gwtJahiaNodeSelectionChangedEvent.getSelectedItem();
                if (selected != null) {
                    if (selected.hasChildren()) {
                        JahiaContentManagementService.App.getInstance().ls(gwtJahiaNodeSelectionChangedEvent.getSelectedItem(), null, null, null, null, false, new AsyncCallback<List<GWTJahiaNode>>() {
                            public void onFailure(Throwable throwable) {
                                // TODO
                            }

                            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                                setDisplayContent(gwtJahiaNodes);
                            }
                        });
                    } else {
                        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>(1);
                        list.add(selected);
                        setDisplayContent(list);
                    }
                } else {
                    setDisplayContent(null);
                }
                editLinker.onBrowseTreeSelection(selected);
            }
        });
        TreePanelDragSource contentTreeSource = new ContentTreeDragSource(m_tree);
        contentTreeSource.addDNDListener(editManager.getDndListener());
        browse.add(m_tree);

        // searching
        TabItem search = new TabItem("Search");
        FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        final TextField<String> searchField = new TextField<String>();
        searchField.setFieldLabel("Search for");
        final CheckBox tags = new CheckBox();
        tags.setFieldLabel("Within");
        tags.setBoxLabel("tag");
        final CheckBox nameDesc = new CheckBox();
        nameDesc.setBoxLabel("name and description");
        final CheckBox props = new CheckBox();
        props.setBoxLabel("all properties");
        final CheckBox content = new CheckBox();
        content.setBoxLabel("file content");
        final DateField date = new DateField();
        date.setFieldLabel("Newer than");
        final TextField<String> searchRoot = new TextField<String>();
        searchRoot.setFieldLabel("Search in");
        Button ok = new Button("Search", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                search(searchField.getValue(), date.getValue(), searchRoot.getValue());
            }
        });
        searchForm.add(searchField);
        searchForm.add(tags);
        searchForm.add(nameDesc);
        searchForm.add(props);
        searchForm.add(content);
        searchForm.add(date);
        searchForm.add(searchRoot);
        searchForm.add(ok);
        search.add(searchForm);

        repositoryTabs.add(create);
        repositoryTabs.add(browse);
        repositoryTabs.add(search);
        repository.add(repositoryTabs);
        return repository;
    }

    private ToolBar getToolbar() {
        ToolBar toolbar = new ToolBar();
        templateListStore = new ListStore<GWTJahiaBasicDataBean>();
        ComboBox<GWTJahiaBasicDataBean> templateBox = new ComboBox<GWTJahiaBasicDataBean>();
        templateBox.setStore(templateListStore);
        templateBox.setDisplayField(GWTJahiaBasicDataBean.DISPLAY_NAME);
        templateBox.clearSelections();
        templateBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaBasicDataBean>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaBasicDataBean> gwtJahiaNodeSelectionChangedEvent) {
                editManager.getEditLinker().onTemplateBoxSelection(gwtJahiaNodeSelectionChangedEvent.getSelectedItem());
            }
        });
        /*templateListStore.add(new GWTJahiaBasicDataBean("template1", "template1"));
        templateListStore.add(new GWTJahiaBasicDataBean("template2", "template2"));
        templateListStore.add(new GWTJahiaBasicDataBean("template3", "template3"));*/
        editManager.getEditLinker().setTemplateBox(templateBox);
        Button save = new Button();
        save.setIcon(ACTION_ICONS.save());
        save.setToolTip("save");
        save.addSelectionListener(editManager.getEditLinker().getSaveButtonListener(save));
        save.setEnabled(false);
        Button lock = new Button();
        lock.setIcon(ACTION_ICONS.lock());
        lock.setToolTip("lock");
        lock.addSelectionListener(editManager.getEditLinker().getLockButtonListener(lock));
        lock.setEnabled(false);
        Button edit = new Button();
        edit.setIcon(ACTION_ICONS.edit());
        edit.setToolTip("edit");
        edit.addSelectionListener(editManager.getEditLinker().getEditButtonListener(edit));
        edit.setEnabled(false);
        Button delete = new Button();
        delete.setIcon(ACTION_ICONS.delete());
        delete.addSelectionListener(editManager.getEditLinker().getDeleteButtonListener(delete));
        delete.setToolTip("delete");
        delete.setEnabled(false);
        toolbar.add(templateBox);
        toolbar.add(save);
        toolbar.add(lock);
        toolbar.add(edit);
        toolbar.add(delete);
        return toolbar;
    }

    /**
     * Method used by the search form
     *
     * @param query      the query string
     * @param date       search for items newer than date
     * @param searchRoot search within this path
     */
    private void search(String query, Date date, String searchRoot) {
        JahiaContentManagementService.App.getInstance().search(query, 500, new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable throwable) {
                // TODO
            }

            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                setDisplayContent(gwtJahiaNodes);
            }
        });
    }

    /**
     * Clear the display list and add the provided list.
     *
     * @param content items to add
     */
    private void setDisplayContent(List<GWTJahiaNode> content) {
        contentList.removeAll();
        displayStore.removeAll();
        if (content != null) {
            displayStore.add(content);
        }
        contentList.add(displayGrid);
        contentList.layout();
    }

    private void setDisplayTypesContent(List<GWTJahiaNodeType> content) {
        contentList.removeAll();
        displayTypesStore.removeAll();
        if (content != null) {
            displayTypesStore.add(content);
        }
        contentList.add(displayTypesGrid);
        contentList.layout();
    }

    private native String getTemplate() /*-{
    return ['<tpl for=".">',
        '<div class="thumb-wrap" id="{name}" style="border: 1px solid white">',
        '<div class="thumb"><img src="{icon}" title="{label}"></div>',
        '<span class="x-editable">{label}</span></div>',
        '</tpl>',
        '<div class="x-clear"></div>'].join("");

}-*/;

    public class PreviewDragSource extends EditModeDragSource {
        private final PreviewTabItem previewTabItem;

        public PreviewDragSource(PreviewTabItem previewTabItem) {
            super(previewTabItem);
            this.previewTabItem = previewTabItem;
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.setCancelled(false);
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
            nodes.add(previewTabItem.getNode());
            e.setData(nodes);
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);

            List<GWTJahiaNode> list = (List<GWTJahiaNode>) e.getData();
            e.getStatus().setData("size", list.size());

            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
            if (getStatusText() == null) {
                e.getStatus().update(DOM.clone(previewTabItem.getWidget(0).getElement(), true));
            }
            super.onDragStart(e);
        }
    }

    public class PreviewTabItem extends TabItem {
        HTML html;
        GWTJahiaNode node;

        public PreviewTabItem(String s) {
            super(s);
            sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
            addListener(Events.OnDoubleClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    if (html != null && node != null) {
                        Window w = new Window();
                        final String text = "Preview of " + node.getPath();
                        w.setHeading(text);
                        w.setScrollMode(Style.Scroll.AUTO);
                        w.setModal(true);
                        w.setClosable(true);
                        w.setSize(800, 600);
                        w.setBlinkModal(true);
                        w.setPlain(true);
                        w.setToolTip(text);
                        w.add(new HTML(html.getHTML()));
                        w.show();                        
                    }
                }
            });
        }

        public HTML getHtml() {
            return html;
        }

        public void setHtml(HTML html) {
            this.html = html;
        }

        public GWTJahiaNode getNode() {
            return node;
        }

        public void setNode(GWTJahiaNode node) {
            this.node = node;
        }
    }
}


