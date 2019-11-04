package org.iomedia.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.*;
import org.iomedia.framework.Driver.HashMapNew;

public class DBActivities {
	
  private HashMapNew Environment;
  private HashMapNew Dictionary;
  static Logger log = LoggerFactory.getLogger(DBActivities.class);
  private String driverType;
  static boolean bThreadFlag1 = false;
  
  public DBActivities(WebDriverFactory driverFactory, HashMapNew Dict, HashMapNew Env) {
	  Dictionary = Dict == null || Dict.size() == 0 ? driverFactory.getDictionary().get() : Dict;
	  Environment = Env == null || Env.size() == 0 ? driverFactory.getEnvironment().get() : Env; 
	  driverType = driverFactory.getDriverType() == null ? null : driverFactory.getDriverType().get();
  }
  
  public Connection fConnectToOracle(String dbtype) {
    try {      
      Class.forName("oracle.jdbc.driver.OracleDriver");
      String url = "jdbc:oracle:thin:@" + Environment.get(dbtype.toUpperCase() + "_DB_HOSTNAME") + ":" + Environment.get(dbtype.toUpperCase() + "_DB_PORT") + "/" + Environment.get(dbtype.toUpperCase() + "_DB_SERVICENAME");
      String username = Environment.get(dbtype.toUpperCase() + "_DB_USER");
      String password = Environment.get(dbtype.toUpperCase() + "_DB_PASS");
      return DriverManager.getConnection(url, username, password);
    }
    catch (Exception e) {
    	log.info("Threw a Exception in DBActivities::fConnectToOracle, full stack trace follows:", e);
    }
    return null;
  }
  
  public Connection fConnectToSqlite(String sqlitefile) {
    try
    {      
    	Class.forName("org.sqlite.JDBC");
    	return DriverManager.getConnection("jdbc:sqlite:" + Environment.get(sqlitefile));
    }
    catch (Exception e)
    {
    	log.info("Threw a Exception in DBActivities::fConnectToSqlite, full stack trace follows:", e);
    }
    return null;
  }
  
  public ResultSet fExecuteQuery(String sSQL, Connection conn) {
    try
    {      
      Statement stmnt = null;
      stmnt = conn.createStatement();
      ResultSet rs = null;
      if(sSQL.toUpperCase().indexOf("INSERT") > -1 || sSQL.toUpperCase().indexOf("UPDATE") > -1 || sSQL.toUpperCase().indexOf("CREATE") > -1 || sSQL.toUpperCase().indexOf("DROP") > -1){
    	  int count = stmnt.executeUpdate(sSQL);
    	  Dictionary.put("UPDATE_COUNT", String.valueOf(count));
    	  Dictionary.put("STMT_TYPE", sSQL.toUpperCase().split(" ")[0].trim());
      }
      else{
    	  rs = stmnt.executeQuery(sSQL);
      }
      stmnt.closeOnCompletion();
	  return rs; 
    }
    catch (SQLException eSQL)
    {
    	log.info("Threw a Exception in DBActivities::fExecuteQuery, full stack trace follows:", eSQL);
      return null;
    }
    catch (Exception e)
    {
    	log.info("Threw a Exception in DBActivities::fExecuteQuery, full stack trace follows:", e);
    }
    return null;
  }
  
