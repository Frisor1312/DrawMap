package com.drawmap.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.swing.JMapFrame.Tool;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.InfoAction;
import org.geotools.swing.action.NoToolAction;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.geotools.swing.control.JCoordsStatusBarItem;
import org.geotools.swing.control.JMapStatusBar;
import org.geotools.swing.dialog.JExceptionReporter;
import org.geotools.swing.styling.JSimpleStyleDialog;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.drawmap.db.DbControl;
import com.drawmap.shp.ShpFiles;
import com.drawmap.shp.ShpFiles.ShpFileType;
import com.drawmap.util.Convert;


public class JMyMpF extends JFrame{
	
	private boolean Menubar = true;
	private JMenuBar menuBar = new JMenuBar();
	private boolean showToolBar = true;
    private Set<Tool> toolSet;
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    /*
     * UI elements
     */
    private JMyMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private JToolBar toolBar;

    private boolean showStatusBar = true;
    private boolean showLayerTable = true;
    
    /** Name assigned to toolbar button for feature info queries. */
    public static final String TOOLBAR_INFO_BUTTON_NAME = "ToolbarInfoButton";
    /** Name assigned to toolbar button for map panning. */
    public static final String TOOLBAR_PAN_BUTTON_NAME = "ToolbarPanButton";
    /** Name assigned to toolbar button for default pointer. */
    public static final String TOOLBAR_POINTER_BUTTON_NAME = "ToolbarPointerButton";
    /** Name assigned to toolbar button for map reset. */
    public static final String TOOLBAR_RESET_BUTTON_NAME = "ToolbarResetButton";
    /** Name assigned to toolbar button for map zoom in. */
    public static final String TOOLBAR_ZOOMIN_BUTTON_NAME = "ToolbarZoomInButton";
    /** Name assigned to toolbar button for map zoom out. */
    public static final String TOOLBAR_ZOOMOUT_BUTTON_NAME = "ToolbarZoomOutButton";
 	
    private MapContent map = new MapContent();
    
    private boolean exportWithBg = true;
    
    private JMapStatusBar jmsbBar;  //状态栏中的比例尺控件
    private String saveBmpPath;
    private final int titleBmpWidth = 200;   //切片的宽度
    private final int titleBmpHeight = 200;  //切片的高度
    
    private static final String MAPFILE_PATH = "D:/workspace/三幅地图/";
    private List<ShpFiles> shplist; 
    private List<Layer> pointLayers = new ArrayList<Layer>();
  

