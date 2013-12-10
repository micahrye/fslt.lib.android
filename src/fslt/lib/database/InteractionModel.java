package fslt.lib.database;

public class InteractionModel {
	private long id; 
	private long page_id; 
	private String media_name; 
	private long action_id; 
	
	public InteractionModel(){
		
	}
	public InteractionModel(long page_id, String media_name, long action_id){
		this.page_id = page_id; 
		this.media_name = media_name; 
		this.action_id = action_id; 
	}
	
	//setters
	public void setId(long id){
		this.id = id; 
	}
	public void setPageId(long page_id){
		this.page_id = page_id; 
	}
	public void setMediaName(String media_name){
		this.media_name = media_name; 
	}
	public void setActionId(long action_id){
		this.action_id = action_id; 
	}
	//getters 
	public long getId(){
		return this.id; 
	}
	public long getPageId(){
		return this.page_id; 
	}
	public String getMediaName(){
		return this.media_name; 
	}
	public long getActionId(){
		return this.action_id; 
	}
}