  public boolean fDBActivities() throws SQLException, IOException {
	  String tempdesfinal = null, tempexpfinal = null, tempactfinal = null, sStat = null;
	  String CommonFile = null;
	 
	  CommonFile = Environment.get("CURRENTEXECUTIONCOMMONSHEET");
	  
	  HashMapNew outDictionary = new HashMapNew();
	  boolean fDBActivitiesFlag = false;
	  int iID_Index = 0, iPK_Index = 0;
	  
	  outDictionary.clear();
	  
	  List<List<String>> commonFileData = null;
	  
	  if(Environment.get("auto_dp").trim().toUpperCase().equals("Y")){
		  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{3, 4}, new String[]{"DP", "DP"});
	  }
	  else if(Dictionary.containsKey("GROUP") == true){
		  if(!Dictionary.get("GROUP").isEmpty()){
			  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{3}, new String[]{ Dictionary.get("GROUP") });  
		  }
		else{ 
			commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{2}, new String[]{ Dictionary.get("PK") });
		}
	  }
	  else if(Dictionary.containsKey("PK")){
		  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{2}, new String[]{ Dictionary.get("PK") });
	  }
	  
	  if(commonFileData == null){
		  log.info("DB Validation query isn't available for " + Dictionary.get("ACTION") + ". Result set is null");		  
		  return false;
	  }
	  
	  if (commonFileData.size() == 0) {
		  log.info("Common File Data : No data found for matching PK or GROUP");
		  return false;
      }
	  
	  ArrayList<String> arrColumns = fGetColumnName(CommonFile, "DB_SQL");
	  
	  int iExpResCnt = 1;
	  Dictionary.put("EXP_RESULTS", "");
	  Dictionary.put("TestStepExpectedResult", "");
	  Dictionary.put("TestStepActualResult", "");
	  
	  int k = 0;
	  
	  do{
		List<String> row = commonFileData.get(k);
		  
		String PK = row.get(2);
		String ID = row.get(0);
		  
		for(int i = 1; i<= arrColumns.size() ; i++){
			if(arrColumns.get(i - 1).equals("PK")){
				if(outDictionary.containsKey(arrColumns.get(i - 1) + "_" + iPK_Index) != true){
					outDictionary.put(arrColumns.get(i - 1) + "_" + iPK_Index, PK);
				}
				else{
					outDictionary.put(arrColumns.get(i - 1) + "_" + iPK_Index, PK);
				}
				iPK_Index = iPK_Index + 1;
				Dictionary.put("PK", PK);
			}
			if(arrColumns.get(i - 1).equals("ID")){
				if(outDictionary.containsKey(arrColumns.get(i - 1) + "_" + iID_Index) != true){
					outDictionary.put(arrColumns.get(i - 1) + "_" + iID_Index, ID);
				}
				else{
					outDictionary.put(arrColumns.get(i - 1) + "_" + iID_Index, ID);
				}
				iID_Index = iID_Index + 1;
				Dictionary.put("XL_COMMON_ID", ID);
			}
		}		
		
		if(Dictionary.containsKey("EXP_RESULTS_" + iExpResCnt) == true && !Dictionary.get("EXP_RESULTS_" +  iExpResCnt).isEmpty()){
			Dictionary.put("EXP_RESULTS", Dictionary.get("EXP_RESULTS_" +  iExpResCnt));
		}
		
		outDictionary = fDBCheck(Dictionary);
		sStat = "";		
		
		if(Dictionary.get("UPDATE_COUNT").trim().equalsIgnoreCase("")){
			if(outDictionary != null){
				sStat = "Successful";
				fDBActivitiesFlag = true;
			}
			else{
				sStat = "Not Successful";
				fDBActivitiesFlag = false;
				Dictionary.put("FLAG_FOR_DB_SYNC","1");
			}
		}
		else{
			if(Dictionary.get("STMT_TYPE").trim().equalsIgnoreCase("CREATE") || Dictionary.get("STMT_TYPE").trim().equalsIgnoreCase("DROP")){
				sStat = "Successful";
				fDBActivitiesFlag = true;
			}
			else{
				int count = Integer.valueOf(Dictionary.get("UPDATE_COUNT").trim());
				if(count > 0){
					sStat = "Successful";
					fDBActivitiesFlag = true;
				}
				else{
					sStat = "Not Successful";
					fDBActivitiesFlag = false;
					Dictionary.put("FLAG_FOR_DB_SYNC","1");
				}
			}
		}
		
		iExpResCnt =   iExpResCnt +1;
		Dictionary.put("EXP_RESULTS", "");
		
		String tempdes = Dictionary.get("TestStepDescription");
		
		if(tempdesfinal == ""){
			tempdesfinal = tempdes;
		}
		else{
			tempdesfinal = tempdesfinal + " ;  " + tempdes;
		}
		
		String tempexp = Dictionary.get("TestStepExpectedResult");
		
		if(tempexpfinal == ""){
			tempexpfinal = tempexp;
		}
		else{
			tempexpfinal = tempexpfinal + "  ;  " + tempexp;
		}
		
		String tempact =   Dictionary.get("TestStepActualResult");
		
		if(tempactfinal == ""){
			tempactfinal = tempact;
		}
		else{
			tempactfinal = tempactfinal + " ;  " + tempact;
		}
		
		log.info("SQL > " + tempdes);
		log.info("Expected > " + tempexp);
		log.info("Actual > " + tempact);
		log.info("Query Status > " + sStat);
		
		Dictionary.put("TestStepDescription", "");
		tempdes = "";
		Dictionary.put("TestStepExpectedResult", "");
		tempexp = "";
		Dictionary.put("TestStepActualResult", "");
		tempact = "";
		sStat = "";
		
		k++;
		
	  } while(k < commonFileData.size());
	  
	  Dictionary.put("TestStepDescription", tempdesfinal);
	  Dictionary.put("TestStepExpectedResult", tempexpfinal);
	  Dictionary.put("TestStepActualResult", tempactfinal);
	  
	  log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	  
	  Dictionary.put("XL_COMMON_ID", "");
	  Dictionary.put("FLAG_FOR_DB_SYNC","");
	  Dictionary.put("TestStepDescription","");
	  
	  if(fDBActivitiesFlag == true){
		  return true;
	  }
	  return false;	  
  }
  
  public synchronized ArrayList<String> fGetColumnName(String filepath, String sheet_name) {
	  while (bThreadFlag1) {
		  try{
			  Thread.sleep(500L);
		  }
		  catch (Exception localException1) {}
	  }
	  bThreadFlag1 = true;
	  ArrayList<String> arrColumns = new ArrayList<String>();
	  if(driverType != null){
		  if(!driverType.equalsIgnoreCase("null") && !driverType.trim().equalsIgnoreCase("")){
			  FileInputStream file = null;
			  HSSFWorkbook workbook = null;
			  try {
				  file = new FileInputStream(new File(filepath));
				  synchronized(file){
					  workbook = new HSSFWorkbook (file);
				  }
				  HSSFSheet sheet = (HSSFSheet)workbook.getSheet(sheet_name);
				  DataFormatter _formatter = new DataFormatter();
				  
				  Iterator<Row> rowIterator = sheet.iterator();
				  while(rowIterator.hasNext()) {
					  Row row = rowIterator.next();
					  Iterator<Cell> cellIterator = row.cellIterator();
					  while(cellIterator.hasNext()) {
						  Cell cell = cellIterator.next();
						  arrColumns.add(_formatter.formatCellValue(cell));
					  }
					  break;
				  }
				  
			  } catch (Exception e1) {
				  log.info("Threw a Exception in DBActivities::fGetColumnName, full stack trace follows:", e1);
			  }
			  finally{
				  try {
					  if(workbook != null)
						  workbook.close();
					file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info("Threw a Exception in DBActivities::fGetColumnName, full stack trace follows:", e);
				} 
			  }
		  }
	  }
	  bThreadFlag1 = false;
	  return arrColumns;
  }
  
  public HashMapNew fDBCheck(HashMapNew GblDictionary) throws SQLException, IOException {
	  
	  boolean ExpectedResFlag = false;
	  String CommonFile = Environment.get("CURRENTEXECUTIONCOMMONSHEET");
	  String[] ArryExp = null;
	  
	  HashMapNew outGbl = new HashMapNew();
	  outGbl.clear();
	  
	  List<List<String>> commonFileData = null;
	  
	  if(!Dictionary.get("XL_COMMON_ID").isEmpty()){
		  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{0}, new String[]{ Dictionary.get("XL_COMMON_ID") });
	  }
	  else{
		  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{2}, new String[]{ Dictionary.get("PK") });
	  }
	  
	  if(commonFileData == null){
		  log.info("Common File Data : DB validation query isn't available for  " + Dictionary.get("ACTION"));		  
		  return null;
	  }
	  
	  if (commonFileData.size() == 0)
      {
		  log.info("Common File Data : No data found for matching PK or GROUP");
        return null;
      }	  
	  
	  ArrayList<String> arrColumns = fGetColumnName(CommonFile, "DB_SQL");
	  HashMapNew CommonDict = new HashMapNew();
	  
	  for(int i = 0; i < commonFileData.get(0).size(); i++){
		  CommonDict.put(arrColumns.get(i), commonFileData.get(0).get(i));
	  }
	  
	  String DB_TYPE = CommonDict.get("DB_TYPE").trim();
	  String DB_SYNC_TIME;
	  
	  Object temp = null;
	  
	  temp = CommonDict.get("DB_SYNC_TIME").trim();
	  
	  if(Dictionary.get("FLAG_FOR_DB_SYNC").equals("1")){
		DB_SYNC_TIME = "1";  
	  }
	  else{
		  if(temp == null){
			  DB_SYNC_TIME = "1";
		  }
		  else{
			  DB_SYNC_TIME = temp.toString();  
		  }
		  
	  }	  
	  
	  temp = null;
	  
	  String ExpectedRes = null;
	  
	  temp = CommonDict.get("EXP_RESULTS").trim();
	  
	  if(!Dictionary.get("EXP_RESULTS").isEmpty()){
		  ExpectedRes = Dictionary.get("EXP_RESULTS");
	  }
	  else if(temp != null){
		  ExpectedRes = temp.toString(); 
	  }
	  else{
		  ExpectedRes = "";
	  }
	  
	  String SAVE_TO_KR = CommonDict.get("SAVE_TO_KR").trim();
	  String SAVE_PARAM_NAME = CommonDict.get("SAVE_PARAM_NAME").trim();
	  
	  String SQL = "";
	  
	  int iCounter = 1;
	  
	  String a = CommonDict.get("SQL_" + iCounter).trim();
	  
	  try{
		  do{
			  SQL = (SQL + " " + a).trim();
			  iCounter = iCounter + 1;
			  a = CommonDict.get("SQL_" + iCounter).trim();
		  }while(!a.trim().isEmpty());
	  }
	  catch(Exception ex){		  
		  //Do Nothing
	  }
	  
	  String Pk = null;
	  
	  if(SQL.indexOf("PK<<") > 0){
		  Pk = SQL.replace("PK<<", "");
		  Pk = Pk.replace(">>","").trim();
		  
		  commonFileData = fRetrieveDataExcel(CommonFile, "DB_SQL",  new int[]{2}, new String[]{ Pk });
		  
		  if(commonFileData == null){
			  log.info("Common File Data : DB Validation isn't available for PK : " + Pk);		  
			  return null;
		  }
		  
		  if (commonFileData.size() == 0)
	      {
			  log.info("Common Data File : No data found for PK : " + Pk);
	        return null;
	      }
	  }	  
	  	  
	  for(String key : GblDictionary.keySet()){
		  SQL = SQL.replace("<"+ key +">", Dictionary.get(key).replace("'", "''"));
	  }
	  
	  SQL = fReplaceSpecialParameterInSQL(SQL);
	
	  if(!ExpectedRes.isEmpty()){
		  for(String key : GblDictionary.keySet()){
			  ExpectedRes = ExpectedRes.replace("<"+ key +">", Dictionary.get(key));
		  }
	  }
	  
	  Connection conn = null;
	  
	  if(Dictionary.get("DB_NO_CONNECT").isEmpty()){
		  conn = fConnectToSqlite(DB_TYPE);
	  }
	  
	  Dictionary.put("TestStepDescription", SQL);
	  
	  if(DB_SYNC_TIME == ""){
		  DB_SYNC_TIME = "1";
	  }
	  
	  boolean flag = false;
	  int iSync = 0;
	  boolean resDBCheck = false;
	  
	  SQL = fReplaceStrAllMatches(SQL, "[\n\r\t]", " ");
	  
	  ResultSet rs1 = null;
	  
	  while(iSync < Integer.parseInt(DB_SYNC_TIME) && ((resDBCheck == false	&& ExpectedResFlag == true) || iSync == 0)){
		  log.info("Executing Query : " + SQL);
		  
		  for(int i = 0; i < Integer.parseInt(DB_SYNC_TIME); i++){
			  try {
				Thread.sleep(1);
				} catch (InterruptedException e) {					
					log.info("Threw a Exception in DBActivities::fDBCheck, full stack trace follows:", e);
				}
			  rs1 = fExecuteQuery(SQL, conn);
			  
			  if(SQL.toUpperCase().indexOf("INSERT") > -1 || SQL.toUpperCase().indexOf("UPDATE") > -1 || SQL.toUpperCase().indexOf("CREATE") > -1 || SQL.toUpperCase().indexOf("DROP") > -1){
				  if(SQL.toUpperCase().indexOf("CREATE") > -1 || SQL.toUpperCase().indexOf("DROP") > -1){
					  flag = true;
					  break;
				  }
				  if(Integer.valueOf((Dictionary.get("UPDATE_COUNT").trim())) > 0){
					  flag = true;
					  break;
				  }
				  else{
					  flag = false;
				  }
			  }
			  else{
				  if(rs1 == null){
					  log.info("Execute Common Xls Sql : DB validation query isn't available for  " + Dictionary.get("ACTION"));		  
					  return null;
				  }
				  
				  rs1.next();
				  int rowCount = rs1.getRow();
				  
				  if (rowCount == 0)
			      {		        
			        flag = false;
			      }
				  else{			  
					  flag = true;
			          break;
				  }
			  }
		  }
		  
		  if(SQL.toUpperCase().indexOf("INSERT") > -1 || SQL.toUpperCase().indexOf("UPDATE") > -1 || SQL.toUpperCase().indexOf("CREATE") > -1 || SQL.toUpperCase().indexOf("DROP") > -1){
			  if(flag = false){
				  log.info("Sql returned error or did not return any records.  Query: " + SQL);
				  Dictionary.put("TestStepActualResult", "Sql returned error or did not return any records.  Query: " + SQL);
				  Dictionary.put("TestStepExpectedResult", ExpectedRes);
				  return null;
			  }
		  }
		  else{
			  if(flag == false || rs1.isAfterLast() == true){
				  log.info("Sql returned error or did not return any records.  Query: " + SQL );
				  Dictionary.put("TestStepActualResult", "Sql returned error or did not return any records.  Query: " + SQL);
				  Dictionary.put("TestStepExpectedResult", ExpectedRes);
				  return null;
			  }
		  }
		  
		  if(SQL.toUpperCase().indexOf("INSERT") == -1 && SQL.toUpperCase().indexOf("UPDATE") == -1 && SQL.toUpperCase().indexOf("CREATE") == -1 && SQL.toUpperCase().indexOf("DROP") == -1){
			  ArrayList<String> colName = fGetResultSetColumnName(SQL, conn);
			  do{
				  for(int i = 0 ; i < colName.size() ; i++){
					  outGbl.put(colName.get(i), outGbl.get(colName.get(i)) + rs1.getString(i + 1) + "||");
				  }
			  }while(rs1.next());
			  
			  for(int i = 0 ; i < colName.size() ; i++){
				  outGbl.put(colName.get(i), outGbl.get(colName.get(i)).substring(0, outGbl.get(colName.get(i)).length() - String.valueOf("||").length()));
			  }
		  }
		  
		  if(!ExpectedRes.isEmpty()){
			  ExpectedResFlag = true;
			  ArryExp = ExpectedRes.split(";");
		  }
		  else{
			ExpectedResFlag = false;  
		  }
		  
		  String[] items = (String[]) outGbl.values().toArray(new String[outGbl.size()]);
		  
		  if(SAVE_TO_KR != null){
			  if(SAVE_TO_KR.toUpperCase().equals("TRUE")){
				  if(Dictionary.containsKey("DBSQL")){
					  Dictionary.remove("DBSQL");
					  Dictionary.put("DBSQL", SQL);
				  }
				  else{
					  Dictionary.put("DBSQL", SQL);
				  }			  
				  
				  if(SAVE_PARAM_NAME != null){
					  if(fSetReferenceVerificationData(SAVE_PARAM_NAME, items) == false){
						  return null;
					  }
					  else{
						  Dictionary.put("TestStepDescription", "Checking Database recored");				  
					  }
				  }
			  }
		  }
		  
		  if(ExpectedResFlag == true){
			  Dictionary.put("FAIL_TEST", "N");
			  resDBCheck = fDBCompareArry(ArryExp, items, 0);
			  Dictionary.put("FAIL_TEST", "");
		  }
		  else{
			  resDBCheck = true;
		  }
		  
		  try {
			Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.info("Threw a Exception in DBActivities::fDBCheck, full stack trace follows:", e);
			}
		  
		  iSync = iSync + 1;
	  }
	  
	  if(SQL.toUpperCase().indexOf("INSERT") == -1 && SQL.toUpperCase().indexOf("UPDATE") == -1 && SQL.toUpperCase().indexOf("CREATE") == -1 && SQL.toUpperCase().indexOf("DROP") == -1){
		  rs1.close();
	  }
      rs1 = null;
      conn.close();
     	  
	  return outGbl;
  }
  
  public String fReplaceStrAllMatches(String strText, String strFindPattern, String strReplace) {
	  Pattern regEx = Pattern.compile(strFindPattern);
	  String strNewText = strText;
	  String sStrLeft = "", sStrRight = "";
	  
	  int iMatch, iEndMatch;
	  String sMatch = "";
	  
	  while((sMatch = fTextExist(strFindPattern, strNewText).trim()).equalsIgnoreCase("") == false){
		  iMatch = strNewText.indexOf(sMatch);
		  iEndMatch = iMatch + sMatch.length() - 1;
		  sStrLeft = sStrLeft + strNewText.substring(0, iEndMatch);
		  sStrRight = strNewText.substring(iEndMatch, strNewText.length());
		  
		  sStrLeft = regEx.toString().replace(sStrLeft, strReplace);
		  strNewText = sStrRight;
	  }
	  
	  strNewText = sStrLeft + sStrRight;
	  
	  if(strNewText.equals("")){
		  return strText;
	  }
	  else{
		  return strNewText;
	  }
	  
  }
  
  public String fTextExist(String patrn, String strng) {
	  Pattern regEx = Pattern.compile(patrn);	  	  
	  Matcher m = regEx.matcher(strng);
	  String sMatch = "";
	  
	  if(m.find()){
		  String oMatch = m.group(0);
		  sMatch = oMatch + "";
		  return sMatch;
	  }
	  
	  return sMatch;
  }
  
  public boolean fDBCompareArry(String[] in_arry1, String[] in_arry2, int first_index) {
	  int cnt1, cnt2, i = 0;
	  boolean flag;
	  String iExpectedValue = null, iActualValue = null;
	  String[] ArrTemp;
	  
	  flag = true;
	  cnt1 = in_arry1.length;
	  cnt2 = in_arry2.length;
	  
	  if(cnt1 != cnt2){
		  log.info("Array Compare : Arrays size is not equal");
		  return false;
	  }
	  
	  for(i = first_index; i < cnt1; i++){
		 iExpectedValue = in_arry1[i].trim().toLowerCase();
		 iActualValue = in_arry2[i].trim().toLowerCase();
		 
		 if((iExpectedValue.indexOf(">") == 0) && (iExpectedValue.indexOf("<") == 0) && (iExpectedValue.indexOf("=") == 0)){
			 iExpectedValue = "=" + iExpectedValue;
		 }
		 
		 if((iExpectedValue.indexOf(">") > 0) || (iExpectedValue.indexOf("<") > 0) || (iExpectedValue.indexOf("=") > 0)){
			 if(iExpectedValue.indexOf(">=") > 0){
				 ArrTemp = iExpectedValue.split(">=");
				 flag =  (Integer.parseInt(ArrTemp[1]) <= Integer.parseInt(iActualValue));
			 }
			 else if(iExpectedValue.indexOf("=>") > 0){
				 ArrTemp = iExpectedValue.split("=>");
				 flag =  (Integer.parseInt(ArrTemp[1]) <= Integer.parseInt(iActualValue));
			 }
			 else if(iExpectedValue.indexOf("=<") > 0){
				 ArrTemp = iExpectedValue.split("=<");
				 flag =  (Integer.parseInt(ArrTemp[1]) >= Integer.parseInt(iActualValue));
			 }
			 else if(iExpectedValue.indexOf("<=") > 0){
				 ArrTemp = iExpectedValue.split("<=");
				 flag =  (Integer.parseInt(iActualValue) <= Integer.parseInt(ArrTemp[1]));
			 }
			 else if(iExpectedValue.indexOf("<>") > 0){
				 ArrTemp = iExpectedValue.split("<>");
				 flag =  (Integer.parseInt(iActualValue) != Integer.parseInt(ArrTemp[1]));
			 }
			 else if(iExpectedValue.indexOf("<") > 0){
				 ArrTemp = iExpectedValue.split("<");
				 flag =  (Integer.parseInt(ArrTemp[1]) > Integer.parseInt(iActualValue));
			 }
			 else if(iExpectedValue.indexOf(">") > 0){
				 ArrTemp = iExpectedValue.split(">");
				 flag =  (Integer.parseInt(ArrTemp[1]) < Integer.parseInt(iActualValue));
			 }
			 else if(iExpectedValue.indexOf("=") > 0){
				 ArrTemp = iExpectedValue.split("=");
				 flag =  (Integer.parseInt(ArrTemp[1]) == Integer.parseInt(iActualValue));
			 }
		 }
		 
		 if(flag == false){
			 if(Dictionary.get("FAIL_TEST") != "N"){
				 log.info("Compare DB Value to Expected : Check logs in database ~ The SQL ended with failure:\n Actual Value is : " + in_arry2[i] + "\n Expected Value is : " + in_arry1[i] );
			 }
			 
			 Dictionary.put("DB_ERR_MSG", "Actual Value is : " + in_arry2[i] + "\n Expected Value is : " + in_arry1[i]);
			 
			 if(Dictionary.get("TestStepExpectedResult") != ""){
				 Dictionary.put("TestStepExpectedResult", Dictionary.get("TestStepExpectedResult") + " ; " + in_arry1[i]);
			 }
			 else{
				 Dictionary.put("TestStepExpectedResult", in_arry1[i]);
			 }
			 
			 if(Dictionary.get("TestStepActualResult") != ""){
				 Dictionary.put("TestStepActualResult", Dictionary.get("TestStepActualResult") + " ; " + in_arry2[i]);
			 }
			 else{
				 Dictionary.put("TestStepActualResult", in_arry2[i]);
			 }
		 }
		 else{
			 log.info("Compare DB Value to Expected : Check logs for entity in database ~ The SQL ended successfully :\n Actual Value is : " + in_arry2[i] + "\n Expected Value is : " + in_arry1[i] );
			 
			 if(Dictionary.get("TestStepExpectedResult") != ""){
				 Dictionary.put("TestStepExpectedResult", Dictionary.get("TestStepExpectedResult") + " ; " + in_arry1[i]);
			 }
			 else{
				 Dictionary.put("TestStepExpectedResult", in_arry1[i]);
			 }
			 
			 if(Dictionary.get("TestStepActualResult") != ""){
				 Dictionary.put("TestStepActualResult", Dictionary.get("TestStepActualResult") + " ; " + in_arry2[i]);
			 }
			 else{
				 Dictionary.put("TestStepActualResult", in_arry2[i]);
			 }
		 }
	  }
	  
	  return flag;
  }
  
  public boolean fGetReferenceVerificationData(String ParamName, String ParamValue) throws SQLException, IOException {
	  
	  String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");
	  List<List<String>> calendarFileData = fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ ParamName });
	  
	  if(calendarFileData == null){
		  log.info("Calendar File Data ~ Result set is null");		  
		  return false;
	  }
	  
	  if (calendarFileData.size() == 0)
      {
		  log.info("Calendar File Data : No Rows Found");
        return false;
      }
	  
	  ParamValue = calendarFileData.get(0).get(2);
	  
	  return true;
  }
  
  public String fReplaceSpecialParameterInSQL(String sSrcSQL) throws SQLException, IOException {
	  
	  String[] ArrTemp1, ArrTemp2;
	  String ParamName, ParamValue = null;
	  	  
	  while(sSrcSQL.indexOf("<&") > 0){
		ArrTemp1 = sSrcSQL.split("<&");
		ArrTemp2 = ArrTemp1[1].split(">");
		ParamName = ArrTemp2[0];
		
		boolean rc = fGetReferenceVerificationData(ParamName, ParamValue);
		
		if(rc == false){
			return sSrcSQL;
		}
		else{
			sSrcSQL = sSrcSQL.replace("<&" + ParamName + ">", "'" + ParamValue + "'");
		}
	  }
	  
	  return sSrcSQL;
  }
  
  public boolean fSetReferenceVerificationData(String ParamName, String[] ParamValue) throws SQLException, IOException {
	  
	  String calendar = (String)this.Environment.get("CURRENTEXECUTIONDATASHEET");
	  String[] ArrParamName;
	  
	  if(!ParamName.isEmpty()){
		  ArrParamName = ParamName.split(";");
	  }
	  else{
		  String[] TmpArrTemp1 = Dictionary.get("DBSQL").toUpperCase().split("FROM");
		  String[] TmpArrTemp2 = TmpArrTemp1[0].split("SELECT");
		  ArrParamName = TmpArrTemp2[1].split(",");
		  
		  if(ArrParamName.length == 0){
			  ArrParamName = TmpArrTemp2[1].trim().split(" ");
		  }
	  }
	  
	  int iCnt = ParamValue.length;
	  
	  String sTempParamName, sTempParamValue;	  
	  
	  for(int iIndexArr = 0 ; iIndexArr < iCnt; iIndexArr++){
		  sTempParamName = ArrParamName[iIndexArr].trim();
		  sTempParamValue = ParamValue[iIndexArr].trim();
		  
		  if(sTempParamValue.indexOf("'") > 0){
			  sTempParamValue = sTempParamValue.replace("'", "''");
		  }
		  
		  List<List<String>> calendarFileData = fRetrieveDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(),  new int[]{1}, new String[]{ sTempParamName });
		  
		  if(calendarFileData == null){
			  log.info("Calendar File Data ~ Result set is null");		  
			  return false;
		  }
		  
		  boolean success = false;
		  
		  if(calendarFileData.size() == 0){
			 success = fInsertDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(), new int[]{1,2}, new String[]{sTempParamName, sTempParamValue});
		  }
		  else{
			  success = fUpdateDataExcel(calendar, "KEEP_REFER_" + this.driverType.toUpperCase(), new int[]{1}, new String[]{ sTempParamName }, new int[]{2}, new String[]{ sTempParamValue });
		  }
          
          if (success == false)
          {
        	  log.info("Updating the KEEP_REFER sheet in the Data Table > Not Successful");
            return false;
          }
          else{
        	  log.info("Updating the KEEP_REFER sheet in the Data Table > Successful");
          }
          
          if(Dictionary.containsKey(sTempParamName)){
        	  Dictionary.put(sTempParamName, sTempParamValue);
          }
          else{
        	  Dictionary.put(sTempParamName, sTempParamValue);
          }
	  }
	  
	  return true;
  }
  
  public synchronized List<List<String>> fRetrieveDataExcel(String filepath, String sheet_name, int[] column_no, String[] column_value) {
	  while (bThreadFlag1) {
		  try{
			  Thread.sleep(500L);
		  }
		  catch (Exception localException1) {}
	  }
	  bThreadFlag1 = true;
	  	List<List<String>> listOfLists = new ArrayList<List<String>>();
	  	
	  	if(driverType != null){
			  if(!driverType.equalsIgnoreCase("null") && !driverType.trim().equalsIgnoreCase("")){
				FileInputStream file = null;
				HSSFWorkbook workbook = null;
				try{
					file = new FileInputStream(new File(filepath));
					synchronized(file){
						workbook = new HSSFWorkbook (file);
					}
					HSSFSheet sheet = (HSSFSheet)workbook.getSheet(sheet_name);
					DataFormatter _formatter = new DataFormatter();
					
					Iterator<Row> rowIterator = sheet.iterator();
					while(rowIterator.hasNext()) {
						Row row = rowIterator.next();
						int flag = 0;
						if(column_no != null){
							for(int i = 0 ; i < column_no.length; i++){
								if(row.getCell(column_no[i]) == null){
									if(column_value[i].trim().equalsIgnoreCase("")){
										flag = 1;
									}
									else{
										flag = 0;
										break;
									}
								}
								else{
									if(column_value[i].contains(" or ")){
										String[] columnValues = column_value[i].split(" or ");
										int mFlag = 0;
										for(int m = 0 ; m < columnValues.length; m++){
											if(_formatter.formatCellValue(row.getCell(column_no[i])).equalsIgnoreCase(columnValues[m].trim())){
												mFlag = 1;
												break;
											}
											else{
												mFlag = 0;
											}
										}
										if(mFlag == 1){
											flag = 1;
										}
										else{
											flag = 0;
											break;
										}
									}
									else{
										if(_formatter.formatCellValue(row.getCell(column_no[i])).equalsIgnoreCase(column_value[i])){
											flag = 1;
										}
										else{
											flag = 0;
											break;
										}
									}
								}
							}
						}
						else{
							flag = 1;
						}
						
						if(flag == 1){
							listOfLists.add(new ArrayList<String>());
							for(int m = 0 ; m < row.getLastCellNum(); m++){
								if(row.getCell(m) == null){
									Cell cell = row.createCell(m);
									cell.setCellValue("");
								}
							}
							Iterator<Cell> cellIterator = row.cellIterator();
							while(cellIterator.hasNext()) {
				                Cell cell = cellIterator.next();
				                switch(cell.getCellType()){
				                	case Cell.CELL_TYPE_NUMERIC:
				                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
				                		break;
				                	case Cell.CELL_TYPE_STRING:
				                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
			                			break;
				                	case Cell.CELL_TYPE_BOOLEAN:
				                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
				                		break;
				                	case Cell.CELL_TYPE_FORMULA:
				                		switch(cell.getCachedFormulaResultType()) {
				                			case Cell.CELL_TYPE_STRING:
				                				HSSFRichTextString str = (HSSFRichTextString) cell.getRichStringCellValue();
				                                if(str != null && str.length() > 0) {
				                                	listOfLists.get(listOfLists.size() - 1).add(str.toString());
				                                }
				                				break;
				                			case Cell.CELL_TYPE_NUMERIC:
				                				HSSFCellStyle style = (HSSFCellStyle) cell.getCellStyle();
				                                if(style == null) {
				                                	listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
				                                } else {
				                                	listOfLists.get(listOfLists.size() - 1).add(
				                                    _formatter.formatRawCellContents(
				                                          cell.getNumericCellValue(),
				                                          style.getDataFormat(),
				                                          style.getDataFormatString()
				                                    )
	                                			);
				                                }
				                				break;
				                			case Cell.CELL_TYPE_BOOLEAN:
						                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
						                		break;
				                			case Cell.CELL_TYPE_ERROR:
						                		break;
				                			case Cell.CELL_TYPE_BLANK:
						                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
						                		break;
				                		}
				                		break;
				                	case Cell.CELL_TYPE_ERROR:
				                		break;
				                	case Cell.CELL_TYPE_BLANK:
				                		listOfLists.get(listOfLists.size() - 1).add(_formatter.formatCellValue(cell));
				                		break;
				                }
							}
						}
					}
				}
				catch(Exception ex){
					log.info("Threw a Exception in DBActivities::fRetrieveDataExcel, full stack trace follows:", ex);
				}
				finally{
					try {
						if(workbook != null)
							workbook.close();
						file.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.info("Threw a Exception in DBActivities::fRetrieveDataExcel, full stack trace follows:", e);
					} 
				}
			}
	  	}
		bThreadFlag1 = false;
		return listOfLists;
	}
  
  public ArrayList<String> fGetResultSetColumnName(String sql, Connection conn) throws SQLException {
	  ResultSet rs = fExecuteQuery(sql, conn);
	  ArrayList<String> arrColumns = new ArrayList<String>();
	  
	  if(rs == null){
		  log.info("DB Validation query isn't available for " + Dictionary.get("ACTION") + ". Result set is null");		  
		  return arrColumns;
	  }
	  
	  rs.next();
	  int rowCount = rs.getRow();
	  
	  if (rowCount == 0)
      {
		  log.info(sql + " : No Rows Found");
        rs.close();        
        rs = null;
        return arrColumns;
      }
	  
	  ResultSetMetaData rsmd = rs.getMetaData(); 
	  int colCount = rsmd.getColumnCount();
	  	
	  for(int intLoop = 1; intLoop <= colCount; intLoop++){
		  arrColumns.add(rsmd.getColumnName(intLoop));
	  }
	  
	  return arrColumns;
  }

  synchronized boolean fInsertDataExcel(String filepath, String sheet_name, int[] column_no, String[] column_value) {
	  while (bThreadFlag1) {
		  try{
			  Thread.sleep(500L);
		  }
		  catch (Exception localException1) {}
	  }
	  bThreadFlag1 = true;
	  if(driverType != null){                                                                   
		  if(!driverType.equalsIgnoreCase("null") && !driverType.trim().equalsIgnoreCase("")){
			FileInputStream file = null;
			HSSFWorkbook workbook = null;
			
			try{
				file = new FileInputStream(new File(filepath));
				synchronized(file){
					workbook = new HSSFWorkbook (file);
				}
				HSSFSheet sheet = (HSSFSheet)workbook.getSheet(sheet_name);
				
				CellStyle headerStyle = workbook.createCellStyle();
				headerStyle.setAlignment(CellStyle.ALIGN_LEFT);
				
				Iterator<Row> rowIterator = sheet.iterator();
				int count = 0;
				while(rowIterator.hasNext()) {
					rowIterator.next();
					count ++;
				}
				
				Row dataRow = sheet.createRow(count);
				
				for(int i = 0 ; i < column_no.length; i++){
					Cell cell = dataRow.createCell(column_no[i]);
					cell.setCellValue(column_value[i]);
					cell.setCellStyle(headerStyle);
				}
				
				file.close();
				FileOutputStream out = new FileOutputStream(new File(filepath));
				workbook.write(out);
				
				out.close();
			}
			catch(Exception ex){
				log.info("Threw a Exception in DBActivities::fInsertDataExcel, full stack trace follows:", ex);
			}
			finally{
				try {
					if(workbook != null)
						workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info("Threw a Exception in DBActivities::fInsertDataExcel, full stack trace follows:", e);
				} 
			}
		  }
	  }
	  bThreadFlag1 = false;
	  return true;
  }
  
  public synchronized boolean fUpdateDataExcel(String filepath, String sheet_name, int[] column_no, String[] column_value, int[] set_column_no, String[] set_column_value) {
	  while (bThreadFlag1) {
		  try{
			  Thread.sleep(500L);
		  }
		  catch (Exception localException1) {}
	  }
	  bThreadFlag1 = true;
	  boolean status = false; 
	  
	  if(driverType != null){                                                                   
		  if(!driverType.equalsIgnoreCase("null") && !driverType.trim().equalsIgnoreCase("")){
			FileInputStream file = null;
			HSSFWorkbook workbook = null;
			
			try{
				file = new FileInputStream(new File(filepath));
				synchronized(file){
					workbook = new HSSFWorkbook (file);
				}
				HSSFSheet sheet = (HSSFSheet)workbook.getSheet(sheet_name);
				
				CellStyle headerStyle = workbook.createCellStyle();
				headerStyle.setAlignment(CellStyle.ALIGN_LEFT);
				
				DataFormatter _formatter = new DataFormatter();
				
				int count = 0;
				
				Iterator<Row> rowIterator = sheet.iterator();
				while(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					int flag = 0;
					if(column_no != null){
						for(int i = 0 ; i < column_no.length; i++){
							if(row.getCell(column_no[i]) == null){
								if(column_value[i].trim().equalsIgnoreCase("")){
									flag = 1;
								}
								else{
									flag = 0;
									break;
								}
							}
							else{
								if(column_value[i].contains(" or ")){
									String[] columnValues = column_value[i].split(" or ");
									int mFlag = 0;
									for(int m = 0 ; m < columnValues.length; m++){
										if(_formatter.formatCellValue(row.getCell(column_no[i])).equalsIgnoreCase(columnValues[m].trim())){
											mFlag = 1;
											break;
										}
										else{
											mFlag = 0;
										}
									}
									if(mFlag == 1){
										flag = 1;
									}
									else{
										flag = 0;
										break;
									}
								}
								else{
									if(_formatter.formatCellValue(row.getCell(column_no[i])).equalsIgnoreCase(column_value[i])){
										flag = 1;
									}
									else{
										flag = 0;
										break;
									}
								}
							}
						}
					}
					else{
						flag = 1;
					}
					
					if(flag == 1){
						for(int j = 0; j < set_column_no.length; j++){
							if(row.getCell(set_column_no[j]) == null){
								Cell cell = row.createCell(set_column_no[j]);
								cell.setCellValue(set_column_value[j]);
								//cell.setCellStyle(headerStyle);
							}
							else{
								row.getCell(set_column_no[j]).setCellValue(set_column_value[j]);
							}
						}
						count++;
					}
				}
				
				file.close();
				FileOutputStream out = new FileOutputStream(new File(filepath));
				workbook.write(out);
				
				out.close();
				
				if(count > 0){
					status = true;
				}
			}
			catch(Exception ex){
				log.info("Threw a Exception in DBActivities::fUpdateDataExcel, full stack trace follows:", ex);
			}
			finally{
				try {
					if(workbook != null)
						workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info("Threw a Exception in DBActivities::fUpdateDataExcel, full stack trace follows:", e);
				}
			}
		  }
	  }
	  bThreadFlag1 = false;
	  return status;
	}
}