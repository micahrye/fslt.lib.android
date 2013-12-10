package fslt.lib.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	//Logcat tag
	private static final String TAG = DatabaseHelper.class.getSimpleName();
	// Database Version	
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "storyscape_database";
	//Tables
	private static final String TABLE_STORY = "story";
	private static final String TABLE_PAGE = "page"; 
	private static final String TABLE_INTERACTION = "interaction"; 
	private static final String TABLE_ACTION = "action"; 

	//Common column names 
	private static final String ID = "_id";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	//Story column names
	private static final String STORY_TITLE = "title"; 
	private static final String STORY_DATE= "date"; 
	//Page column names
	private static final String STORY_ID = "story_id";
	private static final String PAGE_NUMBER = "page_number"; 
	//Interaction column names
	private static final String PAGE_ID = "page_id"; 
	private static final String MEDIA_NAME = "media_name"; 
	private static final String ACTION_ID = "action_id"; 
	//Actions column names
	private static final String ACTION_NAME = "action_name"; 

	// Table Create Statements
	// Story table create statement
	private static final String CREATE_TABLE_STORY = "CREATE TABLE "
			+ TABLE_STORY + "(" + ID + " INTEGER PRIMARY KEY," + STORY_TITLE
			+ " TEXT," + STORY_DATE + " DATE," + START_TIME 
			+ " TIME" + ")";

	//Page table create statment
	private static final String CREATE_TABLE_PAGE = "CREATE TABLE "
			+ TABLE_PAGE + "(" + ID + " INTEGER PRIMARY KEY," 
			+ STORY_ID + " INTEGER, " + PAGE_NUMBER
			+ " INTEGER," + START_TIME + " TIME," + END_TIME  
			+ " TIME" + ")";

	//Interaction table create statement
	private static final String CREATE_TABLE_INTERACTION = "CREATE TABLE "
			+ TABLE_INTERACTION + "(" + ID + " INTEGER PRIMARY KEY," 
			+ PAGE_ID + " INTEGER, " + MEDIA_NAME + " TEXT," + ACTION_ID 
			+ " INTEGER," + START_TIME   
			+ " TIME" + ")";

	//Actions table create statment 
	private static final String CREATE_TABLE_ACTIONS = "CREATE TABLE "
			+ TABLE_ACTION + "(" + ID + " INTEGER PRIMARY KEY," 
			+ ACTION_NAME + " TEXT" + ")";

	private Context mCtx; 

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context; 
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// creating required tables
		db.execSQL(CREATE_TABLE_STORY);
		db.execSQL(CREATE_TABLE_PAGE);
		db.execSQL(CREATE_TABLE_INTERACTION);
		db.execSQL(CREATE_TABLE_ACTIONS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_STORY);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_PAGE);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_INTERACTION);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_ACTIONS);
		// create new tables
		onCreate(db);
	}

	/*
	Ê* Creating a story
	Ê*/
	public long createStory(StoryModel story) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(STORY_TITLE, story.getTitle() );
		values.put(STORY_DATE, getDateTime() );
		values.put(START_TIME, getCurrentTimeInMilliseconds() );
		// insert row
		long story_id = db.insert(TABLE_STORY, null, values);

		return story_id;
	}

	/*
	Ê* Creating a page
	Ê*/
	public long createPage(PageModel page, long storyId) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(STORY_ID, storyId );
		values.put(PAGE_NUMBER, page.getPageNumber() );
		values.put(START_TIME, getCurrentTimeInMilliseconds() );
		// insert row
		long page_id = db.insert(TABLE_PAGE, null, values);

		return page_id;
	}
	public long getPageIdForPageNumber(long storyId, int pageNumber){
		SQLiteDatabase db = this.getWritableDatabase();

		String[] tableColumns = new String[] {ID, STORY_ID, PAGE_NUMBER };
		String whereClause = "STORY_ID = ? AND PAGE_NUMBER = ?";
		String[] whereArgs = new String[] {
				Long.toString(storyId),
				Integer.toString(pageNumber)
		};
		String orderBy = STORY_ID; 

		Cursor c = db.query(TABLE_PAGE, tableColumns, whereClause, whereArgs,
				null, null, orderBy);
		long rtn; 
		if(c.moveToFirst()){
			int index = c.getColumnIndex(ID); 
			rtn = c.getLong(index); 
			return rtn;
		}
		return -1;
	}
	public int setPageEndTime(long pageId){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put( END_TIME, getCurrentTimeInMilliseconds() );
		String whereClause = "_id = ?"; 
		String [] whereArgs = { Long.toString(pageId) }; 
		int rowsEffected = db.update(TABLE_PAGE, values, whereClause, whereArgs);
		return rowsEffected; 
	}
	public void setStoryEndTime(long storyId){

	}
	/*
	Ê* Creating a page
	Ê*/
	public long createInteraction(InteractionModel interaction) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(PAGE_ID, interaction.getPageId() );
		values.put(MEDIA_NAME, interaction.getMediaName() );
		values.put(ACTION_ID, interaction.getActionId() );
		values.put(START_TIME, getCurrentTimeInMilliseconds() );
		// insert row
		long interaction_id = db.insert(TABLE_INTERACTION, null, values);
		return interaction_id;
	}
	/*
	Ê* Creating a action
	Ê*/
	public long createAction(ActionModel action) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ACTION_NAME, action.getActionName() );
		// insert row
		long action_id = db.insert(TABLE_ACTION, null, values);

		return action_id;
	}

	public void dumpDatabase(){
		SQLiteDatabase db = this.getReadableDatabase();
		File dbFile = mCtx.getDatabasePath(DATABASE_NAME);
		InputStream in = null;

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String fileName = "backupDatabase.txt";

		// Not sure if the / is on the path or not
		OutputStream out = null;
		File outFile = new File(baseDir + File.separator + fileName);
		try{
			in = new FileInputStream(dbFile.getPath());
			out = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		}catch(IOException e) {
			Log.e(TAG, "shit"); 
		}  
	}

	// closing database
	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();
	}

	//Date and time methods
	public String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
	public long getCurrentTimeInMilliseconds(){
		return System.currentTimeMillis(); 
	}

}
