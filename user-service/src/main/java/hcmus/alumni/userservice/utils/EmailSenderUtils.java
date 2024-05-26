package hcmus.alumni.userservice.utils;
import java.io.IOException;
import java.util.HashMap;

import models.SendEnhancedRequestBody;
import models.SendEnhancedResponseBody;
import models.SendRequestMessage;
import services.Courier;
import services.SendService;

public class EmailSenderUtils {
	
	private static volatile EmailSenderUtils instance  = null;
	
	private EmailSenderUtils() {
        super();
    }
	
	public static EmailSenderUtils getInstance() {
        if (instance == null) {
            synchronized (EmailSenderUtils.class) {
                if (instance == null) {
                    instance = new EmailSenderUtils();
                }
            }
        }
        return instance;
    }
	
    public void sendPasswordEmail(String userEmail, String newPwd) throws Exception {
    	Courier.init("pk_prod_D8HF91KG5AM7G4HBKFEECG9KARHM");

        SendEnhancedRequestBody sendEnhancedRequestBody = new SendEnhancedRequestBody();
        SendRequestMessage sendRequestMessage = new SendRequestMessage();
        HashMap<String, String> to = new HashMap<String, String>();
        to.put("email", userEmail);
        sendRequestMessage.setTo(to);
        sendRequestMessage.setTemplate("M9A9EPFXWK4V43JEK8HNHG72GCPW");

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("recipientName", userEmail);
        data.put("newPwd", newPwd);

        sendRequestMessage.setData(data);

        sendEnhancedRequestBody.setMessage(sendRequestMessage);

        try {
          SendEnhancedResponseBody response = new SendService().sendEnhancedMessage(sendEnhancedRequestBody);
          System.out.println("Password sent successfully to " + userEmail);
        } catch (IOException e) {
        	System.err.println("Error sending email: " + e.getMessage());
        }
    }
    
//    public void sendPasswordResetEmail(String userEmail, String resetUrl) {
//        Courier.init("PK_PROD_D8HF91KG5AM7G4HBKFEECG9KARHM");
//
//        SendEnhancedRequestBody sendEnhancedRequestBody = new SendEnhancedRequestBody();
//        SendRequestMessage sendRequestMessage = new SendRequestMessage();
//        HashMap<String, String> to = new HashMap<String, String>();
//        to.put("email", userEmail);
//        sendRequestMessage.setTo(to);
//        sendRequestMessage.setTemplate("KGA5V18RZ8428BPEA18A0ABTRQRX"); 
//
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("recipientName", userEmail);
//        data.put("resetUrl", resetUrl);
//
//        sendRequestMessage.setData(data);
//
//        sendEnhancedRequestBody.setMessage(sendRequestMessage);
//
//        try {
//            SendEnhancedResponseBody response = new SendService().sendEnhancedMessage(sendEnhancedRequestBody);
//            System.out.println("Password reset email sent successfully to " + userEmail);
//        } catch (IOException e) {
//            System.err.println("Error sending password reset email: " + e.getMessage());
//        }
//    }
}