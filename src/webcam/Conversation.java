//https://developer.ibm.com/recipes/tutorials/integration-of-ibm-watson-conversation-service-to-your-java-application/

package webcam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

public class Conversation {

	public static void main(String[] args) {
		Util.loadConfig();
		
		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Enter something : ");
				String input = br.readLine();
				if ("q".equals(input)) {
					System.out.println("Exit!");
					System.exit(0);
				}
				
				System.out.println("———–");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}