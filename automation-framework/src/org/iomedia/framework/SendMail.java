package org.iomedia.framework;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {
	
	 final static String username = System.getProperty("mail.username");
	 final static String password = System.getProperty("mail.password");
	 final static String OS = System.getProperty("os.name").toLowerCase();
	
	 public static boolean sendMail(String To, String cc, String bcc, String Subject, String Message, String zipName, List<String> files, boolean attachSSInEmail, boolean zipping, boolean attachLogFile){
		 OSValidator.setPropValues(OS);
		 
		 if(username == null || password == null)
			 return false;
		 
		 System.out.println("Username :: " + username);
		 System.out.println("Password :: " + password);
		 
		 if(zipping && files != null && files.size() > 0){
			 zipFiles(zipName, files, attachSSInEmail);
		 }
		 
		 String to = To;	
		 String from = "qa-automation@io-media.com";
		 String host = "email-smtp.us-east-1.amazonaws.com";//"smtp.gmail.com";
		 
		 Properties properties = System.getProperties();
		 properties.put("mail.smtp.host", host);
		 properties.put("mail.smtp.socketFactory.port", "465");
		 properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		 properties.put("mail.smtp.auth", "true");
		 properties.put("mail.smtp.port", "465");
		 properties.put("mail.user", username);
		 properties.put("mail.password", password);
		 properties.put("mail.transport.protocol", "smtps");
		 properties.put("mail.debug", "false");
		 properties.put("mail.smtp.ssl.trust", "*");
		 properties.put("mail.smtp.ssl.checkserveridentity", "false");
		 properties.put("mail.smtp.localhost", "localhost");
		 properties.put("mail.smtp.ssl.enable", "true");
		 properties.put("mail.smtp.starttls.enable", "true");
		 
		 Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			 protected PasswordAuthentication getPasswordAuthentication() {
				 return new PasswordAuthentication(username, password);
			 }
		 });
		 
		 try{
			 MimeMessage message = new MimeMessage(session);
			 message.setFrom(new InternetAddress(from));
			 
			 if(to != null && !to.trim().equals("")){
				 for (int i = 0; i < to.split(";").length; i++)
				 {
					 if(!to.split(";")[i].trim().equals("")){
						 message.addRecipient(RecipientType.TO, new InternetAddress(to.split(";")[i]));
					 }
				 }
			 }
			 if(cc != null && !cc.trim().equals("")){
				 for (int i = 0; i < cc.split(";").length; i++)
				 {
					 if(!cc.split(";")[i].trim().equals("")){
						 message.addRecipient(RecipientType.CC, new InternetAddress(cc.split(";")[i]));
					 }
				 }
			 }
			 if(bcc != null && !bcc.trim().equals("")){
				 for (int i = 0; i < bcc.split(";").length; i++)
				 {
					 if(!bcc.split(";")[i].trim().equals("")){
						 message.addRecipient(RecipientType.BCC, new InternetAddress(bcc.split(";")[i]));
					 }
				 }
			 }
			
			 message.setSubject(Subject);
			
			 // Create the message part
			 BodyPart messageBodyPart = new MimeBodyPart();
			 messageBodyPart.setContent(Message, "text/html");
			 Multipart multipart = new MimeMultipart();
			 multipart.addBodyPart(messageBodyPart);
			 
			 if(files != null && files.size() > 0){
				 messageBodyPart = new MimeBodyPart();
				 String filename = zipName;
				 if(zipping) {
					 DataSource source = new FileDataSource(filename);
					 messageBodyPart.setDataHandler(new DataHandler(source));
					 messageBodyPart.setFileName(filename.split(OSValidator.delimiter)[filename.split(OSValidator.delimiter).length - 1]);
					 multipart.addBodyPart(messageBodyPart);
				 } else if(attachLogFile && !zipName.trim().endsWith(".zip") && new File(files.get(0)).length() < 10000000){
					 System.out.println("Log size :: " + String.valueOf(new File(files.get(0)).length()));
					 DataSource source = new FileDataSource(files.get(0));
					 messageBodyPart.setDataHandler(new DataHandler(source));
					 messageBodyPart.setFileName(filename);
					 multipart.addBodyPart(messageBodyPart);
				 }
			 }

			 message.setContent(multipart);
			 Transport.send(message);
			 System.out.println("Sent message successfully....");
		 }catch (MessagingException mex) {
			 mex.printStackTrace();
			 return false;
		 }
		 return true;
	 }
	
	 public static void zipFiles(String zipName, List<String> files, boolean attachSSInEmail){
		FileOutputStream fos = null;         
		ZipOutputStream zipOut = null;         
		FileInputStream fis = null;         
		try {             
			fos = new FileOutputStream(zipName);             
			zipOut = new ZipOutputStream(new BufferedOutputStream(fos));             
			for(String filePath:files){    
				File input = new File(filePath);
				if(input.isDirectory()){
					File[] _files = input.listFiles();
					for(int i = 0 ; i < _files.length; i++){
						if(attachSSInEmail || _files[i].getAbsolutePath().trim().endsWith(".html") || _files[i].getAbsolutePath().trim().endsWith(".txt")){
							ZipEntry ze = null;
							if(_files[i].getAbsolutePath().indexOf("Snapshots") > -1)
								ze = new ZipEntry(_files[i].getAbsolutePath().substring(_files[i].getAbsolutePath().indexOf("Snapshots"), _files[i].getAbsolutePath().length()));
							else if(_files[i].getAbsolutePath().indexOf("Logs") > -1)
								ze = new ZipEntry(_files[i].getAbsolutePath().substring(_files[i].getAbsolutePath().indexOf("Logs"), _files[i].getAbsolutePath().length()));
							else
								continue;
							System.out.println("Zipping the file: " + _files[i].getName());                 
							zipOut.putNextEntry(ze);                 
							byte[] tmp = new byte[4*1024];                 
							int size = 0;
							fis = new FileInputStream(_files[i]);
							while((size = fis.read(tmp)) != -1){                     
								zipOut.write(tmp, 0, size);                 
							}                 
							zipOut.flush();
							fis.close();
						}
					}
				}
				else{
					fis = new FileInputStream(input);                 
					ZipEntry ze = new ZipEntry(input.getName());                 
					System.out.println("Zipping the file: " + input.getName());                 
					zipOut.putNextEntry(ze);                 
					byte[] tmp = new byte[4*1024];                 
					int size = 0;                 
					while((size = fis.read(tmp)) != -1){                     
						zipOut.write(tmp, 0, size);                 
					}                 
					zipOut.flush();                 
					fis.close(); 
				}
			}             
			zipOut.close();             
			System.out.println("Done... Zipped the files...");         
			} 
		catch (FileNotFoundException e) {             
			// TODO Auto-generated catch block             
			e.printStackTrace();         
			} 
		catch (IOException e) {
			// TODO Auto-generated catch block             
			e.printStackTrace();         
			} 
		finally{             
			try{                 
				if(fos != null) 
					fos.close();             
				} catch(Exception ex){  
					
			}   
		}
	} 
}
