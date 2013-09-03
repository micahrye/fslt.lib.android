package fslt.lib.file.utilites;


import android.util.Log; 

import java.io.BufferedOutputStream;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 
 

public class Decompress { 
	private String _zipFile; 
	private String _location; 
 
  /**
   * Decompresses the file at zipFile into location
   * 
   * @param zipFile
   * @param location
   */
	public Decompress(String zipFile, String location) { 
		_zipFile = zipFile; 
		_location = location; 
 
		_dirChecker(""); 
	} 
 
	public void unzip() throws Exception { 
	    try  { 
	      FileInputStream fin = new FileInputStream(_zipFile); 
	      ZipInputStream zin = new ZipInputStream(fin); 
	      ZipEntry ze = null; 
	      while ((ze = zin.getNextEntry()) != null) { 
	        Log.d("Decompress", "Unzipping " + ze.getName()); 
	        if(ze.isDirectory()) { 
	          _dirChecker(ze.getName()); 
	        } else { 
	        	int size;
	            byte[] buffer = new byte[2048];
	        	String unzip_loc = _location + ze.getName(); 
	        	// if file exist remove it and unzip 
	        	File f = new File(unzip_loc);
	        	if( f.exists() ){
	        		f.delete(); 
	        	}
	        	FileOutputStream fout = new FileOutputStream(unzip_loc); 
	        	BufferedOutputStream bufferOut = new BufferedOutputStream(fout, buffer.length);
	        	// we read up to our buffer size (2048 bits) per read and then write that out
	        	while((size = zin.read(buffer, 0, buffer.length)) != -1) {
	                bufferOut.write(buffer, 0, size);
	            }
	
	            bufferOut.flush();
	            bufferOut.close();
	            zin.closeEntry(); 
	        	fout.close(); 
	        } 
	         
	      } 
	      zin.close(); 
	    } catch(Exception e) { 
	      Log.e("Decompress", "unzip", e); 
	      throw e; 
	    } 
	 } 
 
	private void _dirChecker(String dir) { 
		File f = new File(_location + dir); 
 
		if(!f.isDirectory()) { 
			f.mkdirs(); 
		} 
	} 
} 