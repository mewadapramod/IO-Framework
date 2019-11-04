package org.iomedia.framework;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.CharMatcher;

import org.iomedia.common.EncryptDecrypt;
import org.iomedia.framework.Driver.HashMapNew;

public class Infra {
	
  private Driver.HashMapNew Dictionary;
  private HashMapNew Environment = new HashMapNew();
  private HashMap<String, String> objGlobalDictOriginal = new HashMap<String, String>();
  private WebDriverFactory driverFactory;
  private String driverType;
  static Logger log = LoggerFactory.getLogger(Infra.class);
  
  public Infra(WebDriverFactory driverFactory, Driver.HashMapNew Dict, HashMapNew Env, HashMap<String, String> DictOriginal) {
    this.Dictionary = Dict == null || Dict.size() == 0 ? driverFactory.getDictionary().get() : Dict;
    this.Environment = Env ==  null || Env.size() == 0 ? driverFactory.getEnvironment().get() : Env;
    this.objGlobalDictOriginal = DictOriginal;
    this.driverFactory = driverFactory;
    driverType = driverFactory.getDriverType() == null ? null : driverFactory.getDriverType().get();
  }
  
  public void fUpdateTestCaseRowSkip(int row, String status) {
    DBActivities objDB2 = new DBActivities(this.driverFactory, this.Dictionary,this.Environment);
    String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");
    
    String sUpdateChar = "X";
    switch(status.trim().toUpperCase()){
    	case "PASS":
    		sUpdateChar = "P";
    		break;
    	case "FAIL":
    		sUpdateChar = "F";
    		break;
    	case "SKIP":
			sUpdateChar = "N";
			break;
		default:
			sUpdateChar = "X";
    }
    
    try {
      int skipColumnNo = objDB2.fGetColumnName(calendar, "MAIN").indexOf("SKIP_" + this.driverType.toUpperCase().replace(" ", ""));
      boolean success = objDB2.fUpdateDataExcel(calendar, "MAIN", new int[]{0}, new String[]{ java.lang.String.valueOf(row) }, new int[]{ skipColumnNo }, new String[]{ sUpdateChar });	
      if (success == false) {
        log.info("SKIP not set in the calendar");
        Thread.currentThread().interrupt();
      }
    }
    catch (Exception e) {
    	log.info("Threw a Exception in Infra::fUpdateTestCaseRowSkip, full stack trace follows:", e);
    	Thread.currentThread().interrupt();
    }
  }
  
