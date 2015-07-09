/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package com.drawmap.control;

import org.geotools.swing.AbstractMapPane;
import org.geotools.swing.MapPane;
import org.geotools.swing.locale.LocaleUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.swing.control.DnDList;
import org.geotools.swing.control.DnDListModel;
import org.geotools.swing.event.MapPaneAdapter;
import org.geotools.swing.event.MapPaneEvent;
import org.opengis.feature.type.FeatureType;

import com.drawmap.control.MapLayerTableCellRenderer.LayerControlItem;

/**显示一个地图图层的列表
 * Displays a list of the map layers in an associated {@linkplain MapPane} and
 * provides controls to set the visibility, selection and style of each layer.
 * <p>
 * Implementation note: DefaultMapContext stores its list of MapLayer objects
 * in rendering order, ie. the layer at index 0 is rendererd first, followed by
 * index 1 etc. MapLayerTable stores its layers in the reverse order since it
 * is more intuitive for the user to think of a layer being 'on top' of other
 * layers.
 *
 * @author Michael Bedward
 * @since 2.6
 *
 * @source $URL$
 * @version $Id$
 */
public class MapLayerTable extends JPanel {
    // used to get localized strings from LocaleUtils class
    private static final String CLASS_NAME = "MapLayerTable";
    
    private static final String LIST_TITLE = LocaleUtils.getValue(CLASS_NAME, "ListTitle");
    
    private static final String SHOW_HIDE_LAYER = LocaleUtils.getValue(CLASS_NAME, "ShowHideLayer");
    private static final String SHOW_ALL_LAYERS = LocaleUtils.getValue(CLASS_NAME, "ShowAllLayers");
    private static final String HIDE_ALL_LAYERS = LocaleUtils.getValue(CLASS_NAME, "HideAllLayers");
    
    private static final String SELECT_LAYER = LocaleUtils.getValue(CLASS_NAME, "SelectLayer");
    private static final String SELECT_ALL_LAYERS = LocaleUtils.getValue(CLASS_NAME, "SelectAllLayers");
    private static final String DESELECT_ALL_LAYERS = LocaleUtils.getValue(CLASS_NAME, "DeselectAllLayers");
    
    private static final String RENAME_LAYER = LocaleUtils.getValue(CLASS_NAME, "RenameLayer");
    private static final String RENAME_LAYER_MESSAGE = LocaleUtils.getValue(CLASS_NAME, "RenameLayer_Message");
    
    private static final String REMOVE_LAYER = LocaleUtils.getValue(CLASS_NAME, "RemoveLayer");
    private static final String REMOVE_LAYER_MESSAGE = LocaleUtils.getValue(CLASS_NAME, "RemoveLayer_ConfirmMessage");
    private static final String REMOVE_LAYER_TITLE = LocaleUtils.getValue(CLASS_NAME, "RemoveLayer_ConfirmTitle");
    
    
    private static final String STYLE_LAYER = LocaleUtils.getValue(CLASS_NAME, "StyleLayer");
    

    private MapPane mapPane;
    private DnDListModel<Layer> listModel;  //代表了存储Layer的模型，可以存储多个，在DnDList上显示DnDListModel的内容
    private DnDList<Layer> list;
    private JScrollPane scrollPane;
    
    /* For detecting mouse double-clicks */
    private static final long DOUBLE_CLICK_TIME = 500;
    private long lastClickTime = 0; 
    private Set<String> layerNamesSet = new HashSet<String>();

    /*
     * Whether to prompt for confirmation before removing a layer.
     * @todo introduce a setter or property for this
     */
    private boolean confirmRemove = true;

    /**
     * Default constructor. A subsequent call to {@linkplain #setMapPane}
     * will be required.
     */
    public MapLayerTable() {
        this(null);
    }

    /**
     * Constructor.
     * @param mapPane the map pane this MapLayerTable will service.
     */
    public MapLayerTable(MapPane mapPane) {
        listener = new Listener(this);
        initComponents();
        doSetMapPane(mapPane);
    }

    /**
     * Set the map pane that this MapLayerTable will service.
     *
     * @param mapPane the map pane
     */
    public void setMapPane(MapPane mapPane) {
        doSetMapPane(mapPane);
    }

    /**
     * Helper for {@link #setMapPane(MapPane). This is just defined so that 
     * it can be called from the constructor without a warning from the compiler
     * about calling a public overridable method.
     * 
     * @param mapPane the map pane
     */
    private Listener listener;
    private void doSetMapPane(MapPane newMapPane) {
        listener.disconnectFromMapPane();
        mapPane = newMapPane;
        listener.connectToMapPane(newMapPane);
    }

