/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.subscription;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionService;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a widget for managing e.g. newsletter subscriptions.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionManager extends LayoutContainer {

	private class ImportWindow extends Window {
		public ImportWindow() {
			super();
			setHeading(Messages.get("label.import", "Import"));
			setSize(400, 150);
			setResizable(true);
			setModal(true);
		}

		@Override
		protected void onRender(Element parent, int pos) {
			super.onRender(parent, pos);
			setStyleAttribute("margin", "10px");

			final FormPanel panel = new FormPanel();
			panel.setHeaderVisible(false);
			panel.setFrame(false);
			panel.setBorders(false);
			panel.setAction((JahiaGWTParameters.getServiceEntryPoint() != null ? JahiaGWTParameters
			        .getServiceEntryPoint() : "/gwt/") + "fileupload");
			panel.setEncoding(Encoding.MULTIPART);
			panel.setMethod(Method.POST);
			panel.setButtonAlign(HorizontalAlignment.CENTER);
			panel.setLabelWidth(75);
			panel.setWidth(390);
			panel.setHeight(80);

			final FileUpload file = new FileUpload();
			file.setName("asyncupload");
			file.setWidth("300px");
			AdapterField adapter = new AdapterField(file);
			adapter.setFieldLabel(Messages.get("fileMenu.label", "File"));
			panel.add(adapter);

			final Window theWindor = this;
			Button btn = new Button(Messages.get("label.cancel", "Cancel"));
			btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					theWindor.hide();
				}
			});
			panel.addButton(btn);

			btn = new Button(Messages.get("label.doImport", "Import"));
			btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					if (file.getFilename() == null || file.getFilename().trim().length() == 0) {
						return;
					}
					try {
						panel.submit();
					} catch (Exception e) {
						panel.unmask();
						MessageBox.alert(Messages.get("label.error", "Error"), e.getMessage(), null);
					}
				}
			});

			panel.addButton(btn);

			panel.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
				public void handleEvent(FormEvent formEvent) {
					panel.mask(Messages.get("label.loading", "Loading..."));
				}
			});
			panel.addListener(Events.Submit, new Listener<FormEvent>() {
				public void handleEvent(FormEvent formEvent) {
					String result = formEvent.getResultHtml();
					String fileKey = null;
					if (result != null && result.contains(" key=\"")) {
						result = result.substring(result.indexOf(" key=\"") + " key=\"".length());
						fileKey = result.indexOf("\"") != -1 ? result.substring(0,
						        result.indexOf("\"")) : null;
					}
					if (fileKey != null) {
						doImport(theWindor, fileKey);
					} else {
						MessageBox.alert(Messages.get("label.error", "Error"),
						        Messages.get("failure.upload", "Upload of the file failed"), null);
					}
				}
			});

			add(panel);
		}
	}

	private abstract class SubscriptionAsyncCallback<T> implements AsyncCallback<T> {
		public void onFailure(Throwable caught) {
			MessageBox.alert(Messages.get("label.error", "Error"), caught.getMessage(), null);
			loader.load();
		}
	}

	private static final int ITEMS_PER_PAGE = 20;

	private Button btnRemove;

	private Button btnResume;

	private Button btnSuspend;

	private Grid<GWTSubscription> grid;

	private BasePagingLoader<PagingLoadResult<GWTSubscription>> loader;

	private SubscriptionServiceAsync service;

	private String source;

    private Linker linker;

    private EngineContainer container;

	/**
	 * Initializes an instance of this class.
	 * 
	 */
	public SubscriptionManager(String nodeIdentifier, Linker linker, EngineContainer engineContainer) {
		super();
		this.source = nodeIdentifier;
		this.linker = linker;
		setLayout(new FitLayout());

        this.container = engineContainer;

        container.setEngine(this, Messages.get("label.subscriptionManager", "Subscription Manager"), null, linker);
	}

	private BasePagingLoader<PagingLoadResult<GWTSubscription>> createDataLoader() {
		// data proxy
		RpcProxy<PagingLoadResult<GWTSubscription>> proxy = new RpcProxy<PagingLoadResult<GWTSubscription>>() {

			@Override
			protected void load(Object loadConfig,
			        AsyncCallback<PagingLoadResult<GWTSubscription>> callback) {
				service.getSubscriptions(source, (PagingLoadConfig) loadConfig, callback);
			}
		};

		final BasePagingLoader<PagingLoadResult<GWTSubscription>> loader = new BasePagingLoader<PagingLoadResult<GWTSubscription>>(
		        proxy);
		loader.setRemoteSort(true);
		loader.setSortField("subscriber");
		loader.setSortDir(SortDir.ASC);
		loader.setLimit(ITEMS_PER_PAGE);

		return loader;
	}
	
	private Grid<GWTSubscription> createGrid(BasePagingLoader<PagingLoadResult<GWTSubscription>> loader) {
		ListStore<GWTSubscription> store = new ListStore<GWTSubscription>(loader);

		final CheckBoxSelectionModel<GWTSubscription> sm = new CheckBoxSelectionModel<GWTSubscription>();
		sm.addSelectionChangedListener(new SelectionChangedListener<GWTSubscription>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<GWTSubscription> se) {
				updateToolbar(se.getSelection());
			}
		});

		final String msgYes = Messages.get("label.yes", "yes").toLowerCase();
		final String msgNo = Messages.get("label.no", "no").toLowerCase();

		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

		columns.add(sm.getColumn());

		ColumnConfig column = new ColumnConfig();
		column.setId("id");
		column.setHidden(true);
		columns.add(column);

		columns.add(new ColumnConfig("subscriber", Messages.get("label.subscriber", "Subscriber"), 140));
		column = new ColumnConfig("lastName", Messages.get("org.jahia.admin.lastName.label", "Last name"), 140);
		column.setSortable(false);
		columns.add(column);

		column = new ColumnConfig("firstName", Messages.get("org.jahia.admin.firstName.label", "First name"), 140);
		column.setSortable(false);
		columns.add(column);

		column = new ColumnConfig("email", Messages.get("label.email", "Email"), 200);
		column.setSortable(false);
		columns.add(column);

		column = new ColumnConfig("provider", Messages.get("column.provider.label",
		        "Provider"), 70);
		column.setAlignment(HorizontalAlignment.CENTER);
		columns.add(column);

		column = new ColumnConfig("confirmed", Messages.get("label.confirmed", "Confirmed"), 60);
		column.setRenderer(new GridCellRenderer<GWTSubscription>() {
			public Object render(GWTSubscription model, String property, ColumnData config,
			        int rowIndex, int colIndex, ListStore<GWTSubscription> store, Grid<GWTSubscription> grid) {
				return (Boolean) model.get(property) ? msgYes : "<span style='color:red'>" + msgNo
				        + "</span>";
			}
		});
		column.setAlignment(HorizontalAlignment.CENTER);
		columns.add(column);

		column = new ColumnConfig("suspended", Messages.get("label.suspended", "Suspended"), 80);
		column.setRenderer(new GridCellRenderer<GWTSubscription>() {
			public Object render(GWTSubscription model, String property, ColumnData config,
			        int rowIndex, int colIndex, ListStore<GWTSubscription> store, Grid<GWTSubscription> grid) {
				return (Boolean) model.get(property) ? "<span style='color:red'>" + msgYes
				        + "</span>" : msgNo;
			}
		});
		column.setAlignment(HorizontalAlignment.CENTER);
		columns.add(column);

		Grid<GWTSubscription> grid = new Grid<GWTSubscription>(store, new ColumnModel(columns));
		grid.setLoadMask(true);
		grid.setBorders(true);
		grid.setSelectionModel(sm);
		grid.addPlugin(sm);

		return grid;
	}

	private ToolBar createTopToolBar(final BasePagingLoader<PagingLoadResult<GWTSubscription>> loader) {
		final ToolBar toolBar = new ToolBar();

		Button btn = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				doAdd();
			}
		});
		btn.setIcon(ToolbarIconProvider.getInstance().getIcon("newAction"));
		toolBar.add(btn);

		btnRemove = new Button(Messages.get("label.remove", "Remove"),
		        new SelectionListener<ButtonEvent>() {
			        @Override
			        public void componentSelected(ButtonEvent ce) {
				        MessageBox.confirm(Messages.get("label.remove", "Remove"), Messages.getWithArgs(
				                "message.subscriptions.removeConfirm",
				                "Do you really want to permanemtly remove {0} subscriptions?",
				                new Object[] { String.valueOf(grid.getSelectionModel()
				                        .getSelectedItems().size()) }),
				                new Listener<MessageBoxEvent>() {
					                public void handleEvent(MessageBoxEvent be) {
						                if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked()
						                        .getText())) {
							                doRemove();
						                }
					                }
				                });
			        }
		        });
		btnRemove.setIcon(ToolbarIconProvider.getInstance().getIcon("delete"));
		btnRemove.setEnabled(false);
		toolBar.add(btnRemove);

		toolBar.add(new SeparatorToolItem());

		btnSuspend = new Button(Messages.get("label.suspend", "Suspend"),
		        new SelectionListener<ButtonEvent>() {
			        @Override
			        public void componentSelected(ButtonEvent ce) {
				        MessageBox.confirm(Messages.get("label.suspend", "Suspend"), Messages
				                .getWithArgs(
				                        "message.subscriptions.suspendConfirm",
				                        "Do you really want to suspend {0} subscription(s)?",
				                        new Object[] { String.valueOf(grid.getSelectionModel()
				                                .getSelectedItems().size()) }),
				                new Listener<MessageBoxEvent>() {
					                public void handleEvent(MessageBoxEvent be) {
						                if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked()
						                        .getText())) {
							                doSuspend();
						                }
					                }
				                });
			        }
		        });
		btnSuspend.setIcon(ToolbarIconProvider.getInstance().getIcon("suspend"));
		btnSuspend.setEnabled(false);
		toolBar.add(btnSuspend);

		btnResume = new Button(Messages.get("label.resume", "Resume"),
		        new SelectionListener<ButtonEvent>() {
			        @Override
			        public void componentSelected(ButtonEvent ce) {
				        MessageBox.confirm(Messages.get("label.resume", "Resume"), Messages.getWithArgs(
				                "message.subscriptions.resumeConfirm",
				                "Do you really want to resume {0} subscription(s)?",
				                new Object[] { String.valueOf(grid.getSelectionModel()
				                        .getSelectedItems().size()) }),
				                new Listener<MessageBoxEvent>() {
					                public void handleEvent(MessageBoxEvent be) {
						                if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked()
						                        .getText())) {
							                doResume();
						                }
					                }
				                });
			        }
		        });

		btnResume.setIcon(ToolbarIconProvider.getInstance().getIcon("resume"));
		btnResume.setEnabled(false);
		toolBar.add(btnResume);

		toolBar.add(new SeparatorToolItem());

		btn = new Button(Messages.get("label.import", "Import"), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				new ImportWindow().show();
			}
		});
		btn.setIcon(ToolbarIconProvider.getInstance().getIcon("import"));
		toolBar.add(btn);

		toolBar.add(new SeparatorToolItem());

		return toolBar;
	}

	private void doAdd() {
		new UserGroupSelect(new UserGroupAdder() {
			public void addGroups(List<GWTJahiaGroup> groups) {
				// do nothing
			}

			public void addUsers(List<GWTJahiaUser> users) {
				service.subscribe(source, users, new SubscriptionAsyncCallback<Void>() {
					public void onSuccess(Void result) {
						loader.load();
					}
				});
			}
		}, UserGroupSelect.VIEW_USERS, "currentSite");
	}

	private void doImport(final Window window, String fileKey) {
		service.subscribe(source, fileKey, new SubscriptionAsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				window.hide();
				super.onFailure(caught);
			}

			public void onSuccess(Void result) {
				window.hide();
				MessageBox.info(Messages.get("label.import", "Import"),
				        Messages.get("message.success", "Operation successfully completed"), null);
				loader.load();
			}
		});
	}

	private void doRemove() {
		final List<GWTSubscription> toRemove = grid.getSelectionModel().getSelectedItems();
		service.cancel(toRemove, new SubscriptionAsyncCallback<Void>() {
			public void onSuccess(Void result) {
				for (GWTSubscription subscriber : toRemove) {
					grid.getStore().remove(subscriber);
				}
				loader.load();
			}
		});
	}

	private void doResume() {
		final List<GWTSubscription> toResume = grid.getSelectionModel().getSelectedItems();
		service.resume(toResume, new SubscriptionAsyncCallback<Void>() {
			public void onSuccess(Void result) {
				for (GWTSubscription subscriber : toResume) {
					if (subscriber.isSuspended()) {
						subscriber.setSuspended(false);
						grid.getStore().update(subscriber);
					}
				}
				updateToolbar(grid.getSelectionModel().getSelectedItems());
			}
		});
	}

	private void doSuspend() {
		final List<GWTSubscription> toSuspend = grid.getSelectionModel().getSelectedItems();
		service.suspend(toSuspend, new SubscriptionAsyncCallback<Void>() {
			public void onSuccess(Void result) {
				for (GWTSubscription subscriber : toSuspend) {
					if (!subscriber.isSuspended()) {
						subscriber.setSuspended(true);
						grid.getStore().update(subscriber);
					}
				}
				updateToolbar(grid.getSelectionModel().getSelectedItems());
			}
		});
	}

	@Override
	protected void onRender(Element parent, int pos) {
	    super.onRender(parent, pos);

		service = SubscriptionService.App.getInstance();

		loader = createDataLoader();
		loader.load();

		// main component
		grid = createGrid(loader);
        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setFrame(true);
        panel.setHeaderVisible(false);
        panel.setCollapsible(false);
        panel.add(grid, new FitData());

		// top toolbar
		panel.setTopComponent(createTopToolBar(loader));

		// bottom toolbar
		final PagingToolBar bottomToolBar = new PagingToolBar(ITEMS_PER_PAGE);
		bottomToolBar.bind(loader);
		panel.setBottomComponent(bottomToolBar);
		
        Button cancel = new Button(Messages.get("label.close", "Close"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                container.closeEngine();
            }
        });
        panel.addButton(cancel) ;
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
		
        add(panel);
	}

	private void updateToolbar(List<GWTSubscription> currentSelection) {
		if (currentSelection.isEmpty()) {
			btnRemove.setEnabled(false);
			btnResume.setEnabled(false);
			btnSuspend.setEnabled(false);
		} else {
			btnRemove.setEnabled(true);
			btnResume.setEnabled(false);
			btnSuspend.setEnabled(false);
			for (GWTSubscription subscr : currentSelection) {
				if (subscr.isSuspended()) {
					btnResume.setEnabled(true);
					break;
				}
			}
			for (GWTSubscription subscr : currentSelection) {
				if (!subscr.isSuspended()) {
					btnSuspend.setEnabled(true);
					break;
				}
			}
		}
	}
}