  public void fClearSkip(String sActionValue) {
    DBActivities objDB2 = new DBActivities(this.driverFactory,this.Dictionary,this.Environment);
    String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");
    
    try {
      ArrayList<String> columnNames = objDB2.fGetColumnName(calendar, "MAIN");
      int skipColumnNo = columnNames.indexOf("SKIP_" + this.driverType.toUpperCase().replace(" ", ""));
      int headerColumnNo = columnNames.indexOf("HEADER");
      int groupNameColumnNo = columnNames.indexOf("GROUP_NAME");
      
      if(skipColumnNo == -1)
    	  return;
      
      if (sActionValue.equals("A")) { 
    	  if(Environment.containsKey("group") && !Environment.get("group").trim().equalsIgnoreCase("")){
    		  String[] groups = Environment.get("group").trim().split(",");
    		  String groupsName = "";
    		  int m = 0;
    		  for(m = 0; m < groups.length - 1; m++){
    			  groupsName = groupsName + groups[m] + " or ";
    		  }
    		  groupsName = groupsName + groups[m];
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, groupNameColumnNo, headerColumnNo}, new String[]{"P or F or N", groupsName, ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
    	  else{
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, headerColumnNo}, new String[]{"P or F or N", ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
      } else if (sActionValue.equals("F")) {
    	  if(Environment.containsKey("group") && !Environment.get("group").trim().equalsIgnoreCase("")){
    		  String[] groups = Environment.get("group").trim().split(",");
    		  String groupsName = "";
    		  int m = 0;
    		  for(m = 0; m < groups.length - 1; m++){
    			  groupsName = groupsName + groups[m] + " or ";
    		  }
    		  groupsName = groupsName + groups[m];
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, groupNameColumnNo, headerColumnNo}, new String[]{"F or N", groupsName, ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
    	  else{
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, headerColumnNo}, new String[]{"F or N", ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
      } else if (sActionValue.equals("P")) {
    	  if(Environment.containsKey("group") && !Environment.get("group").trim().equalsIgnoreCase("")){
    		  String[] groups = Environment.get("group").trim().split(",");
    		  String groupsName = "";
    		  int m = 0;
    		  for(m = 0; m < groups.length - 1; m++){
    			  groupsName = groupsName + groups[m] + " or ";
    		  }
    		  groupsName = groupsName + groups[m];
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, groupNameColumnNo, headerColumnNo}, new String[]{"P or N", groupsName, ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
    	  else{
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, headerColumnNo}, new String[]{"P or N", ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
      } else if (sActionValue.equals("N")) {
    	  if(Environment.containsKey("group") && !Environment.get("group").trim().equalsIgnoreCase("")){
    		  String[] groups = Environment.get("group").trim().split(",");
    		  String groupsName = "";
    		  int m = 0;
    		  for(m = 0; m < groups.length - 1; m++){
    			  groupsName = groupsName + groups[m] + " or ";
    		  }
    		  groupsName = groupsName + groups[m];
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, groupNameColumnNo, headerColumnNo}, new String[]{"N", groupsName, ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
    	  else{
    		  objDB2.fUpdateDataExcel(calendar, "MAIN",  new int[]{skipColumnNo, headerColumnNo}, new String[]{"N", ""}, new int[]{ skipColumnNo }, new String[]{ "" });
    	  }
      } else {
    	  log.info("Update SKIP Column in Data Table - The Action: " + sActionValue + " is not valid, the valid actions to be performed are A, F, or P");
      }
    }
    catch (Exception e)
    {
    	log.info("Threw a Exception in Infra::fClearSkip, full stack trace follows:", e);
      Thread.currentThread().interrupt();
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
public boolean fGetReferenceData() {
    String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");
    this.Dictionary = getDict(calendar, this.Dictionary);
    Set<?> set = this.Dictionary.entrySet();
    Iterator<?> i = set.iterator();
    while (i.hasNext()) {
      try
      {
        Map.Entry me = (Map.Entry)i.next();
        String key = me.getKey() != null ? me.getKey().toString() : "";
        String value = me.getValue() != null ? me.getValue().toString() : "";
        
        if (value.startsWith("&", 0)) {
          value = value.substring(1);
          DBActivities objDB = new DBActivities(this.driverFactory,this.Dictionary,this.Environment);
          List<List<String>> calendarFileData = objDB.fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ value });
          
          try {
            if (calendarFileData != null && calendarFileData.size() > 0) { 
              value = calendarFileData.get(0).get(2);
              if(value.trim().startsWith("%{") && value.trim().endsWith("}")) {
            	  String tempKey = value.trim().substring(value.trim().indexOf("{") + 1, value.trim().length() - 1);
            	  value = Environment.get(tempKey).trim();
              }
            }
            else {
              value = ((String)this.Dictionary.get(key).trim()).startsWith("&", 0) ? "" : (String)this.Dictionary.get(key);
            }
            me.setValue(value);
          }
          catch (Exception e)
          {
        	  log.info("Threw a Exception in Infra::fGetReferenceData, full stack trace follows:", e);
            return false;
          }
        }
        else if (value.startsWith("@", 0))
        {
        	int vFlag = 0;
        	if(value.endsWith("#")){
        		value = value.substring(1, value.length() - 1);
        		vFlag = 1;
        	}
        	else{
        		value = value.substring(1);
        		vFlag = 0;
        	}
        	
            DBActivities objDB = new DBActivities(this.driverFactory,this.Dictionary,this.Environment);
            List<List<String>> calendarFileData = objDB.fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ value });
            
            try
            {
              if (calendarFileData != null && calendarFileData.size() > 0)
              {
            	if(vFlag == 1){
            		value = ((String)this.Dictionary.get(key).trim()).startsWith("@", 0) ? "" : (String)this.Dictionary.get(key);
            	}
            	else {
            		value = calendarFileData.get(0).get(2);
            		if(value.trim().startsWith("%{") && value.trim().endsWith("}")) {
                  	  String tempKey = value.trim().substring(value.trim().indexOf("{") + 1, value.trim().length() - 1);
                  	  value = Environment.get(tempKey).trim();
                    }
            	}
              }
              else
              {
                value = ((String)this.Dictionary.get(key).trim()).startsWith("@", 0) ? "" : (String)this.Dictionary.get(key);
              }
              me.setValue(value);
            }
            catch (Exception e)
            {
            	log.info("Threw a Exception in Infra::fGetReferenceData, full stack trace follows:", e);
              return false;
            }
        }
      }
      catch (Exception err)
      {
    	  log.info("Threw a Exception in Infra::fGetReferenceData, full stack trace follows:", err);
        return false;
      }
    }
    return true;
  }
  
  @SuppressWarnings("rawtypes")
  HashMapNew getDict(String calendar, HashMapNew Dictionary){
	  	Set<?> set = Dictionary.entrySet();
	    Iterator<?> i = set.iterator();
	    HashMapNew localDict = new HashMapNew();
	    while(i.hasNext()){
	    	try{
	    		Map.Entry me = (Map.Entry)i.next();
	            String key = me.getKey() != null ? me.getKey().toString() : "";
	            String value = me.getValue() != null ? me.getValue().toString() : "";
	            if(value.contains("<GD_")  || key.contains("<GD_")){
		            String newKey = getGDValue(calendar, key);
		            String newValue = getGDValue(calendar, value);
		            if(!newValue.contains("<GD_") && !newKey.contains("<GD_")){
		            	localDict.put(key, newKey + " :: " + newValue);
		            }
	            }
	    	}
	    	catch (Exception err) {
	    		log.info("Threw a Exception in Infra::getDict, full stack trace follows:", err);
	          return null;
	        }
	    }
	    
	    set = localDict.entrySet();
	    i = set.iterator();
	    while(i.hasNext()){
	    	try{
	    		Map.Entry me = (Map.Entry)i.next();
	    		String key = me.getKey() != null ? me.getKey().toString() : "";
	            String value = me.getValue() != null ? me.getValue().toString() : "";
	    		Dictionary.put(value.trim().split("::")[0].trim(), value.trim().split("::")[1].trim());
	    		Dictionary.remove(key);
	    	}
	    	catch (Exception err) {
	    		log.info("Threw a Exception in Infra::getDict, full stack trace follows:", err);
	          return null;
	        }
	    }
	    return Dictionary;
  	}
  
  @SuppressWarnings("rawtypes")
  HashMap<String,String> getDict(String calendar, HashMap<String,String> Dictionary){
	  	Set<?> set = Dictionary.entrySet();
	    Iterator<?> i = set.iterator();
	    HashMapNew localDict = new HashMapNew();
	    while(i.hasNext()){
	    	try{
	    		Map.Entry me = (Map.Entry)i.next();
	    		String key = me.getKey() != null ? me.getKey().toString() : "";
	            String value = me.getValue() != null ? me.getValue().toString() : "";
	            if(value.contains("<GD_")  || key.contains("<GD_")){
		            String newKey = getGDValue(calendar, key);
		            String newValue = getGDValue(calendar, value);
		            if(!newValue.contains("<GD_") && !newKey.contains("<GD_")){
		            	localDict.put(key, newKey + " :: " + newValue);
		            }
	            }
	    	}
	    	catch (Exception err)
	        {
	    		log.info("Threw a Exception in Infra::getDict, full stack trace follows:", err);
	          return null;
	        }
	    }
	    
	    set = localDict.entrySet();
	    i = set.iterator();
	    while(i.hasNext()){
	    	try{
	    		Map.Entry me = (Map.Entry)i.next();
	    		String key = me.getKey() != null ? me.getKey().toString() : "";
	            String value = me.getValue() != null ? me.getValue().toString() : "";
	    		Dictionary.put(value.trim().split("::")[0].trim(), value.trim().split("::")[1].trim());
	    		Dictionary.remove(key);
	    	}
	    	catch (Exception err)
	        {
	    		log.info("Threw a Exception in Infra::getDict, full stack trace follows:", err);
	          return null;
	        }
	    }
	    return Dictionary;
	}

  String getValueFromRefer(String calendar, String value){
	  
	  if (value.startsWith("&", 0))
      {
        value = value.substring(1);
        DBActivities objDB = new DBActivities(this.driverFactory,this.Dictionary,this.Environment);
        
        List<List<String>> calendarFileData = objDB.fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ value });
        
        try
        {
          if (calendarFileData != null && calendarFileData.size() > 0)
          { 
            value = calendarFileData.get(0).get(2);
          }
          else
          {
            value = "";
          }
        }
        catch (Exception e)
        {
        	log.info("Threw a Exception in Infra::getValueFromRefer, full stack trace follows:", e);
          return "";
        }
      }
      else if (value.startsWith("@", 0))
      {
      	int vFlag = 0;
      	if(value.endsWith("#")){
      		value = value.substring(1, value.length() - 1);
      		vFlag = 1;
      	}
      	else{
      		value = value.substring(1);
      		vFlag = 0;
      	}
      	
          DBActivities objDB = new DBActivities(this.driverFactory,this.Dictionary,this.Environment);
          List<List<String>> calendarFileData = objDB.fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ value });
          
          try
          {
            if (calendarFileData != null && calendarFileData.size() > 0)
            {
          	if(vFlag == 1){
          		value = "";
          	}
          	else
          		value = calendarFileData.get(0).get(2);
            }
            else
            {
              value = "";
            }
          }
          catch (Exception e)
          {
        	  log.info("Threw a Exception in Infra::getValueFromRefer, full stack trace follows:", e);
            return "";
          }
      }
	  
	  return value;
  }
  
  String getGDValue(String calendar, String value){
  	while(value.indexOf("<GD_") > -1){
  		String k = value.substring(value.indexOf("<GD_"), value.indexOf(">") + 1);
  		String r = k.replace("<GD_", "").replace(">", "");
  		String v = Dictionary.get(r).trim();
  		v = getValueFromRefer(calendar, v);
  		if(!v.equalsIgnoreCase("")){
  			value = value.replace(k, v.trim().toUpperCase().replace(" ", "_"));
  		}
  		else{
  			break;
  		}
  	}
  	return value;
  }
  
  @SuppressWarnings("rawtypes")
  public boolean fSetReferenceData() {
    String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");  
    this.objGlobalDictOriginal = getDict(calendar, this.objGlobalDictOriginal);
    Set<?> set = this.objGlobalDictOriginal.entrySet();    
    Iterator<?> i = set.iterator();
    
    try
    {
	 while (i.hasNext())
      {
        Map.Entry me = (Map.Entry)i.next();
        String key = me.getKey() != null ? me.getKey().toString() : "";
        String value = me.getValue() != null ? me.getValue().toString() : "";
        if (value.startsWith("@", 0))
        {
          String tempKey = "";
          if(value.endsWith("#")){
        	  tempKey = value.substring(1, value.length() - 1);
          }
          else
        	  tempKey = value.substring(1);
          
          String tempValue;
          
          if (!this.Dictionary.get(key).equalsIgnoreCase(((String)this.objGlobalDictOriginal.get(key)).substring(1))) {
            tempValue = this.Dictionary.get(key);
          } else {
            tempValue = "";
          }
          
          DBActivities objDB = new DBActivities(this.driverFactory, this.Dictionary,this.Environment);
          List<List<String>> calendarFileData = objDB.fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ tempKey });
          
          if (calendarFileData == null) {
            return false;
          }
          
          boolean success = false;
          
          try {
            if (calendarFileData.size() == 0) {
              success = objDB.fInsertDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(), new int[]{1,2}, new String[]{tempKey, tempValue});
            } else {
            	success = objDB.fUpdateDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(), new int[]{1}, new String[]{ tempKey }, new int[]{2}, new String[]{ tempValue });
            }
            
            if (success == false)
            {
              return false;
            }
          }
          catch (Exception e) {
        	  log.info("Threw a Exception in Infra::fSetReferenceData, full stack trace follows:", e);
            return false;
          }
        }
      }
    }
    catch (Exception err) {
    	log.info("Threw a Exception in Infra::fSetReferenceData, full stack trace follows:", err);
    	return false;
    }
    return true;
  }
  
  public String createConsolidatedReport(String sConsolidatedReportFile, String strTimeDifference, String resultPath, HashMapNew duration, HashMapNew csvs) throws Exception {
	  if(!new File(resultPath).exists()){
		  return null;
	  }
	  FileOutputStream foutStrm = null;
	  foutStrm = new FileOutputStream(sConsolidatedReportFile, true);
	  PrintStream stream = new PrintStream(foutStrm);
	  String msg = null, msg1 = null;
	  try{
		  
		  //Write in Report file
		  String str = "<html> <head> <meta http-equiv=Content-Type content=\"text/html; charset=utf-8\"> <meta name=ProgId content=Excel.Sheet> <meta name=Generator content=\"Microsoft Excel 15\"> <link rel=File-List href=\"Mail%20View.fld/filelist.xml\"> <style> <!--table {mso-displayed-decimal-separator:\"\\.\"; mso-displayed-thousand-separator:\"\\,\";} @page {margin:.75in .7in .75in .7in; mso-header-margin:.3in; mso-footer-margin:.3in;} .style0 {mso-number-format:General; text-align:general; vertical-align:bottom; white-space:nowrap; mso-rotate:0; mso-background-source:auto; mso-pattern:auto; color:black; font-size:10.0pt; font-weight:400; font-style:normal; text-decoration:none; font-family:Arial; mso-generic-font-family:auto; mso-font-charset:0; border:none; mso-protection:locked visible; mso-style-name:Normal; mso-style-id:0;} td {mso-style-parent:style0; padding-top:1px; padding-right:1px; padding-left:1px; mso-ignore:padding; color:black; font-size:10.0pt; font-weight:400; font-style:normal; text-decoration:none; font-family:Arial; mso-generic-font-family:auto; mso-font-charset:0; mso-number-format:General; text-align:general; vertical-align:bottom; border:none; mso-background-source:auto; mso-pattern:auto; mso-protection:locked visible; white-space:nowrap; mso-rotate:0;} .xl65 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; border:.5pt solid black; background:white; mso-pattern:white none;} .xl66 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid black; background:white; mso-pattern:white none;} .xl67 {mso-style-parent:style0; color:windowtext; border:.5pt solid black;} .xl68 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border:.5pt solid black; background:black; mso-pattern:black none;} .xl69 {mso-style-parent:style0; color:#6AA84F; font-weight:700; border:.5pt solid black; background:black; text-align: center; mso-pattern:black none;} .xl70 {mso-style-parent:style0; color:#CC0000; font-weight:700; border:.5pt solid black; background:black; text-align: center; mso-pattern:black none;} .xl71 {mso-style-parent:style0; color:white; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black; background:black; mso-pattern:black none;} .xl72 {mso-style-parent:style0; color:windowtext; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:none;} .xl73 {mso-style-parent:style0; color:windowtext; border-top:.5pt solid black; border-right:.5pt solid black; border-bottom:.5pt solid black; border-left:none;} .xl74 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black; background:black; mso-pattern:black none;} .xl75 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black;} .xl76 {mso-style-parent:style0; text-align:center; vertical-align:middle; border-top:.5pt solid black; border-right:.5pt solid black; border-bottom:none; border-left:.5pt solid black; background:white; mso-pattern:white none;} .xl77 {mso-style-parent:style0; color:windowtext; border-top:none; border-right:.5pt solid black; border-bottom:.5pt solid black; border-left:.5pt solid black;} .xl78 {mso-style-parent:style0; color:white; font-weight:700; font-family:Arial, sans-serif; mso-font-charset:0; text-align:center; vertical-align:middle; border:.5pt solid windowtext; background:black; mso-pattern:#6FA8DC none;} .xl79 {mso-style-parent:style0; font-weight:700; text-align:center; vertical-align:middle; border:.5pt solid windowtext; background:#6FA8DC; mso-pattern:#6FA8DC none;} .xl80 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:white; mso-pattern:white none;} .xl81 {mso-style-parent:style0; color:windowtext; border:.5pt solid windowtext;} .xl82 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:#6D9EEB; mso-pattern:#6D9EEB none;} .xl83 {mso-style-parent:style0; color:white; font-family:Arial, sans-serif; mso-font-charset:0; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl84 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; vertical-align:middle; border:.5pt solid windowtext;} .xl85 {mso-style-parent:style0; color:windowtext; text-align:center; border:.5pt solid windowtext;} .xl86 {mso-style-parent:style0; color:windowtext; mso-number-format:0%; text-align:center; border:.5pt solid windowtext;} .xl87 {mso-style-parent:style0; color:windowtext; font-weight:700; border:.5pt solid windowtext;} .xl88 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl89 {mso-style-parent:style0; color:white; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl90 {mso-style-parent:style0; color:#6AA84F; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl91 {mso-style-parent:style0; color:#CC0000; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl92 {mso-style-parent:style0; color:#F1C232; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl93 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:#6FA8DC; mso-pattern:#6FA8DC none;} --> </style> <title> Consolidated Report </title> </head> <body>";
		  msg = str + "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>";
		  String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		  String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
		  String machineName = env.toUpperCase();
		  try{
			  machineName = InetAddress.getLocalHost().getHostName();
		  } catch(Exception ex){
			  //Do nothing;
		  }
		  msg += "<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>";
		  msg += "<br/>";
		  
		  stream.println(msg);
			
		  GlobalFunctions gblFunctions = new GlobalFunctions();
		  HashMapNew result = gblFunctions.readResultsCsv(resultPath, duration, csvs);
		  
		  String[] severity = result.get("SEVERITY").trim().split(",");
		  Arrays.sort(severity);
		  String[] testTypes = result.get("TEST_TYPES").trim().split(",");
		  
		  msg1 = "<table border=\"0\" cellpadding=\"3\" cellspacing=\"1\" width=\"100%\" style=\"border-collapse:collapse;\"><tbody>";
		  HashMapNew temp;
		  String environment = "";
		  String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
		  if(!envConfig.trim().equalsIgnoreCase("")) {
			  temp = GetXMLNodeValueFromString(envConfig, "//CONFIG", 0);
			  if(!temp.get("APP_URL").trim().equalsIgnoreCase("")) {
				  String appurl = temp.get("APP_URL").trim();
				  if(appurl.trim().endsWith("/"))
					  appurl = appurl.trim().substring(0, appurl.trim().length() - 1);
				  String clientId = appurl.substring(appurl.lastIndexOf("/") + 1).trim().toUpperCase();
				  environment = clientId;
			  }
		  } else {
			  environment = Environment.get("env").trim().toUpperCase(); 
		  }
		  String appVersion = Environment.get("appVersion").trim().equalsIgnoreCase("") ? "" : " - " + Environment.get("appVersion").trim(); 
		  msg1 += "<tr height=\"20\" style=\"background-color: black;\"> <td colspan=\"" + (((severity.length+1) * 2) + 1) + "\" height=\"20\" class=\"xl74\" width=\"100%\" style=\"border-right:.5pt solid black;height:15.75pt;\">" + environment + appVersion + "</td></tr>";
		  msg1 += "<tr height=20> <td colspan=" + (((severity.length+1) * 2) + 1) + " height=20 class=xl71 style='border-right:.5pt solid black; height:15.75pt;'>Total Execution Time : " + strTimeDifference + "</td></tr>";
		  msg1 += "<tr height=20 style='height:15.75pt'> <td rowspan=2 height=40 class=xl76 style='border-bottom:.5pt solid black; height:31.5pt;border-top:none'>&nbsp;</td> ";
		  
		  for (int i = 0; i < severity.length; i++) {
			msg1 += "<td colspan=2 class=xl75 style='border-right:.5pt solid black;border-left: none'>" + severity[i] + "</td> ";
		  }
		  msg1 += "<td colspan=2 class=xl79 style='border-right:.5pt solid black;border-left: none'>TOTAL</td></tr>";
		  msg1 += "<tr height=20 style='height:15.75pt'>";
		  
		  for (int i = 0; i < severity.length; i++) {
			  msg1 += "<td height=20 class=xl65 style='border-top:none;border-left:none'>Pass</td>";
			  msg1 += "<td class=xl65 style='border-top:none;border-left:none'>Fail</td>"; 
		  }
		  
		  msg1 += "<td height=20 class=xl79 style='border-top:none;border-left:none'>Pass</td>";
		  msg1 += "<td class=xl79 style='border-top:none;border-left:none'>Fail</td>";
		  msg1 += "</tr>";
		  
		  String color = "#EFEFEF";
		  int[][] severity_total = new int[severity.length][2];
		  int[] total_total = new int[]{0, 0};
		  
		  for(int i = 0; i < severity_total.length; i++) {
			  severity_total[i][0] = 0;
			  severity_total[i][1] = 0;
		  }
		  
		  String filename = sConsolidatedReportFile.trim().substring(sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter) + 1);
//		  String folderPath = sConsolidatedReportFile.trim().substring(sConsolidatedReportFile.lastIndexOf("Reports"), sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter));
		  
		  for (int i = 0; i < testTypes.length; i++) {
			  String reportFile = sConsolidatedReportFile.trim().substring(0, sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter) + 1) + testTypes[i] + "_" + filename.trim().substring(filename.lastIndexOf("_") + 1);
			  reportFile = createDetailedReport(testTypes[i], reportFile, result);
			  String reportsUrl = testTypes[i] + "_" + filename.trim().substring(filename.lastIndexOf("_") + 1);
//			  if(System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")){
//					String jenkinsIp = System.getProperty("jenkinsIp") != null && !System.getProperty("jenkinsIp").trim().equalsIgnoreCase("") ? System.getProperty("jenkinsIp").trim().toLowerCase() : Environment.get("jenkinsIpAddress").trim().toLowerCase();
//					String url = folderPath;
////					if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")){
////						url += "/" + System.getProperty("branch").trim();
////					}
//					url += "/" +  reportsUrl;
//					reportsUrl = "http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + url;
//			  }
			  msg1 += "<tr height=20 style='height:15.75pt'><td height=20 class=xl66 style='height:15.75pt;border-top:none;font-size:10.0pt;color:black;font-weight:700;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid black;background:white;mso-pattern:white none'><a style=\"color:black;\" href='" + reportsUrl + "'>" + testTypes[i] + "</a></td>";
			  if(i%2 == 0) {
				  color = "white";
			  } else {
				  color = "#EFEFEF";
			  }
			  int total_pass_value = 0;
			  int total_fail_value = 0;
			  for(int j = 0; j < severity.length; j++) {
				  String pass_value = result.get(testTypes[i] + "_" + severity[j] + "_" + "PASS");
				  String fail_value = result.get(testTypes[i] + "_" + severity[j] + "_" + "FAIL");
				  pass_value = pass_value.trim().equalsIgnoreCase("") ? "0" : pass_value.trim();
				  fail_value = fail_value.trim().equalsIgnoreCase("") ? "0" : fail_value.trim();
				  
				  String pass_url = "#", fail_url = "#";
				  if(!pass_value.trim().equalsIgnoreCase("0")) {
					  pass_url = createConsolidatedTestTypeSeverityBasedReport(sConsolidatedReportFile, filename, testTypes[i], severity[j], "PASS", result);
			  	  }
				  if(!fail_value.trim().equalsIgnoreCase("0")) {
					  fail_url = createConsolidatedTestTypeSeverityBasedReport(sConsolidatedReportFile, filename, testTypes[i], severity[j], "FAIL", result);
				  }
				  
				  msg1 += "<td class=xl66 align=right style='border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid black;background:" + color + ";mso-pattern:" + color + " none'><a style=\"text-decoration:none;\" href='" + pass_url + "'>" + pass_value + "</a></td>";
				  msg1 += "<td class=xl66 align=right style='border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid black;background:" + color + ";mso-pattern:" + color + " none'><a style=\"text-decoration:none;\" href='" + fail_url + "'>" + fail_value + "</a></td>";
				  total_pass_value += Integer.valueOf(pass_value);
				  total_fail_value += Integer.valueOf(fail_value);
				  severity_total[j][0] += Integer.valueOf(pass_value);
				  severity_total[j][1] += Integer.valueOf(fail_value);
			  }
			  
			  msg1 += "<td class=xl66 align=right style='border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid black;background:" + color + ";mso-pattern:" + color + " none'>" + total_pass_value + "</td>";
			  msg1 += "<td class=xl66 align=right style='border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid black;background:" + color + ";mso-pattern:" + color + " none'>" + total_fail_value + "</td>";
			  total_total[0] += total_pass_value;
			  total_total[1] += total_fail_value;
			  msg1 += "</tr>";
		  }
		  
		  msg1 += "<tr height=20 style='height:15.75pt'>";
		  msg1 += "<td height=20 class=xl68 style='height:15.75pt;border-top:none'>Total</td>";
		  
		  for(int i = 0; i < severity_total.length; i++) {
			  msg1 += "<td class=xl69 align=right style='border-top:none;border-left:none'>" + severity_total[i][0] + "</td>";
			  msg1 += "<td class=xl70 align=right style='border-top:none;border-left:none'>" + severity_total[i][1] + "</td>";
		  }
		  
		  msg1 += "<td class=xl69 align=right style='border-top:none;border-left:none'>" + total_total[0] + "</td>";
		  msg1 += "<td class=xl70 align=right style='border-top:none;border-left:none'>" + total_total[1] + "</td>";
		  msg1 += "</tr></tbody></table></body></html>";
		  stream.println(msg1);
		  
		  log.info("Consolidated Report File Path : " + sConsolidatedReportFile);
	  } finally {
		  //Close the object
		  if(stream != null) {
			  stream.close();
		  }
		  if(foutStrm != null)
			  foutStrm.close();
	  }
	  
	  if(msg != null && msg1 != null) {
		  return msg+msg1;
	  } else
		  return null;
  }
  
  public String createDetailedReport(String reportType, String reportFile, HashMapNew result) throws Exception {
	  FileOutputStream foutStrm = null;
	  foutStrm = new FileOutputStream(reportFile, true);
	  PrintStream stream = new PrintStream(foutStrm);
	  String msg = null, msg1 = null;
	  try{
		  //Write in Report file
		  String str = "<html> <head> <meta http-equiv=Content-Type content=\"text/html; charset=utf-8\"> <meta name=ProgId content=Excel.Sheet> <meta name=Generator content=\"Microsoft Excel 15\"> <link rel=File-List href=\"Mail%20View.fld/filelist.xml\"> <style> <!--table {mso-displayed-decimal-separator:\"\\.\"; mso-displayed-thousand-separator:\"\\,\";} @page {margin:.75in .7in .75in .7in; mso-header-margin:.3in; mso-footer-margin:.3in;} .style0 {mso-number-format:General; text-align:general; vertical-align:bottom; white-space:nowrap; mso-rotate:0; mso-background-source:auto; mso-pattern:auto; color:black; font-size:10.0pt; font-weight:400; font-style:normal; text-decoration:none; font-family:Arial; mso-generic-font-family:auto; mso-font-charset:0; border:none; mso-protection:locked visible; mso-style-name:Normal; mso-style-id:0;} td {mso-style-parent:style0; padding-top:1px; padding-right:1px; padding-left:1px; mso-ignore:padding; color:black; font-size:10.0pt; font-weight:400; font-style:normal; text-decoration:none; font-family:Arial; mso-generic-font-family:auto; mso-font-charset:0; mso-number-format:General; text-align:general; vertical-align:bottom; border:none; mso-background-source:auto; mso-pattern:auto; mso-protection:locked visible; white-space:nowrap; mso-rotate:0;} .xl65 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; border:.5pt solid black; background:white; mso-pattern:white none;} .xl66 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid black; background:white; mso-pattern:white none;} .xl67 {mso-style-parent:style0; color:windowtext; border:.5pt solid black;} .xl68 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border:.5pt solid black; background:black; mso-pattern:black none;} .xl69 {mso-style-parent:style0; color:#6AA84F; font-weight:700; border:.5pt solid black; background:black; text-align: center; mso-pattern:black none;} .xl70 {mso-style-parent:style0; color:#CC0000; font-weight:700; border:.5pt solid black; background:black; text-align: center; mso-pattern:black none;} .xl71 {mso-style-parent:style0; color:white; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black; background:black; mso-pattern:black none;} .xl72 {mso-style-parent:style0; color:windowtext; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:none;} .xl73 {mso-style-parent:style0; color:windowtext; border-top:.5pt solid black; border-right:.5pt solid black; border-bottom:.5pt solid black; border-left:none;} .xl74 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black; background:black; mso-pattern:black none;} .xl75 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; border-top:.5pt solid black; border-right:none; border-bottom:.5pt solid black; border-left:.5pt solid black;} .xl76 {mso-style-parent:style0; text-align:center; vertical-align:middle; border-top:.5pt solid black; border-right:.5pt solid black; border-bottom:none; border-left:.5pt solid black; background:white; mso-pattern:white none;} .xl77 {mso-style-parent:style0; color:windowtext; border-top:none; border-right:.5pt solid black; border-bottom:.5pt solid black; border-left:.5pt solid black;} .xl78 {mso-style-parent:style0; color:white; font-weight:700; font-family:Arial, sans-serif; mso-font-charset:0; text-align:center; vertical-align:middle; border:.5pt solid windowtext; background:black; mso-pattern:#6FA8DC none;} .xl79 {mso-style-parent:style0; font-weight:700; text-align:center; vertical-align:middle; border:.5pt solid windowtext; background:#6FA8DC; mso-pattern:#6FA8DC none;} .xl80 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:white; mso-pattern:white none;} .xl81 {mso-style-parent:style0; color:windowtext; border:.5pt solid windowtext;} .xl82 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:#6D9EEB; mso-pattern:#6D9EEB none;} .xl83 {mso-style-parent:style0; color:white; font-family:Arial, sans-serif; mso-font-charset:0; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl84 {mso-style-parent:style0; color:windowtext; font-weight:700; text-align:center; vertical-align:middle; border:.5pt solid windowtext;} .xl85 {mso-style-parent:style0; color:windowtext; text-align:center; border:.5pt solid windowtext;} .xl86 {mso-style-parent:style0; color:windowtext; mso-number-format:0%; text-align:center; border:.5pt solid windowtext;} .xl87 {mso-style-parent:style0; color:windowtext; font-weight:700; border:.5pt solid windowtext;} .xl88 {mso-style-parent:style0; color:white; font-weight:700; text-align:center; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl89 {mso-style-parent:style0; color:white; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl90 {mso-style-parent:style0; color:#6AA84F; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl91 {mso-style-parent:style0; color:#CC0000; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl92 {mso-style-parent:style0; color:#F1C232; font-weight:700; border:.5pt solid windowtext; background:black; mso-pattern:black none;} .xl93 {mso-style-parent:style0; font-weight:700; text-align:center; border:.5pt solid windowtext; background:#6FA8DC; mso-pattern:#6FA8DC none;} --> </style> <title> Consolidated Report </title> </head> <body>";
		  msg = str + "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>";
		  String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		  String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
		  String machineName = env.toUpperCase();
		  try{
			  machineName = InetAddress.getLocalHost().getHostName();
		  } catch(Exception ex){
			  //Do nothing;
		  }
		  msg += "<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>";
		  msg += "<br/>";
		  
		  stream.println(msg);
			
		  String[] severity = result.get("SEVERITY").trim().split(",");
		  Arrays.sort(severity);
		  String[] modules = result.get(reportType).trim().split(",");
		  
		  msg1 = "<table border=\"0\" cellpadding=\"3\" cellspacing=\"1\" width=\"100%\" style=\"border-collapse:collapse;\"><tbody>";
		  msg1 += "<tr height=\"20\" style=\"mso-height-source:userset;height:15.75pt\"> <td rowspan=\"2\" height=\"40\" class=\"xl78\" style=\"height:31.5pt\">"+ reportType + "</td> <td rowspan=2 class=\"xl79\">Browser</td> <td rowspan=2 class=xl79>Duration</td> <td rowspan=2 class=xl79>Pass %</td>";
		  for (int i = 0; i < severity.length; i++) {
			msg1 += "<td colspan=3 class=xl80 style='border-left:none'>" + severity[i] + "</td> ";
		  }
		  msg1 += "<td colspan=3 class=xl79 style='border-left:none'>TOTAL</td> </tr>";
		  
		  for (int i = 0; i < severity.length; i++) {
			  msg1 += "<td height=20 class=xl80 style='height:15.75pt;border-top:none;border-left:none'>Pass</td>";
			  msg1 += "<td class=\"xl80\" style=\"border-top:none;border-left:none\">Fail</td>"; 
			  msg1 += "<td class=\"xl80\" style=\"border-top:none;border-left:none\">Skip</td>";
		  }
		  
		  msg1 += "<td height=20 class=xl79 style='height:15.75pt;border-top:none;border-left:none'>Pass</td>";
		  msg1 += "<td class=\"xl79\" style=\"border-top:none;border-left:none\">Fail</td>"; 
		  msg1 += "<td class=\"xl79\" style=\"border-top:none;border-left:none\">Skip</td></tr>";
		  
		  String color = "#EFEFEF";
		  int[][] severity_total = new int[severity.length][3];
		  int[] total_total = new int[]{0, 0, 0};
		  
		  for(int i = 0; i < severity_total.length; i++) {
			  severity_total[i][0] = 0;
			  severity_total[i][1] = 0;
			  severity_total[i][2] = 0;
		  }
		  
		  int count = 0;
		  for (int i = 0; i < modules.length; i++) {
			  String[] browsers = result.get(reportType + "_" + modules[i]).trim().split(",");
			  msg1 += "<tr height=\"20\" style=\"mso-height-source:userset;height:15.75pt\">";
			  msg1 += "<td rowspan=\"" + browsers.length + "\" height=\"100%\" class=\"xl84\" style=\"border-top:none\">" + modules[i] + "</td>";
			  
			  String duration = "";
			  String summaryUrl = "";
			  
			  for(int j = 0 ; j < browsers.length; j++) {
				  if((count+j)%2 == 0) {
					  color = "white";
				  } else {
					  color = "#EFEFEF";
				  }
				  String msg2 = "";
				  if(j > 0) {
					  msg2 += "<tr height=\"20\" style=\"mso-height-source:userset;height:15.75pt\">";
				  }
				  duration = result.get(reportType + "_" + modules[i] + "_" + browsers[j] + "_" + "DURATION");
				  summaryUrl = result.get(reportType + "_" + modules[i] + "_" + browsers[j] + "_" + "SUMMARY_URL");
				  
				  int total_pass_value = 0;
				  int total_fail_value = 0;
				  int total_skip_value = 0;
				  String msg3 = "";
				  for(int k = 0; k < severity.length; k++) {
					  String pass_value = result.get(reportType + "_" + modules[i] + "_" + browsers[j] + "_" + severity[k] + "_" + "PASS");
					  String fail_value = result.get(reportType + "_" + modules[i] + "_" + browsers[j] + "_" + severity[k] + "_" + "FAIL");
					  String skip_value = result.get(reportType + "_" + modules[i] + "_" + browsers[j] + "_" + severity[k] + "_" + "SKIP");
					  pass_value = pass_value.trim().equalsIgnoreCase("") ? "0" : pass_value.trim();
					  fail_value = fail_value.trim().equalsIgnoreCase("") ? "0" : fail_value.trim();
					  skip_value = skip_value.trim().equalsIgnoreCase("") ? "0" : skip_value.trim();
					  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + pass_value + "</td>";
					  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + fail_value + "</td>";
					  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + skip_value + "</td>";
					  total_pass_value += Integer.valueOf(pass_value);
					  total_fail_value += Integer.valueOf(fail_value);
					  total_skip_value += Integer.valueOf(skip_value);
					  severity_total[k][0] += Integer.valueOf(pass_value);
					  severity_total[k][1] += Integer.valueOf(fail_value);
					  severity_total[k][2] += Integer.valueOf(skip_value);
				  }
				  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:700;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + total_pass_value + "</td>";
				  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:700;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + total_fail_value + "</td>";
				  msg3 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:700;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">" + total_skip_value + "</td></tr>";
				  total_total[0] += total_pass_value;
				  total_total[1] += total_fail_value;
				  total_total[2] += total_skip_value;
				  String passpercentage = String.valueOf((int)Math.round(((float)(total_pass_value)/(total_pass_value + total_fail_value)) * 100)) + "%";
				  msg2 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\"><a style=\"text-decoration:none;\" href='" + summaryUrl + "'>" + browsers[j] + "</a></td>";
				  msg2 += "<td class=\"xl85\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\">"+ duration +"</td>";
				  msg2 += "<td class=\"xl86\" style=\"border-top:none;border-left:none;font-size:10.0pt;color:windowtext;font-weight:400;text-decoration:none;text-underline-style:none;text-line-through:none;font-family:Arial;border:.5pt solid windowtext;background:" + color + ";mso-pattern:" + color + " none\"><a style=\"text-decoration:none;\" href='" + summaryUrl + "'>" + passpercentage + "</a></td>";
				  
				  msg1 += msg2 + msg3; 
			  }
			  count += browsers.length;
		  }
		  msg1 += "<tr height=\"20\" style=\"mso-height-source:userset;height:15.75pt\">";
		  msg1 += "<td height=\"20\" class=\"xl88\" style=\"height:15.75pt;border-top:none\">Total</td>";
		  msg1 += "<td class=\"xl89\" style=\"border-top:none;border-left:none\">&nbsp;</td>";
		  msg1 += "<td class=\"xl89\" style=\"border-top:none;border-left:none\">&nbsp;</td>";
		  msg1 += "<td class=\"xl89\" style=\"border-top:none;border-left:none\">&nbsp;</td>";
		  
		  for(int i = 0; i < severity_total.length; i++) {
			  msg1 += "<td class=\"xl90\" style=\"border-top:none;border-left:none\" align=\"center\">" + severity_total[i][0] + "</td>";
			  msg1 += "<td class=\"xl91\" align=\"center\" style=\"border-top:none;border-left:none\">" + severity_total[i][1] + "</td>";
			  msg1 += "<td class=\"xl92\" align=\"center\" style=\"border-top:none;border-left:none\">" + severity_total[i][2] + "</td>";
		  }
		  msg1 += "<td class=\"xl90\" style=\"border-top:none;border-left:none\" align=\"center\">" + total_total[0] + "</td>";
		  msg1 += "<td class=\"xl91\" align=\"center\" style=\"border-top:none;border-left:none\">" + total_total[1] + "</td>";
		  msg1 += "<td class=\"xl92\" align=\"center\" style=\"border-top:none;border-left:none\">" + total_total[2] + "</td></tr>";
		  msg1 += "</tbody></table></body></html>";
		  stream.println(msg1);
		  
		  log.info("Detailed Report File Path for " + reportType + " : " + reportFile);
	  } finally {
		  //Close the object
		  if(stream != null) {
			  stream.close();
		  }
		  if(foutStrm != null)
			  foutStrm.close();
	  }

	  return reportFile;
  	}	  
  
  String createConsolidatedTestTypeSeverityBasedReport(String sConsolidatedReportFile, String filename, String testType, String severity, String status, HashMapNew result) throws IOException, ParseException {
//	  String folderPath = sConsolidatedReportFile.trim().substring(sConsolidatedReportFile.lastIndexOf("Reports"), sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter));
	  String reportFile = sConsolidatedReportFile.trim().substring(0, sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter) + 1) + testType + "_" + severity + "_" + status.trim().toUpperCase() + "_" + filename.trim().substring(filename.lastIndexOf("_") + 1);
	  String reportsUrl = testType + "_" + severity + "_" + status.trim().toUpperCase() + "_" + filename.trim().substring(filename.lastIndexOf("_") + 1);