    /**
     * Add a new layer to those listed in the table. This method will be called
     * by the associated map pane automatically as part of the event sequence
     * when a new MapLayer is added to the pane's MapContext.
     *
     * @param layer the map layer
     */
    public void onAddLayer(Layer layer) {//在监听器处理函数中调用
    	String name = layer.getFeatureSource().getName().getLocalPart();
    	if(!layerNamesSet.contains(name)){
    		listModel.insertItem(listModel.getSize(), layer);
    		layerNamesSet.add(name);
    		
    	}
    	
    	
//        Throwable throwable = new Throwable();
//        StackTraceElement[] frames = throwable.getStackTrace();
//        for (StackTraceElement stackTraceElement : frames) {
//        	System.out.println("165: "+stackTraceElement);
//		}
        
    }

    /**
     * Remove a layer from those listed in the table. This method will be called
     * by the associated map pane automatically as part of the event sequence
     * when a new MapLayer is removed from the pane's MapContext.
     *
     * @param layer the map layer
     */
    void onRemoveLayer(Layer layer) {//在监听器处理函数中调用
        listModel.removeItem(layer);
    }

    /**
     * Repaint the list item associated with the specified MapLayer object
     *
     * @param layer the map layer
     */
    public void repaint(Layer layer) {//在监听器处理函数中调用
        int index = listModel.indexOf(layer);
        list.repaint(list.getCellBounds(index, index));
    }

    /**
     * Removes all items from the table. This is called by the
     * {@code MapPane} or other clients and is not intended for
     * general use.
     */
    public void clear() {
        listModel.clear();
    }

