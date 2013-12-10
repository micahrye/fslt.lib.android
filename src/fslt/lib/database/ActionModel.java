package fslt.lib.database;

public class ActionModel {
	private long id; 
	private String action_name; 

	public ActionModel(){

	}
	public ActionModel(String action_name){
		this.action_name = action_name; 
	}
	public ActionModel(long id, String action_name){
		this.id = id; 
		this.action_name = action_name;
	}
	
	//setters 
	public void setId(int id){
		this.id = id; 
	}
	public void setActionName(String action_name){
		this.action_name = action_name; 
	}
	//getters
	public long setId(){
		return this.id;  
	}
	public String getActionName(){
		return this.action_name; 
	}
}
