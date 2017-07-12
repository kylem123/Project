package webcam;

public class ResultObj {
	
	public String cl;
	public float score;
	public float count = 1;
	
	public ResultObj(String cl, String score) {
		this.cl = cl;
		this.score = Float.parseFloat(score);
	}
}