    /**
     * Called by the constructor. This method lays out the components that
     * make up the MapLayerTable and registers a mouse listener.
     */
    private void initComponents() {
        listModel = new DnDListModel<Layer>();
        list = new DnDList<Layer>(listModel) {  //表示图层列表的容器
            private static final long serialVersionUID = 1289744440656016412L;
            /*
             * We override setToolTipText to provide tool tips
             * for the control labels displayed for each list item
             */
            @Override
            public String getToolTipText(MouseEvent e) {   //表示鼠标点击之后对鼠标事件进行处理
                int item = list.locationToIndex(e.getPoint());

                if (item >= 0) {
                    Rectangle r = list.getCellBounds(item, item);
                    if (r.contains(e.getPoint())) {
                        Point p = new Point(e.getPoint().x, e.getPoint().y - r.y);

                        if (MapLayerTableCellRenderer.hitSelectionLabel(p)) {
                            return SELECT_LAYER;

                        }else if (MapLayerTableCellRenderer.hitVisibilityLabel(p)) {
                            return SHOW_HIDE_LAYER;

                        } 
                        else if (MapLayerTableCellRenderer.hitStyleLabel(p)) {
                            return STYLE_LAYER;

                        } else if (MapLayerTableCellRenderer.hitRemoveLabel(p)) {
                            return REMOVE_LAYER;

                        } else if (MapLayerTableCellRenderer.hitNameLabel(p)) {
                            return RENAME_LAYER;
                        }
                    }
                }
                
                return null;
            }
			@Override
			public void dragDropEnd(DragSourceDropEvent dsde) {  //当拖动事件结束之后调用
				// TODO Auto-generated method stub
				super.dragDropEnd(dsde);
				((AbstractMapPane)mapPane).layerMoved(null);
//				repaint(mapPane.getMapContent().layers().get(listSeclectedIndex));
			}
            
        };

        // Listen for drag-reordering of the list contents which
        // will be received via the contentsChanged method
        listModel.addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {}
            
            public void intervalRemoved(ListDataEvent e) {}

            public void contentsChanged(ListDataEvent e) {
                onReorderLayers(e);
            }
        });

        list.setCellRenderer(new MapLayerTableCellRenderer());  //设置列表中每项的显示方式
        list.setFixedCellHeight(MapLayerTableCellRenderer.getCellHeight());  //设置每一项的显示高度

        list.addMouseListener(new MouseAdapter() {  //创建鼠标事件监听器
            @Override
            public void mouseClicked(MouseEvent e) {
                long clickTime = System.currentTimeMillis();
                boolean doubleClick = clickTime - lastClickTime < DOUBLE_CLICK_TIME;
                lastClickTime = clickTime;
                onLayerItemClicked(e, doubleClick);
                
            }
        });
        
        //添加滚动条
        scrollPane = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder(LIST_TITLE));
        
        //底部的容器
        JPanel btnPanel = new JPanel();   //创建添加组件的容器
        //显示所有图层的按钮
        Icon showIcon = MapLayerTableCellRenderer.LayerControlItem.VISIBLE.getIcon();
        JButton btn = null;        
        btn = new JButton(showIcon);
        btn.setToolTipText(SHOW_ALL_LAYERS);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onShowAllLayers();
            }
        });
        btnPanel.add(btn);
        //隐藏所有图层的按钮
        Icon hideIcon = MapLayerTableCellRenderer.LayerControlItem.VISIBLE.getOffIcon();
        btn = new JButton(hideIcon);
        btn.setToolTipText(HIDE_ALL_LAYERS);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onHideAllLayers();
            }
        });
        btnPanel.add(btn);
        //选中所有图层
        /*
        Icon onIcon = MapLayerTableCellRenderer.LayerControlItem.SELECTED.getIcon();
        btn = new JButton(onIcon);
        btn.setToolTipText(SELECT_ALL_LAYERS);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelectAllLayers();
            }
        });
        btnPanel.add(btn);
        //取消选中所有图层
        Icon offIcon = MapLayerTableCellRenderer.LayerControlItem.SELECTED.getOffIcon();
        btn = new JButton(offIcon);
        btn.setToolTipText(DESELECT_ALL_LAYERS);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onUnselectAllLayers();
            }
        });
        */
        btnPanel.add(btn);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**点击列表中某一行的某个图标时调用
     * Handle a mouse click on a cell in the JList that displays
     * layer names and states.
     *
     * @param ev the mouse event
     * @param doubleClick true if this is the second click of a double-click; false otherwise
     */
    private void onLayerItemClicked(MouseEvent ev, boolean doubleClick) {
        int item = list.locationToIndex(ev.getPoint());  //获得选中的条目

        if (item >= 0) {    
            Rectangle r = list.getCellBounds(item, item);
            if (r.contains(ev.getPoint())) {
                Layer layer = listModel.getElementAt(item);
                Point p = new Point(ev.getPoint().x, ev.getPoint().y - r.y);
                
                	if(MapLayerTableCellRenderer.hitVisibilityLabel(p)) {  //如果点击的是显示/隐藏图层
                		layer.setVisible(!layer.isVisible());   
                	}else if (MapLayerTableCellRenderer.hitSelectionLabel(p)) {
//                  	layer.setSelected(!layer.isSelected());  //因为在列表项中将图层选中按钮删除掉了
                		doSetStyle(layer);
                	} else if (MapLayerTableCellRenderer.hitStyleLabel(p)) {
//                  	doSetStyle(layer);
                		doRemoveLayer(layer);
              
                	} else if (MapLayerTableCellRenderer.hitRemoveLabel(p)) {
	//                  doRemoveLayer(layer);
                		if (doubleClick) {
	                      doSetLayerName(layer);
	                  }
                	} 
	//              else if (MapLayerTableCellRenderer.hitNameLabel(p)) {
	//                  if (doubleClick) {
	//                      doSetLayerName(layer);
	//                  }
	//              }
                /*
                if (MapLayerTableCellRenderer.hitSelectionLabel(p)) {
                    layer.setSelected(!layer.isSelected());  
                } else if (MapLayerTableCellRenderer.hitVisibilityLabel(p)) {
                    layer.setVisible(!layer.isVisible());   
                } else if (MapLayerTableCellRenderer.hitStyleLabel(p)) {
                    doSetStyle(layer);
                
                } else if (MapLayerTableCellRenderer.hitRemoveLabel(p)) {
                    doRemoveLayer(layer);
                } 
                else if (MapLayerTableCellRenderer.hitNameLabel(p)) {
                    if (doubleClick) {
                        doSetLayerName(layer);
                    }
                }*/
            }
        }
    }

    /**
     * Show a style dialog to create a new Style for the layer
     *
     * @param layer the layer to be styled
     */
    private void doSetStyle(Layer layer) {   //点击了风格按钮后执行
        if (layer instanceof StyleLayer) {
            StyleLayer styleLayer = (StyleLayer) layer;
            Style style = JSimpleStyleDialog.showDialog(this, styleLayer);
//            for (FeatureTypeStyle fts : style.getFeatureTypeStyles()) {
//				Rule[] rs = fts.getRules();
//				for(int i=0; i<rs.length; i++){
//					;
//				}
//			}
            
            if (style != null) {
                styleLayer.setStyle(style);
            }
        }
    }

    /**
     * Prompt for a new title for the layer
     *
     * @param layer the layer to be renamed
     */
    private void doSetLayerName(Layer layer) {
        String name = JOptionPane.showInputDialog(RENAME_LAYER_MESSAGE);
        if (name != null && name.trim().length() > 0) {
            layer.setTitle(name.trim());
        }
    }

    /**
     * Called when the user has clicked on the remove layer item.
     *
     * @param layer the layer to remove
     */
    private void doRemoveLayer(Layer layer) {
        if (confirmRemove) {
            int confirm = JOptionPane.showConfirmDialog(null,
                    REMOVE_LAYER_MESSAGE,
                    REMOVE_LAYER_TITLE,
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        mapPane.getMapContent().removeLayer(layer);
    }

    /**
     * Handle a ListDataEvent signallying a drag-reordering of the map layers.
     * The event is published by the list model after the layers have been
     * reordered there.
     *
     * @param ev the event
     */
    private void onReorderLayers(ListDataEvent ev) {
        ((JComponent) mapPane).setIgnoreRepaint(true);
        for (int pos = ev.getIndex0(); pos <= ev.getIndex1(); pos++) {
            Layer layer = listModel.getElementAt(pos);

            /*
             * MapLayerTable stores layers in the reverse order to
             * the MapContent layer list
             */
            int newContextPos = listModel.getSize() - pos - 1;
            int curContextPos = mapPane.getMapContent().layers().indexOf(layer);

            if (curContextPos != newContextPos) {
                mapPane.getMapContent().moveLayer(curContextPos, newContextPos);
            }
        }
        ((JComponent) mapPane).setIgnoreRepaint(false);
        ((JComponent) mapPane).repaint();
    }

    private void onShowAllLayers() {
        if (mapPane != null && mapPane.getMapContent() != null) {
            for (Layer layer : mapPane.getMapContent().layers()) {
                if (!layer.isVisible()) {
                    layer.setVisible(true);
                }
            }
        }
    }

    private void onHideAllLayers() {
        if (mapPane != null && mapPane.getMapContent() != null) {
            for (Layer layer : mapPane.getMapContent().layers()) {
                if (layer.isVisible()) {
                    layer.setVisible(false);
                }
            }
        }
    }

    private void onSelectAllLayers() {
        if (mapPane != null && mapPane.getMapContent() != null) {
            for (Layer layer : mapPane.getMapContent().layers()) {
                if (!layer.isSelected()) {
                    layer.setSelected(true);
                }
            }
        }
    }

    private void onUnselectAllLayers() {
        if (mapPane != null && mapPane.getMapContent() != null) {
            for (Layer layer : mapPane.getMapContent().layers()) {
                if (layer.isSelected()) {
                    layer.setSelected(false);
                }
            }
        }
    }

    
    private static final class Listener extends MapPaneAdapter implements MapLayerListListener {
        private final MapLayerTable table;
        private WeakReference<MapPane> paneRef;
        private WeakReference<MapContent> contentRef;

        Listener(MapLayerTable table) {
            this.table = table;
        }
        
        void connectToMapPane(MapPane newMapPane) {
            if (newMapPane != null) {
                paneRef = new WeakReference<MapPane>(newMapPane);
                newMapPane.addMapPaneListener(this);   //向JMapPane中注册监听器
                
                disconnectFromMapContent();
                connectToMapContent(newMapPane.getMapContent());
            }
        }
        
        void disconnectFromMapPane() {
            if (paneRef != null) {
                MapPane prevMapPane = paneRef.get();
                paneRef = null;
                
                if (prevMapPane != null) {
                    prevMapPane.removeMapPaneListener(this);
                }
            }
        }
        
        void connectToMapContent(MapContent newMapContent) {
            if (newMapContent != null) {
                contentRef = new WeakReference<MapContent>(newMapContent);
                newMapContent.addMapLayerListListener(this);  //向MapContent注册监听器
            
                for (Layer layer : newMapContent.layers()) {
                    table.onAddLayer(layer);
                }
            }
        }
        
        private void disconnectFromMapContent() {
            if (contentRef != null) {
                MapContent prevMapContent = contentRef.get();
                contentRef = null;
                if (prevMapContent != null) {
                    prevMapContent.removeMapLayerListListener(this);
                }
                
  //          table.clear();
            }
        }

        @Override
        public void onNewMapContent(MapPaneEvent ev) {
  //          table.clear();
            disconnectFromMapContent();
            MapContent newMapContent = (MapContent) ev.getData();
            connectToMapContent(newMapContent);
            if (newMapContent != null) {
                for (Layer layer : newMapContent.layers()) {
                    table.onAddLayer(layer);
                }
            }
        }

        public void layerAdded(MapLayerListEvent event) {
            table.onAddLayer(event.getElement());
        }

        public void layerRemoved(MapLayerListEvent event) {
            table.onRemoveLayer(event.getElement());
        }

        public void layerChanged(MapLayerListEvent event) {
            table.repaint(event.getElement());
        }

        public void layerMoved(MapLayerListEvent event) {
        }

        public void layerPreDispose(MapLayerListEvent event) {
        }

    }
}
