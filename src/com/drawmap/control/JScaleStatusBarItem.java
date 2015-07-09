package com.drawmap.control;

import java.awt.Font;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.measure.Measure;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.swing.AbstractMapPane;
import org.geotools.swing.JMapPane;
import org.geotools.swing.MapPane;
import org.geotools.swing.control.JMapStatusBar;
import org.geotools.swing.control.StatusBarItem;
import org.geotools.swing.event.MapPaneAdapter;
import org.geotools.swing.event.MapPaneEvent;
import org.geotools.swing.event.MapPaneListener;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
//比例尺状态栏�?
public class JScaleStatusBarItem extends StatusBarItem{
	
	private final JLabel label;
	private AbstractMapPane mapPane;
	private CoordinateReferenceSystem crs;
	private int scale;
	private int dpi = getToolkit().getDefaultToolkit().getScreenResolution();  //获得屏幕的密度，代表�?��寸多少像�?
	private Measure m;

	public JScaleStatusBarItem(final MapPane mapPane) {
		super("比例尺状态栏项");
		this.mapPane = (AbstractMapPane) mapPane;
		if (mapPane == null) {
	        throw new IllegalArgumentException("mapPane must not be null");
	    }
		
		label = new JLabel();
		label.setFont(new Font("Courier", Font.PLAIN, 12));
		label.setText("比例尺");
		add(label);
		
		//向MapPane中添加监听器
		mapPane.addMapPaneListener(new MapPaneAdapter(){

			@Override
			public void onNewMapContent(MapPaneEvent ev) {   //首次创建时调�?
				super.onNewMapContent(ev);
				scale = calculateScale(ev);
				label.setText("1："+scale+"m");
				
			}

			@Override
			public void onDisplayAreaChanged(MapPaneEvent ev) {   //显示区域改变时调�?
				super.onDisplayAreaChanged(ev);
				
				scale = calculateScale(ev);
				label.setText("1："+scale+"m");
				
				ReferencedEnvelope rEnvelope = mapPane.getMapContent().getMaxBounds();   //获得地图图层的最大范�?
				
				Point2D minpDouble = mapPane.getWorldToScreenTransform().transform(new Point2D.Double(rEnvelope.getMinX(), rEnvelope.getMinY()), null);
				Point2D maxpDouble = mapPane.getWorldToScreenTransform().transform(new Point2D.Double(rEnvelope.getMaxX(), rEnvelope.getMaxY()), null);
				
				
				int bmpWidth = (int) (maxpDouble.getX()-minpDouble.getX());  //计算绘制的图像的宽度
				int bmpHeight = (int) (minpDouble.getY()-maxpDouble.getY()); //计算绘制的图像的高度
				
//				System.out.println("w = "+bmpWidth+"; h = "+bmpHeight);
				
			}
		});
	
	}
	
	private int calculateScale(MapPaneEvent ev){  //获得比例尺，屏幕上没厘米代表多少�?
		int scale;
		ReferencedEnvelope re = ev.getSource().getDisplayArea();   //经纬度坐�?
		double minX = re.getMinX();
		double maxX = re.getMaxX();
		double minY = re.getMaxY();
		float panewidth = ((JComponent) mapPane).getVisibleRect().width;
		if(crs == null)
			crs = mapPane.getDisplayArea().getCoordinateReferenceSystem();
		m = ((AbstractCRS)crs).distance(new double[]{minX, minY}, new double[]{maxX, minY});  //计算距离长度,单位m
		scale = (int) (m.floatValue()/panewidth*dpi/2.54);   //表示屏幕上的每厘米代表地图上的实际距�?
		return scale;
	}
	
	public int getScale(){
		return scale;
	}
	
	public float getDistancePerPixel(){
		float panewidth = ((JComponent) mapPane).getVisibleRect().width;
		return m.floatValue()/panewidth;
	}

}
