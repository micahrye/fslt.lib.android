package fslt.lib.database;

public class StoryModel {
	private long id; 
	private String title; 
	private String date;  
	private String start_time; 
	private String end_time; 

	public StoryModel(){

	}

	public StoryModel(long id, String title, String date, String start_time){
		this.id = id; 
		this.title = title; 
		this.date = date; 
		this.start_time = start_time; 
	}
	
	//setters
	public void setId(long id){
		this.id = id; 
	}
	public void setTitle(String title){
		this.title = title; 
	}
	public void setDate(String date){
		this.date = date; 
	}
	public void setStartTime(String start_time){
		this.start_time = start_time; 
	}
	public void setEndTime(String end_time){
		this.end_time = end_time; 
	}
	//getters 
	public long getId(){
		return this.id; 
	}
	public String getTitle(){
		return this.title; 
	}
	public String getDate(){
		return this.date; 
	}
	public String getStartTime(){
		return this.start_time; 
	}
	public String getEndTime(){
		return this.end_time; 
	}
}
