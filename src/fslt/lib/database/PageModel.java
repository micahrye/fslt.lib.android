package fslt.lib.database;

public class PageModel {
	private long id; 
	private long story_id; 
	private int page_number; 
	private String start_time; 
	private String end_time; 
	
	public PageModel(){
		
	}
	public PageModel(long story_id){
		this.story_id = story_id; 
	}
	public PageModel(long id, long story_id, String start_time){
		this.id = id; 
		this.story_id = story_id; 
		this.start_time = start_time; 
	}
	//setters 
	public void setId(long id){
		this.id = id; 
	}
	public void setStoryId(long id){
		this.story_id = id; 
	}
	public void setPageNumber(int page_number){
		this.page_number = page_number; 
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
	public long getStoryId(){
		return this.story_id; 
	}
	public int getPageNumber(){
		return this.page_number; 
	}
	public String getStartTime(){
		return this.start_time; 
	}
	public String getEndTime(){
		return this.end_time; 
	}

}
