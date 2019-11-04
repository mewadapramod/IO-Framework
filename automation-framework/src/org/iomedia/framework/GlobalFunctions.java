package org.iomedia.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.iomedia.framework.Driver.HashMapNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class GlobalFunctions {
  static Logger log = LoggerFactory.getLogger(GlobalFunctions.class);
	
  public void fCopyXLS(String inputXLS, String destXLS) throws IOException {
      File f1 = new File(inputXLS);
      File f2 = new File(destXLS);
      InputStream in = new FileInputStream(f1);
      OutputStream out = new FileOutputStream(f2);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0)
      {        
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
  }
  
  public void fGlobalDeleteFolder(File FolderPath) {
    if (FolderPath.isDirectory())
    {
      String[] arrChildNodes = FolderPath.list();
      for (int i = 0; i < arrChildNodes.length; i++) {
        fGlobalDeleteFolder(new File(FolderPath, arrChildNodes[i]));
      }
    }
    FolderPath.delete();
  }
  
  public HashMapNew readResultsCsv(String resultPath, HashMapNew duration, HashMapNew csvs) throws Exception {
	  HashMapNew result = new HashMapNew();
	  if(new File(resultPath).exists()){
		  CSVReader reader = null;
		  try{
			  reader = new CSVReader(new FileReader(resultPath));
			  List<String[]> csv = reader.readAll();
			  if(csv != null){
				  for(int i = 0 ; i < csv.size(); i++){
					  String[] data = csv.get(i);
					  String driverType = data[0].trim().toUpperCase();
					  String className = data[2].trim();
					  String testName = data[3].trim();
					  String tcduration = data[9].trim();
					  String status = data[12].trim().toUpperCase();
					  String passed = data[13].trim();
					  String failed = data[14].trim();
					  String reportPath = data[15].trim();
					  String skipped = data[18].trim();
					  String browser = data[19].trim();
					  String testType = data[20].trim();
					  String severity = data[21].trim().toUpperCase();
					  result.put("SEVERITY", result.get("SEVERITY").trim().contains(severity) ? result.get("SEVERITY").trim() : result.get("SEVERITY").trim() + severity + ",");
					  result.put("TEST_TYPES", result.get("TEST_TYPES").trim().contains(testType) ? result.get("TEST_TYPES").trim() : result.get("TEST_TYPES").trim() + testType + ",");
					  String key = testType + "_" + severity + "_" + status;
					  result.put(key, result.get(key).trim().equalsIgnoreCase("") ? "0" : result.get(key).trim());
					  if(status.equalsIgnoreCase("Pass")) {
						  result.put(key, String.valueOf(Integer.valueOf(result.get(key).trim()) + Integer.valueOf(passed)));
					  } else if(status.equalsIgnoreCase("Fail")) {
						  result.put(key, String.valueOf(Integer.valueOf(result.get(key).trim()) + Integer.valueOf(failed)));
					  }
					  
					  key = testType + "_" + className + "_" + browser + "_" + severity + "_" + status;
					  result.put(key, result.get(key).trim().equalsIgnoreCase("") ? "0" : result.get(key).trim());
					  if(status.equalsIgnoreCase("Pass")) {
						  result.put(key, String.valueOf(Integer.valueOf(result.get(key).trim()) + Integer.valueOf(passed)));
					  } else if(status.equalsIgnoreCase("Fail")) {
						  result.put(key, String.valueOf(Integer.valueOf(result.get(key).trim()) + Integer.valueOf(failed)));
					  } else
						  result.put(key, String.valueOf(Integer.valueOf(result.get(key).trim()) + Integer.valueOf(skipped)));
					  
					  key = testType;
					  result.put(key, result.get(key).trim().contains(className) ? result.get(key).trim() : result.get(key).trim() + className + ",");
					  
					  key = testType + "_" + className;
					  result.put(key, result.get(key).trim().contains(browser) ? result.get(key).trim() : result.get(key).trim() + browser + ",");
					  
					  String kduration = driverType + "_" + className.toUpperCase() + "_" + "DURATION";
					  key = testType + "_" + className + "_" + browser + "_" + "DURATION";
					  result.put(key, !duration.get(kduration).trim().equalsIgnoreCase("") ? duration.get(kduration).trim() : "0d 0h 0m 0s");
					  
					  String ksummaryurl = driverType + "_" + className.toUpperCase() + "_" + "SUMMARY_URL";
					  key = testType + "_" + className + "_" + browser + "_" + "SUMMARY_URL";
					  result.put(key, !csvs.get(ksummaryurl).trim().equalsIgnoreCase("") ? csvs.get(ksummaryurl).trim() : "#");
					  
					  key = testType + "_" + severity + "_" + status + "_" + "TEST_NAME";
					  result.put(key, result.get(key).trim().contains(browser +  "||" + className + "||" + testName + "||" + tcduration + "||" + reportPath) ? result.get(key).trim() : result.get(key).trim() + browser +  "||" + className + "||" + testName + "||" + tcduration + "||" + reportPath + "####");
				  }
			  }
		  } catch(Exception ex){
			  throw ex;
		  }
		  finally{
			  if(reader != null)
				  reader.close();
		  }
	  }
	  return result;
  }
}