	public JMyMpF() {
		// TODO Auto-generated constructor stub
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   //
		setTitle("桌面地图客户端");
		//设置保存的路径
		saveBmpPath = "E:/地图数据";
		
		toolSet  = EnumSet.allOf(Tool.class);
		 // the map pane is the one element that is always displayed  绘制地图的组件
        mapPane = new JMyMapPane();
        mapPane.setBackground(Color.WHITE);
        mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        
        // give keyboard focus to the map pane
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                mapPane.requestFocusInWindow();
            }
        });
        
        mapPane.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void focusLost(FocusEvent e) {
                mapPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        });
        
        mapPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mapPane.requestFocusInWindow();
            }
        });
		initComponents();
	}
	
	private void initComponents(){
		if(Menubar){
			JMenu file = new JMenu("文件");
			JMenu edit = new JMenu("编辑");
			JMenu export = new JMenu("导出");
			JMenuItem openItem = new JMenuItem("打开", new ImageIcon("icon/open.png"));
			JMenuItem newItem = new JMenuItem("新建");
			JMenuItem saveItem = new JMenuItem("保存");
			
			openItem.addActionListener(new ActionListener() {  //打开菜单
				
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					String cmd = e.getActionCommand();
					FileDialog openFiles = getOpenFileDialog();
					openFiles.setMultipleMode(true);
					openFiles.setVisible(true);
					File[] files = openFiles.getFiles();
					
//					JFileChooser fileChooser = getFileChooser();
//					fileChooser.setVisible(true);
//					File[] files = fileChooser.getSelectedFiles();
					shplist = new ArrayList<ShpFiles>();  //保存打开的shp文件
					for (File file : files) {
//						System.out.println(file2.getAbsolutePath());
						ShpFiles newFiles = new ShpFiles(file.getAbsolutePath());
						shplist.add(newFiles);
					}
					try {
						initMapPanel(shplist);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
						
			file.add(openItem);
			file.add(newItem);
			file.add(saveItem);
			
			JMenuItem exportWithBg = new JMenuItem("加背景导出");
			JMenuItem exportWithoutBg = new JMenuItem("透明背景导出");
			exportWithBg.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					JMyMpF.this.exportWithBg = true;
					cutMap();
				}
			});
			exportWithoutBg.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					JMyMpF.this.exportWithBg = false;
					cutMap();
				}
			});
			export.add(exportWithBg);
			export.add(exportWithoutBg);
			
			menuBar.add(file);
			menuBar.add(edit);
			menuBar.add(export);
			
			this.setJMenuBar(menuBar);
		}
		
		/*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        StringBuilder sb = new StringBuilder();
        if (!toolSet.isEmpty()) {
            sb.append("[]"); // fixed size
        }
        sb.append("[grow]"); // map pane and optionally layer table fill space
        if (showStatusBar) {
            sb.append("[min!]"); // status bar height
        }
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0", // layout constrains: 1 component per row, no insets 布局约束：一个组件一行，没有插图
                "[grow]", // column constraints: col grows when frame is resized列约束：当窗口调整大小时，列随之改变
                sb.toString() ));

        /*
         * A toolbar with buttons for zooming in, zooming out,
         * panning, and resetting the map to its full extent.
         * The cursor tool buttons (zooming and panning) are put
         * in a ButtonGroup.
         *
         * Note the use of the XXXAction objects which makes constructing
         * the tool bar buttons very simple.
         */
        if (showToolBar) {
            toolBar = new JToolBar();
            toolBar.setOrientation(JToolBar.HORIZONTAL);
            toolBar.setFloatable(false);

            JButton btn;
            ButtonGroup cursorToolGrp = new ButtonGroup();
            
            if (toolSet.contains(Tool.POINTER)) {  //箭头工具栏
                btn = new JButton(new NoToolAction(mapPane));  //添加事件响应函数
                btn.setName(TOOLBAR_POINTER_BUTTON_NAME);
                toolBar.add(btn);
                cursorToolGrp.add(btn);
            }

            if (toolSet.contains(Tool.ZOOM)) {
                btn = new JButton(new ZoomInAction(mapPane));  //放大按钮，添加事件响应函数
                btn.setName(TOOLBAR_ZOOMIN_BUTTON_NAME);
                toolBar.add(btn);
                cursorToolGrp.add(btn);

                btn = new JButton(new ZoomOutAction(mapPane));  //缩小按钮，添加事件响应函数
                btn.setName(TOOLBAR_ZOOMOUT_BUTTON_NAME);
                toolBar.add(btn);
                cursorToolGrp.add(btn);

                toolBar.addSeparator();
            }

            if (toolSet.contains(Tool.PAN)) {
                btn = new JButton(new PanAction(mapPane));  //拖动按钮
                btn.setName(TOOLBAR_PAN_BUTTON_NAME);
                toolBar.add(btn);
                cursorToolGrp.add(btn);

                toolBar.addSeparator();
            }

            if (toolSet.contains(Tool.INFO)) {
                btn = new JButton(new InfoAction(mapPane));  //元素信息按钮
                btn.setName(TOOLBAR_INFO_BUTTON_NAME);
                toolBar.add(btn);

                toolBar.addSeparator();
            }

            if (toolSet.contains(Tool.RESET)) {
                btn = new JButton(new ResetAction(mapPane));  //全屏显示
                btn.setName(TOOLBAR_RESET_BUTTON_NAME);
                toolBar.add(btn);
                
                toolBar.addSeparator();
            }
            
            //添加导出图片按钮
            btn = new JButton(new AbstractAction( null, new ImageIcon("icons/cutmap.png")) {
				public void actionPerformed(ActionEvent e) {
					
					cutMap();
				}
			});
            btn.setName("切割地图");
            toolBar.add(btn);

            panel.add(toolBar, "grow");
        }

        if (showLayerTable) {
            mapLayerTable = new MapLayerTable(mapPane);

            /*
             * We put the map layer panel and the map pane into a JSplitPane
             * so that the user can adjust their relative sizes as needed
             * during a session. The call to setPreferredSize for the layer
             * panel has the effect of setting the initial position of the
             * JSplitPane divider
             */
            mapLayerTable.setPreferredSize(new Dimension(200, -1));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                    false, 			//分隔条停止改变时才会重绘
                    mapLayerTable, 		//左边的容器
                    mapPane);		//右边的容器
            splitPane.setDividerSize(2);   //设置分隔条的大小
            panel.add(splitPane, "grow");

        } else {
            /*
             * No layer table, just the map pane
             */
            panel.add(mapPane, "grow");
        }

        if (showStatusBar) {
        	jmsbBar = JMapStatusBar.createDefaultStatusBar(mapPane);
        	jmsbBar.addItem(new JScaleStatusBarItem(mapPane));   //在状态栏中添加比例尺
        	panel.add(jmsbBar, "grow");
        	
//            panel.add(JMapStatusBar.createDefaultStatusBar(mapPane), "grow");
        }

        this.getContentPane().add(panel);
	
		
	}
	
	private FileDialog getOpenFileDialog(){
		FileDialog fd  = null;
		fd = new FileDialog(this, "请选择需要打开的文件", FileDialog.LOAD);
		fd.setFilenameFilter(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				if(name.contains("shp")||name.contains("SHP")){
//					System.out.println(name);
					return false;
				}
				return true;
			}
		});
		return fd;
	}
	
	private JFileChooser getFileChooser(){
		JFileChooser fileChooser = new JFileChooser(MAPFILE_PATH);
		fileChooser.setFileFilter(new FileNameExtensionFilter(null, ".shp"));
		
		return fileChooser;
	}
	
	private void initMapPanel(List<ShpFiles> shplist) throws IOException{
		Collection<Layer> c = new ArrayList<Layer>();
		for (ShpFiles shpFiles : shplist) {
			File file = new File(shpFiles.getFileMap().get(ShpFileType.SHP+""));
			ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());
			store.setCharset(Charset.forName("GBK"));  //设置编码(防止中文标注乱码)
			SimpleFeatureSource featureSource = store.getFeatureSource();
			Style style = createStyle(file, featureSource);
//			Style style = SLD.createSimpleStyle(featureSource.getSchema());
			Layer layer = new FeatureLayer(featureSource, style);
			c.add(layer);
//			System.out.println(file.getAbsolutePath());
//			CachingFeatureSource cache = new CachingFeatureSource(featureSource);
//			Layer layer = new FeatureLayer(cache, style);
		}
		map.addLayers(c);
		if(mapPane.getMapContent() == null)
			mapPane.setMapContent(map);
		else{
			mapPane.addLayers(c);
		}
		
	}
	
	/**
     * Create a Style to display the features. If an SLD file is in the same
     * directory as the shapefile then we will create the Style by processing
     * this. Otherwise we display a JSimpleStyleDialog to prompt the user for
     * preferences.
     */
    private Style createStyle(File file, FeatureSource featureSource) {
        //获取文件目录中的style文件
    	File sld = toSLDFile(file);
        if (sld != null) {
            return createFromSLD(sld);
        }
        //创建对话框，获得style
//        SimpleFeatureType schema = (SimpleFeatureType)featureSource.getSchema();
//        return JSimpleStyleDialog.showDialog(null, schema);
        return SLD.createSimpleStyle(featureSource.getSchema());
    }
    // start sld
    /**
     * Figure out if a valid SLD file is available.
     */
    public File toSLDFile(File file)  {
        String path = file.getAbsolutePath();
        String base = path.substring(0,path.length()-4);
        String newPath = base + ".sld";
        File sld = new File( newPath );
        if( sld.exists() ){
            return sld;
        }
        newPath = base + ".SLD";
        sld = new File( newPath );
        if( sld.exists() ){
            return sld;
        }
        return null;
    }

    /**
     * Create a Style object from a definition in a SLD document
     */
    private Style createFromSLD(File sld) {
        try {
            SLDParser stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
            Style[] style = stylereader.readXML();
            return style[0];
            
        } catch (Exception e) {
            //ExceptionMonitor.show(null, e, "Problem creating style");
        	JExceptionReporter.showDialog(e, "Problem creating style");
        }
        return null;
    }
	
	
	private void cutMap(){
		//判断是否有地图已经绘制，如果没有，那么切片失败
		RenderedImage ri = mapPane.getBaseImage();
		if(ri == null){
			JOptionPane.showMessageDialog(this, "还未绘制图片！", "切片失败", 
					JOptionPane.ERROR_MESSAGE);
			return ;
		}
		
		//创建一个进度条对话框
		final JDialog dialog = new JDialog(this);
		JPanel mainPane = new JPanel(null);
		final JProgressBar progressBar = new JProgressBar();
        final JLabel msg = new JLabel("正在切片，请耐心等待...");
        progressBar.setIndeterminate(true);
        mainPane.add(msg);
        mainPane.add(progressBar);
        dialog.getContentPane().add(mainPane);
        dialog.setUndecorated(true);//除去title
        dialog.setResizable(true);
        dialog.setSize(390, 100);
        dialog.setLocationRelativeTo(null); //设置此窗口相对于指定组件的位置
        
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // 不允许关闭
        mainPane.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                msg.setBounds(20, 20, 350, 15);
                progressBar.setBounds(20, 50, 350, 25);
            }
        });
        dialog.setVisible(true);
		
        new SwingWorker<Integer, Void>(){

        	
			@Override
			protected Integer doInBackground() throws Exception {
//				System.out.println("isEDT?"+SwingUtilities.isEventDispatchThread());
				int mapScale = 0;   //地图图片的比例尺，表示当前电脑屏幕上每厘米代表实际的距离
				float distancePerPixel = 0;  //表示当前地图图片所表示的地图范围
				for (Component component : jmsbBar.getComponents()) {
					if(component instanceof JScaleStatusBarItem){
						mapScale = ((JScaleStatusBarItem)component).getScale();
						distancePerPixel = ((JScaleStatusBarItem)component).getDistancePerPixel();
					}
				}
				
				ReferencedEnvelope rEnvelope = mapPane.getMapContent().getMaxBounds();   //获得地图图层的最大范围		
				Point2D minpDouble = mapPane.getWorldToScreenTransform().transform(new Point2D.Double(rEnvelope.getMinX(), rEnvelope.getMinY()), null);
				Point2D maxpDouble = mapPane.getWorldToScreenTransform().transform(new Point2D.Double(rEnvelope.getMaxX(), rEnvelope.getMaxY()), null);
				
				int bmpWidth = (int) (maxpDouble.getX()-minpDouble.getX());  //计算绘制的图像的宽度
				int bmpHeight = (int) (minpDouble.getY()-maxpDouble.getY()); //计算绘制的图像的高度
				BufferedImage baseImage = GraphicsEnvironment.getLocalGraphicsEnvironment().
		                getDefaultScreenDevice().getDefaultConfiguration().
		                createCompatibleImage(bmpWidth, bmpHeight, Transparency.TRANSLUCENT);
				
				Graphics2D baseImageGraphics = baseImage.createGraphics();
				if(exportWithBg){
					baseImageGraphics.setColor(new Color(245, 243, 240));
					baseImageGraphics.fillRect(0, 0, bmpWidth, bmpHeight);
				}
				//去除点图层的绘制
				List<Layer> bmpLayers = mapPane.getMapContent().layers();  //获取所有的图层
				for (Layer layer : bmpLayers) {
					SimpleFeatureType layerFT = (SimpleFeatureType)layer.getFeatureSource().getSchema();
					GeometryType gt = layerFT.getGeometryDescriptor().getType();
					if(gt.getName().toString().equals("Point")){
						pointLayers.add(layer);
//						layer.setVisible(false);
						
						String layerName = layerFT.getTypeName();  //获取shp文件的文件名
						System.out.println("图层："+layerName);
	//					mapPane.getMapContent().removeLayer(layer);
//						System.out.println((layer == null));
					}
					System.out.println("图层："+layerFT.getTypeName()+"isVisible="+layer.isVisible());
				}
				
				//将地图绘制到baseImage上
				mapPane.getMapContent().getViewport().setScreenArea(new Rectangle(bmpWidth, bmpHeight));
				mapPane.changeDisplayArea(rEnvelope);
				mapPane.otherDrawLayers(false, baseImageGraphics);
				//输出的位置
				String filepath = saveBmpPath+"/test/泾县宣纸"+mapScale;
				try {

					File file = new File(filepath);  //保存图片的目录
					if(!file.exists())
						file.mkdir();
					if(exportWithBg){  //判断导出的图片文件是否包含背景
						ImageIO.write(baseImage, "png", new File(filepath+"/mapWithBg.png"));
					}else{
						ImageIO.write(baseImage, "png", new File(filepath+"/mapWithoutBg.png"));
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				//执行切片程序
				System.out.println("正在切片...");
				msg.setText("正在切片...");
				System.out.println("输出目录为："+filepath);
//				Image image = baseImage.getScaledInstance(bmpWidth, bmpWidth, Image.SCALE_DEFAULT);
				
				int row, col;    //计算纵向与横向切片的数量
				if(bmpWidth%titleBmpWidth == 0)
					col = bmpWidth/titleBmpWidth;
				else
					col = bmpWidth/titleBmpWidth+1;
				if(bmpHeight%titleBmpHeight == 0)
					row = bmpHeight/titleBmpHeight;
				else
					row = bmpHeight/titleBmpHeight+1;
				//循环切片
				CropImageFilter cropFilter;
				Image img;
				int width=0, height=0;
				for(int i=0; i<row; i++)
				{
					for(int j=0; j<col; j++)
					{
						if(j==(col-1) && i !=(row-1))  //表示最右边一列，但不是最后一个的切片
						{
							width = bmpWidth-j*titleBmpWidth;
							height = titleBmpHeight;
						}else if(j!=(col-1) && i ==(row-1))  //表示最下面一行，但不是最后一个的切片
						{
							width = titleBmpWidth;
							height = bmpHeight-i*titleBmpHeight;
						}else if(j==(col-1) && i ==(row-1))  //表示最后一张切片
						{
							width = bmpWidth-j*titleBmpWidth;
							height = bmpHeight-i*titleBmpHeight;	
						}else
						{
							width = titleBmpWidth;
							height = titleBmpHeight;
						}
						cropFilter = new CropImageFilter(j*titleBmpWidth, i*titleBmpHeight, width, height);
						img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(baseImage.getSource(), cropFilter));
						
						
						//创建切片
						BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						
						Graphics2D g2d = tag.createGraphics();
						g2d.drawImage(img, 0, 0, null);
						g2d.dispose();
						try {
							ImageIO.write(tag, "gif", new File(filepath+"//"+(i*col+j+1)+".gif"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				//输出标注信息
				exportLabel(pointLayers, filepath);
				
				//输出日志信息
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("已经完成地图图片的切片\r\n");
				strBuilder.append("地图的经纬度范围时：("+rEnvelope.getMinX()+"°E, "+rEnvelope.getMinY()+"°N); ("+rEnvelope.getMaxX()+"°E, "+rEnvelope.getMaxY()+"°N)\r\n");
				strBuilder.append("地图图片的分辨率为：width = "+bmpWidth+"; height = "+bmpHeight+"\r\n");
				strBuilder.append("切片共有"+row*col+"个，横向有："+col+"\r\n");
				strBuilder.append("在本电脑屏幕中，每厘米代表实际地图上的"+mapScale+"m\r\n");
				strBuilder.append("图片中每像素代表的实际距离为："+distancePerPixel+"m");
				strBuilder.append("包含的图层有：\r\n");
				for (Layer layer : mapPane.getMapContent().layers()) {
					strBuilder.append("\t"+layer.getFeatureSource().getName()+"\r\n");			
				}
				
				
				byte[] readmes = strBuilder.toString().getBytes();
				
				try {
					File readmeFile = new File(filepath+"/readme");
					if(!readmeFile.exists())
						readmeFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(readmeFile);
					fos.write(readmes);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
//				
				
				return null;
			}

			@Override
			protected void done() {
				System.out.println("切片导出成功！");
				// 关闭进度提示框
		        dialog.dispose();
		        if(pointLayers.size()>0){
			        for (Layer layer : pointLayers) {
//			        	layer.setVisible(true);
//			        	System.out.println((layer == null));
//			        	mapPane.getMapContent().addLayer(layer);
					}
		        }
				mapPane.getMapContent().getViewport().setScreenArea(mapPane.getVisibleRect());
			}
			
        	
        }.execute();
		
		return ;
	}
	
	private void exportLabel(List<Layer> pointLayers, String filePath){ //导出标注信息
		String dbFilePath = filePath+"/label.db";
		//创建数据库
		File file = new File(dbFilePath);
		if(file.exists())
			file.delete();
		//
		DbControl dbc = new DbControl(dbFilePath);  //创建数据库连接
		dbc.execute("create table layers(id integer primary key autoincrement ,name string ,"
				+ " labelname string)");
		
//		List<Layer> mapLayers = mapPane.getMapContent().layers();
		for (Layer layer : pointLayers) {
			
			Style style = layer.getStyle();
			SimpleFeatureType layerFT = (SimpleFeatureType)layer.getFeatureSource().getSchema();
			int c = layerFT.getAttributeCount();
			String layerName = layerFT.getTypeName();  //获取shp文件的文件名
			GeometryType gt = layerFT.getGeometryDescriptor().getType();
			if(gt.getName().toString().equals("Point")){  //表示这个图层是点图层，那么将这个点图层的数据存储进数据库
				//获取这个点图层上标注的属性名称
				String labelArrName = null;
				List<FeatureTypeStyle> ftsList= style.featureTypeStyles();
				List<Rule> rList = ftsList.get(0).rules();
				Rule rule = rList.get(0);
				Symbolizer[] sb = rule.getSymbolizers();
				for (Symbolizer symbolizer : sb) {
					if(symbolizer instanceof TextSymbolizer)
					{
						//获取标注层属性的名字
						labelArrName = ((TextSymbolizer) symbolizer).getLabel().toString();
					}
				}
				//向layers表格中添加这个layer
				dbc.execute("insert into layers ('name' , 'labelname') values('"
						+layerName+"','"+labelArrName+"')");
				//创建存储该图层的表格
				dbc.execute("create table "+layerName+"(id integer primary key autoincrement ,turn short ,x double, y double, name string)");
				//读取shp文件跟dbf文件的路径
				String layerShpPath = null, layerDbfPath = null;
				for (ShpFiles shpFiles : shplist) {
					Map<String, String> map = shpFiles.getFileMap();
					if(map.get(ShpFileType.SHP+"").contains(layerName)){
						layerShpPath = map.get(ShpFileType.SHP+"");
						layerDbfPath = map.get(ShpFileType.DBF+"");
						
					}
					
				}

				//读取shp文件
				System.out.println("正在读取文件："+layerShpPath);
				int size = Convert.getFileSize(layerShpPath);
				byte[] shpbytes = new byte[size];  //存放shp文件的文件信息
				try{
					FileInputStream fs = new FileInputStream(layerShpPath);
					if(fs.read(shpbytes)>0)
					{
//						double xmin = Convert.littleEndiantodouble(shpbytes, 36);
//						double ymin = Convert.littleEndiantodouble(shpbytes, 44);
//						double xmax = Convert.littleEndiantodouble(shpbytes, 52);
//						double ymax = Convert.littleEndiantodouble(shpbytes, 60);
						
						int count = (size-100)/28;  //获取一共有多少个图元，每个图元占28B(已知)
						for(int j=0; j<count; j++)
						{							
							int turn = Convert.bigEndiantoint(shpbytes, 100+j*28);  //获取次序
							//获取x、y轴坐标
							double x = Convert.littleEndiantodouble(shpbytes, 112+j*28);
							double y = Convert.littleEndiantodouble(shpbytes, 120+j*28);
							
							//将Y坐标进行转换
//							double ys = Ymin+Ymax-y;
							dbc.execute("insert into "+layerName+"('turn','x','y') values("+turn+","+x+","+y+")");
						}
						
						fs.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//读取dbf文件
				try {
					System.out.println("正在读取文件："+layerDbfPath);
					FileInputStream fis = new FileInputStream(layerDbfPath);
					size = Convert.getFileSize(layerDbfPath);
//					System.out.println("文件的长度为："+ size);
					byte[] dbfbytes = new byte[size];  //存放shp文件的文件头部信息
					if(fis.read(dbfbytes)>0)
					{
						//获取记录条数，每个图元对应一条记录行
						int rowCount = Convert.littleEndiantoint(dbfbytes, 4);
						int columnNo;  //表格的列数
						int dbfHeadSize = Convert.littleEndiantoshort(dbfbytes, 8);	//文件头的字节长度
						columnNo = (dbfHeadSize-33)/32;
//						System.out.println("文件头中的字节数"+dbfHeadSize);
//						System.out.println("数据表的列数"+columnNo);
						int rowlenth = Convert.littleEndiantoshort(dbfbytes, 10);
//						System.out.println("一条记录中的字节长度"+rowlenth);					
						//读取表格的列，然后存放在columnlist变量中，存储的是列的名称
						//columnleth存储的是列的长度
						List<String> columnlist = new ArrayList<String>();
						List<Integer> columnleth = new ArrayList<Integer>();
						for(int j=1; j<=columnNo; j++)
						{
							StringBuffer str = new StringBuffer();
							for(int k=0; k<11; k++)
							{
								char c1 = (char) dbfbytes[k+32*j];
								if(c1!=0)
									str.append(String.valueOf(c1));
							}
							String columnname = str.toString().toUpperCase();						
							columnlist.add(columnname);
							int d = dbfbytes[16+32*j] & 0xff;//长度
							columnleth.add(d);
//							System.out.println(d);
//							map.put(str.toString(), d);   //默认长度为0
							
						}
						dbfHeadSize++;
						int length = 0;  //记录dbf文件头部后面的偏移量，这个偏移量是代表每条记录开始到文件头部结束之间的偏移量
						for(int j=0; j<rowCount; j++)  
						{
							length = j*rowlenth;
							int length2 = 0 ;	//代表偏移量
							//读取每个记录内容
							for(int k=0; k<columnNo; k++)
							{						
								String columnName = columnlist.get(k);  //获取名称 
								int leth = columnleth.get(k);//获取长度
								
								if(columnName.equals(labelArrName))
								{									
									byte[] position_name = new byte[leth];
									for(int l=0; l<leth; l++)
									{
										position_name[l] = dbfbytes[l+dbfHeadSize+length];										
//										char c = (char) dbfbytes[l+dbfHeadSize+length];										
//										if(c!=' ')
//											str.append(String.valueOf(c));
									}								
									//获取标注
									String str = new String(position_name);
									
									if(str.trim().length()!= 0)
									{
										String sql = "update "+layerName+" set name = '"+str+"' where turn = "+(j+1);
										dbc.execute(sql);
									}else  //删除那一行
									{
										String sql = "delete from lable_font_point"+" where turn = "+(j+1);
										dbc.execute(sql);
									}
										
									
						//			System.out.println("这个点对象的名称为： "+str);
									//往数据库添加数据
									
								}
								
								length2 += leth;
							}
							
						}
						
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			System.out.println(style.toString());
		}
	}
	
	

}
