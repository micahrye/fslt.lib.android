/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class FileOperations {
	private static final String TAG = FileOperations.class.getSimpleName();

	public static final int INTERANL_STORAGE = 1;
	public static final int EXTERNAL_STORAGE = 2;
	public static final int ASSETS_STORAGE = 3;

	private final Context mCtx;
	public static AssetManager assetManager;

	public FileOperations(Context context) {
		mCtx = context;
		assetManager = mCtx.getAssets();
	}

	/**
	 * Make directory and directory parent path if it does not exist as
	 * subdirectory of the root internal or external storage location.
	 * 
	 * @param location
	 *            Use FileOperations.INTERNAL_STORAGE or
	 *            FileOperations.EXTERNAL_STORAGE or
	 *            FileOperations.ASSETS_STORAGE to indicate root of file
	 *            location.
	 * @param dirName
	 *            The directory name including any parent directory structure
	 *            information that will be all under the storage location
	 *            direcotry.
	 * @return boolean Returns true if directory created
	 */
	public boolean makeDirectory(int location, String dirName) {
		dirName = checkLeadingForwardSlash(dirName);

		if (location == EXTERNAL_STORAGE) {
			if (!externalStorageAvailable())
				return false;
			File externalStorageLocation = new File(Environment.getExternalStorageDirectory(), dirName);
			if (externalStorageLocation.exists()) {
				return true;
			}
			boolean made = externalStorageLocation.mkdirs();
			return made;
		} else if (location == INTERANL_STORAGE) {
			//Note: Returns the absolute path to the directory on the filesystem 
			//      where files created with openFileOutput(String, int) are stored
			File internalStorageLocation = new File(new File(mCtx.getFilesDir(), ""), dirName);
			if (internalStorageLocation.exists())
				return true;
			return internalStorageLocation.mkdirs();
		}
		return false;
	}

	/**
	 * Check if leading character in path is '/', if not add '/' to path string.
	 */
	private String checkLeadingForwardSlash(String path) {
		char forwardSlash = '/';
		char firstChar = path.charAt(0);
		if (forwardSlash != firstChar)
			path = forwardSlash + path;
		return path;
	}

	/**
	 * Check if external storage is available for use.
	 * 
	 * @return boolean Returns true if external storage mounted with read/write,
	 *         false otherwise.
	 */
	public boolean externalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return false;
		}
		return false;
	}

	/**
	 * Write string data output to file
	 * 
	 * @param location
	 *            Use FileOperations.INTERNAL_STORAGE or
	 *            FileOperations.EXTERNAL_STORAGE to indicate root of file
	 *            location. Note that you cannot write to assets.
	 * @param output
	 *            String output data to write to file as string.
	 * @param fileName
	 *            The file name including any parent directory structure
	 *            information that will all be relative to the storage
	 *            'location' directory.
	 * @return boolean Return true if writing data successful, false otherwise.
	 */
	public boolean writeStringToStorageLocation(int location, String output, String fileName) {
		fileName = checkLeadingForwardSlash(fileName);
		if (location == EXTERNAL_STORAGE) {
			if (!externalStorageAvailable())
				return false;
			//TODO: think about how you want to handle possible nullpointerexception from null fileName
			File externalStorageLocation = new File(Environment.getExternalStorageDirectory(), fileName);
			return writeStringOutputToFileAndClose(output, externalStorageLocation);
		} else if (location == INTERANL_STORAGE) {
			File internalStorageLocation = new File(new File(mCtx.getFilesDir(), ""), fileName);
			return writeStringOutputToFileAndClose(output, internalStorageLocation);
		}
		return false;
	}

	/**
	 * Write string output to file
	 * 
	 * @param output
	 *            String output data to write to file as string.
	 * @param file
	 *            The file to write ouput to
	 * @return boolean Return true if writing data successful, false otherwise.
	 */
	private boolean writeStringOutputToFileAndClose(String output, File file) {
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(output);
			out.flush();
			out.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Given an InputStream return it as a string
	 * 
	 * @param inputStream
	 *            inputStream to read as string
	 * @return string
	 * @throws IOException
	 */
	public String inputStreamToString(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			throw new IOException("Problem reading file input.");
		}

		return stringBuilder.toString();
	}

	/**
	 * Open and return the file contents from storage location as an InputStream
	 * for user to do with as needed.
	 * 
	 * @param location
	 *            Use FileOperations.INTERNAL_STORAGE or
	 *            FileOperations.EXTERNAL_STORAGE or
	 *            FileOperations.ASSETS_STORAGE to indicate root of file
	 *            location.
	 * @param fileName
	 *            The file name including any parent directory structure
	 *            information that will all be relative to the storage
	 *            'location' directory.
	 * @return inputStream of file from storage location.
	 */
	public InputStream getInputStreamFromStorageLocation(int location, String fileName) {
		InputStream inputStream = null;
		if (location == EXTERNAL_STORAGE) {
			if (!externalStorageAvailable()) {
				//TODO: talk with others, do we want to throw an exception or something else?
				//throw new IOException("sdcard not readable, cannot open file");
			}
			//TODO: think about how you want to handle possible nullpointerexception from null fileName
			try {
				inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (location == INTERANL_STORAGE) {
			String path = mCtx.getFilesDir().getPath() + File.separator + fileName;
			try {
				inputStream = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (location == ASSETS_STORAGE) {
			try {
				inputStream = assetManager.open(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return inputStream;
	}

	/*
	 * Delete file or delete directory recursively. Similar to the 
	 * command line 'rm -rf <file or dir>' Note that this is a static method.
	 * 
	 * @param fileOrDirectory
	 * 				The file or directory to delete
	 * @return boolean
	 * 				return true on successful deletion of file or direcotry, 
	 * 				otherwise return false. 
	 */
	public static boolean deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}
		return fileOrDirectory.delete();
	}

	/**
	 * 
	 * @param location
	 * @param dirName
	 * @return
	 */
	public String[] listStorageLocationFiles(int location, String dirName) {
		String fileNames[] = null;
		File fileDir = null;
		if (location == EXTERNAL_STORAGE) {
			if (!externalStorageAvailable()) {
				//TODO: talk with others, do we want to throw an exception or something else?
				//throw new IOException("sdcard not readable, cannot open file");
			}
			fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + dirName);
		} else if (location == INTERANL_STORAGE) {
			String path = mCtx.getFilesDir().getPath() + File.separator + dirName;
			fileDir = new File(path);
		} else if (location == ASSETS_STORAGE) {
			try {
				fileNames = assetManager.list(dirName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fileDir != null) {
			List<String> list = new ArrayList<String>();
			for (File child : fileDir.listFiles()) {
				list.add(child.getName());
			}
			fileNames = list.toArray(new String[list.size()]);
		}
		return fileNames;
	}

	/*
	 * Encryption code adapted from FUNF. 
	 * https://code.google.com/p/funf-open-sensing-framework
	 */
	private SecretKey mKey = null;
	private String mAlgorithm = null;
	private Cipher mCipher = null;

	public void setEncryptionKey(SecretKey key) {
		setEncryptionKeyAndAlgorithm(key, "DES");
	}

	public void setEncryptionKeyAndAlgorithm(SecretKey key, String algorithm) {
		assert key != null && algorithm != null;
		mKey = key;
		mAlgorithm = algorithm;
	}

	public boolean copyEncypt(File sourceFile, File destinationFile) {
		assert mKey != null && mAlgorithm != null;

		Cipher ecipher = getCipher();
		if (ecipher == null)
			return false;

		InputStream in = null;
		OutputStream out = null;
		CipherOutputStream co = null;
		try {
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(destinationFile);
			co = new CipherOutputStream(out, ecipher);
			byte[] buf = new byte[128 * 4096];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				co.write(buf, 0, len);
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found", e);
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			return false;
		} finally {
			try {
				in.close();
				co.close();
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "Problem closing files after copyEncypt", e);
			}
		}
		Log.i(TAG, "done copy");
		return true;
	}

	private Cipher getCipher() {
		if (mCipher == null) {
			try {
				mCipher = Cipher.getInstance(mAlgorithm);
				mCipher.init(Cipher.ENCRYPT_MODE, mKey);
			} catch (Exception e) {
				Log.e(TAG, "Error creating cipher", e);
			}
		}
		return mCipher;
	}
}
