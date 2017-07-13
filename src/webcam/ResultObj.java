package webcam;

public class ResultObj {
	
	public String cl;
	public float score;
	public String type_hierarchy;
	public float count = 1;
	
	public ResultObj(String cl, String score, String type_hierarchy) {
		this.cl = cl;
		this.score = Float.parseFloat(score);
		this.type_hierarchy = type_hierarchy;
	}
}