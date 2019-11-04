package org.iomedia.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.iomedia.framework.Driver.HashMapNew;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class Reporting {
	private HashMapNew Dictionary;
	private HashMapNew Environment;
	private String suiteTestName;
	static Logger log = LoggerFactory.getLogger(Reporting.class);
	private String driverType;
	private WebDriverFactory driverFactory;
	
	private ThreadLocal<HashMapNew> sTestDetails = new ThreadLocal<HashMapNew>(){
		@Override protected HashMapNew initialValue() {
			return null;
		}	
	};
	
	private ThreadLocal<FileOutputStream> sFoutStrm = new ThreadLocal<FileOutputStream>(){
		@Override protected FileOutputStream initialValue() {
			return null;
		}	
	};
  
	public Reporting(WebDriverFactory driverFactory, TestSuite testSuite, HashMapNew Dict, HashMapNew Env, ThreadLocal<HashMapNew> sTestDetails) {
		this.driverFactory = driverFactory;
		this.suiteTestName = testSuite.getTestSuiteName();
		driverType = driverFactory.getDriverType() == null ? null : driverFactory.getDriverType().get();
		this.Dictionary = Dict == null || Dict.size() == 0 ? driverFactory.getDictionary().get() : Dict;
		this.Environment = Env == null || Env.size() == 0 ? driverFactory.getEnvironment().get() : Env;
		this.sTestDetails = sTestDetails.get() == null ? driverFactory.getTestDetails() : sTestDetails;	
	}
	  
	public HashMapNew fnCreateSummaryReport() {
		HashMapNew testsetDetails = new HashMapNew();
		testsetDetails.put("G_ITCPASSED", "0");
		testsetDetails.put("G_ITCFAILED", "0");
		testsetDetails.put("G_ITCSKIPPED", "0");
		testsetDetails.put("G_ITESTCASENO", "0");
		
		SimpleDateFormat sdfr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String g_SummaryStartTime = sdfr.format(new Date());
		testsetDetails.put("G_SUMMARYSTARTTIME", g_SummaryStartTime);
		
		try {
		  FileOutputStream foutStrm = sFoutStrm.get();
		  foutStrm = new FileOutputStream(Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);

		  new PrintStream(foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>");
		  String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		  String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
		  String machineName = env.toUpperCase();
		  try{
			  machineName = InetAddress.getLocalHost().getHostName();
		  } catch(Exception ex){
			  //Do nothing;
		  }
		  new PrintStream(foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting [" + Dictionary.get("TEST_CLASS_NAME") + "]</B><FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + " on " + this.suiteTestName + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>");
		  new PrintStream(foutStrm).println("<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
		  new PrintStream(foutStrm).println("<TR COLS=6 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TC No.</B></FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Name</B></FONT></TD><TD BGCOLOR=" + Environment.get("reportColor") + " WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Status</B></FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Duration</B></FONT></TD></TR>");

		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnCreateSummaryReport, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
		
		return testsetDetails;
	}
	  
	public void fnCreateHtmlReport(String strTestName) {
		sTestDetails.get().put("G_OPERATIONCOUNT", "0");
		sTestDetails.get().put("G_IPASSCOUNT", "0");
		sTestDetails.get().put("G_IFAILCOUNT", "0");
		sTestDetails.get().put("G_ISNAPSHOTCOUNT", "0");
		Date today = new Date();
	    Timestamp now = new Timestamp(today.getTime());
	    String[] tempNow = now.toString().split("\\.");
	    String timeStamp = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");

		String g_strScriptName = strTestName + timeStamp;
		sTestDetails.get().put("SCRIPT_NAME", g_strScriptName);
		
		String g_strTestCaseReport = ((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "Report_" + g_strScriptName + ".html");
		sTestDetails.get().put("REPORT_NAME", g_strTestCaseReport);
		sTestDetails.get().put("REL_REPORT_NAME", ((String)this.Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "Report_" + g_strScriptName + ".html"));

		String g_strSnapshotFolderName = ((String)this.Environment.get("SNAPSHOTSFOLDER") + OSValidator.delimiter + g_strScriptName);
		String g_strLogFolderName = ((String)this.Environment.get("LOGSFOLDER") + OSValidator.delimiter + g_strScriptName);
		String g_strRelSnapshotFolderName = ((String)this.Environment.get("RELSNAPSHOTSFOLDER") + OSValidator.delimiter + g_strScriptName);
		String g_strRelLogFolderName = ((String)this.Environment.get("RELLOGSFOLDER") + OSValidator.delimiter + g_strScriptName);
		sTestDetails.get().put("REL_SNAPSHOTS_NAME", Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + g_strRelSnapshotFolderName);
		sTestDetails.get().put("REL_LOGS_NAME", Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + g_strRelLogFolderName);
		
		sTestDetails.get().put("G_STRSNAPSHOTFOLDERNAME", g_strSnapshotFolderName);
		sTestDetails.get().put("G_STRRELSNAPSHOTFOLDERNAME", g_strRelSnapshotFolderName);
		sTestDetails.get().put("G_STRRELLOGFOLDERNAME", g_strRelLogFolderName);
		
		File file = new File(g_strSnapshotFolderName);
		if (file.exists()) {
		  file.delete();
		}
		file.mkdir();
		file = new File(g_strLogFolderName);
		if (file.exists()) {
		  file.delete();
		}
		file.mkdir();
		FileOutputStream foutStrm = sFoutStrm.get();
		try {
		  foutStrm = new FileOutputStream(g_strTestCaseReport);
		}
		catch (FileNotFoundException fe) {
			log.info("Threw a FileNotFoundException in Reporting::fnCreateHtmlReport, full stack trace follows:", fe);
		}
		try {
		  new PrintStream(foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=" + Environment.get("reportColor") + ">");
		  String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		  String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
		  String machineName = env.toUpperCase();
		  try{
			  machineName = InetAddress.getLocalHost().getHostName();
		  } catch(Exception ex){
			  //Do nothing;
		  }
		  new PrintStream(foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting [" + Dictionary.get("TEST_CLASS_NAME") + "]</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + " on " + this.suiteTestName);
		  new PrintStream(foutStrm).println("</B></FONT></TD></TR>");
		  new PrintStream(foutStrm).println("<TR HEIGHT=5></TR></TABLE>");
		  new PrintStream(foutStrm).println("<TABLE BORDER=0 BORDERCOLOR=WHITE CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
		  new PrintStream(foutStrm).println("<TR><TD BGCOLOR=BLACK WIDTH=20%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Test Name:</B></FONT></TD><TD COLSPAN=6 BGCOLOR=BLACK><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>" + sTestDetails.get().get("TEST_NAME").trim() + "</B></FONT></TD></TR>");
		  
		  new PrintStream(foutStrm).println("</TABLE><BR/><TABLE WIDTH=100% CELLPADDING=3>");
		  new PrintStream(foutStrm).println("<TR WIDTH=100%><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=5%><FONT FACE=VERDANA SIZE=2>Step No.</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=28%><FONT FACE=VERDANA SIZE=2>Step Description</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=25%><FONT FACE=VERDANA SIZE=2>Expected Value</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=25%><FONT FACE=VERDANA SIZE=2>Actual Value</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=7%><FONT FACE=VERDANA SIZE=2>Result</FONT></TH></TR>");
		  
		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnCreateHtmlReport, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
		SimpleDateFormat sdfr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String g_StartTime = sdfr.format(new Date());
		sTestDetails.get().put("G_STARTTIME", g_StartTime);
	}
	  
	public HashMapNew fnWriteTestSummary(String strTestCaseName, String strResult, String strDuration, HashMapNew testsetdetails) {
		FileOutputStream foutStrm = sFoutStrm.get();
		int g_iTCPassed = Integer.valueOf(testsetdetails.get("G_ITCPASSED"));
		int g_iTCFailed = Integer.valueOf(testsetdetails.get("G_ITCFAILED"));
		int g_iTCSkipped = Integer.valueOf(testsetdetails.get("G_ITCSKIPPED"));
		int g_iTestCaseNo = Integer.valueOf(testsetdetails.get("G_ITESTCASENO"));
		
		try
		{
		  foutStrm = new FileOutputStream((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);
		  String sColor;
		  if ((strResult.toUpperCase().equals("PASSED")) || (strResult.toUpperCase().equals("PASS"))) {
			sColor = "GREEN";
			g_iTCPassed += 1;
		  }
		  else {        
			if ((strResult.toUpperCase().equals("FAILED")) || (strResult.toUpperCase().equals("FAIL"))) {
			  sColor = "RED";
			  g_iTCFailed += 1;
			} else if ((strResult.toUpperCase().equals("SKIPPED")) || (strResult.toUpperCase().equals("SKIP"))) {
				sColor = "BLUE";
				g_iTCSkipped += 1;
			} else {
			  sColor = "" + Environment.get("reportColor") + "";
			}
		  }
		  g_iTestCaseNo += 1;
		  String sRowColor;
		  
		  if (g_iTestCaseNo % 2 == 0) {
			sRowColor = "#EEEEEE";
		  } else {
			sRowColor = "#D3D3D3";
		  }
		  
//		  if ((strResult.toUpperCase().equals("SKIPPED")) || (strResult.toUpperCase().equals("SKIP"))) {
//			  new PrintStream(foutStrm).println("<TR COLS=3 BGCOLOR=" + sRowColor + "><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + g_iTestCaseNo + "</FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA SIZE=2>" + strTestCaseName + "</FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA SIZE=2 COLOR=" + sColor + "><B>" + strResult + "</B></FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA SIZE=2>" + strDuration + "</FONT></TD></TR>");
//		  } else{
			  new PrintStream(foutStrm).println("<TR COLS=3 BGCOLOR=" + sRowColor + "><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + g_iTestCaseNo + "</FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA SIZE=2>" + strTestCaseName + "</FONT></TD><TD  WIDTH=15%><A HREF='" + "Report_" + sTestDetails.get().get("SCRIPT_NAME").trim() + ".html'><FONT FACE=VERDANA SIZE=2 COLOR=" + sColor + "><B>" + strResult + "</B></FONT></A></TD><TD  WIDTH=15%><FONT FACE=VERDANA SIZE=2>" + strDuration + "</FONT></TD></TR>");
//		  }
		
		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnWriteTestSummary, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
		
		testsetdetails.put("G_ITCPASSED", String.valueOf(g_iTCPassed));
		testsetdetails.put("G_ITCFAILED", String.valueOf(g_iTCFailed));
		testsetdetails.put("G_ITCSKIPPED", String.valueOf(g_iTCSkipped));
		testsetdetails.put("G_ITESTCASENO", String.valueOf(g_iTestCaseNo));
		
		return testsetdetails;
	}
	  
	public HashMapNew fnCloseHtmlReport(String status, HashMapNew testsetdetails) throws Exception {
		String strTestCaseResult = null;
		FileOutputStream foutStrm = sFoutStrm.get();
		try {
		  foutStrm = new FileOutputStream(sTestDetails.get().get("REPORT_NAME"), true);
		}
		catch (FileNotFoundException fe) {
			log.info("Threw a FileNotFoundException in Reporting::fnCloseHtmlReport, full stack trace follows:", fe);
		}
		Date g_EndTime = new Date();
		
		SimpleDateFormat sdfr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date g_StartTime = sdfr.parse(sTestDetails.get().get("G_STARTTIME"));
		
		Timestamp now = new Timestamp(g_StartTime.getTime());
		String[] tempNow = now.toString().split("\\.");
		String timeStamp = tempNow[0].replaceAll(" ", "T");
		
		sTestDetails.get().put("TC_START_TIME", timeStamp.split("T")[1]);
		String strTimeDifference = fnTimeDiffference(g_StartTime.getTime(), g_EndTime.getTime());
		sTestDetails.get().put("TC_EXEC_TOTAL_DURATION", strTimeDifference);
		
		int g_iFailCount = Integer.valueOf(sTestDetails.get().get("G_IFAILCOUNT"));
		int g_iPassCount = Integer.valueOf(sTestDetails.get().get("G_IPASSCOUNT"));
		
		try {
		  new PrintStream(foutStrm).println("<TR></TR><TR><TD BGCOLOR=BLACK WIDTH=5%></TD><TD BGCOLOR=BLACK WIDTH=28%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Time Taken : " + strTimeDifference + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=25%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Pass Count : " + g_iPassCount + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=25%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Fail Count : " + g_iFailCount + "</b></FONT></TD><TD BGCOLOR=Black WIDTH=7%></TD></TR></TABLE><TABLE WIDTH=100%><TR><TD><BR/></TD></TR><TR>");
		  
		  if(!sTestDetails.get().get("SEARCH_QUERY").trim().equalsIgnoreCase("")){
			  String searchQuery = sTestDetails.get().get("SEARCH_QUERY").trim();
			  sTestDetails.get().remove("SEARCH_QUERY");
			  String splunkUrl = Environment.get("splunkUrl").trim() + searchQuery;
			  String kibanaUrl = Environment.get("kibanaUrl").trim() + "(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-24h,mode:quick,to:now))&_a=(columns:!(_source),index:'logstash-*',interval:auto,sort:!('@timestamp',desc),query:(query_string:(analyze_wildcard:!t,query:'" + searchQuery + "')))";
			  if(Environment.get("splunkLogIntegration").trim().equalsIgnoreCase("true"))
				  new PrintStream(foutStrm).println("<TD ALIGN=CENTER><A TARGET='_blank' HREF='" + splunkUrl + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>SPLUNK LOGS</FONT></A></TD>");
			  if(Environment.get("kibanaLogIntegration").trim().equalsIgnoreCase("true"))
				  new PrintStream(foutStrm).println("<TD ALIGN=CENTER><A TARGET='_blank' HREF=\"" + kibanaUrl + "\"><FONT FACE=VERDANA COLOR=BLACK SIZE=2>KIBANA LOGS</FONT></A></TD>");
		  }
		  if(!sTestDetails.get().get("SAUCE_LABS_SESSION_ID").trim().equalsIgnoreCase("")){
			  String sauceLabsSessionId = sTestDetails.get().get("SAUCE_LABS_SESSION_ID").trim();
			  String authToken = sTestDetails.get().get("SAUCE_LABS_AUTH_TOKEN").trim();
			  sTestDetails.get().remove("SAUCE_LABS_SESSION_ID");
			  sTestDetails.get().remove("SAUCE_LABS_AUTH_TOKEN");
			  String sauceUrl = "https://saucelabs.com/jobs/" + sauceLabsSessionId + "?auth=" + authToken;
			  new PrintStream(foutStrm).println("<TD ALIGN=CENTER><A TARGET='_blank' HREF='" + sauceUrl + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>SAUCE REPORT</FONT></A></TD>");
		  }
		  if(sTestDetails.get().get("VIDEO_GIF").trim().equalsIgnoreCase("true")) {
			  sTestDetails.get().remove("VIDEO_GIF");
			  String screenShotPath = sTestDetails.get().get("G_STRRELLOGFOLDERNAME") + OSValidator.delimiter;
			  new PrintStream(foutStrm).println("<TD ALIGN=CENTER><A TARGET='_blank' HREF='" + screenShotPath + "video.gif" + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>VIDEO GIF</FONT></A></TD>");
		  }
		  if(!sTestDetails.get().get("BROWSER_CONSOLE_LOGS_PATH").trim().equalsIgnoreCase("")) {
			  String consoleLogs = sTestDetails.get().get("BROWSER_CONSOLE_LOGS_PATH").trim();
			  sTestDetails.get().remove("BROWSER_CONSOLE_LOGS_PATH");
			  new PrintStream(foutStrm).println("<TD ALIGN=CENTER><A TARGET='_blank' HREF='" + consoleLogs + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>CONSOLE LOGS</FONT></A></TD>");
		  }
		  
		  new PrintStream(foutStrm).println("</TR><TR><TD COLSPAN=6 ALIGN=RIGHT><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=1>&copy; " + Environment.get("orgName") + "</FONT></TD></TR></TABLE></BODY></HTML>");
		  foutStrm.close();
		}
		catch (IOException io){
			log.info("Threw a IOException in Reporting::fnCloseHtmlReport, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
		if (g_iFailCount != 0) {
		  strTestCaseResult = "Fail";
		} else {
		  strTestCaseResult = "Pass";
		}
		
		if (status.equals("Passed")) {
			strTestCaseResult = "Pass";
			g_iFailCount = 0;
		} else if (status.equals("Failed")) {
		  strTestCaseResult = "Fail";
		} else if (status.equals("Skipped")) {
			strTestCaseResult = "Skip";
			g_iFailCount = 0;
		}
		
		testsetdetails = fnWriteTestSummary(sTestDetails.get().get("TEST_NAME"), strTestCaseResult, strTimeDifference, testsetdetails);
		return testsetdetails;
	}
	  
	public void fnCloseTestSummary(HashMapNew testsetdetails) throws ParseException {
		Date g_SummaryEndTime = new Date();
		SimpleDateFormat sdfr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date g_SummaryStartTime = sdfr.parse(testsetdetails.get("G_SUMMARYSTARTTIME"));
		if(g_SummaryStartTime == null)
			return;
		int g_iTCPassed = Integer.valueOf(testsetdetails.get("G_ITCPASSED"));
		
		String strTimeDifference = fnTimeDiffference(g_SummaryStartTime.getTime(), g_SummaryEndTime.getTime());
		FileOutputStream foutStrm = sFoutStrm.get();
		try {
		  foutStrm = new FileOutputStream((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);
		  new PrintStream(foutStrm).println("</TABLE><TABLE WIDTH=100%><TR>");
		  new PrintStream(foutStrm).println("<TD BGCOLOR=BLACK WIDTH=10%></TD><TD BGCOLOR=BLACK WIDTH=60%><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B></B></FONT></TD><TD BGCOLOR=BLACK WIDTH=15%><FONT FACE=WINGDINGS SIZE=4>2</FONT><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B>Total Passed: " + g_iTCPassed + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=15%><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B>" + strTimeDifference + "</B></FONT></TD>");
		  new PrintStream(foutStrm).println("</TR></TABLE>");
		  new PrintStream(foutStrm).println("<TABLE WIDTH=100%><TR><TD ALIGN=RIGHT><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=1>&copy; " + Environment.get("orgName") + "</FONT></TD></TR></TABLE></BODY></HTML>");

		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnCloseTestSummary, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
	}
	  
	public void log(String strDescription, String strExpectedValue, String strObtainedValue, String strResult) {
		String sStep;
		if (this.sTestDetails.get().containsKey("STEP")) {
		  sStep = this.sTestDetails.get().get("STEP") + "<NS>" + strDescription + "<ND>" + strExpectedValue + "<ND>" + strObtainedValue + "<ND>" + strResult;
		  this.sTestDetails.get().remove("STEP");
		} else {
		  sStep = strDescription + "<ND>" + strExpectedValue + "<ND>" + strObtainedValue + "<ND>" + strResult;
		}
		this.sTestDetails.get().put("STEP", sStep);
		FileOutputStream foutStrm = sFoutStrm.get();
		try {
		  foutStrm = new FileOutputStream(sTestDetails.get().get("REPORT_NAME"), true);
		}
		catch (FileNotFoundException fe) {
			log.info("Threw a FileNotFoundException in Reporting::log, full stack trace follows:", fe);
		}
		
		int g_OperationCount = Integer.valueOf(sTestDetails.get().get("G_OPERATIONCOUNT"));
		int g_iPassCount = Integer.valueOf(sTestDetails.get().get("G_IPASSCOUNT"));
		int g_iFailCount = Integer.valueOf(sTestDetails.get().get("G_IFAILCOUNT"));
		int g_iSnapshotCount = Integer.valueOf(sTestDetails.get().get("G_ISNAPSHOTCOUNT"));
		
		g_OperationCount += 1;
		String sRowColor;
		
		if (g_OperationCount % 2 == 0) {
		  sRowColor = "#EEEEEE";
		} else {
		  sRowColor = "#D3D3D3";
		}    
		
		if(System.getProperty("takeScreenshot") != null && !System.getProperty("takeScreenshot").trim().equalsIgnoreCase("")) {
			Environment.put("takeScreenshot", System.getProperty("takeScreenshot").trim());
		}
		
		if(Environment.get("takeScreenshot").trim().equalsIgnoreCase("false") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("n") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("no")){
			if(strResult.trim().equalsIgnoreCase("pass")){
				strResult = "Done";
			}
		}
		
		boolean extraCheck = (Environment.get("takeScreenshot").trim().equalsIgnoreCase("false") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("n") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("no")) && (sTestDetails.get().containsKey("RAW_RESPONSE") && !sTestDetails.get().get("RAW_RESPONSE").trim().equalsIgnoreCase("")) && strResult.trim().equalsIgnoreCase("Done");
		if (strResult.toUpperCase().equals("PASS") || extraCheck) {
		  g_iPassCount += 1;
		  g_iSnapshotCount += 1;
		  
		  String snapshotFilePath = sTestDetails.get().get("G_STRSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  String relSnapshotFilePath = sTestDetails.get().get("G_STRRELSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  snapshotFilePath = fTakeScreenshot(snapshotFilePath);
		  if(snapshotFilePath.trim().endsWith(".html")){
			  relSnapshotFilePath = relSnapshotFilePath.replace(".gif", ".html");
		  }
		  String path = relSnapshotFilePath;
		  new PrintStream(foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 ><B>" + g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><A HREF='" + path + "'><FONT FACE=VERDANA SIZE=2 COLOR=GREEN><B>" + "Pass" + " </B></FONT></A></TD></TR>");
		}
		else if (strResult.toUpperCase().equals("FAIL")) {
		  sTestDetails.get().put("PAGE_SOURCE", "");
		  try{
//			  sTestDetails.get().put("PAGE_SOURCE", driverFactory.getDriver().get().getPageSource());
		  } catch(Exception ex){
			  //Do Nothing
		  }
		  g_iSnapshotCount += 1;
		  g_iFailCount += 1;     

		  String snapshotFilePath = sTestDetails.get().get("G_STRSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  String relSnapshotFilePath = sTestDetails.get().get("G_STRRELSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  snapshotFilePath = fTakeScreenshot(snapshotFilePath);
		  if(snapshotFilePath.trim().endsWith(".html")){
			  relSnapshotFilePath = relSnapshotFilePath.replace(".gif", ".html");
		  }
		  String path = relSnapshotFilePath;
		  new PrintStream(foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 ><B>" + g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><A HREF='" + path + "'><FONT FACE=VERDANA SIZE=2 COLOR=RED><B>" + "Fail" + " </B></FONT></A></TD></TR>");
		}
		else if (strResult.toUpperCase().equals("SKIP")) {
		  sTestDetails.get().put("PAGE_SOURCE", "");
		  g_iSnapshotCount += 1;

		  String snapshotFilePath = sTestDetails.get().get("G_STRSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  String relSnapshotFilePath = sTestDetails.get().get("G_STRRELSNAPSHOTFOLDERNAME") + OSValidator.delimiter + "SS_" + g_iSnapshotCount + ".gif";
		  snapshotFilePath = fTakeScreenshot(snapshotFilePath);
		  if(snapshotFilePath.trim().endsWith(".html")){
			  relSnapshotFilePath = relSnapshotFilePath.replace(".gif", ".html");
		  }
		  String path = relSnapshotFilePath;
		  new PrintStream(foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 ><B>" + g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><A HREF='" + path + "'><FONT FACE=VERDANA SIZE=2 COLOR=BLUE><B>" + "Skip" + " </B></FONT></A></TD></TR>");
		  
		} else if (strResult.toUpperCase().equals("DONE")) {
		  strResult = "Pass";
		  new PrintStream(foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2><B>" + g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 COLOR=LimeGreen><B>" + strResult + "</B></FONT></TD></TR>");
		}
		try {
		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::log, full stack trace follows:", io);
		}
		
		sTestDetails.get().put("G_OPERATIONCOUNT", String.valueOf(g_OperationCount));
		sTestDetails.get().put("G_IPASSCOUNT", String.valueOf(g_iPassCount));
		sTestDetails.get().put("G_IFAILCOUNT", String.valueOf(g_iFailCount));
		sTestDetails.get().put("G_ISNAPSHOTCOUNT", String.valueOf(g_iSnapshotCount));
	}
	  
	public String fTakeScreenshot(String SSPath) {
		try
		{
		  WebDriver screenDriver;
		  if ((this.driverType.contains("ANDROID")) || (this.driverType.contains("IOS"))) {
			screenDriver = driverFactory.getDriver().get();
		  } else {
			screenDriver = new Augmenter().augment(driverFactory.getDriver().get());
		  }
		  if(screenDriver != null){
			  String HTMLPath = null;
			  if(sTestDetails.get().containsKey("RAW_RESPONSE") && !sTestDetails.get().get("RAW_RESPONSE").trim().equalsIgnoreCase("")) {
				  HTMLPath = SSPath.replace(".gif", ".html");
				  FileOutputStream fout = new FileOutputStream(HTMLPath, true);
				  new PrintStream(fout).println("<HTML><BODY><TABLE ALIGN=CENTER WIDTH=100% BORDER=1><THEAD><TR><TH WIDTH=50% ALIGN=LEFT>REQUEST</TH><TH WIDTH=50% ALIGN=LEFT>RESPONSE</TH></TR></THEAD><TR VALIGN=TOP><TD WIDTH=50% ALIGN=LEFT>");
				  new PrintStream(fout).println(sTestDetails.get().get("RAW_REQUEST") + "</TD><TD WIDTH=50% ALIGN=LEFT>");
				  new PrintStream(fout).println(sTestDetails.get().get("RAW_RESPONSE") + "</TD></TR></TABLE>");
				  new PrintStream(fout).println("</BODY></HTML>"); 
				  fout.close();
				  fout = null;
				  
				  sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + "\n" + "Request :" + "\n\n");
				  sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + sTestDetails.get().get("LOG_RAW_REQUEST") + "\n");
				  sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + "\n" + "Response :" + "\n\n");
				  sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + sTestDetails.get().get("LOG_RAW_RESPONSE") + "\n");
				  
				  sTestDetails.get().remove("RAW_RESPONSE");
				  sTestDetails.get().remove("RAW_REQUEST");
				  sTestDetails.get().remove("LOG_RAW_RESPONSE");
				  sTestDetails.get().remove("LOG_RAW_REQUEST");
			  } else if(!sTestDetails.get().get("STACKTRACE").trim().equalsIgnoreCase("")){
				  HTMLPath = SSPath.replace(".gif", ".html");
				  String pagesource = SSPath.replace(".gif", "") + "_pagesource.xml";
				  FileOutputStream fpagesource = new FileOutputStream(pagesource, false);
				  new PrintStream(fpagesource).println(sTestDetails.get().get("PAGE_SOURCE"));
				  sTestDetails.get().remove("PAGE_SOURCE");
				  fpagesource.close();
				  fpagesource = null;
				  
				  FileOutputStream fout = new FileOutputStream(HTMLPath, true);
				  new PrintStream(fout).println("<HTML><BODY><A HREF='" + SSPath.substring(SSPath.lastIndexOf(OSValidator.delimiter) + 1, SSPath.length()) + "'>SCREENSHOT</A><BR/>");
				  if(!sTestDetails.get().get("PAGE_SOURCE").trim().equalsIgnoreCase(""))
					  new PrintStream(fout).println("<A HREF='" + pagesource.substring(pagesource.lastIndexOf(OSValidator.delimiter) + 1, pagesource.length()) + "'>PAGE SOURCE</A><BR/>");
				  new PrintStream(fout).println(sTestDetails.get().get("STACKTRACE"));
				  new PrintStream(fout).println("</BODY></HTML>"); 
				  sTestDetails.get().remove("STACKTRACE");
				  fout.close();
				  fout = null;
			  } else if(!sTestDetails.get().get("PAGE_SOURCE").trim().equalsIgnoreCase("")){
				  HTMLPath = SSPath.replace(".gif", ".html");
				  String pagesource = SSPath.replace(".gif", "") + "_pagesource.xml";
				  FileOutputStream fpagesource = new FileOutputStream(pagesource, false);
				  new PrintStream(fpagesource).println(sTestDetails.get().get("PAGE_SOURCE"));
				  sTestDetails.get().remove("PAGE_SOURCE");
				  fpagesource.close();
				  fpagesource = null;
				  
				  FileOutputStream fout = new FileOutputStream(HTMLPath, true);
				  new PrintStream(fout).println("<HTML><BODY><A HREF='" + SSPath.split(OSValidator.delimiter)[SSPath.split(OSValidator.delimiter).length - 1] + "'>SCREENSHOT</A><BR/>");
				  new PrintStream(fout).println("<A HREF='" + pagesource.split(OSValidator.delimiter)[pagesource.split(OSValidator.delimiter).length - 1] + "'>PAGE SOURCE</A><BR/>");
				  new PrintStream(fout).println("</BODY></HTML>"); 
				  fout.close();
				  fout = null;
			  }
			  
			  if(screenDriver != null){
				  try{
					  File scrFile = (File)((TakesScreenshot)screenDriver).getScreenshotAs(OutputType.FILE);
					  FileUtils.copyFile(scrFile, new File(SSPath));
					  FileUtils.deleteQuietly(scrFile);
					  scrFile = null;
					  try{
						Thread.sleep(1L);
					  }
					  catch (InterruptedException e){
						  log.info("Threw a InterruptedException in Reporting::fTakeScreenshot, full stack trace follows:", e);
					  }
				  } catch(Exception ex){
					  //Do Nothing
				  }
			  }
			  screenDriver = null;
			  if(HTMLPath != null){
				  SSPath = HTMLPath;
			  }
		  }
		  else{
			  SSPath = SSPath.replace(".gif", ".html");
			  FileOutputStream fout = new FileOutputStream(SSPath, true);
			  if(!sTestDetails.get().get("STACKTRACE").trim().equalsIgnoreCase("")){
				  new PrintStream(fout).println("<HTML><BODY>" + sTestDetails.get().get("STACKTRACE") + "</BODY></HTML>");
				  sTestDetails.get().remove("STACKTRACE");
			  }
			  else{
				  new PrintStream(fout).println("<HTML><BODY>"); 
				  for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
					  new PrintStream(fout).println(ste);
				  }
				  new PrintStream(fout).println("</BODY></HTML>"); 
			  }
			  fout.close();
			  fout = null;
		  }
		}
		catch (Exception e) {
			log.info("Threw a Exception in Reporting::fTakeScreenshot, full stack trace follows:", e);
		}
		
		return SSPath;
	}
	  
	public String fnTimeDiffference(long startTime, long endTime) {
		long delta = endTime - startTime;
		
		int days = (int)delta / 86400000;

		delta = (int)delta % 86400000;
		
		int hrs = (int)delta / 3600000;

		delta = (int)delta % 3600000;
		
		int min = (int)delta / 60000;

		delta = (int)delta % 60000;
		
		int sec = (int)delta / 1000;
		
		String strTimeDifference = days + "d " + hrs + "h " + min + "m " + sec + "s";
		return strTimeDifference;
	  }
	  
	public void fnWriteThreadReport(int iThreadCount, String sReportFile, String sCalendar, String sSummaryFile) {
		FileOutputStream foutStrm = sFoutStrm.get();
		try {
		  foutStrm = new FileOutputStream(sReportFile, true);
		  String sRowColor;
		  
		  if ((iThreadCount) % 2 == 0) {
			sRowColor = "#EEEEEE";
		  } else {
			sRowColor = "#D3D3D3";
		  }
		  new PrintStream(foutStrm).println("<TR COLS=3 BGCOLOR=" + sRowColor + "><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + iThreadCount + "</FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA SIZE=2>" + this.suiteTestName + "</FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA SIZE=2>" + sCalendar + "</FONT></TD><TD  WIDTH=20%><A HREF='" + sSummaryFile + "'><FONT FACE=VERDANA SIZE=2 COLOR=BLUE><B>Report</B></FONT></A></TD></TR>");
		  
		  foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnWriteThreadReport, full stack trace follows:", io);
		}
		sFoutStrm.set(null);
	}
		
	public static void copyFolder(File src, File dest) throws IOException{
		if(src.isDirectory()){
 
			//if directory not exists, create it
			if(!dest.exists()){
			   dest.mkdir();
			   log.info("Directory copied from "+ src + "  to " + dest);
			}
 
			//list all the directory contents
			String files[] = src.list();
 
			for (String file : files) {
			   //construct the src and dest file structure
			   File srcFile = new File(src, file);
			   File destFile = new File(dest, file);
			   //recursive copy
			   copyFolder(srcFile,destFile);
			}
 
		}else{
			//if file, then copy it
			//Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest); 
 
			byte[] buffer = new byte[1024];
 
			int length;
			//copy the file content in bytes 
			while ((length = in.read(buffer)) > 0){
			   out.write(buffer, 0, length);
			}
 
			in.close();
			out.close();
//			log.info("File copied from " + src + " to " + dest);
		}
	}

	@SuppressWarnings({ "unused" })
	private static String getIPOfNode(RemoteWebDriver remoteDriver) {
		String hostFound = null;
		try {
		  HttpCommandExecutor ce = (HttpCommandExecutor) remoteDriver.getCommandExecutor();
		  String hostName = ce.getAddressOfRemoteServer().getHost();
		  int port = ce.getAddressOfRemoteServer().getPort();
		  HttpHost host = new HttpHost(hostName, port);
		  @SuppressWarnings("resource")
		  DefaultHttpClient client = new DefaultHttpClient();
		  URL sessionURL = new URL("http://" + hostName + ":" + port
			+ "/grid/api/testsession?session=" + remoteDriver.getSessionId());
		  BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest(
			  "POST", sessionURL.toExternalForm());
		  HttpResponse response = client.execute(host, r);
		  JSONObject object = extractObject(response);
		  URL myURL = new URL(object.getString("proxyId"));
		  if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
			hostFound = myURL.getHost();
		  }
		} catch (Exception e) {
			log.info("Threw a Exception in Reporting::getIPOfNode, full stack trace follows:", e);
		}
		return hostFound;
	  }

	private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		InputStream contents = resp.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(contents, writer, "UTF8");
		JSONObject objToReturn = new JSONObject(writer.toString());
		return objToReturn;
	}
	
	public void fnUpdateThreadReport(String sReportFile, String sCalendar, HashMapNew testsetdetails) {
		int g_iTCPassed = Integer.valueOf(testsetdetails.get("G_ITCPASSED"));
		int g_iTCFailed = Integer.valueOf(testsetdetails.get("G_ITCFAILED"));
		int g_iTCSkipped = Integer.valueOf(testsetdetails.get("G_ITCSKIPPED"));
		
		try {
			File originalFile = new File(sReportFile);
	        BufferedReader br = new BufferedReader(new FileReader(originalFile));

	        // Construct the new file that will later be renamed to the original filename.
	        File tempFile = new File(sReportFile.replace(".html", "") + "_temp.html");
	        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

	        String line = null;
	        // Read from the original file and write to the new unless content matches data to be removed.
	        while ((line = br.readLine()) != null) {
	            if (line.contains(sCalendar) && line.contains(this.driverType)) {
	            	//<FONT FACE=VERDANA SIZE=2 COLOR=GREEN><B>Report</B></FONT>
	            	if(g_iTCFailed > 0){
	            		if(line.contains(">Report<")){
	            			line = line.replace("COLOR=BLUE><B>Report<", "COLOR=RED><B>Fail<");
	            		} else if(line.contains(">Pass<")){
	            			line = line.replace("COLOR=GREEN><B>Pass<", "COLOR=RED><B>Fail<");
	            		} else{
	            			line = line.replace("COLOR=BLUE><B>Skip<", "COLOR=RED><B>Fail<");
	            		}
	            	} else{
	            		if(g_iTCPassed == 0 && g_iTCSkipped > 0){
	            			if(line.contains(">Report<")){
		            			line = line.replace("COLOR=BLUE><B>Report<", "COLOR=BLUE><B>Skip<");
		            		}
	            		} else{
	            			if(line.contains(">Report<")){
		            			line = line.replace("COLOR=BLUE><B>Report<", "COLOR=GREEN><B>Pass<");
		            		} else{
		            			line = line.replace("COLOR=BLUE><B>Skip<", "COLOR=GREEN><B>Pass<");
		            		}
	            		}
	            	}
	            }
	            pw.println(line);
	            pw.flush();
	        }
	        pw.close();
	        br.close();
	        
	        // Rename the new file to the filename the original file had.
	        if (!tempFile.renameTo(originalFile)){
	            System.out.println("Could not rename file");
        	}
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::fnWriteThreadReport, full stack trace follows:", io);
		}
	}
}