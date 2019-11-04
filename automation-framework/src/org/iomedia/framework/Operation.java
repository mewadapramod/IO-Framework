package org.iomedia.framework;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.iomedia.common.BaseUtil;
import org.iomedia.framework.Driver.HashMapNew;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Operation extends BaseUtil {
	
	public Operation(WebDriverFactory driverFactory, HashMapNew Dictionary, HashMapNew Environment, Reporting Reporter, org.iomedia.framework.Assert Assert, org.iomedia.framework.SoftAssert SoftAssert) {
		super(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert);
	}

	String tagValue = null;
	String type = null;
	String parent = null;
	boolean params = false;
	List<Object> paramList = null;
	String returnObject = null;
	int count = 0;
	boolean condition_result = false;
	boolean ifCheck = false;
	static boolean success = false;
	
	/**
	 * Replace dictionary key with its value
	 * 
	 * @author Prateek Ladha
	 * @param value
	 * @return Object
	 */
	Object replaceGDValue(Object value) {
		Object newValue = null;
		if(value instanceof String){
			if(((String)value).trim().endsWith("L") && StringUtils.isNumeric(((String)value).trim().substring(0, ((String)value).trim().length() - 1)))
				return Long.valueOf(((String)value).trim().substring(0, ((String)value).trim().length() - 1));
			newValue = "";
			if(((String) value).contains("||")){
				String[] subValues = ((String) value).split("\\|\\|");
				int i = 0;
				for(; i < subValues.length - 1; i++){
					if(subValues[i].startsWith("GD_")){
						subValues[i] = subValues[i].replaceFirst("GD_", "");
						subValues[i] = Dictionary.containsKey(subValues[i].trim()) ? Dictionary.get(subValues[i].trim()).trim() : Environment.get(subValues[i].trim()).trim();
					}
					newValue += subValues[i] + "||";
				}
				if(subValues[i].startsWith("GD_")){
					subValues[i] = subValues[i].replaceFirst("GD_", "");
					subValues[i] = Dictionary.containsKey(subValues[i].trim()) ? Dictionary.get(subValues[i].trim()).trim() : Environment.get(subValues[i].trim()).trim();
				}
				else{
					if(subValues[i].trim().contains("<GD_")){
						while(subValues[i].trim().indexOf("<GD_") > -1){
							String k = subValues[i].substring(subValues[i].indexOf("<GD_"), subValues[i].indexOf(">") + 1);
							String r = k.replaceFirst("<GD_", "").replaceFirst(">", "");
							String v = Dictionary.containsKey(r.trim()) ? Dictionary.get(r.trim()).trim() : Environment.get(r.trim()).trim();
							subValues[i] = subValues[i].replace(k, v);
					  	}
					}
				}
				newValue += subValues[i];
			}
			else{
				if(((String) value).startsWith("GD_")){
					value = ((String) value).replaceFirst("GD_", "");
					value = Dictionary.containsKey(((String) value).trim()) ? Dictionary.get(((String) value).trim()).trim() : Environment.get(((String) value).trim()).trim();
				}
				else{
					if(((String) value).trim().contains("<GD_")){
						while(((String) value).trim().indexOf("<GD_") > -1){
							String k = ((String) value).substring(((String) value).indexOf("<GD_"), ((String) value).indexOf(">") + 1);
							String r = k.replaceFirst("<GD_", "").replaceFirst(">", "");
							String v = Dictionary.containsKey(r.trim()) ? Dictionary.get(r.trim()).trim() : Environment.get(r.trim()).trim();
							value = ((String) value).replace(k, v);
					  	}
					}
				}
				newValue = value;
			}
		}
		else{
			newValue = value;
		}
		return newValue;
	}

	/**
	 * Parse array
	 * 
	 * @author Prateek Ladha
	 * @param object2
	 * @throws ParseException
	 * @throws JSONException
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ScriptException 
	 */
	public boolean parseArray(Object object2, List<Object> paramLst) throws ParseException, JSONException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ScriptException {
	    JSONArray jsonArr = (JSONArray) object2;
	    for (int k = 0; k < jsonArr.length(); k++) {
	        if (jsonArr.get(k) instanceof JSONObject) {
	            success = parseJson((JSONObject) jsonArr.get(k));
	            if(!success)
	            	return false;
	        }
	        else if(jsonArr.get(k) instanceof JSONArray){
	        	if(params == true || paramLst != null){
        			List<Object> lst = new ArrayList<Object>();
        			count++;
        			boolean successFlag = parseArray(jsonArr.get(k), lst);
        			if(count > 0)
    					count--;
        			if(successFlag == true){
        				success = true;
        				paramLst.add(lst);
        			}
        			else{
        				success = false;
        				return false;
        			}
	        	}
	        	else{
	        		success = parseArray(jsonArr.get(k), paramLst); 
		        	if(!success)
		        		return false;
	        	}
	        }
	        else {
        		if(params == false){
        			//Do Nothing
        		}
        		else{
        			if(paramLst != null){
        				paramLst.add(replaceGDValue(jsonArr.get(k)));
        			}
        		}
	        }
	    }
	    
		if((paramLst != null || params == true) && count == 0){
    		tagValue = getTagValue(parent, 0, type, 0, System.getProperty("user.dir") + Environment.get("jsonConfig").trim());
    		if(tagValue == null){
    			System.out.println("Method not found for key - '" + parent + "' in " + Environment.get("jsonConfig").trim());
    			throw new AssertionError("Method not found for key - '" + parent + "' in " + Environment.get("jsonConfig").trim());
    		}
    		String[] tags = tagValue.trim().split("\\.");
    		String methodName = tags[tags.length - 1];
			String className = tagValue.trim().substring(0, tagValue.trim().indexOf("." + methodName));
			Method[] methods;
			Object busFunctions;
			if(className.trim().contains("BaseUtil")) {
				BaseUtil base = new BaseUtil(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert, sTestDetails);
				busFunctions = base;
				methods = base.getClass().getDeclaredMethods();
			} else if(className.trim().contains("Assert")) {
	    	    busFunctions = Assert;
	    	    methods = Assert.getClass().getMethods();
			} else {
				Class<?> thisClass = Class.forName(className);
	    	    busFunctions = thisClass.getConstructor(new Class[] { WebDriverFactory.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class, ThreadLocal.class }).newInstance(new Object[] { this.driverFactory, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert, this.sTestDetails });
	    	    methods = thisClass.getDeclaredMethods();
			}
    	    Method method = null;
    	    boolean flag = false;
    	    for(int i = 0 ; i < methods.length; i++){
    	    	if(methods[i].getName().trim().equalsIgnoreCase(methodName.trim())){
    	    		method = methods[i];
    	    		if(method.getParameterTypes().length == paramLst.size()){
    	    			Object[] parameters = new Object[method.getParameterTypes().length];
    	    			boolean check = false;
    	    			for(int j = 0; j < method.getParameterTypes().length; j++){
    	    				Class<?> paramClass = method.getParameterTypes()[j];
    	    				if(paramClass.isInstance(paramLst.get(j))){
    	    					parameters[j] = paramClass.cast(paramLst.get(j));
    	    					check = true;
    	    				}
    	    				else{
    	    					check = false;
    	    					break;
    	    				}
    	        	    }
    	    			if(method.getParameterTypes().length == 0)
    	    				check = true;
    	    			
    	    			if(check == true){
    	    				flag = true;
    	    				Object objReturn = method.invoke(busFunctions, parameters);
    	    				System.out.println("Executed : " + type + " - " + parent + " - " + method.toString());
    	    				count = 0;
    	    				paramLst = null;
    	    				paramList = null;
    	    	    	    params = false;
    	    	    	    if(objReturn != null) {
		    	    	    	if(returnObject != null){
		    	    	    		Dictionary.put(returnObject, objReturn.toString().trim());
		    	    	    		returnObject = null;
		    	    	    	}
		    	    	    	else{
		    	    	    		Dictionary.put("TEMP_RETURN_VALUE", objReturn.toString().trim());
		    	    	    	}
		    	    	    	break;
    	    	    	    }
    	    			}
    	    			else{
    	    				flag = false;
    	    			}
    	    		}
    	    	}
    	    }
    	    
    	    if(flag == false){
    	    	System.out.println("Method not found : " + type + " - " + parent + " - " + tagValue);
    	    	throw new AssertionError("Method not found : " + type + " - " + parent + " - " + tagValue);
    	    }
    	    else
    	    	return true;
		}
		else{
			return success;
		}
	}

	/**
	 * Parse JSON
	 * 
	 * @author Prateek Ladha
	 * @param jsonObject
	 * @throws ParseException
	 * @throws JSONException
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException
	 * @throws ScriptException 
	 */
	@SuppressWarnings({ })
	public boolean parseJson(JSONObject jsonObject) throws ParseException, JSONException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ScriptException {
	    Iterator<String> iterator = jsonObject.keys();
	    while (iterator.hasNext()) {
	        Object obj = iterator.next();
	        if (jsonObject.get((String) obj) instanceof JSONArray) {
	        	if(obj.toString().trim().equalsIgnoreCase("USING") && type != null){
	        		count = 0;
	        		paramList = new ArrayList<Object>();
	        		params = true;
	        		Dictionary.put("TEMP_RETURN_VALUE", "");
	        	}
	        	else{
	        		paramList = null;
	        		params = false;
	        	}
	        	success = parseArray(jsonObject.get((String) obj), paramList);
	            if(!success)
	            	return false;
	        } else {
	            if (jsonObject.get((String) obj) instanceof JSONObject) {
	            	if(type != null){
	            		parent = obj.toString().trim();
	            		count = 0;
	            		paramList = null;
		        		params = false;
		        		Dictionary.put("TEMP_RETURN_VALUE", "");
		        		returnObject = null;
		        		success = parseJson((JSONObject) jsonObject.get((String) obj));
		        		if(!success)
		        			return false;
	            	}
	            } else {
	            	if(type != null && type.trim().equalsIgnoreCase("RESET")){
	        	    	if(Dictionary.get(obj.toString().trim()).trim().equalsIgnoreCase("") || !jsonObject.get((String) obj).toString().trim().contains(Dictionary.get(obj.toString().trim()).trim()))
	        	    		Dictionary.put(obj.toString().trim(), (String) replaceGDValue(jsonObject.get((String) obj).toString().trim()));
	        	    	success = true;
	        		}
	            	if(obj.toString().trim().equalsIgnoreCase("IF_EXISTS")){
	            		ifCheck = true;
						javax.script.ScriptEngine engine = new javax.script.ScriptEngineManager().getEngineByName("JavaScript");
						String expr = (String)replaceGDValue(jsonObject.get((String) obj).toString().trim());
								
	            		try {
							condition_result = (Boolean)engine.eval(expr);
						} catch (ScriptException e) {
							condition_result = false;
						}
	            	}
	            	if(obj.toString().trim().equalsIgnoreCase("SCREEN")){
	            		if(ifCheck && condition_result)
	            			type = jsonObject.get((String) obj).toString().trim();
	            		else if(!ifCheck)
	            			type = jsonObject.get((String) obj).toString().trim();
	            		else
	            			type = null;
	            		ifCheck = false;
	            		count = 0;
	            		paramList = null;
		        		params = false;
		        		Dictionary.put("TEMP_RETURN_VALUE", "");
		        		returnObject = null;
	            	}
	            	if(obj.toString().trim().equalsIgnoreCase("SAVE")){
	            		returnObject = jsonObject.get((String) obj).toString().trim();
	            		if(!Dictionary.get("TEMP_RETURN_VALUE").trim().equalsIgnoreCase("")){
	            			Dictionary.put(returnObject, Dictionary.get("TEMP_RETURN_VALUE").trim());
	            			Dictionary.put("TEMP_RETURN_VALUE", "");
	            			returnObject = null;
	            		}
	            	}
	            }
	        }
	    }
	    return true;
	}
	
	/**
	 * Retreive tag value based on tag name and parent name
	 * 
	 * @author Prateek Ladha
	 * @param tagName
	 * @param index
	 * @param parent : Root will be considered in case of null or empty value
	 * @param parentIndex
	 * @param path : path of response file
	 * @return String : tag value
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public String getTagValue(String tagName, int index, String parent, int parentIndex, String path) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		File fXmlFile = new File(path);
		String tagValue = null;
		
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
		Document xmldoc = docBuilder.parse(fXmlFile);
		
		if(parent == null || parent.trim().equalsIgnoreCase(""))
			tagValue = xmldoc.getElementsByTagName(tagName).item(index).getTextContent();
		else{
			XPathFactory xPathfac = XPathFactory.newInstance();
		    XPath xpath = xPathfac.newXPath();
		    XPathExpression expr = xpath.compile("//" + parent.trim().replace(" ", "_"));
		    if(expr != null){
		    	Node n = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex);
		    	if(n != null){
				    NodeList nl = n.getChildNodes();
				    int count = 0;
				    for (int child = 0; child < nl.getLength(); child++) {
				    	if(nl.item(child).getNodeName().trim().equalsIgnoreCase(tagName)){
				    		if(count == index){
				    			tagValue = nl.item(child).getTextContent();
				    			break;
				    		}
				    		else{
				    			count++;
				    		}
				    	}
				    }
		    	}
		    }
		    
		    if(tagValue == null){
		    	expr = xpath.compile("//COMMON");
		    	NodeList nl = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex).getChildNodes();
			    int count = 0;
			    for (int child = 0; child < nl.getLength(); child++) {
			    	if(nl.item(child).getNodeName().trim().equalsIgnoreCase(tagName)){
			    		if(count == index){
			    			tagValue = nl.item(child).getTextContent();
			    			break;
			    		}
			    		else{
			    			count++;
			    		}
			    	}
			    }
		    }
		}
		
		return tagValue.trim();
	}
	
	/**
	 * Retreive tag value based on tag name and parent name from xml
	 * 
	 * @author Prateek Ladha
	 * @param tagName
	 * @param index
	 * @param parent : Root will be considered in case of null or empty value
	 * @param parentIndex
	 * @param xml
	 * @return String : tag value
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public String getTagValueFromXMLStream(String tagName, int index, String parent, int parentIndex, String xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		String tagValue = null;
		
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		Document xmldoc = docBuilder.parse(is);
		
		if(parent == null || parent.trim().equalsIgnoreCase(""))
			tagValue = xmldoc.getElementsByTagName(tagName).item(index).getTextContent();
		else{
			XPathFactory xPathfac = XPathFactory.newInstance();
		    XPath xpath = xPathfac.newXPath();
		    XPathExpression expr = xpath.compile("//" + parent);
		    NodeList nl = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex).getChildNodes();
		    int count = 0;
		    for (int child = 0; child < nl.getLength(); child++) {
		    	if(nl.item(child).getNodeName().trim().equalsIgnoreCase(tagName)){
		    		if(count == index){
		    			tagValue = nl.item(child).getTextContent();
		    			break;
		    		}
		    		else{
		    			count++;
		    		}
		    	}
		    }
		}
		
		return tagValue.trim();
	}
	
	public String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
}
