package hcmus.alumni.userservice.utils;
import java.io.IOException;
import java.util.HashMap;

import models.SendEnhancedRequestBody;
import models.SendEnhancedResponseBody;
import models.SendRequestMessage;
import services.Courier;
import services.SendService;

public class EmailSenderUtils {

    public static void sendEmail(String userEmail) {
    	String authorizeCode = AuthorizationCodeGeneratorUtils.generateAuthorizeCode();

    	Courier.init("pk_test_Z0DMJFA8XYM8VFNMDE8RV0BYAKGS");

        SendEnhancedRequestBody sendEnhancedRequestBody = new SendEnhancedRequestBody();
        SendRequestMessage sendRequestMessage = new SendRequestMessage();
        HashMap<String, String> to = new HashMap<String, String>();
        to.put("email", userEmail);
        sendRequestMessage.setTo(to);
        sendRequestMessage.setTemplate("KGA5V18RZ8428BPEA18A0ABTRQRX");

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("recipientName", userEmail);
        data.put("authorizeCode", authorizeCode);

        sendRequestMessage.setData(data);

        sendEnhancedRequestBody.setMessage(sendRequestMessage);

        try {
          SendEnhancedResponseBody response = new SendService().sendEnhancedMessage(sendEnhancedRequestBody);
          System.out.println(response);
          System.out.println("Email sent successfully to " + userEmail);
        } catch (IOException e) {
        	System.err.println("Error sending email: " + e.getMessage());
        }
    }
    
}
