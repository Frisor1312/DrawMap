package com.drawmap.control;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.swing.AbstractMapPane;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;
import org.geotools.swing.event.MapPaneEvent;
import org.opengis.geometry.Envelope;

/**
 * A lightweight map pane which uses a single renderer and backing image.
 * Used by {@linkplain JMapFrame} for the GeoTools tutorial applications.
 *
 * @author Michael Bedward
 * @author Ian Turton
 * @since 2.6
 *
 * @source $URL$
 * @version $Id$
 */
public class JMyMapPane extends JMapPane {

    private GTRenderer renderer;
    private BufferedImage baseImage;
    private Graphics2D baseImageGraphics;
    
    /**
     * Creates a new map pane. 
     */
    public JMyMapPane() {
    	super(null);
    }
    
    /**
     * Creates a new map pane.
     *
     * @param content the map content containing the layers to display
     *     (may be {@code null})
     */
    public JMyMapPane(MapContent content) {
        this(content, null, null);
    }

    /**
     * Creates a new map pane. Any or all arguments may be {@code null}
     *
     * @param content the map content containing the layers to display
     * @param executor the rendering executor to manage drawing
     * @param renderer the renderer to use for drawing layers
     */
    public JMyMapPane(MapContent content, RenderingExecutor executor, GTRenderer renderer) {
    	super();
        doSetRenderer(renderer);
        
    }
    

    /**
     * Gets the renderer, creating a default one if required.
     *
     * @return the renderer
     */
    public GTRenderer getRenderer() {
        if (renderer == null) {
            doSetRenderer(new StreamingRenderer());
        }
        return renderer;
    }

    
    @Override
	public void print(Graphics g) {
		super.print(g);
	}

	/**
     * Sets the renderer to be used by this map pane.
     *
     * @param renderer the renderer to use
     */
    public void setRenderer(GTRenderer renderer) {
        doSetRenderer(renderer);
    }

    private void doSetRenderer(GTRenderer newRenderer) {
        if (newRenderer != null) {
            Map<Object, Object> hints = newRenderer.getRendererHints();
            if (hints == null) {
                hints = new HashMap<Object, Object>();
            }
            
            if (newRenderer instanceof StreamingRenderer) {
                if (hints.containsKey(StreamingRenderer.LABEL_CACHE_KEY)) {
                    labelCache = (LabelCache) hints.get(StreamingRenderer.LABEL_CACHE_KEY);
                } else {
                    labelCache = new LabelCacheImpl();
                    hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
                }
            }
            
            newRenderer.setRendererHints(hints);

            if (mapContent != null) {
                newRenderer.setMapContent(mapContent);
            }
        }
        
        renderer = newRenderer;
    }
    
    /**
     * Retrieve the map pane's current base image.
     * <p>
     * The map pane caches the most recent rendering of map layers
     * as an image to avoid time-consuming rendering requests whenever
     * possible. The base image will be re-drawn whenever there is a
     * change to map layer data, style or visibility; and it will be
     * replaced by a new image when the pane is resized.
     * <p>
     * This method returns a <b>live</b> reference to the current
     * base image. Use with caution.
     *
     * @return a live reference to the current base image
     */
    public RenderedImage getBaseImage() {
        return this.baseImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (drawingLock.tryLock()) {
            try {
                if (baseImage != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.drawImage(baseImage, imageOrigin.x, imageOrigin.y, null);
                }
            } finally {
                drawingLock.unlock();
            }
        }
    }

    @Override
    protected void drawLayers(boolean createNewImage) {  //第一次绘制，以及拖动、缩放地图时都会被调�?
        drawingLock.lock();
        try {
            if (mapContent != null
                    && !mapContent.getViewport().isEmpty()
                    && acceptRepaintRequests.get()) {

                Rectangle r = getVisibleRect();
                if (baseImage == null || createNewImage) {
                    baseImage = GraphicsEnvironment.getLocalGraphicsEnvironment().
                            getDefaultScreenDevice().getDefaultConfiguration().
                            createCompatibleImage(r.width, r.height, Transparency.TRANSLUCENT);

                    if (baseImageGraphics != null) {
                        baseImageGraphics.dispose();
                    }

                    baseImageGraphics = baseImage.createGraphics();
                    clearLabelCache.set(true);

                } else {
//                    baseImageGraphics.setBackground(getBackground());
                    baseImageGraphics.setBackground(new Color(245, 243, 240));
                    baseImageGraphics.clearRect(0, 0, r.width, r.height);
                }

                if (mapContent != null && !mapContent.layers().isEmpty()) {  //都会执行到这�?
                    getRenderingExecutor().submit(mapContent, getRenderer(), baseImageGraphics, this);
                }
            }
        } finally {
            drawingLock.unlock();
        }
    }
    
    public void addLayers(Collection<Layer> c){
    	mapContent.addLayers(c);
    	publishEvent( new MapPaneEvent(this,     //向监听器类发送消�?
                MapPaneEvent.Type.NEW_MAPCONTENT,
                mapContent) );
//    	repaint();
    }
    
    public void changeDisplayArea(Envelope e){
    	doSetDisplayArea(e);
    }

    protected void otherDrawLayers(boolean createNewImage, Graphics2D baseImageGraphics) {  
        drawingLock.lock();
        
        try {
            if (mapContent != null && !mapContent.layers().isEmpty()) {  //都会执行到这�?
//            	List<Layer> bmpLayers = getMapContent().layers();  //获取所有的图层
//        		for (Layer layer : bmpLayers) {
//        			System.out.println("isVisiable="+layer.isVisible());
//        		}
            	getRenderingExecutor().submit(mapContent, getRenderer(), baseImageGraphics, this);
//                System.out.println("is here");
            }
        } finally {
            drawingLock.unlock();
        }
    }

}

