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
		ConversationService service = new ConversationService("2017-02-03");
		service.setUsernameAndPassword(Util.cr_conv_u, Util.cr_conv_p);
		
		BufferedReader br = null;
		MessageResponse response = null;
		Map context = new HashMap();
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Enter something : ");
				String input = br.readLine();
				if ("q".equals(input)) {
					System.out.println("Exit!");
					System.exit(0);
				}
				response = conversationAPI(service, input, context);
				System.out.println("Watson Response:" + response.getText().get(0));
				context = response.getContext();
				System.out.println("———–");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static MessageResponse conversationAPI(ConversationService service, String input, Map context){
		MessageRequest newMessage = new MessageRequest.Builder().inputText(input).context(context).build();
		String workspaceId = Util.cr_conv_wid;
		MessageResponse response = service.message(workspaceId, newMessage).execute();
		return response;
	}
}