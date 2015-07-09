package com.drawmap.shp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.factory.FactoryRegistryException;
import org.opengis.referencing.FactoryException;


public class ShpFiles {
	
	private Map<String, String> filesMap = new HashMap<String, String>();   //
	
	public ShpFiles(){
		
	}

	public ShpFiles(String path) {
		
		String filename = path.substring(path.lastIndexOf('\\')+1,path.lastIndexOf('.'));
		String pathString = path.substring(0, path.lastIndexOf('\\')+1);
		char lastChar = path.charAt(path.length()-1);
		boolean upperCase = Character.isUpperCase(lastChar);
		
		for (ShpFileType type : ShpFileType.values()) {
			String extensionWithPeriod = type.extensionWithPeriod;
			if (upperCase) {   //后缀名是大写
                extensionWithPeriod = extensionWithPeriod.toUpperCase();
            } else {
                extensionWithPeriod = extensionWithPeriod.toLowerCase();
            }
			String otherFileName = pathString+filename+extensionWithPeriod;
			File file = new File(otherFileName);
			if(file.exists()){   //如果文件存在
				filesMap.put(type+"", otherFileName);
			}
		}
		
	}
	
	public Map<String, String> getFileMap() {
		return filesMap;
	}
	
	
	
	
	public static enum ShpFileType{
		/**
	     * The .shp file. It contains the geometries of the shapefile
	     */
	    SHP("shp"),
	    /**
	     * the .dbf file, it contains the attribute information of the shapefile
	     */
	    DBF("dbf"),
	    /**
	     * the .shx file, it contains index information of the existing features
	     */
	    SHX("shx"),
	    /**
	     * the .prj file, it contains the projection information of the shapefile
	     */
	    PRJ("prj"),
	    /**
	     * the .qix file, A quad tree spatial index of the shapefile. It is the same
	     * format the mapservers shptree tool generates
	     */
	    QIX("qix"),
	    /**
	     * the .fix file, it contains all the Feature IDs for constant time lookup
	     * by fid also so that the fids stay consistent across deletes and adds
	     */
	    FIX("fix"),
	    /**
	     * the .shp.xml file, it contains the metadata about the shapefile
	     */
	    SHP_XML("shp.xml");
	    
	    public final String extension;
	    public final String extensionWithPeriod;
	    
	    private ShpFileType(String extension){
	    	this.extension = extension.toLowerCase();
	        this.extensionWithPeriod = "." + this.extension;
	    };
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(this == obj)  return true;
		if(obj == null)  return false;
		if(getClass() != obj.getClass())
			return false;
		ShpFiles sFiles = (ShpFiles) obj;
		String shpPath = sFiles.getFileMap().get(ShpFileType.SHP+"");
		if(filesMap.get(ShpFileType.SHP+"").equals(shpPath))
			return true;
		else
			return false;
	}

	
	
	

}