//	  if(System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")){
//			String jenkinsIp = System.getProperty("jenkinsIp") != null && !System.getProperty("jenkinsIp").trim().equalsIgnoreCase("") ? System.getProperty("jenkinsIp").trim().toLowerCase() : Environment.get("jenkinsIpAddress").trim().toLowerCase();
//			String url = folderPath;
////			if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")){
////				url += "/" + System.getProperty("branch").trim();
////			}
//			url += "/" +  reportsUrl;
//			reportsUrl = "http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + url;
//	  }
	  createSummaryReport(reportFile, testType, severity, status);
	  String data = result.get(testType + "_" + severity + "_" + status.trim().toUpperCase() + "_" + "TEST_NAME");
	  String[] testcaseDetails = data.trim().split("####");
	  for(int i = 0; i < testcaseDetails.length; i++) {
		  if(!testcaseDetails[i].trim().equalsIgnoreCase("")) {
			  String[] details = testcaseDetails[i].trim().split("\\|\\|");
			  String browser = details[0].trim();
			  String moduleName = details[1].trim();
			  String testcasename = details[2].trim();
			  String duration = details[3].trim();
			  String reportsPath = details[4].trim();
			  writeTestSummary(reportFile, testcasename, status, duration, browser, moduleName, reportsPath, i);
		  }
	  }
	  closeTestSummary(reportFile);
	  return reportsUrl;
  }
  
  void createSummaryReport(String reportFile, String testType, String severity, String status) throws IOException {
		FileOutputStream foutStrm = null;
		foutStrm = new FileOutputStream(reportFile, true);
		PrintStream stream = new PrintStream(foutStrm);
		try {
		  stream.println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>");
		  String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		  String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
		  String machineName = env.toUpperCase();
		  try{
			  machineName = InetAddress.getLocalHost().getHostName();
		  } catch(Exception ex){
			  //Do nothing;
		  }
		  stream.println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting [" + toCamelCase(testType) + " - " + toCamelCase(severity) + "]</B><FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>");
		  stream.println("<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
		  stream.println("<TR COLS=5 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=20%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Device/Browser Name</B></FONT></TD><TD  WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Module Name</B></FONT></TD><TD  WIDTH=50%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Name</B></FONT></TD><TD BGCOLOR=" + Environment.get("reportColor") + " WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Status</B></FONT></TD><TD  WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Duration</B></FONT></TD></TR>");
		} finally {
			//Close the object
			  if(stream != null) {
				  stream.close();
			  }
			  if(foutStrm != null)
				  foutStrm.close();
		}
  	}
  
  void writeTestSummary(String reportFile, String strTestCaseName, String strResult, String strDuration, String browser, String moduleName, String reportsPath,  int index) throws IOException {
	  FileOutputStream foutStrm = null;
	  foutStrm = new FileOutputStream(reportFile, true);
	  PrintStream stream = new PrintStream(foutStrm);
	  try {
		  String sColor;
		  if ((strResult.toUpperCase().equals("PASSED")) || (strResult.toUpperCase().equals("PASS"))) {
			sColor = "GREEN";
		  }
		  else {        
			if ((strResult.toUpperCase().equals("FAILED")) || (strResult.toUpperCase().equals("FAIL"))) {
			  sColor = "RED";
			} else if ((strResult.toUpperCase().equals("SKIPPED")) || (strResult.toUpperCase().equals("SKIP"))) {
				sColor = "BLUE";
			} else {
			  sColor = "" + Environment.get("reportColor") + "";
			}
		  }
		  String sRowColor;
		  if (index % 2 == 0) {
			sRowColor = "#EEEEEE";
		  } else {
			sRowColor = "#D3D3D3";
		  }
		  stream.println("<TR COLS=5 BGCOLOR=" + sRowColor + "><TD  WIDTH=20%><FONT FACE=VERDANA SIZE=2>" + browser + "</FONT></TD><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + moduleName + "</FONT></TD><TD  WIDTH=50%><FONT FACE=VERDANA SIZE=2>" + strTestCaseName + "</FONT></TD><TD  WIDTH=10%><A HREF='" + reportsPath + "'><FONT FACE=VERDANA SIZE=2 COLOR=" + sColor + "><B>" + toCamelCase(strResult) + "</B></FONT></A></TD><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + strDuration + "</FONT></TD></TR>");
	  } finally {
			//Close the object
		  if(stream != null) {
			  stream.close();
		  }
		  if(foutStrm != null)
			  foutStrm.close();
	  }
	}
  
  	public void closeTestSummary(String reportFile) throws ParseException, IOException {
  		FileOutputStream foutStrm = null;
  		foutStrm = new FileOutputStream(reportFile, true);
  		PrintStream stream = new PrintStream(foutStrm);
  		try {
  			stream.println("</TABLE><TABLE WIDTH=100%><TR>");
  			stream.println("<TD BGCOLOR=BLACK WIDTH=20%></TD><TD BGCOLOR=BLACK WIDTH=10%></TD><TD BGCOLOR=BLACK WIDTH=50%></TD><TD BGCOLOR=BLACK WIDTH=10%></TD><TD BGCOLOR=BLACK WIDTH=10%></TD>");
  			stream.println("</TR></TABLE>");
  			stream.println("<TABLE WIDTH=100%><TR><TD ALIGN=RIGHT><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=1>&copy; " + Environment.get("orgName") + "</FONT></TD></TR></TABLE></BODY></HTML>");
  		} finally {
			//Close the object
  		  if(stream != null) {
  			  stream.close();
  		  }
  		  if(foutStrm != null)
  			  foutStrm.close();
  		}
	}
  	
  	public String toCamelCase(String s){
  	   String[] parts = s.split(" ");
  	   String camelCaseString = "";
  	   for (String part : parts){
  	      camelCaseString = camelCaseString + toProperCase(part) + " ";
  	   }
  	   return camelCaseString.trim();
  	}
     
     String toProperCase(String s) {
 	    return s.substring(0, 1).toUpperCase() +
 	               s.substring(1).toLowerCase();
 	}
     
     public HashMapNew GetXMLNodeValueFromString(String str, String parentNode, int index) {
    	 str = CharMatcher.is('\'').trimFrom(str);
    	 if(!str.trim().toUpperCase().contains("<CONFIG>") && !str.trim().toLowerCase().contains("<selenium>")) {
     		str = EncryptDecrypt.getEnvConfig(str.trim());
 			System.setProperty("envConfig", str);
 		}
 		HashMapNew dict = new HashMapNew();
 	    try
 	    {
 	      DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
 	      DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
 	      InputStream stream = new ByteArrayInputStream(str.getBytes("UTF-8"));
 	      Document xmldoc = docBuilder.parse(stream);
 	      
 	      XPathFactory xPathfac = XPathFactory.newInstance();
 	      XPath xpath = xPathfac.newXPath();

 	      XPathExpression expr = xpath.compile(parentNode);
 	      Object obj = expr.evaluate(xmldoc, XPathConstants.NODESET);
 	      if(obj != null){
 	    	  Node node = ((NodeList)obj).item(index);
 	    	  if(node != null){
 			      NodeList nl = node.getChildNodes();
 			      for (int child = 0; child < nl.getLength(); child++) {
 			    	  dict.put(nl.item(child).getNodeName().trim(), nl.item(child).getTextContent().trim());
 			      }
 	    	  }
 	      }
 	    }
 	    catch (Exception excep){
 	    	excep.printStackTrace();
 	    }
 	    
 	    return dict;
 	}
}