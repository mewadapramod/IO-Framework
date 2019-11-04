package org.iomedia.common;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sun.misc.BASE64Decoder;

@SuppressWarnings("restriction")
public class EncryptDecrypt {
	
	public static String getSauceAuthToken(String sauceUsername, String sauceAccessKey, String job_id) {
		String KEY = sauceUsername + ":" + sauceAccessKey;
		String message = job_id;
		SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "HmacMD5");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacMD5");
			mac.init(key);
	        byte[] encVal = mac.doFinal(message.getBytes());
	        return Hex.encodeHexString(encVal).trim();
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public static String getEnvConfig(String cipherText_base64) {
    	fixKeyLength();
    	byte[] KEY = DatatypeConverter.parseHexBinary("bcb04b7e103a0cd8b54763051cef08bc55abe029fdebae5e1d417e2ffb2a00a3");
    	SecretKeySpec key = new SecretKeySpec(KEY, "AES");
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int ivSize = cipher.getBlockSize();
	    	byte[] decordedValue = new BASE64Decoder().decodeBuffer(cipherText_base64);
	    	String ciphertext_dec = new String(decordedValue, "Latin1");
	    	if(!ciphertext_dec.trim().equalsIgnoreCase("")) {
	    		byte[] iv_desc = Arrays.copyOfRange(decordedValue, 0, ivSize);
	    		byte[] byte_ciphertext_dec = Arrays.copyOfRange(decordedValue, ivSize, decordedValue.length);
	    		IvParameterSpec ivspec = new IvParameterSpec(iv_desc);
	    		cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
	    		byte[] dcryptdata = cipher.doFinal(byte_ciphertext_dec);
	    		String decryptedValue = new String(dcryptdata, "Latin1");
	    		String decodedString = new String(new BASE64Decoder().decodeBuffer(decryptedValue), "Latin1");
	    		decodedString = decodedString.substring(decodedString.indexOf("{"), decodedString.lastIndexOf("}") + 1);
	    		org.json.JSONObject json = new org.json.JSONObject(decodedString);
	    		return generateXml(json);
	    	}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ParserConfigurationException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return cipherText_base64;
    }
	
	private static String generateXml(JSONObject obj) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
		Document xmldoc;
		xmldoc = docBuilder.newDocument();
		Element config = xmldoc.createElement("CONFIG");
		Node root = xmldoc.appendChild(config);
		Iterator<String> iter = obj.keys();
		while(iter.hasNext()) {
			String key = iter.next().trim();
			String value = obj.getString(key).trim();
			Element childNode = xmldoc.createElement(key.toUpperCase());
			childNode.appendChild(xmldoc.createTextNode(value));
			root.appendChild(childNode);
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	      
		DOMSource source = new DOMSource(xmldoc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
		return writer.toString();
	}
    
	@SuppressWarnings("unchecked")
	private static void fixKeyLength() {
        String errorString = "Failed manually overriding key-length permissions.";
        int newMaxKeyLength;
        try {
            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
                Class<?> c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                Constructor<?> con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissionCollection = con.newInstance();
                Field f = c.getDeclaredField("all_allowed");
                f.setAccessible(true);
                f.setBoolean(allPermissionCollection, true);

                c = Class.forName("javax.crypto.CryptoPermissions");
                con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissions = con.newInstance();
                f = c.getDeclaredField("perms");
                f.setAccessible(true);
                ((Map<String, Object>) f.get(allPermissions)).put("*", allPermissionCollection);

                c = Class.forName("javax.crypto.JceSecurityManager");
                f = c.getDeclaredField("defaultPolicy");
                f.setAccessible(true);
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(null, allPermissions);

                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            }
        } catch (Exception e) {
            throw new RuntimeException(errorString, e);
        }
        if (newMaxKeyLength < 256)
            throw new RuntimeException(errorString); // hack failed
    }
}
