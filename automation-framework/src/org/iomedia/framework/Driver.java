package org.iomedia.framework;

import org.iomedia.framework.Assert;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.iomedia.framework.DBActivities;
import org.iomedia.framework.GlobalFunctions;
import org.iomedia.framework.Infra;
import org.iomedia.framework.Driver;
import org.apache.commons.io.FileUtils;
import org.iomedia.common.BaseUtil;
import org.iomedia.common.EncryptDecrypt;
import org.iomedia.common.GifSequenceWriter;
import org.iomedia.common.RandomUserAgent;

import com.galenframework.api.Galen;
import com.galenframework.config.GalenConfig;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.TestReport;
import com.galenframework.reports.TestStatistic;
import com.galenframework.reports.model.LayoutObject;
import com.galenframework.reports.model.LayoutReport;
import com.galenframework.reports.model.LayoutSection;
import com.galenframework.reports.model.LayoutSpec;
import com.galenframework.speclang2.pagespec.SectionFilter;
import com.galenframework.support.GalenJavaTestBase;
import com.galenframework.support.GalenReportsContainer;
import com.galenframework.support.LayoutValidationException;
import com.galenframework.testng.GalenTestNgReportsListener;
import com.google.common.base.CharMatcher;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import cucumber.api.testng.PickleEventWrapper;
import cucumber.api.testng.TestNGCucumberRunner;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;

@Listeners(GalenTestNgReportsListener.class)
public abstract class Driver extends GalenJavaTestBase implements Runnable {
	
	String orgEnv;
	static Logger log = LoggerFactory.getLogger(Driver.class);
	protected String OS = System.getProperty("os.name").toLowerCase();
	protected String tcsReportsUrl;
	static String sReportFile;
	static String sConsolidatedReportFile;
	static String sConsolidatedReportsUrl;
	static String reportsUrl;
	static String resultPath;
	static String logPath;
	static String ReportFilePath;
	protected HashMapNew Environment;
	protected HashMapNew Dictionary;
	protected HashMapNew Testset;
	private HashMap<String, String> objGlobalDictOriginal;
	protected HashMap<Integer, RecordSet> RecordSetMap;
	private DBActivities objDB;
	protected WebDriverFactory driverFactory;
	protected String driverType;
	protected TestSuite testSuite;
	protected Assert Assert;
	protected BaseUtil BaseUtil;
	static HashMapNew deviceList;
	protected Reporting Reporter;
	static boolean bThreadFlag = false;
	static boolean bThreadFlag1 = false;
	
	static int threadCount = 0;
	
	protected SoftAssert SoftAssert;
	private static CSVWriter csvOutput = null;
	
	private static ThreadLocal<Date> sg_StartTime = new ThreadLocal<Date>(){
		@Override protected Date initialValue() {
			return null;
		}	
	};
	
	private Date g_StartTime;
	private Date g_EndTime;
	private Date c_StartTime;
	private Date c_EndTime;

	private static int totalPassedTCs;
	private static int totalFailedTCs;
	private static int totalPassedMtds;
	private static int totalFailedMtds;
	private static int totalSkippedMtds;
	private static HashMapNew duration;
	public static HashMapNew csvs;
    String suiteTestName;
	Driver objDriverClass;
	protected TestNGCucumberRunner testNGCucumberRunner;
	
	protected GlobalFunctions gblFunctions = new GlobalFunctions();
	
	protected ThreadLocal<Thread> sThread = new ThreadLocal<Thread>(){
		@Override protected Thread initialValue() {
			return null;
		}	
	};
	
	private static List<Thread> sThreadGroup = new ArrayList<Thread>();
	
	protected ThreadLocal<HashMapNew> sTestDetails = new ThreadLocal<HashMapNew>(){
		@Override protected HashMapNew initialValue() {
			return null;
		}	
	};
	
	protected static ThreadLocal<HashMapNew> sEnvironment = new ThreadLocal<HashMapNew>(){
		@Override protected HashMapNew initialValue() {
			return null;
		}	
	};
	
	protected static ThreadLocal<String> sTcsReportUrl = new ThreadLocal<String>(){
		@Override protected String initialValue() {
			return null;
		}	
	};
	
	protected static ThreadLocal<Boolean> sReplaceCalendarFile = new ThreadLocal<Boolean>(){
		@Override protected Boolean initialValue() {
			return true;
		}	
	};
	
	protected ThreadLocal<HashMapNew> sTestSetDetails = new ThreadLocal<HashMapNew>(){
		@Override protected HashMapNew initialValue() {
			return null;
		}	
	};
	
	protected static ThreadLocal<HashMap<Integer, RecordSet>> sRecordSetMap = new ThreadLocal<HashMap<Integer, RecordSet>>(){
		@Override protected HashMap<Integer, RecordSet> initialValue() {
			return null;
		}
	};
	
	private static  ThreadLocal<HashMap<String, String>> sobjGlobalDictOriginal = new ThreadLocal<HashMap<String, String>>(){
		@Override public HashMap<String, String> initialValue() {
			return null;
		}
	};
	
	protected static ThreadLocal<String> sSuiteTestName = new ThreadLocal<String>(){
		@Override protected String initialValue() {
			return null;
		}	
	};
	
	private ThreadLocal<HashMap<Integer, String>> sTemp = new ThreadLocal<HashMap<Integer, String>>(){
		@Override public HashMap<Integer, String> initialValue() {
			return null;
		}
	};
	
	private ThreadLocal<String> sSkip = new ThreadLocal<String>(){
		@Override public String initialValue() {
			return null;
		}
	};
	
	ThreadLocal<LayoutReport> layoutReport = new ThreadLocal<>();
	
	public Driver() {
		OSValidator.setPropValues(OS);
		Dictionary = new HashMapNew();
		Testset = new HashMapNew();
		driverFactory = new WebDriverFactory();
		testSuite = new TestSuite();
		Environment = getEnvValues();
		orgEnv = Environment.get("env").trim();
		objDB = new DBActivities(driverFactory, Dictionary, Environment);
		BaseUtil = new BaseUtil(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert, sTestDetails);
	}
	
	public static void deleteFile(File element) {
	    if (element.isDirectory()) {
	        for (File sub : element.listFiles()) {
	            deleteFile(sub);
	        }
	    }
	    element.delete();
	}
	
	@BeforeSuite(alwaysRun = true)
	public void setup(final ITestContext testContext) throws IOException{
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(System.getProperty("user.dir") + Environment.get("cucumber.properties").trim());
			prop.load(input);
			prop.putAll(System.getProperties());
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.setProperties(prop);
		System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "");
		duration = new HashMapNew();
		csvs = new HashMapNew();
		
		GalenConfig.reloadConfigFromPath(System.getProperty("user.dir") + OSValidator.delimiter + Environment.get("galenConfig").trim());
		String userdir = System.getProperty("user.dir");
		
		//Deleting the screenshot folder
		if(Environment.get("screenshotsFolder").trim().equalsIgnoreCase("")){
			Environment.put("screenshotsFolder", OSValidator.delimiter + "screenshots" + OSValidator.delimiter);
		}
		String screenshotPath = userdir + Environment.get("screenshotsFolder").trim();
		String galenReportFolder = userdir + OSValidator.delimiter + "target" + OSValidator.delimiter + "galen-html-reports" + OSValidator.delimiter;
		
		deleteFile(new File(screenshotPath));
		
		g_StartTime = new Date();
		sg_StartTime.set(g_StartTime);
		threadCount = 0;
		totalPassedTCs = 0;
		totalFailedTCs = 0;
		totalPassedMtds = 0;
		totalFailedMtds = 0;
		totalSkippedMtds = 0;
		try{
			//******************* Fetch Current TimeStamp ************************
			java.util.Date today = new java.util.Date();
			Timestamp now = new java.sql.Timestamp(today.getTime());
			String tempNow[] = now.toString().split("\\.");
			final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
			
			suiteTestName = testContext.getCurrentXmlTest().getSuite().getName();
			
			ReportFilePath = System.getProperty("user.dir") + OSValidator.delimiter +  "Reports" + OSValidator.delimiter + suiteTestName + "_" + sStartTime;
			reportsUrl = "Reports" + OSValidator.delimiter + suiteTestName + "_" + sStartTime;
			sConsolidatedReportsUrl = "Reports" + OSValidator.delimiter + suiteTestName + "_" + sStartTime;
			
//			if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")){
//				ReportFilePath += OSValidator.delimiter + System.getProperty("branch").trim();
//				reportsUrl += "/" + System.getProperty("branch").trim();
//				sConsolidatedReportsUrl += "/" + System.getProperty("branch").trim();
//			}
			
			sReportFile = ReportFilePath + OSValidator.delimiter + "report.html";
			sConsolidatedReportFile = ReportFilePath + OSValidator.delimiter + "index.html";
			sConsolidatedReportsUrl += "/" +  "index.html";
			reportsUrl += "/" +  "report.html";
			resultPath = ReportFilePath + OSValidator.delimiter + "results.csv";
			logPath = ReportFilePath + OSValidator.delimiter + "TestRunLogs.log";
			
			if(!new File(ReportFilePath).exists()){
				new File(ReportFilePath).mkdirs();
			}
			
			if(new File(resultPath).exists()){
				new File(resultPath).delete();
			}
			
			if(new File(logPath).exists()){
				new File(logPath).delete();
			}
			  
			csvOutput = new CSVWriter(new FileWriter(resultPath, true));
			  
			  //Create report file                  
			  FileOutputStream foutStrm = new FileOutputStream(sReportFile, true);
		           
			  //Write in Report file
	          new PrintStream(foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>");
	          String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
	          String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Environment.get("env").trim();
			  String machineName = env.toUpperCase();
			  try{
				  machineName = InetAddress.getLocalHost().getHostName();
			  } catch(Exception ex){
				  //Do nothing;
			  }
			  new PrintStream(foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine/Env " + machineName + " by user " + user + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>");  
	          new PrintStream(foutStrm).println("<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
        	  new PrintStream(foutStrm).println("<TR COLS=4 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Thread No.</B></FONT></TD><TD WIDTH=35%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Device/Browser Name</B></FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Module Name</B></FONT></TD><TD  WIDTH=20%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Report</B></FONT></TD></TR>");
	          //Close the object
	          foutStrm.close();	      
			  log.info("Report File Path : " + sReportFile);
			  
			//Move galen reports to separate folder
			if(new File(galenReportFolder).exists()) {
				Reporting.copyFolder(new File(galenReportFolder), new File(ReportFilePath + OSValidator.delimiter + "GalenReport"));
				deleteFile(new File(galenReportFolder));
			}
			  
		  }catch(Exception e){
			  log.info("Threw a Exception in Driver::setUpSuite, full stack trace follows:", e);
		  }
		
		BaseUtil = new BaseUtil(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert, sTestDetails);
		
		deviceList = new HashMapNew();
		
		if(System.getProperty("deviceId") != null && !System.getProperty("deviceId").trim().equalsIgnoreCase("")){
			String[] androidDeviceUids = System.getProperty("deviceId").trim().replace("\r\n", "\n").replace("\n\n", "\n").split("\n");
			for(int i = 0 ; i < androidDeviceUids.length; i++){
				deviceList.put("ANDROID" + (i+1) , androidDeviceUids[i]);
			}
		} 
		else if(Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("true") || Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("y") || Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("yes")){
			List<String> androidDeviceUids = new ArrayList<String>();
			String output = BaseUtil.runCommandUsingTerminal("adb devices", true, "1");
			String[] adeviceList = output.toString().split("\n");
			for(int i = 1; i < adeviceList.length; i++){
				if(adeviceList[i].toLowerCase().contains("device")){
					androidDeviceUids.add(adeviceList[i].split("device")[0].trim());
				}
			}
			
			if(androidDeviceUids != null && androidDeviceUids.size() > 0){
				for(int i = 0 ; i < androidDeviceUids.size(); i++){
					deviceList.put("ANDROID" + (i+1), androidDeviceUids.get(i));
				}
			}
		}
		
		if(Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("true") || Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("y") || Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("yes")){
			List<String> iosDeviceUids = new ArrayList<String>();
			String output = BaseUtil.runCommandUsingTerminal("instruments -s devices | grep -v '[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*'", true, "1");
			String[] adeviceList = output.split("\n");
			for(int i = 1; i < adeviceList.length; i++){
				if(adeviceList[i].toLowerCase().contains("[") && !adeviceList[i].toLowerCase().contains("instruments")){
					String deviceId = adeviceList[i].substring(adeviceList[i].indexOf("[") + 1).trim().substring(0, adeviceList[i].substring(adeviceList[i].indexOf("[") + 1).trim().length() - 1);
					if(!Pattern.matches("[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*", deviceId))
						iosDeviceUids.add(deviceId);
				}
			}
			
			if(iosDeviceUids != null && iosDeviceUids.size() > 0){
				for(int i = 0 ; i < iosDeviceUids.size(); i++){
					deviceList.put("IOS" + (i+1) , iosDeviceUids.get(i));
				}
			}
		}
	}
	
	public HashMapNew getEnvValues() {
		String environment = "";
		String clientName = "";
		HashMapNew temp;
		temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//common", 0);
		String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
		envConfig = CharMatcher.is('\'').trimFrom(envConfig);
		if(!envConfig.trim().equalsIgnoreCase("")) {
			if(!envConfig.trim().toUpperCase().contains("<CONFIG>") && !envConfig.trim().toLowerCase().contains("<selenium>")) {
				envConfig = EncryptDecrypt.getEnvConfig(envConfig.trim());
				System.setProperty("envConfig", envConfig);
			}
			temp.putAll(GetXMLNodeValueFromString(envConfig, "//CONFIG", 0));
			if(!temp.get("APP_URL").trim().equalsIgnoreCase("")) {
				String appurl = temp.get("APP_URL").trim();
				if(appurl.trim().endsWith("/"))
					appurl = appurl.trim().substring(0, appurl.trim().length() - 1);
				String clientId = appurl.substring(appurl.lastIndexOf("/") + 1).trim().toUpperCase();
				clientName = clientId;
				temp.put("env", clientId);
				String relatedEnv = null;
				relatedEnv = getRelatedEnv(temp.get("APP_URL"));
				if(relatedEnv != null && !relatedEnv.trim().equalsIgnoreCase("")) {
					environment = relatedEnv;
				} else {
					environment = temp.get("env");
				}
			}
		} else {
			if(temp != null){
				String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : temp.get("env");
				temp.put("env", env);
				String version = temp.get("version");
				String envFilePath = temp.get("envFilePath");
				if(!envFilePath.trim().equalsIgnoreCase("")){
					if(!env.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase(""))
						temp.putAll(GetXMLNodeValue(envFilePath, "//" + env + "/" + version, 0));
					else if(!env.trim().equalsIgnoreCase(""))
						temp.putAll(GetXMLNodeValue(envFilePath, "//" + env, 0));
				}
				environment = temp.get("env");
				clientName = environment; 
			}
		}
		
		if(temp.get("TM_OAUTH_URL").trim().endsWith("/")) {
			temp.put("TM_OAUTH_URL", temp.get("TM_OAUTH_URL").trim().substring(0, temp.get("TM_OAUTH_URL").trim().length() - 1));
		}
		if(temp.get("APP_URL").trim().endsWith("/")) {
			temp.put("APP_URL", temp.get("APP_URL").trim().substring(0, temp.get("APP_URL").trim().length() - 1));
		}
		if(temp.get("TM_HOST").trim().endsWith("/")) {
			temp.put("TM_HOST", temp.get("TM_HOST").trim().substring(0, temp.get("TM_HOST").trim().length() - 1));
		}
		if(temp.get("APP_URL").trim().split("//").length > 2) {
			String[] vars = temp.get("APP_URL").trim().split("//");
			String clientId = temp.get("APP_URL").trim().substring(temp.get("APP_URL").trim().lastIndexOf("/") + 1);
			temp.put("APP_URL", vars[0].trim() + "//" + vars[1].trim() + "/" + clientId);
		}
		String appCredentialsPath = temp.get("appCredentialsPath").trim();
		appCredentialsPath = appCredentialsPath.substring(0, appCredentialsPath.lastIndexOf(".")) + "_" + clientName.trim().toUpperCase() + ".xml";
		temp.put("appCredentialsPath", appCredentialsPath);
		
		if(!appCredentialsPath.trim().equalsIgnoreCase("")) {
			String version = temp.get("version");
			if(!environment.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase("")) {
				HashMapNew common = GetXMLNodeValue(appCredentialsPath, "//" + environment.trim().toUpperCase() + "/" + version.trim().toUpperCase() + "/COMMON", 0);
				if(common.isEmpty()) {
					String relatedEnvDataPath = appCredentialsPath.trim();
					relatedEnvDataPath = relatedEnvDataPath.substring(0, relatedEnvDataPath.lastIndexOf("_")) + "_" + environment.trim().toUpperCase() + ".xml";
					common = GetXMLNodeValue(relatedEnvDataPath, "//" + environment.trim().toUpperCase() + "/" + version.trim().toUpperCase() + "/COMMON", 0);
				}
		    	temp.putAll(common);
			}
		}
		temp.put("env", environment.trim());
		String splunkLogIntegration = System.getProperty("splunkLogIntegration") != null && !System.getProperty("splunkLogIntegration").trim().equalsIgnoreCase("") ? System.getProperty("splunkLogIntegration").trim() : temp.get("splunkLogIntegration").trim();
		String kibanaLogIntegration = System.getProperty("kibanaLogIntegration") != null && !System.getProperty("kibanaLogIntegration").trim().equalsIgnoreCase("") ? System.getProperty("kibanaLogIntegration").trim() : temp.get("kibanaLogIntegration").trim();
		temp.put("kibanaLogIntegration", kibanaLogIntegration);
		if(kibanaLogIntegration.trim().equalsIgnoreCase("true")) {
			splunkLogIntegration = "true";
		}
		temp.put("splunkLogIntegration", splunkLogIntegration);
		String videoGifIntegration = System.getProperty("videoGifIntegration") != null && !System.getProperty("videoGifIntegration").trim().equalsIgnoreCase("") ? System.getProperty("videoGifIntegration").trim() : temp.get("videoGifIntegration").trim();
		temp.put("videoGifIntegration", videoGifIntegration);
		String consoleLogsIntegration = System.getProperty("consoleLogsIntegration") != null && !System.getProperty("consoleLogsIntegration").trim().equalsIgnoreCase("") ? System.getProperty("consoleLogsIntegration").trim() : temp.get("consoleLogsIntegration").trim();
		temp.put("consoleLogsIntegration", consoleLogsIntegration);
		String generateGifOnlyOnFailure = System.getProperty("generateGifOnlyOnFailure") != null && !System.getProperty("generateGifOnlyOnFailure").trim().equalsIgnoreCase("") ? System.getProperty("generateGifOnlyOnFailure").trim() : temp.get("generateGifOnlyOnFailure").trim();
		temp.put("generateGifOnlyOnFailure", generateGifOnlyOnFailure);
		String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : temp.get("tunnelIdentifier").trim();
		temp.put("tunnelIdentifier", tunnelIdentifier);
		return temp;
	}
	
	public HashMapNew getEnvValues(String env) {
		env = env.trim();
		String environment = env;
		String clientName = "";
		HashMapNew temp;
		String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
		if(!envConfig.trim().equalsIgnoreCase("")) {
			temp = GetXMLNodeValueFromString(envConfig, "//CONFIG", 0);
			if(!temp.get("APP_URL").trim().equalsIgnoreCase("")) {
				String appurl = temp.get("APP_URL").trim();
				if(appurl.trim().endsWith("/"))
					appurl = appurl.trim().substring(0, appurl.trim().length() - 1);
				String clientId = appurl.substring(appurl.lastIndexOf("/") + 1).trim().toUpperCase();
				temp.put("env", clientId);
			} else
				temp.put("env", env.trim());
			clientName = temp.get("env");
			String relatedEnv = null;
			relatedEnv = getRelatedEnv(temp.get("APP_URL"));
			if(relatedEnv != null && !relatedEnv.trim().equalsIgnoreCase("")) {
				environment = relatedEnv;
			} else {
				environment = temp.get("env");
			}
		} else {
			temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//common", 0);
			if(temp != null){
				temp.put("env", env.trim());
				String version = temp.get("version");
				String envFilePath = temp.get("envFilePath");
				if(!envFilePath.trim().equalsIgnoreCase("")){
					if(!env.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase(""))
						temp.putAll(GetXMLNodeValue(envFilePath, "//" + env + "/" + version, 0));
					else if(!env.trim().equalsIgnoreCase(""))
						temp.putAll(GetXMLNodeValue(envFilePath, "//" + env, 0));
				}
				environment = temp.get("env");
				clientName = environment;
			}
		}
		
		if(temp.get("TM_OAUTH_URL").trim().endsWith("/")) {
			temp.put("TM_OAUTH_URL", temp.get("TM_OAUTH_URL").trim().substring(0, temp.get("TM_OAUTH_URL").trim().length() - 1));
		}
		if(temp.get("APP_URL").trim().endsWith("/")) {
			temp.put("APP_URL", temp.get("APP_URL").trim().substring(0, temp.get("APP_URL").trim().length() - 1));
		}
		if(temp.get("TM_HOST").trim().endsWith("/")) {
			temp.put("TM_HOST", temp.get("TM_HOST").trim().substring(0, temp.get("TM_HOST").trim().length() - 1));
		}
		if(temp.get("APP_URL").trim().split("//").length > 2) {
			String[] vars = temp.get("APP_URL").trim().split("//");
			String clientId = temp.get("APP_URL").trim().substring(temp.get("APP_URL").trim().lastIndexOf("/") + 1);
			temp.put("APP_URL", vars[0].trim() + "//" + vars[1].trim() + "/" + clientId);
		}
		String appCredentialsPath = temp.get("appCredentialsPath").trim();
		if(!appCredentialsPath.trim().equalsIgnoreCase("")) {
			appCredentialsPath = appCredentialsPath.substring(0, appCredentialsPath.lastIndexOf(".")) + "_" + clientName.trim().toUpperCase() + ".xml";
			temp.put("appCredentialsPath", appCredentialsPath);
			
			if(!appCredentialsPath.trim().equalsIgnoreCase("")) {
				String version = temp.get("version");
				if(!environment.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase("")) {
					HashMapNew common = GetXMLNodeValue(appCredentialsPath, "//" + environment.trim().toUpperCase() + "/" + version.trim().toUpperCase() + "/COMMON", 0);
					if(common.isEmpty()) {
						String relatedEnvDataPath = appCredentialsPath.trim();
						relatedEnvDataPath = relatedEnvDataPath.substring(0, relatedEnvDataPath.lastIndexOf("_")) + "_" + environment.trim().toUpperCase() + ".xml";
						common = GetXMLNodeValue(relatedEnvDataPath, "//" + environment.trim().toUpperCase() + "/" + version.trim().toUpperCase() + "/COMMON", 0);
					}
			    	temp.putAll(common);
				}
			}
		}
		temp.put("env", environment.trim());
		String splunkLogIntegration = System.getProperty("splunkLogIntegration") != null && !System.getProperty("splunkLogIntegration").trim().equalsIgnoreCase("") ? System.getProperty("splunkLogIntegration").trim() : temp.get("splunkLogIntegration").trim();
		String kibanaLogIntegration = System.getProperty("kibanaLogIntegration") != null && !System.getProperty("kibanaLogIntegration").trim().equalsIgnoreCase("") ? System.getProperty("kibanaLogIntegration").trim() : temp.get("kibanaLogIntegration").trim();
		temp.put("kibanaLogIntegration", kibanaLogIntegration);
		if(kibanaLogIntegration.trim().equalsIgnoreCase("true")) {
			splunkLogIntegration = "true";
		}
		temp.put("splunkLogIntegration", splunkLogIntegration);
		String videoGifIntegration = System.getProperty("videoGifIntegration") != null && !System.getProperty("videoGifIntegration").trim().equalsIgnoreCase("") ? System.getProperty("videoGifIntegration").trim() : temp.get("videoGifIntegration").trim();
		temp.put("videoGifIntegration", videoGifIntegration);
		String consoleLogsIntegration = System.getProperty("consoleLogsIntegration") != null && !System.getProperty("consoleLogsIntegration").trim().equalsIgnoreCase("") ? System.getProperty("consoleLogsIntegration").trim() : temp.get("consoleLogsIntegration").trim();
		temp.put("consoleLogsIntegration", consoleLogsIntegration);
		String generateGifOnlyOnFailure = System.getProperty("generateGifOnlyOnFailure") != null && !System.getProperty("generateGifOnlyOnFailure").trim().equalsIgnoreCase("") ? System.getProperty("generateGifOnlyOnFailure").trim() : temp.get("generateGifOnlyOnFailure").trim();
		temp.put("generateGifOnlyOnFailure", generateGifOnlyOnFailure);
		String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : temp.get("tunnelIdentifier").trim();
		temp.put("tunnelIdentifier", tunnelIdentifier);
		return temp;
	}
	
	@Parameters({ "browser" , "calendar" })
	@BeforeTest(alwaysRun = true)
	public void getDevices(@Optional("chrome") String browser, @Optional("") String calendar, ITestContext context) throws Exception {
        driverType = browser;
        suiteTestName = context.getCurrentXmlTest().getName().trim();
		driverFactory.setDriverType(new ThreadLocal<String>(){@Override public String initialValue() {
			return browser;
		};});
		testSuite.setTestSuiteName(suiteTestName);
		sSuiteTestName.set(suiteTestName);
		objGlobalDictOriginal = new HashMap<String, String>();
		
		String User = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
	    String RootPath = System.getProperty("user.dir");
	    
	    String deviceConf = System.getProperty("deviceConf") != null && !System.getProperty("deviceConf").trim().equalsIgnoreCase("") ? System.getProperty("deviceConf").trim() : "";
    	HashMapNew temp;
    	if(deviceConf.trim().equalsIgnoreCase(""))
			temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//" + driverType.toLowerCase(), 0);
		else
			temp = GetXMLNodeValueFromString(deviceConf, "//" + driverType.toLowerCase(), 0);
    	
    	if(temp != null){
    		Environment.putAll(temp);
    	}
    	
    	if(!Environment.get("appCredentialsPath").trim().equalsIgnoreCase("")) {
    		temp = GetXMLNodeValue(Environment.get("appCredentialsPath").trim(), "//" + Environment.get("env").trim().toUpperCase() + "/" + Environment.get("version").trim().toUpperCase() + "/" + driverType.trim().toUpperCase(), 0);
    	}
    	
    	if(temp != null){
    		Environment.putAll(temp);
    	}
    	
    	//Environment checker
        if(!Environment.get("envCheckMethod").trim().equalsIgnoreCase(""))
        	envcheck(suiteTestName);
    	
    	String Datasheet = !calendar.trim().equalsIgnoreCase("") ? calendar.trim() : System.getProperty("calendar") != null && !System.getProperty("calendar").trim().equalsIgnoreCase("") ? System.getProperty("calendar").trim() : Environment.get("calendar").trim();
    	System.setProperty("calendar", Datasheet);
    	String ExecutionFolderPath = RootPath + OSValidator.delimiter + "Execution";
    	String DatasheetsPath = RootPath + Environment.get("dataSheets").trim();
    	String EnvironmentXLSPath = RootPath + (String)this.Environment.get("envFilePath");
	    String relExecutionFolderPath = "Execution";
	    String reportsExecutionFolderPath = ReportFilePath + OSValidator.delimiter + "Execution";
	    tcsReportsUrl = "Execution";
	    
	    String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
	    String clientName = null;
		if(!envConfig.trim().equalsIgnoreCase("")) {
			HashMapNew config = GetXMLNodeValueFromString(envConfig, "//CONFIG", 0);
			clientName = getClientName(config.get("APP_URL"));
		}
		
		String environment;
		if(clientName != null && !clientName.trim().equalsIgnoreCase("")) {
			environment = clientName;
		} else {
			environment = Environment.get("env").trim();
		}
		
	    String CurrentExecutionFolder = ExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
	    String relCurrentExecutionFolder = relExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
	    String CurrentReportsExecutionFolder = reportsExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
	    tcsReportsUrl += "/" + Datasheet + "/" + environment.trim().toUpperCase() + "/" + User;
	    String CurrentExecutionDatasheet = CurrentExecutionFolder + OSValidator.delimiter + Datasheet + ".xls";
	    String CommonSheet = "COMMON";
	    String CurrentExecutionCommonSheet = CurrentExecutionFolder + OSValidator.delimiter + CommonSheet + ".xls";
	    
	    Environment.put("ROOTPATH", RootPath);
	    Environment.put("EXECUTIONFOLDERPATH", ExecutionFolderPath);
	    Environment.put("CURRENTEXECUTIONFOLDER", CurrentExecutionFolder);
	    Environment.put("RELCURRENTEXECUTIONFOLDER", relCurrentExecutionFolder);
	    Environment.put("CURRENTREPORTSEXECUTIONFOLDER", CurrentReportsExecutionFolder);
	    Environment.put("DATASHEETSPATH", DatasheetsPath);
	    Environment.put("ENVIRONMENTXLSPATH", EnvironmentXLSPath);
	    Environment.put("CURRENTEXECUTIONDATASHEET", CurrentExecutionDatasheet);
	    Environment.put("CURRENTEXECUTIONCOMMONSHEET", CurrentExecutionCommonSheet);
	    
	    new File(CurrentExecutionFolder).mkdirs();

	    Boolean replaceCalendarFile = System.getProperty("replaceCalendarFile") != null && !System.getProperty("replaceCalendarFile").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("replaceCalendarFile").trim()) : Boolean.valueOf(Environment.get("replaceCalendarFile").trim());
	    
	    if (!new File(CurrentExecutionDatasheet).exists() || (replaceCalendarFile && sReplaceCalendarFile.get())) {
	        this.gblFunctions.fCopyXLS(DatasheetsPath + Datasheet + ".xls", CurrentExecutionDatasheet);
	    }
	    if (!new File(CurrentExecutionCommonSheet).exists() || (replaceCalendarFile && sReplaceCalendarFile.get())) {
	    	this.gblFunctions.fCopyXLS(DatasheetsPath + CommonSheet + ".xls", CurrentExecutionCommonSheet);
	  	}

	    if(sReplaceCalendarFile.get())
	    	sReplaceCalendarFile.set(false);
	      
	    if(Environment.containsKey("auto_dp") && Environment.get("auto_dp").trim().equalsIgnoreCase("true")) {
	    	DBActivities objDB1 = new DBActivities(driverFactory, Dictionary, Environment);
	    	try {
	    		objDB1.fDBActivities();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	} catch (SQLException e) {
	    		e.printStackTrace();
	    	}
	    	Environment.put("auto_dp", "false");
	    }
	    
	    while (bThreadFlag1) {
			try{
				Thread.sleep(500L);
			}
			catch (Exception localException1) {}
		}
	    
	    Infra objInfra = new Infra(driverFactory, this.Dictionary, this.Environment, this.objGlobalDictOriginal);
	    bThreadFlag1 = true;
	    String clearX = System.getProperty("clearX") != null && !System.getProperty("clearX").trim().equalsIgnoreCase("") ? System.getProperty("clearX").trim() : Environment.get("clearX").trim();
	    if (!clearX.equals("")) {
	    	objInfra.fClearSkip(clearX);
	    }
	    
	    objDB = new DBActivities(driverFactory, Dictionary, Environment);
	    ArrayList<String> columnNames = objDB.fGetColumnName(CurrentExecutionDatasheet, "MAIN");
	    
	    int skipColumnNo = columnNames.indexOf("SKIP_" + this.driverType.toUpperCase().replace(" ", ""));
	    int headerColumnNo = columnNames.indexOf("HEADER");
	    int testNameColumnNo = columnNames.indexOf("TEST_NAME");
	    int actionNameColumnNo = columnNames.indexOf("ACTION");
	    
	    if(skipColumnNo == -1)
	    	return;
	    
	    List<List<String>> calendarFileData = null;
	    
    	calendarFileData = objDB.fRetrieveDataExcel(CurrentExecutionDatasheet, "MAIN", new int[]{skipColumnNo, headerColumnNo}, new String[]{"", ""});
    	bThreadFlag1 = false;
    	
	    if(calendarFileData == null) {
	    	return;
	    }
	    
	    if(calendarFileData.size() == 0) {
	    	return;
	    }
	    
	    RecordSetMap = new HashMap<Integer, RecordSet>();
	    int iRSCount = 0;
	    do {
	    	RecordSetMap.put(Integer.valueOf(iRSCount + 1), new RecordSet(calendarFileData.get(iRSCount).get(actionNameColumnNo), Integer.valueOf(calendarFileData.get(iRSCount).get(0)) - 1, calendarFileData.get(iRSCount).get(testNameColumnNo), Integer.valueOf(calendarFileData.get(iRSCount).get(0)) + 1));
	    	iRSCount++;
	    } while (iRSCount < calendarFileData.size());
	    
	    sEnvironment.set(Environment);
	    sTcsReportUrl.set(tcsReportsUrl);
	    sRecordSetMap.set(RecordSetMap);
	    sobjGlobalDictOriginal.set(objGlobalDictOriginal);
	    sTestSetDetails.set(Testset);
	}

	@Parameters({"browser", "calendar", "env"})
	@BeforeClass(alwaysRun = true)
	public void setUpTest(@Optional("chrome") String browser, @Optional("") String calendar, @Optional("") String env, ITestContext context) {
		testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
		Environment = sEnvironment.get() != null ? sEnvironment.get() : Environment;
		tcsReportsUrl = sTcsReportUrl.get() != null ? sTcsReportUrl.get() : tcsReportsUrl;
		suiteTestName = sSuiteTestName.get() != null ? sSuiteTestName.get() : suiteTestName;
		RecordSetMap = sRecordSetMap.get() != null ? sRecordSetMap.get() : RecordSetMap;
		objGlobalDictOriginal = sobjGlobalDictOriginal.get() != null ? sobjGlobalDictOriginal.get() : objGlobalDictOriginal;
		Testset = sTestSetDetails.get() != null ? sTestSetDetails.get() : Testset;
		
		if(context.getSuite().getParallel().trim().equalsIgnoreCase("classes")) {
	        driverType = browser;
	        driverFactory.setDriverType(new ThreadLocal<String>(){@Override public String initialValue() {
				return browser;
			};});
			suiteTestName = context.getCurrentXmlTest().getName().trim();
			testSuite.setTestSuiteName(suiteTestName);
			sSuiteTestName.set(suiteTestName);
			objGlobalDictOriginal = new HashMap<String, String>();
			
			String User = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
		    String RootPath = System.getProperty("user.dir");
		    
		    String deviceConf = System.getProperty("deviceConf") != null && !System.getProperty("deviceConf").trim().equalsIgnoreCase("") ? System.getProperty("deviceConf").trim() : "";
	    	HashMapNew temp;
	    	if(deviceConf.trim().equalsIgnoreCase(""))
				temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//" + driverType.toLowerCase(), 0);
			else
				temp = GetXMLNodeValueFromString(deviceConf, "//" + driverType.toLowerCase(), 0);
	    	
	    	if(temp != null){
	    		Environment.putAll(temp);
	    	}
	    	
	    	if(!Environment.get("appCredentialsPath").trim().equalsIgnoreCase("")) {
	    		temp = GetXMLNodeValue(Environment.get("appCredentialsPath").trim(), "//" + Environment.get("env").trim().toUpperCase() + "/" + Environment.get("version").trim().toUpperCase() + "/" + driverType.trim().toUpperCase() , 0);
	    	}
	    	
	    	if(temp != null){
	    		Environment.putAll(temp);
	    	}
	    	
	    	String Datasheet = !calendar.trim().equalsIgnoreCase("") ? calendar.trim() : System.getProperty("calendar") != null && !System.getProperty("calendar").trim().equalsIgnoreCase("") ? System.getProperty("calendar").trim() : Environment.get("calendar").trim();
	    	String ExecutionFolderPath = RootPath + OSValidator.delimiter + "Execution";
	    	String DatasheetsPath = RootPath + Environment.get("dataSheets").trim();
	    	String EnvironmentXLSPath = RootPath + (String)this.Environment.get("envFilePath");
		    String relExecutionFolderPath = "Execution";
		    String reportsExecutionFolderPath = ReportFilePath + OSValidator.delimiter + "Execution";
		    tcsReportsUrl = "Execution";
		    
		    String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
		    String clientName = null;
			if(!envConfig.trim().equalsIgnoreCase("")) {
				HashMapNew config = GetXMLNodeValueFromString(envConfig, "//CONFIG", 0);
				clientName = getClientName(config.get("APP_URL"));
			}
			
			String environment;
			if(clientName != null && !clientName.trim().equalsIgnoreCase("")) {
				environment = clientName;
			} else {
				environment = Environment.get("env").trim();
			}
		    String CurrentExecutionFolder = ExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
		    String relCurrentExecutionFolder = relExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
		    String CurrentReportsExecutionFolder = reportsExecutionFolderPath + OSValidator.delimiter + Datasheet + OSValidator.delimiter + environment.trim().toUpperCase() + OSValidator.delimiter + User;
		    tcsReportsUrl += "/" + Datasheet + "/" + environment.trim().toUpperCase() + "/" + User;
		    String CurrentExecutionDatasheet = CurrentExecutionFolder + OSValidator.delimiter + Datasheet + ".xls";
		    String CommonSheet = "COMMON";
		    String CurrentExecutionCommonSheet = CurrentExecutionFolder + OSValidator.delimiter + CommonSheet + ".xls";
		    
		    Environment.put("ROOTPATH", RootPath);
		    Environment.put("EXECUTIONFOLDERPATH", ExecutionFolderPath);
		    Environment.put("CURRENTEXECUTIONFOLDER", CurrentExecutionFolder);
		    Environment.put("RELCURRENTEXECUTIONFOLDER", relCurrentExecutionFolder);
		    Environment.put("CURRENTREPORTSEXECUTIONFOLDER", CurrentReportsExecutionFolder);
		    Environment.put("DATASHEETSPATH", DatasheetsPath);
		    Environment.put("ENVIRONMENTXLSPATH", EnvironmentXLSPath);
		    Environment.put("CURRENTEXECUTIONDATASHEET", CurrentExecutionDatasheet);
		    Environment.put("CURRENTEXECUTIONCOMMONSHEET", CurrentExecutionCommonSheet);
		    
		    new File(CurrentExecutionFolder).mkdirs();
		    
		    if(Environment.containsKey("auto_dp") && Environment.get("auto_dp").trim().equalsIgnoreCase("true")) {
		    	DBActivities objDB1 = new DBActivities(driverFactory, Dictionary, Environment);
		    	try {
		    		objDB1.fDBActivities();
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	} catch (SQLException e) {
		    		e.printStackTrace();
		    	}
		    	Environment.put("auto_dp", "false");
		    }
		    
		    while (bThreadFlag1) {
				try{
					Thread.sleep(500L);
				}
				catch (Exception localException1) {}
			}
		    
		    Infra objInfra = new Infra(this.driverFactory, this.Dictionary, this.Environment, this.objGlobalDictOriginal);
		    bThreadFlag1 = true;
		    String clearX = System.getProperty("clearX") != null && !System.getProperty("clearX").trim().equalsIgnoreCase("") ? System.getProperty("clearX").trim() : Environment.get("clearX").trim();
		    if (!clearX.equals("")) {
		    	objInfra.fClearSkip(clearX);
		    }
		    
		    objDB = new DBActivities(driverFactory, Dictionary, Environment);
		    ArrayList<String> columnNames = objDB.fGetColumnName(CurrentExecutionDatasheet, "MAIN");
		    
		    int skipColumnNo = columnNames.indexOf("SKIP_" + this.driverType.toUpperCase().replace(" ", ""));
		    int headerColumnNo = columnNames.indexOf("HEADER");
		    int testNameColumnNo = columnNames.indexOf("TEST_NAME");
		    int actionNameColumnNo = columnNames.indexOf("ACTION");
		    
		    if(skipColumnNo == -1)
		    	return;
		    
		    List<List<String>> calendarFileData = null;
		    
	    	calendarFileData = objDB.fRetrieveDataExcel(CurrentExecutionDatasheet, "MAIN", new int[]{skipColumnNo, headerColumnNo}, new String[]{"", ""});
	    	bThreadFlag1 = false;
	    	
		    if(calendarFileData == null) {
		    	return;
		    }
		    
		    if(calendarFileData.size() == 0) {
		    	return;
		    }
		    
		    RecordSetMap = new HashMap<Integer, RecordSet>();
		    int iRSCount = 0;
		    do {
		    	RecordSetMap.put(Integer.valueOf(iRSCount + 1), new RecordSet(calendarFileData.get(iRSCount).get(actionNameColumnNo), Integer.valueOf(calendarFileData.get(iRSCount).get(0)) - 1, calendarFileData.get(iRSCount).get(testNameColumnNo), Integer.valueOf(calendarFileData.get(iRSCount).get(0)) + 1));
		    	iRSCount++;
		    } while (iRSCount < calendarFileData.size());
		    
		    sEnvironment.set(Environment);
		    sTcsReportUrl.set(tcsReportsUrl);
		    sRecordSetMap.set(RecordSetMap);
		    sobjGlobalDictOriginal.set(objGlobalDictOriginal);
		    sTestSetDetails.set(Testset);
		}
		
		c_StartTime = new Date();
        driverType = browser;
        driverFactory.setDriverType(new ThreadLocal<String>(){@Override public String initialValue() {
			return browser;
		};});
		
		Date today = new Date();
	    Timestamp now = new Timestamp(today.getTime());
	    String[] tempNow = now.toString().split("\\.");
	    String timeStamp = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
	    
		String HTMPReports = Environment.get("CURRENTREPORTSEXECUTIONFOLDER") + OSValidator.delimiter + driverType + OSValidator.delimiter + this.getClass().getName() + OSValidator.delimiter + "HTML_REP_" + timeStamp;
		String relHTMPReports = Environment.get("RELCURRENTEXECUTIONFOLDER") + OSValidator.delimiter + driverType + OSValidator.delimiter + this.getClass().getName() + OSValidator.delimiter + "HTML_REP_" + timeStamp;
		tcsReportsUrl += "/" + driverType + "/" + this.getClass().getName() + "/" + "HTML_REP_" + timeStamp;
	    String SnapshotsFolder = HTMPReports + OSValidator.delimiter + "Snapshots";
	    String relSnapshotsFolder = "Snapshots";
	    String LogsFolder = HTMPReports + OSValidator.delimiter + "Logs";
	    String relLogsFolder = "Logs";
	    
	    Environment.put("HTMLREPORTSPATH", HTMPReports);
	    Environment.put("RELHTMLREPORTSPATH", relHTMPReports);
	    Environment.put("SNAPSHOTSFOLDER", SnapshotsFolder);
	    Environment.put("RELSNAPSHOTSFOLDER", relSnapshotsFolder);
	    Environment.put("LOGSFOLDER", LogsFolder);
	    Environment.put("RELLOGSFOLDER", relLogsFolder);
	    
	    new File(LogsFolder).mkdirs();
	    boolean success = new File(SnapshotsFolder).mkdirs();

	    if (success) {
	      log.info("Directories: " + SnapshotsFolder + " created");
	    }
	      
	    String[] words = this.getClass().getName().trim().split("\\.");
		String className = words[words.length - 1];
    	Dictionary.put("TEST_CLASS_NAME", className);
    	
    	System.out.println("Class Name Found :: " + className);
	    
	    Reporter = new Reporting(driverFactory, testSuite, Dictionary, Environment, sTestDetails);
	    Testset = Reporter.fnCreateSummaryReport();
	    sTestSetDetails.set(Testset);
	    
	    while (bThreadFlag) {
			try{
				Thread.sleep(500L);
			}
			catch (Exception localException1) {}
		}
	    
	    bThreadFlag = true;
	    if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")) {
	    	Reporter.fnWriteThreadReport(++threadCount, sReportFile, className, (String)Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html");
	    }
	    else {
	    	Reporter.fnWriteThreadReport(++threadCount, sReportFile, className, (String)Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html");
	    }
	    bThreadFlag = false;
	    
        Dictionary.put("ENV", env.trim().equalsIgnoreCase("") ? Environment.get("env").trim() : env.trim());
        Testset = sTestSetDetails.get();
        
        if(context.getSuite().getParallel().trim().equalsIgnoreCase("classes")) {
        	sEnvironment.set(Environment);
		    sTcsReportUrl.set(tcsReportsUrl);
		    sRecordSetMap.set(RecordSetMap);
		    sobjGlobalDictOriginal.set(objGlobalDictOriginal);
		    sTestSetDetails.set(Testset);
        }
	}
	
	@BeforeMethod(alwaysRun = true)
    public void initReport(Method method, Object[] arguments) {
        GalenTestInfo ti = createTestInfo(method, arguments);
        testInfo.set(ti);
        report.set(GalenReportsContainer.get().registerTest(ti));
    }
	
	/**
     * {@inheritDoc}
     */
    @BeforeMethod(alwaysRun = true)
    public void initDriver(Method method, Object[] args, ITestContext context) {
    	driverFactory.setDriverType(new ThreadLocal<String>(){@Override public String initialValue() {
			return driverType;
		};});
    	
    	if(context.getSuite().getParallel().trim().equalsIgnoreCase("classes")) {
    		Environment = sEnvironment.get() != null ? sEnvironment.get() : Environment;
    		tcsReportsUrl = sTcsReportUrl.get() != null ? sTcsReportUrl.get() : tcsReportsUrl;
    		suiteTestName = sSuiteTestName.get() != null ? sSuiteTestName.get() : suiteTestName;
    		RecordSetMap = sRecordSetMap.get() != null ? sRecordSetMap.get() : RecordSetMap;
    		objGlobalDictOriginal = sobjGlobalDictOriginal.get() != null ? sobjGlobalDictOriginal.get() : objGlobalDictOriginal;
    		Testset = sTestSetDetails.get() != null ? sTestSetDetails.get() : Testset;
    	}
    	
    	Thread newChildThread = new Thread(objDriverClass);
        newChildThread.start();
        
    	String env = System.getProperty("env") != null && !System.getProperty("env").trim().equalsIgnoreCase("") ? System.getProperty("env").trim() : Dictionary.get("ENV").trim();
    	
    	String testName = method.getName().trim();
    	String actionName = method.getName().trim();
    	
    	if (args.length > 0) {
            if (args[0] != null && args[0] instanceof TestDevice) {
                TestDevice device = (TestDevice)args[0];
                actionName += "_" + device.toString();
                if (device.getScreenSize() != null) {
                	testName += " (" + device.toString() + ")";
                }
            } else if (args[0] != null) {
            	String str = String.valueOf(args[0]);
            	actionName += "_" + str.toString();
            	testName += " (" + str.toString() + ")";
            }
        }
    	
    	Environment.putAll(getEnvValues(env));
    	
    	String deviceConf = System.getProperty("deviceConf") != null && !System.getProperty("deviceConf").trim().equalsIgnoreCase("") ? System.getProperty("deviceConf").trim() : "";
    	HashMapNew temp;
    	if(deviceConf.trim().equalsIgnoreCase(""))
			temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//" + driverType.toLowerCase(), 0);
		else
			temp = GetXMLNodeValueFromString(deviceConf, "//" + driverType.toLowerCase(), 0);
    	
    	if(temp != null){
    		Environment.putAll(temp);
    	}
    	
    	if(!Environment.get("appCredentialsPath").trim().equalsIgnoreCase("")) {
			String relatedEnv = null;
			String envConfig = System.getProperty("envConfig") != null && !System.getProperty("envConfig").trim().equalsIgnoreCase("") ? System.getProperty("envConfig").trim() : "";
			if(!envConfig.trim().equalsIgnoreCase("") && !Environment.get("foundCredentailsForRelatedEnvsMtd").trim().equalsIgnoreCase("")) {
    			String[] words = Environment.get("foundCredentailsForRelatedEnvsMtd").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("foundCredentailsForRelatedEnvsMtd").trim().substring(0, Environment.get("foundCredentailsForRelatedEnvsMtd").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriverFactory.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class, ThreadLocal.class }).newInstance(new Object[] { this.driverFactory, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert, this.sTestDetails });
					Method _method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					Object objReturn = _method.invoke(busFunctions, params);
					if (objReturn != null) {
						relatedEnv = (String) objReturn;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
    		}
			
			if(relatedEnv != null && !relatedEnv.trim().equalsIgnoreCase("")) {
	    		temp = GetXMLNodeValue(Environment.get("appCredentialsPath").trim(), "//" + relatedEnv.trim().toUpperCase() + "/" + Environment.get("version").trim().toUpperCase() + "/" + driverType.trim().toUpperCase(), 0);
			} else {
				temp = GetXMLNodeValue(Environment.get("appCredentialsPath").trim(), "//" + Environment.get("env").trim().toUpperCase() + "/" + Environment.get("version").trim().toUpperCase() + "/" + driverType.trim().toUpperCase() , 0);
			}
    	}
    	
    	if(temp != null){
    		Environment.putAll(temp);
    	}
    	
//    	//Environment checker
//        if(!Environment.get("envCheckMethod").trim().equalsIgnoreCase(""))
//        	envcheck(suiteTestName);
    	
        int iRSCount = 0;
        boolean flag = false;
        int requiredStartRowNumber = -1;
        int requiredEndRowNumber = -1;
        int requiredCurrentRowNumber = -1;
        do {
    	  if(RecordSetMap == null)
    		  break;
          RecordSet res1 = (RecordSet)RecordSetMap.get(Integer.valueOf(iRSCount + 1));
          int iScriptStartRow = res1.get_sStartRow();
          int iScriptCurrentRow = iScriptStartRow; 
          int iScriptEndRow = res1.get_sEndRow();
          String sActionName = res1.get_sActionName();
          String newActionName = "";
          
          if(actionName.trim().contains("_"))
        	  newActionName = actionName.trim().substring(0, actionName.trim().indexOf("_"));
          else
        	  newActionName = actionName.trim();
          
          if(sActionName.trim().equalsIgnoreCase(newActionName)){
        	  requiredStartRowNumber = iScriptStartRow;
        	  requiredEndRowNumber = iScriptEndRow;
        	  requiredCurrentRowNumber = iScriptCurrentRow;
        	  flag = true;
        	  break;
          }
          iRSCount++;
        } while(iRSCount < RecordSetMap.size());
        
        if(!flag) {
        	System.out.println("SKIPEXCEPTION :: " + "Need not to execute :: " + actionName + " on " + driverType);
        	throw new SkipException("Need not to execute :: " + actionName);
        }
        
        HashMap<Integer, String> Temp = new HashMap<Integer, String>();
        sTemp.set(Temp);
        String skip = null;
        sSkip.set(skip);
        do {
        	while (bThreadFlag1) {
				try{
					Thread.sleep(500L);
				}
				catch (Exception localException1) {}
			}
		    
        	bThreadFlag1 = true;
	        if ((fProcessDataFile(requiredStartRowNumber) == 1) && (sSkip.get().trim().equalsIgnoreCase(""))){
	        	Infra objInfra = new Infra(this.driverFactory, this.Dictionary, this.Environment, this.objGlobalDictOriginal);
	        	if (!objInfra.fGetReferenceData()) {
	                return;
	        	}
	        }
	        bThreadFlag1 = false;
	        requiredStartRowNumber++;
        } while(requiredStartRowNumber != requiredEndRowNumber);
        
        HashMapNew testDetails = new HashMapNew();
        
        if(!Dictionary.get("COMMENTS").trim().equalsIgnoreCase(""))
        	testName = Dictionary.get("TEST_NAME").trim() + "[" + Dictionary.get("COMMENTS").trim() + "] - " + testName ;
        else
        	testName = Dictionary.get("TEST_NAME").trim() + " - " + testName ;
        
    	testDetails.put("TEST_NAME", testName);
    	testDetails.put("ACTION", actionName);
    	
    	sTestDetails.set(testDetails);
    	driverFactory.setTestDetails(sTestDetails);
    	
    	Reporter = new Reporting(driverFactory, testSuite, Dictionary, Environment, sTestDetails);
    	Reporter.fnCreateHtmlReport(actionName);
        
        sTestDetails.get().put("STARTROWNUMBER", String.valueOf(requiredStartRowNumber - 1));
        sTestDetails.get().put("CURRENTROWNUMBER", String.valueOf(requiredCurrentRowNumber));
        
        log.info("########################" + testName + " EXECUTION STARTED########################");
        sTestDetails.get().put("TEST_RUN_LOG", "########################" + suiteTestName.trim().toUpperCase() + " : " + testName + " EXECUTION STARTED########################");
        
		WebDriver driver = createDriver(args);
		this.driver.set(driver);
    }
	
    @Override
    public WebDriver createDriver(Object[] args) {
    	String dimension = "";
    	if (args.length > 0) {
            if (args[0] != null && args[0] instanceof TestDevice) {
                TestDevice device = (TestDevice)args[0];
                dimension = String.valueOf(device.getScreenSize().getWidth()) + "x" + String.valueOf(device.getScreenSize().getHeight());
            }
        }
    	
    	try{
	    	BaseUtil = new BaseUtil(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert, sTestDetails);
	    	
	    	String profiles = System.getProperty("profiles") != null && !System.getProperty("profiles").trim().equalsIgnoreCase("") ? System.getProperty("profiles").trim().toLowerCase() : Environment.get("profiles").trim().toLowerCase();
	    	boolean runLocally = System.getProperty("runLocally") != null && !System.getProperty("runLocally").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("runLocally").trim().toLowerCase()) : Boolean.valueOf(Environment.get("runLocally").trim().toLowerCase());
	    	boolean callSetSeleniumUri = System.getProperty("callSetSeleniumUri") != null && !System.getProperty("callSetSeleniumUri").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("callSetSeleniumUri").trim().toLowerCase()) : Boolean.valueOf(Environment.get("callSetSeleniumUri").trim().toLowerCase());
	    	
	    	/**Set sauce labs URI based on env**/
	    	if(callSetSeleniumUri && !Environment.get("setSeleniumUri").trim().equalsIgnoreCase("")) {
    			String[] words = Environment.get("setSeleniumUri").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("setSeleniumUri").trim().substring(0, Environment.get("setSeleniumUri").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriverFactory.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class, ThreadLocal.class }).newInstance(new Object[] { this.driverFactory, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert, this.sTestDetails });
					Method _method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					_method.invoke(busFunctions, params);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
    		}
	    	
	    	String seleniumURI = System.getProperty("seleniumURI") != null && !System.getProperty("seleniumURI").trim().equalsIgnoreCase("") ? System.getProperty("seleniumURI").trim() : Environment.get("seleniumURI").trim();
	    	
	    	WebDriver driver = null;
	    	if (driverType.trim().toUpperCase().contains("ANDROID")) {
	    		driver = initializeAndroid(runLocally, seleniumURI, dimension);
	    	} 
	    	else if(driverType.trim().toUpperCase().contains("IOS")) {
	    		driver = initializeIOS(runLocally, seleniumURI, dimension);
	    	}
	    	else if(driverType.trim().toUpperCase().contains("CHROME")) {
				driver = initializeChrome(profiles, runLocally, seleniumURI, dimension);
			}
			else if(driverType.trim().toUpperCase().contains("FIREFOX")){
				driver = initializeFirefox(profiles, runLocally, seleniumURI, dimension);
			}
			else if(driverType.trim().toUpperCase().contains("SAFARI")){
				if(runLocally) {
					BaseUtil.runCommandUsingTerminal("killall cookied", true, "1");
					BaseUtil.runCommandUsingTerminal("rm -rf ~/Library/Caches/com.apple.Safari/*", true, "1");
					BaseUtil.runCommandUsingTerminal("rm -rf ~/Library/Safari/LocalStorage/*", true, "1");
					BaseUtil.runCommandUsingTerminal("rm -rf ~/Library/Cookies/*", true, "1");
				}
				driver = initializeSafari(runLocally, seleniumURI, dimension);
			}
			else if(driverType.trim().toUpperCase().contains("IE")){
				driver = initializeIE(runLocally, seleniumURI, dimension);
			}
			else{
				System.out.println("SKIPEXCEPTION :: " + "Invalid driver type " + driverType);
				throw new SkipException("Invalid driver type " + driverType);
			}
			      
	    	boolean maximizeWindow = System.getProperty("maximizeWindow") != null && !System.getProperty("maximizeWindow").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("maximizeWindow").trim()) : Boolean.valueOf(Environment.get("maximizeWindow").trim());
	    	
	        if (args.length > 0) {
	            if (args[0] != null && args[0] instanceof TestDevice) {
	                TestDevice device = (TestDevice)args[0];
	                if (device.getScreenSize() != null && !driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")) {
	                	if(runLocally) {
	                		driver.manage().window().setSize(device.getScreenSize());
	                	}
	                }
	            } else {
	            	if(runLocally && !driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")) {
	            		if(maximizeWindow) {
	            			int screenSizePadding = 0;
	            	    	if(OSValidator.isWindows(OS)){
	            		    	String screensizepaddingvalue = System.getProperty("screensizepadding") != null && !System.getProperty("screensizepadding").trim().equalsIgnoreCase("") ? System.getProperty("screensizepadding").trim() : Environment.get("screensizepadding").trim();
	            		    	screenSizePadding = screensizepaddingvalue.equalsIgnoreCase("") ? 0 : Integer.valueOf(screensizepaddingvalue);
	            	    	}
	            	    	String _dimension = "1280x960";
	            	    	String[] dimens = _dimension.trim().toLowerCase().split("x");
	            			int x = Integer.valueOf(dimens[0]) + screenSizePadding;
	            			int y = Integer.valueOf(dimens[1]);
	            			driver.manage().window().setSize(new Dimension(x, y));
	            		}
	            	}
	            }
	        } else {
	        	if(runLocally && !driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")) {
	        		if(maximizeWindow) {
            			int screenSizePadding = 0;
            	    	if(OSValidator.isWindows(OS)){
            		    	String screensizepaddingvalue = System.getProperty("screensizepadding") != null && !System.getProperty("screensizepadding").trim().equalsIgnoreCase("") ? System.getProperty("screensizepadding").trim() : Environment.get("screensizepadding").trim();
            		    	screenSizePadding = screensizepaddingvalue.equalsIgnoreCase("") ? 0 : Integer.valueOf(screensizepaddingvalue);
            	    	}
            	    	String _dimension = "1280x960";
            	    	String[] dimens = _dimension.trim().toLowerCase().split("x");
            			int x = Integer.valueOf(dimens[0]) + screenSizePadding;
            			int y = Integer.valueOf(dimens[1]);
            			driver.manage().window().setSize(new Dimension(x, y));
            		}
            	}
	        }
	        
	        driver.manage().timeouts().implicitlyWait(Integer.parseInt(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
	        driverFactory.setDriverType(new ThreadLocal<String>(){@Override public String initialValue() {
				return driverType;
			};});
	        final WebDriver newDriver = driver;
	        driverFactory.setDriver(new ThreadLocal<WebDriver>(){@Override public WebDriver initialValue() {
				return newDriver;
			};});
	        driverFactory.setDictionary(new ThreadLocal<HashMapNew>(){@Override public HashMapNew initialValue() {
				return Dictionary;
			};});
	        driverFactory.setEnvironment(new ThreadLocal<HashMapNew>(){@Override public HashMapNew initialValue() {
				return Environment;
			};});
	        
	        Reporter = new Reporting(driverFactory, testSuite, Dictionary, Environment, sTestDetails);
	        Assert = new Assert(Reporter);
			SoftAssert = new SoftAssert(Reporter);
	        
	        driverFactory.setReporting(new ThreadLocal<Reporting>(){@Override public Reporting initialValue() {
				return Reporter;
			};});
	        driverFactory.setAssert(new ThreadLocal<Assert>(){@Override public Assert initialValue() {
				return Assert;
			};});
	        driverFactory.setSoftAssert(new ThreadLocal<SoftAssert>(){@Override public SoftAssert initialValue() {
				return SoftAssert;
			};});
	        driverFactory.setTestDetails(sTestDetails);
	        
	        BaseUtil = new BaseUtil(driverFactory, Dictionary, Environment, Reporter, Assert, SoftAssert, sTestDetails);
	        
	        return driver;
    	} catch(Exception ex){
    		ex.printStackTrace();
    	}
    	
    	return null;
    }
    
    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@AfterMethod(alwaysRun = true)
    public void quitDriver(ITestResult tr, Object[] args) throws Exception {
    	if(sTestDetails == null || sTestDetails.get() == null || sTestDetails.get().get("REPORT_NAME").trim().equalsIgnoreCase("")) {
    		return;
    	}
    	if(sThread.get() != null) {
    		Thread _thread = sThread.get();
    		_thread.stop();
    		_thread = null;
    		sThread.set(_thread);
    		
    		String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME") + OSValidator.delimiter;
    		File dir = new File(screenShotPath);
    		String[] allFiles = dir.list(new FilenameFilter() {
    			@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("SS") && name.endsWith(".gif");
				}
    		});
    		String[] files = new String[allFiles.length];
    		for(int i = 0; i < files.length; i++) {
    			files[i] = "SS_" + (i + 1) + ".gif";
    		}
    		if(driverFactory != null && driverFactory.getDriver() != null && driverFactory.getDriver().get() != null) {
    			boolean needGif = false;
    			if(Environment.get("generateGifOnlyOnFailure").trim().equalsIgnoreCase("true")) {
    				if(tr.getStatus() == 2) {
    					needGif = true;
    				} else {
    					sTestDetails.get().remove("VIDEO_GIF");
    				}
    			} else {
    				needGif = true;
    			}
    					
    			if(needGif) {
		    		_thread = new Thread("Generating gif for " + suiteTestName.trim() + "-" + driverFactory.getDictionary().get().get("TEST_CLASS_NAME") + "-" + sTestDetails.get().get("SCRIPT_NAME")){
		    			public void run(){
		    				try {
		    					if(files.length > 0)
		    						GifSequenceWriter.generateGif(screenShotPath, files, "video.gif");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			}
		    		};
			    	_thread.start();
			    	sThreadGroup.add(_thread);
    			} else {
    				for(int i=0; i<files.length; i++) {
    					deleteFile(new File(screenShotPath + files[i]));
    				}
    			}
    		}
    	}
    	generateLogs();
    	String dimension = "";
    	if (args.length > 0) {
            if (args[0] != null && args[0] instanceof TestDevice) {
                TestDevice device = (TestDevice)args[0];
                dimension = String.valueOf(device.getScreenSize().getWidth()) + "x" + String.valueOf(device.getScreenSize().getHeight());
            }
        }
    	boolean isUi = false;
    	if(!dimension.trim().equalsIgnoreCase("")) {
    		isUi = true;
    	}
    	Infra objInfra = new Infra(this.driverFactory, this.Dictionary, this.Environment, this.objGlobalDictOriginal);
    	if(sTestDetails.get() != null && !sTestDetails.get().get("CURRENTROWNUMBER").trim().equalsIgnoreCase("")){
    		while (bThreadFlag1) {
				try{
					Thread.sleep(500L);
				}
				catch (Exception localException1) {}
			}
		    
    		bThreadFlag1 = true;
    		objInfra.fUpdateTestCaseRowSkip(Integer.valueOf(sTestDetails.get().get("CURRENTROWNUMBER")), "");
    		bThreadFlag1 = false;
    	
	    	sTestDetails.get().put("STACKTRACE", "");
	    	sTestDetails.get().put("PAGE_SOURCE", "");
			int tcp = 0;
			int tcf = 0;
			int tcs = 0;
			
			if(Reporter != null){
				if(tr.getStatus() == 1){
					Reporter.log(tr.getName(), "Verify method status", "Method passed successfully", "Done");
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
				    
					bThreadFlag1 = true;
					objInfra.fSetReferenceData();
					
					Testset = Reporter.fnCloseHtmlReport("Passed", Testset);
					Reporter.fnUpdateThreadReport(sReportFile, Dictionary.get("TEST_CLASS_NAME"), Testset);
					bThreadFlag1 = false;
					log.info("########################" + sTestDetails.get().get("TEST_NAME").trim() + " EXECUTION PASSED########################");
					sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + "\n" + "########################" + suiteTestName.trim().toUpperCase() + " : " + sTestDetails.get().get("TEST_NAME").trim() + " EXECUTION PASSED########################");
					writeToLog();
					if(sTestDetails.get().get("TEST_NAME").trim().toLowerCase().contains("<br")){
						tcp = sTestDetails.get().get("TEST_NAME").trim().split("</?[Bb][rR]/?>").length;
					}
					else{
						tcp = 1;
					}
					totalPassedMtds += 1;
					writeToCSV("PASS", tcp, tcf, tcs, isUi);
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
				    
					bThreadFlag1 = true;
					objInfra.fUpdateTestCaseRowSkip(Integer.valueOf(sTestDetails.get().get("STARTROWNUMBER")), "PASS");
					bThreadFlag1 = false;
				} else if(tr.getStatus() == 3){
					try{
//						sTestDetails.get().put("PAGE_SOURCE", driverFactory.getDriver().get().getPageSource());
					  } catch(Exception ex){
						  //Do Nothing
					  }
					Throwable throwable = tr.getThrowable();
					if(throwable != null){
						sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + throwable.getMessage());
						if(throwable.getCause() != null){
							sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + throwable.getCause().toString());
						}
						StackTraceElement[] trace = throwable.getStackTrace();
						for(int i = 0 ; i < trace.length; i++){
							sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + trace[i].toString());
						}
					}
					Reporter.log(tr.getName(), "Check validation", "Test case got skipped, please check TestNG reports", "Skip");
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
					bThreadFlag1 = true;
				    Testset = Reporter.fnCloseHtmlReport("Skipped", Testset);
					Reporter.fnUpdateThreadReport(sReportFile, Dictionary.get("TEST_CLASS_NAME"), Testset);
					bThreadFlag1 = false;
					log.info("########################" + (!sTestDetails.get().get("TEST_NAME").trim().equalsIgnoreCase("") ? sTestDetails.get().get("TEST_NAME").trim() : tr.getMethod().getMethodName().trim()) + " EXECUTION SKIPPED########################");
					sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + "\n" + "########################" + suiteTestName.trim().toUpperCase() + " : " + (!sTestDetails.get().get("TEST_NAME").trim().equalsIgnoreCase("") ? sTestDetails.get().get("TEST_NAME").trim() : tr.getMethod().getMethodName().trim()) + " EXECUTION SKIPPED########################");
					writeToLog();
					if(sTestDetails.get().get("TEST_NAME").trim().toLowerCase().contains("<br")){
						tcs = sTestDetails.get().get("TEST_NAME").trim().split("</?[Bb][rR]/?>").length;
					}
					else{
						tcs = 1;
					}
					totalSkippedMtds += 1;
					writeToCSV("SKIP", tcp, tcf, tcs, isUi);
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
					bThreadFlag1 = true;
					objInfra.fUpdateTestCaseRowSkip(Integer.valueOf(sTestDetails.get().get("STARTROWNUMBER")), "SKIP");
					bThreadFlag1 = false;
				}
				else if(tr.getStatus() == 2){
					try{
//						sTestDetails.get().put("PAGE_SOURCE", driverFactory.getDriver().get().getPageSource());
					  } catch(Exception ex){
						  //Do Nothing
					  }
					Throwable throwable = tr.getThrowable();
					if(throwable != null){
						sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + throwable.getMessage());
						if(throwable.getCause() != null){
							sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + throwable.getCause().toString());
						}
						StackTraceElement[] trace = throwable.getStackTrace();
						for(int i = 0 ; i < trace.length; i++){
							sTestDetails.get().put("STACKTRACE", sTestDetails.get().get("STACKTRACE") + "<BR/>" + trace[i].toString());
						}
					}
					Reporter.log(tr.getName(), "Check validation", "Some validation was not successfull, please check TestNG reports", "Fail");
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
				    
					bThreadFlag1 = true;
					objInfra.fSetReferenceData();
					
				    Testset = Reporter.fnCloseHtmlReport("Failed", Testset);
					Reporter.fnUpdateThreadReport(sReportFile, Dictionary.get("TEST_CLASS_NAME"), Testset);
					bThreadFlag1 = false;
					log.info("########################" + sTestDetails.get().get("TEST_NAME").trim() + " EXECUTION FAILED########################");
					sTestDetails.get().put("TEST_RUN_LOG", sTestDetails.get().get("TEST_RUN_LOG") + "\n" + "########################" + suiteTestName.trim().toUpperCase() + " : " + sTestDetails.get().get("TEST_NAME").trim() + " EXECUTION FAILED########################");
					writeToLog();
					if(Environment.get("modifyGalenPassFailInReport").trim().equalsIgnoreCase("true") && !dimension.trim().equalsIgnoreCase("")) {
						TestReport testReport = getReport();
						TestStatistic testStatistic = testReport.fetchStatistic();
						LayoutReport layoutReport = this.layoutReport.get();
						boolean flag = false;
						if(layoutReport != null) {
							List<LayoutSection> sections = layoutReport.getSections();
							for(int i = 0; sections != null && i < sections.size(); i++) {
								LayoutSection section = sections.get(i);
								flag = isCriticalSectionsHasErrors(section, false);
								if(flag) {
									break;
								}
							}
						}
						int passedSpecs = testStatistic.getPassed();
						int totalSpecs = testStatistic.getTotal();
						float percentage = ((float)passedSpecs/(float)totalSpecs) * 100;
						
						if(passedSpecs == totalSpecs && passedSpecs == 1) {
							tcf = 1;
							totalFailedMtds += 1;
							writeToCSV("FAIL", tcp, tcf, tcs, isUi);
						} else {
							int passPercentage = Environment.get("passPercentage").trim().equalsIgnoreCase("") ? 90 : Integer.valueOf(Environment.get("passPercentage").trim()); 
							if(percentage >= passPercentage && !flag){
								tcp = 1;
								totalPassedMtds += 1;
								writeToCSV("PASS", tcp, tcf, tcs, isUi);
							} else {
								tcf = 1;
								totalFailedMtds += 1;
								writeToCSV("FAIL", tcp, tcf, tcs, isUi);
							}
						}
					} else {
						if(sTestDetails.get().get("TEST_NAME").trim().toLowerCase().contains("<br")){
							tcf = sTestDetails.get().get("TEST_NAME").trim().split("</?[Bb][rR]/?>").length;
							if(!sTestDetails.get().get("TC_PASSED_COUNT").trim().equalsIgnoreCase("")){
								int passedCount = Integer.valueOf(sTestDetails.get().get("TC_PASSED_COUNT").trim());
								tcp = passedCount;
								tcf -= passedCount;
							}
						}
						else{
							tcf = 1;
						}
						totalFailedMtds += 1;
						writeToCSV("FAIL", tcp, tcf, tcs, isUi);
					}
					while (bThreadFlag1) {
						try{
							Thread.sleep(500L);
						}
						catch (Exception localException1) {}
					}
				    
					bThreadFlag1 = true;
					objInfra.fUpdateTestCaseRowSkip(Integer.valueOf(sTestDetails.get().get("STARTROWNUMBER")), "FAIL");
					bThreadFlag1 = false;
				}
			}
			
			totalPassedTCs += tcp;
			totalFailedTCs += tcf;
			
			sTestDetails.get().remove("TC_PASSED_COUNT");
	
			if(this.driver.get() != null) {
				try {
					super.quitDriver();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				WebDriver driver = driverFactory.getDriver().get();
				driver = null;
				driverFactory.getDriver().set(driver);
			}
	    	
	    	sTestDetails.get().remove("TEST_NAME");
	    	sTestDetails.get().remove("ACTION");
	    	sTestDetails.get().remove("REPORT_NAME");
    	}
    }
    
    private boolean isCriticalSectionsHasErrors(LayoutSection section, boolean flag){
    	if(flag) {
    		return true;
    	}
		String sectionName = section.getName();
		if(sectionName.trim().toLowerCase().contains("critical")) {
			List<LayoutObject> layoutObjects = section.getObjects();
			for(int j = 0; j < layoutObjects.size(); j++) {
				LayoutObject layoutObject = layoutObjects.get(j);
				List<LayoutSpec> layoutSpecs = layoutObject.getSpecs();
				for(int k = 0; k < layoutSpecs.size(); k++) {
					LayoutSpec layoutSpec = layoutSpecs.get(k);
					if(layoutSpec.getErrors() != null && layoutSpec.getErrors().size() > 0) {
						flag = true;
						break;
					}
				}
				if(flag)
					break;
			}
			if(!flag) {
				List<LayoutSection> sections = section.getSections();
				boolean flag1 = false;
				for(int i = 0; sections != null && i < sections.size(); i++) {
					flag1 = isCriticalSectionsHasErrors(sections.get(i), flag);
					if(flag1) {
						break;
					}
				}
				flag = flag1;
			}
		} else {
			List<LayoutSection> sections = section.getSections();
			boolean flag1 = false;
			for(int i = 0; sections != null && i < sections.size(); i++) {
				flag1 = isCriticalSectionsHasErrors(sections.get(i), flag);
				if(flag1) {
					break;
				}
			}
			flag = flag1;
		}
		return flag;
    }
	
	private void writeToCSV(String status, int tcp, int tcf, int tcs, boolean isUi) throws IOException {
		try{
			csvOutput = new CSVWriter(new FileWriter(resultPath, true));
			if(sTestDetails.get().get("TC_EXEC_TOTAL_DURATION").trim().equalsIgnoreCase("")){
				sTestDetails.get().put("TC_EXEC_TOTAL_DURATION", "0d 0h 0m 0s");
			}
			String user = System.getProperty("BUILD_USER_ID") != null && !System.getProperty("BUILD_USER_ID").trim().equalsIgnoreCase("") ? System.getProperty("BUILD_USER_ID").trim() : System.getProperty("user.name");
			String severity = Dictionary.get("SEVERITY").trim().equalsIgnoreCase("") ? Environment.get("defaultSeverity").trim().toUpperCase() : Dictionary.get("SEVERITY").trim().toUpperCase();
			csvOutput.writeNext(new String[] {driverType, Environment.get("udid"), Dictionary.get("TEST_CLASS_NAME"), sTestDetails.get().get("TEST_NAME").trim(), sTestDetails.get().get("ACTION").trim(), Dictionary.get(driverType.trim().toUpperCase() + "_MANUFACTURER"), Dictionary.get(driverType.trim().toUpperCase() + "_MODEL"), Dictionary.get(driverType.trim().toUpperCase() + "_VERSION"), Dictionary.get(driverType.trim().toUpperCase() + "_OPERATOR"), sTestDetails.get().get("TC_EXEC_TOTAL_DURATION"), user, sTestDetails.get().get("TC_START_TIME"), status.trim().toUpperCase(), String.valueOf(tcp), String.valueOf(tcf), sTestDetails.get().get("REL_REPORT_NAME"), sTestDetails.get().get("REL_SNAPSHOTS_NAME"), sTestDetails.get().get("REL_LOGS_NAME"), String.valueOf(tcs), suiteTestName, isUi ? "UI" : "FUNCTIONAL", severity});
		}
		finally{
			if(csvOutput != null)
				csvOutput.close();
		}
	}
	
	void writeToLog() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logPath, true))) {
			bw.write(sTestDetails.get().get("TEST_RUN_LOG").trim());
		} catch (IOException e) {
			//Do Nothing
		}
		sTestDetails.get().remove("TEST_RUN_LOG");
	}
    
	@Parameters({ "calendar" })
	@AfterClass(alwaysRun = true)
	public void closeTestSummary(@Optional("") String calendar) throws Exception {
		if(Reporter != null)
			Reporter.fnCloseTestSummary(Testset);
		c_EndTime = new Date();
		if(c_StartTime != null && c_EndTime != null){
			String strTimeDifference = fnTimeDiffference(c_StartTime.getTime(), c_EndTime.getTime());
			
			if(System.getProperty("classTo") != null && !System.getProperty("classTo").trim().equalsIgnoreCase("")){
				Environment.put("classTo", System.getProperty("classTo").trim());
				Environment.put("emailNotification", "true");
			}
			
			if(Environment.get("emailNotification").trim().equalsIgnoreCase("true") || Environment.get("emailNotification").trim().equalsIgnoreCase("yes")){
				String subject = "";
				String Datasheet = !calendar.trim().equalsIgnoreCase("") ? calendar.trim() : System.getProperty("calendar") != null && !System.getProperty("calendar").trim().equalsIgnoreCase("") ? System.getProperty("calendar").trim() : Environment.get("calendar").trim();
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
				subject = environment + " : " + Datasheet + " : " + suiteTestName.trim().toUpperCase() + " : " + Dictionary.get("TEST_CLASS_NAME").trim() + " : " + "Execution status";

				duration.put(driverType.trim().toUpperCase() + "_" + Dictionary.get("TEST_CLASS_NAME").trim().toUpperCase() + "_DURATION", strTimeDifference);
				String summaryUrl;
//				if(System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")){
//					String jenkinsIp = System.getProperty("jenkinsIp") != null && !System.getProperty("jenkinsIp").trim().equalsIgnoreCase("") ? System.getProperty("jenkinsIp").trim().toLowerCase() : Environment.get("jenkinsIpAddress").trim().toLowerCase();
//					String folderPath = sConsolidatedReportFile.trim().substring(sConsolidatedReportFile.lastIndexOf("Reports"), sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter));
//					summaryUrl = "http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + folderPath + "/" + tcsReportsUrl + "/SummaryReport.html";
//				} else {
					summaryUrl = tcsReportsUrl + "/SummaryReport.html";
//				}
				
				csvs.put(driverType.trim().toUpperCase() + "_" + Dictionary.get("TEST_CLASS_NAME").trim().toUpperCase() + "_SUMMARY_URL", summaryUrl);
				
				if(!Environment.get("classTo").trim().equalsIgnoreCase("") || !Environment.get("classCc").trim().equalsIgnoreCase("") || !Environment.get("classBcc").trim().equalsIgnoreCase(""))
					classDraftReport(subject, strTimeDifference);
			}
		}
		if(testNGCucumberRunner != null)
			testNGCucumberRunner.finish();
	}
	
	private void classDraftReport(String subject, String totalTime) throws Exception{
		if(new File(resultPath).exists()){
			CSVReader reader = null;
			try{
				reader = new CSVReader(new FileReader(resultPath));
				List<String[]> csv = reader.readAll();
				if(csv != null){
					String message = "";
				    message += "<HTML><BODY><FONT FACE=VERDANA COLOR=BLACK SIZE=2>Hi,<BR/><BR/>Please find the information below:</FONT>";
				    message += "<BR/><BR/><FONT FACE=VERDANA COLOR=BLACK SIZE=2>";
			        
			        int row = 1;
			        String sRowColor = "";
			        String sColor = "BLACK";
			        
			        String body = "";
			        body += "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK><TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><BR/>";
			        body += "<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>";           
			        body += "<TR COLS=6 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>S. No.</B></FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Case Name</B></FONT></TD><TD  WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Status</B></FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Duration</B></FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Execution Start Time</B></FONT></TD></TR>";
					List<String> newFiles = new ArrayList<String>();
					int countF = 0;
					int countP = 0;
					int mCountF = 0;
					int mCountP = 0;
					int mCountS = 0;
					for(int i = 0 ; i < csv.size(); i++){
						String[] data = csv.get(i);
						if(data[0].trim().equalsIgnoreCase(driverType) && data[2].trim().equalsIgnoreCase(Dictionary.get("TEST_CLASS_NAME").trim())){
							String status = data[12];
							String testCaseName = data[3];
							String duration = data[9];
							String startTime = data[11];
							int tcPassed = Integer.valueOf(data[13]);
							int tcFailed = Integer.valueOf(data[14]);
							String reportPath = data[15];
							String snapshotPath = data[16];
							String logPath = data[17];
//							operator = data[8];
							if (row % 2 == 0) {
								sRowColor = "#EEEEEE";
				      		} else {
					      		sRowColor = "#D3D3D3";
				      		}
							
							if(status.trim().contains("FAIL")){
								sColor = "RED";	
								mCountF += 1;
								newFiles.add(reportPath);
								newFiles.add(snapshotPath);
								newFiles.add(logPath);
							} else if(status.trim().contains("SKIP")){
								sColor = "BLUE";	
								mCountS += 1;
							} else{
								sColor = "GREEN";
								mCountP += 1;
							}
							countP += tcPassed;
							countF += tcFailed;
							body += "<TR COLS=6 BGCOLOR=" + sRowColor + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + row + "</FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + testCaseName + "</FONT></TD><TD  WIDTH=5%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + status + "</B></FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + duration + "</FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + startTime + "</FONT></TD></TR>";
					        row = row + 1;
						}
					}
					body += "</TABLE>";
					message += "Total execution time taken : " + totalTime + "<Br/>";
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
					message += "Environment : " + environment + "<Br/>";
					message += "Total test cases passed : " + countP + "<Br/>";
					message += "Total test cases failed : " + countF + "<Br/>";
					message += "Total methods passed : " + mCountP + "<Br/>";
					message += "Total methods failed : " + mCountF + "<Br/>";
					message += "Total methods skipped : " + mCountS + "<Br/>";
					
					if(System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")){
						String jenkinsIp = System.getProperty("jenkinsIp") != null && !System.getProperty("jenkinsIp").trim().equalsIgnoreCase("") ? System.getProperty("jenkinsIp").trim().toLowerCase() : Environment.get("jenkinsIpAddress").trim().toLowerCase();
						String folderPath = sConsolidatedReportFile.trim().substring(sConsolidatedReportFile.lastIndexOf("Reports"), sConsolidatedReportFile.lastIndexOf(OSValidator.delimiter));
						message += "Functional report path : " + "<a href=\"http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + folderPath + "/" + tcsReportsUrl + "/SummaryReport.html" + "\">link</a><Br/>";
					}
					
					message += "<Br/>";
			        message += body;
			        message += "<FONT FACE=VERDANA COLOR=BLACK SIZE=2><BR/><BR/><I>Note: Please refer attached file for failed test cases</I><BR/><BR/><BR/>Thanks & Regards,<BR/>" + Environment.get("orgName") + " Automation Team</FONT></BODY></HTML>";
			        
			        if(mCountP + mCountF + mCountS > 0){
			        	subject += " - " + String.valueOf((int)Math.round(((float)(countP)/(countP + countF)) * 100)) + "%";
			        	boolean attachLogFile = System.getProperty("attachLogFile") != null && !System.getProperty("attachLogFile").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("attachLogFile").trim()) : Boolean.valueOf(Environment.get("attachLogFile").trim());
	        			SendMail.sendMail(Environment.get("classTo"), Environment.get("classCc"), Environment.get("classBcc"), subject, message, ReportFilePath + OSValidator.delimiter + "FailedReports_" + suiteTestName + "_" + Dictionary.get("TEST_CLASS_NAME").trim().replace(".", "_") + ".zip", newFiles, Boolean.valueOf(Environment.get("attachSSInEmail")), Boolean.valueOf(Environment.get("zipping")), attachLogFile);
			        }
				}
			}
			catch(Exception ex){
				throw ex;
			}
			finally{
				if(reader != null)
					reader.close();
			}
		}
	}
	
	private void suiteDraftReport(String subject, String totalTime, String message, String env, String datasheet) throws Exception{
		if(message == null)
			return;
		message += "<br/>";
		if(System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")) {
			String jenkinsIp = System.getProperty("jenkinsIp") != null && !System.getProperty("jenkinsIp").trim().equalsIgnoreCase("") ? System.getProperty("jenkinsIp").trim().toLowerCase() : Environment.get("jenkinsIpAddress").trim().toLowerCase();
			message += "Consolidated report path : " + "<a href=\"http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + sConsolidatedReportsUrl +"\">link</a><Br/>";
			message += "Galen report path : " + "<a href=\"http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/target/galen-html-reports/report.html\">link</a><Br/>";
			message += "Functional report path : " + "<a href=\"http://" + jenkinsIp + "/job/" + System.getProperty("JOB_NAME") + "/ws/" + reportsUrl +"\">link</a><Br/>";
			if(System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase("")) {
				String gatlingHost = System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase("") ? System.getProperty("gatlingHost").trim() : Environment.get("gatlingHost").trim();
				gatlingHost += "?simulation=" + datasheet + "&env=" + env;
				message += "S3 report path : " + "<a href=\"" + gatlingHost + "\">link</a><Br/>";
			}
		} else if(System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase("")) {
			String gatlingHost = System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase("") ? System.getProperty("gatlingHost").trim() : Environment.get("gatlingHost").trim();
			gatlingHost += "?simulation=" + datasheet + "&env=" + env;
			message += "Consolidated report path : " + "<a href=\"" + gatlingHost + "\">link</a><Br/>";
		}
		
        message += "<FONT FACE=VERDANA COLOR=BLACK SIZE=2><BR/><BR/><BR/>Thanks & Regards,<BR/>" + Environment.get("orgName") + " Automation Team</FONT></BODY></HTML>";
    	subject += " - " + String.valueOf((int)Math.round(((float)(totalPassedTCs)/(totalPassedTCs + totalFailedTCs)) * 100)) + "%";
    	boolean attachLogFile = System.getProperty("attachLogFile") != null && !System.getProperty("attachLogFile").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("attachLogFile").trim()) : Boolean.valueOf(Environment.get("attachLogFile").trim());
    	SendMail.sendMail(Environment.get("suiteTo"), Environment.get("suiteCc"), Environment.get("suiteBcc"), subject, message, "Test run.log", Arrays.asList(logPath), Boolean.valueOf(Environment.get("attachSSInEmail")), false, attachLogFile);
	}
	
    @AfterTest(alwaysRun = true)
	public void exitTest() throws Exception{
	}

    @AfterMethod
    public void provideTestEndDate() {
        GalenTestInfo ti = testInfo.get();
        if (ti != null) {
            ti.setEndedAt(new Date());
        }
    }
    
    @Parameters({ "calendar" })
    @AfterSuite(alwaysRun=true)
	public void tearDown(@Optional("") String calendar) throws Exception {
		g_EndTime = new Date();
		g_StartTime = sg_StartTime.get();
		if(g_StartTime != null && g_EndTime != null){
			String strTimeDifference = fnTimeDiffference(g_StartTime.getTime(), g_EndTime.getTime());
			log.info("Total suite execution time : " + strTimeDifference);
			log.info("Total passed test cases : " + totalPassedTCs);
			log.info("Total failed test cases : " + totalFailedTCs);
			log.info("Total passed methods : " + totalPassedMtds);
			log.info("Total failed methods : " + totalFailedMtds);
			log.info("Total skipped methods : " + totalSkippedMtds);
			
			if(totalPassedMtds + totalFailedMtds + totalSkippedMtds > 0) {
				Infra objInfra = new Infra(this.driverFactory, this.Dictionary, this.Environment, this.objGlobalDictOriginal);
				String message = objInfra.createConsolidatedReport(sConsolidatedReportFile, strTimeDifference, resultPath, duration, csvs);
				
				if(System.getProperty("suiteTo") != null && !System.getProperty("suiteTo").trim().equalsIgnoreCase("")){
					Environment.put("suiteTo", System.getProperty("suiteTo").trim());
					Environment.put("emailNotification", "true");
				}
				
				if(Environment.get("emailNotification").trim().equalsIgnoreCase("true") || Environment.get("emailNotification").trim().equalsIgnoreCase("yes")){
					String groupName = System.getProperty("groups") != null && !System.getProperty("groups").trim().equalsIgnoreCase("") ? System.getProperty("groups").trim() : "";
					String subject = "";
					String Datasheet = !calendar.trim().equalsIgnoreCase("") ? calendar.trim() : System.getProperty("calendar") != null && !System.getProperty("calendar").trim().equalsIgnoreCase("") ? System.getProperty("calendar").trim() : Environment.get("calendar").trim();
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
					if(groupName.trim().equalsIgnoreCase(""))
						subject = "Test suite execution report : " + environment + " : " + Datasheet;
					else
						subject = "Test suite execution report : " + environment + " : " + Datasheet + " - " + groupName;
					
					if(!Environment.get("suiteTo").trim().equalsIgnoreCase("") || !Environment.get("suiteCc").trim().equalsIgnoreCase("") || !Environment.get("suiteBcc").trim().equalsIgnoreCase(""))
						suiteDraftReport(subject, strTimeDifference, message, environment, Datasheet);
				}
			}
		}
   	 	if(sThreadGroup != null){
   	 		for(int i = 0 ; i < sThreadGroup.size(); i++) {
   	 			Thread _thread = sThreadGroup.get(i);
   	 			while (_thread.isAlive()) {
   	 				try {
   	 					log.info("Waiting for Child Thread to get finished :" + _thread.getName());
   	 					Thread.sleep(1000L);
   	 				}
   	 				catch (InterruptedException e){
   	 					log.info("Threw a Exception in Driver::threadMain, full stack trace follows:", e);
   	 				}
   	 			}
   	 		}
   	 	}
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

    public void load(String uri) {
    	boolean runLocally = System.getProperty("runLocally") != null && !System.getProperty("runLocally").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("runLocally").trim().toLowerCase()) : Boolean.valueOf(Environment.get("runLocally").trim().toLowerCase());
    	if(driverType.trim().toUpperCase().contains("SAFARI") && runLocally) {
    		getDriver().get(Environment.get("APP_URL").trim() + "/user/logout");
    	}
    	getDriver().get(Environment.get("APP_URL").trim() + uri);
    	
//    	if(!driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")) {
    		try {
    			Object obj = ((JavascriptExecutor) driverFactory.getDriver().get()).executeScript("var obj = drupalSettings.componentConfigData.siteconfig;return JSON.stringify(obj);");
    			JSONObject json = new JSONObject(obj.toString());
    			Environment.put("currency", json.has("currency")? json.getString("currency") : "$");
    			((JavascriptExecutor) driverFactory.getDriver().get()).executeScript("$('#doorbell-button').remove()");
    		} catch(Exception ex) {
    			//Do Nothing
    		}
//    	}
    	Assert.assertTrue(true, "Verify page launched - " + Environment.get("APP_URL").trim() + uri);
    	if(Environment.get("splunkLogIntegration").trim().equalsIgnoreCase("true")) {
	    	Cookie QuantumMetricSessionID = getDriver().manage().getCookieNamed("QuantumMetricSessionID");
	    	Cookie QuantumMetricUserID = getDriver().manage().getCookieNamed("QuantumMetricUserID");
	    	String searchQuery = "";
	    	if(QuantumMetricSessionID != null) {
	    		searchQuery += QuantumMetricSessionID.getValue() + "%20OR%20" + QuantumMetricUserID.getValue();
	    	}
	    	sTestDetails.get().put("SEARCH_QUERY", searchQuery.trim());
    	}
    }
    
    public void runScenario(String scenarioName) throws Throwable {
    	Object[][] scenarios = testNGCucumberRunner.provideScenarios();
		Assert.assertTrue(scenarios.length > 0, "Verify scenarios exists in feature");
		PickleEventWrapper pickleEvent = null;
		for(int i = 0; i < scenarios.length; i++) {
			Object[] scenario = scenarios[i];
			pickleEvent = (PickleEventWrapper) scenario[0];
			String pickleEventName = pickleEvent.toString();
			String actualScenarioName = pickleEventName.trim().substring(1, pickleEventName.trim().length() - 1);
			if(actualScenarioName.equalsIgnoreCase(scenarioName.trim().toString())) {
				break;
			}
			pickleEvent = null;
		}
		Assert.assertNotNull(pickleEvent, "Verify scenario found");
		testNGCucumberRunner.runScenario(pickleEvent.getPickleEvent());
    }
    
    public void checkLayout(String specFileName, List<String> includedTags) throws IOException{
    	String specPath = System.getProperty("user.dir");
    	if(Environment.get("specFolderPath").trim().equalsIgnoreCase(""))
    		specPath += OSValidator.delimiter + "specs" + OSValidator.delimiter;
    	else
    		specPath += Environment.get("specFolderPath").trim();
    		specPath += specFileName;
    		
		String javascript = Environment.get("javascript").trim();
		inject(javascript);
		String text = !Environment.get("currency").trim().equalsIgnoreCase("") ? Environment.get("currency") : "$";
		
		Map <String, Object> vars = new HashMap<String, Object>();
		vars.put("currencyIdentifier", text);
		
		String title = "Check layout " + specPath;
		SectionFilter sectionFilter = new SectionFilter(includedTags, Collections.<String>emptyList());
        LayoutReport layoutReport = Galen.checkLayout(getDriver(), specPath, sectionFilter, new Properties(), vars);
        getReport().layout(layoutReport, title);
        this.layoutReport.set(layoutReport);

        if (layoutReport.errors() > 0) {
            throw new LayoutValidationException(specPath, layoutReport, sectionFilter);
        }
//		checkLayout(specPath, new SectionFilter(includedTags, Collections.<String>emptyList()), new Properties(), vars);
    }
    
    public void checkLayout(String specFileName, List<String> includedTags, Map<String, Object> variables) throws IOException{
    	String specPath = System.getProperty("user.dir");
    	if(Environment.get("specFolderPath").trim().equalsIgnoreCase(""))
    		specPath += OSValidator.delimiter + "specs" + OSValidator.delimiter;
    	else
    		specPath += Environment.get("specFolderPath").trim();
    		specPath += specFileName;
    		
		String javascript = Environment.get("javascript").trim();
		inject(javascript);
		String text = !Environment.get("currency").trim().equalsIgnoreCase("") ? Environment.get("currency") : "$";
		
		Map <String, Object> vars = new HashMap<String, Object>();
		vars.put("currencyIdentifier", text);
		
		Set<String> keys = variables.keySet();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			Object value = variables.get(key);
			vars.put(key, value);
		}
		String title = "Check layout " + specPath;
		SectionFilter sectionFilter = new SectionFilter(includedTags, Collections.<String>emptyList());
        LayoutReport layoutReport = Galen.checkLayout(getDriver(), specPath, sectionFilter, new Properties(), vars);
        getReport().layout(layoutReport, title);
        this.layoutReport.set(layoutReport);
        
        if (layoutReport.errors() > 0) {
            throw new LayoutValidationException(specPath, layoutReport, sectionFilter);
        }
//		checkLayout(specPath, new SectionFilter(includedTags, Collections.<String>emptyList()), new Properties(), vars);
    }
    
    @DataProvider(name = "scenarios", parallel = false)
    public Object[][] scenarios() {
        return testNGCucumberRunner.provideScenarios();
    }

    @DataProvider(name = "devices", parallel = true)
    public Object[][] devices () throws IOException, ParseException {
    	boolean runLocally = System.getProperty("runLocally") != null && !System.getProperty("runLocally").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("runLocally").trim().toLowerCase()) : Boolean.valueOf(Environment.get("runLocally").trim().toLowerCase());
    	int screenSizePadding = 0;
    	
    	if(OSValidator.isWindows(OS)){
	    	String screensizepaddingvalue = System.getProperty("screensizepadding") != null && !System.getProperty("screensizepadding").trim().equalsIgnoreCase("") ? System.getProperty("screensizepadding").trim() : Environment.get("screensizepadding").trim();
	    	screenSizePadding = screensizepaddingvalue.equalsIgnoreCase("") ? 0 : Integer.valueOf(screensizepaddingvalue);
    	}
    	
    	String deviceList = System.getProperty("deviceList") != null && !System.getProperty("deviceList").trim().equalsIgnoreCase("") ? System.getProperty("deviceList").trim() : Environment.get("deviceList").trim();
    	String[] deviceLists = deviceList.trim().split(",");
    	
    	List<Object> testDevices = new ArrayList<Object>();
    	JSONParser parser = new JSONParser();
    	Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + Environment.get("dimenFilePath").trim()));
    	JSONArray jsonArray = (JSONArray) obj;
    	for(int i = 0; i < jsonArray.size(); i++){
    		org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonArray.get(i);
    		JSONArray browsers = (JSONArray) jsonObject.get("browsers");
    		boolean flag = false;
    		for(int j = 0; j < browsers.size(); j++){
    			if(suiteTestName.trim().toLowerCase().contains(((String) browsers.get(j)).trim().toLowerCase())){
    				flag = true;
    				break;
    			}
    		}
    		if(flag){
    			String name = (String) jsonObject.get("name");
    			String dimension = (String) jsonObject.get("dimension");
    			JSONArray tags = (JSONArray) jsonObject.get("tags");
    			List<String> tagsL = new ArrayList<String>();
    			for(int k = 0; k < tags.size(); k++){
    				tagsL.add((String) tags.get(k));
    			}
    			
    			String[] dimens = dimension.trim().toLowerCase().split("x");
    			int x = Integer.valueOf(dimens[0]) + screenSizePadding;
    			int y = Integer.valueOf(dimens[1]);
    			TestDevice td = new TestDevice(suiteTestName, name, new Dimension(x, y), screenSizePadding, tagsL);
    			if(!runLocally && (!suiteTestName.trim().toLowerCase().contains("android") && !suiteTestName.trim().toLowerCase().contains("ios")) && (name.trim().equalsIgnoreCase("mobile") || name.trim().equalsIgnoreCase("mini-tablet"))) {
    				//Do Nothing
    			}
    			else {
    				boolean bflag = false;
    				if(deviceList.trim().equalsIgnoreCase(""))
    					bflag = true;
    				else {
	    				for(int l = 0; l < deviceLists.length; l++) {
	    					if(name.trim().equalsIgnoreCase(deviceLists[l].trim())) {
	    						bflag = true;
	    						break;
	    					}
	    				}
    				}
    				if(bflag){
    					if(!suiteTestName.trim().toLowerCase().contains("android") && !suiteTestName.trim().toLowerCase().contains("ios"))
    						testDevices.add(td);
    					else {
	    					HashMap<String, String> deviceTypeMapping = new HashMap<String, String>();
	    					deviceTypeMapping.put("mini-tablet", "tablet");
	    					deviceTypeMapping.put("mobile", "phone");
	    					String deviceType = Environment.get("deviceType").trim();
	    					if(deviceType.trim().equalsIgnoreCase(deviceTypeMapping.get(name.trim().toLowerCase())))
	    						testDevices.add(td);
    					}
    				}
    			}
    		}
    	}
    	
    	Object[][] o = new Object[testDevices.size()][];
    	for(int i = 0; i < o.length; i++){
    		o[i] = new Object[1];
    		o[i][0] = testDevices.get(i);
    	}
    	
    	return o;
    }

    public static class TestDevice {
    	private final String driverType;
        private final String name;
        private final Dimension screenSize;
        private final List<String> tags;
        private int screensizepadding;

        public TestDevice(String driverType, String name, Dimension screenSize, int screensizepadding, List<String> tags) {
        	this.driverType = driverType;
            this.name = name;
            this.screenSize = screenSize;
            this.screensizepadding = screensizepadding;
            this.tags = tags;
        }

        public String getName() {
            return name;
        }

        public Dimension getScreenSize() {
            return screenSize;
        }
        
        public int getScreenSizePadding() {
            return screensizepadding;
        }

        public List<String> getTags() {
            return tags;
        }

        @Override
        public String toString() {
        	int width = screenSize.width;
    		width = width - screensizepadding;
            return String.format("%s-%s %dx%d", driverType.trim().toLowerCase(), name, width, screenSize.height);
        }
    }
    
    public static class HashMapNew extends HashMap<String, String>{
		static final long serialVersionUID = 1L;
    
		public String get(Object key){
			String value = (String)super.get(key);
			if (value == null) {
				return "";
			}
			return value;
		}
		
		public String put(String key, String value) {
			String val = super.put(key, value);
			WebDriverFactory driverFactory = new WebDriverFactory();
			HashMapNew me = this;
			while (bThreadFlag) {
				try{
					Thread.sleep(500L);
				}
				catch (Exception localException1) {}
			}
		    
		    bThreadFlag = true;
			if(this.toString().contains("HEADER="))
				driverFactory.setDictionary(new ThreadLocal<HashMapNew>(){@Override public HashMapNew initialValue() {
					return me;
				};});
			else if(this.toString().contains("DATASHEETSPATH="))
				driverFactory.setEnvironment(new ThreadLocal<HashMapNew>(){@Override public HashMapNew initialValue() {
					return me;
				};});
			bThreadFlag = false;
			return val;
		}
	}
    
    public HashMapNew GetXMLNodeValue(String path, String parentNode, int index){
		HashMapNew dict = new HashMapNew();
	    String RootPath = System.getProperty("user.dir");
	    try
	    {
	      String xmlPath = RootPath + path;
	      File fXmlFile = new File(xmlPath);
	      
	      if(!fXmlFile.exists())
	    	  return dict;
	      
	      DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
	      DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
	      Document xmldoc = docBuilder.parse(fXmlFile);
	      
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
    
    public void run() {}
    
    void envcheck(String suitetestname){
		boolean flag = false;
		boolean envCheck = System.getProperty("envCheck") != null && !System.getProperty("envCheck").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("envCheck").trim()) : Boolean.valueOf(Environment.get("envCheck").trim());
		if(envCheck && !Environment.get("envCheckMethod").trim().equalsIgnoreCase("")) {
			String polling = !Environment.get("envCheckPoll").trim().equalsIgnoreCase("") ? Environment.get("envCheckPoll").trim() : "60";
			int counter = Integer.valueOf(polling);
			do{
				String[] words = Environment.get("envCheckMethod").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("envCheckMethod").trim().substring(0, Environment.get("envCheckMethod").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriverFactory.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class, ThreadLocal.class }).newInstance(new Object[] { this.driverFactory, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert, this.sTestDetails });
					Method method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					Object objReturn = method.invoke(busFunctions, params);
					if (objReturn.equals(Boolean.valueOf(true))) {
						flag = true;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				counter--;
			}while(counter > 0 && flag == false);
		}
		
		if(envCheck && !flag) {
			String classTo = Environment.get("classTo");
			if(System.getProperty("classTo") != null && !System.getProperty("classTo").trim().equalsIgnoreCase("")){
				classTo = System.getProperty("classTo").trim();
			} else if(System.getProperty("suiteTo") != null && !System.getProperty("suiteTo").trim().equalsIgnoreCase("")){
				classTo = System.getProperty("suiteTo").trim();
			}
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
			String subject = environment + " is not up with status - " + Environment.get("ENV_CHECK_RESPONSE_CODE").trim() + ". Automation execution aborted for " + suitetestname + ".";
			String message = "";
			if(!Environment.get("ENV_CHECK_OUTPUT").trim().equalsIgnoreCase(""))
				message = "<HTML><BODY><FONT FACE=VERDANA COLOR=BLACK SIZE=2>"
						+ "<B><U>Request:</U></B><BR/><BR/>" + Environment.get("ENV_CHECK_REQUEST").trim() + "<BR/><BR/>"
						+ "<B><U>Response:</U></B><BR/><BR/>" + Environment.get("ENV_CHECK_OUTPUT").trim() + "</FONT></BODY></HTML>";
			boolean attachLogFile = System.getProperty("attachLogFile") != null && !System.getProperty("attachLogFile").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("attachLogFile").trim()) : Boolean.valueOf(Environment.get("attachLogFile").trim());
			SendMail.sendMail(classTo, Environment.get("classCc"), Environment.get("classBcc"), subject, message, null, null, Boolean.valueOf(Environment.get("attachSSInEmail")), false, attachLogFile);
			System.out.println("SKIPEXCEPTION :: " + subject);
			throw new SkipException(subject);
		}
	}
    
    public class RecordSet {
        private int sStartRow;
        private String sTestName;
        private String actionName;
        private int sEndRow;
        
        public RecordSet(String actionName, int sSRow, String sTCName, int sERow) {
          this.actionName = actionName;
          this.sStartRow = sSRow;
          this.sTestName = sTCName;
          this.sEndRow = sERow;
        }
        
        public int get_sStartRow() {
          return this.sStartRow;
        }
        
        public String get_sTestName() {
          return this.sTestName;
        }
        
        public int get_sEndRow() {
          return this.sEndRow;
        }
        
        public String get_sActionName() {
        	return this.actionName;
        }
    }
    
    public int fProcessDataFile(int rowID) {
    	DBActivities objDB = new DBActivities(driverFactory, Dictionary, Environment);
        int iret = 0;    

        List<List<String>> calendarFileData = null;
    	calendarFileData = objDB.fRetrieveDataExcel((String)this.Environment.get("CURRENTEXECUTIONDATASHEET"), "MAIN",  new int[]{0}, new String[]{ java.lang.String.valueOf(rowID) });
        if (calendarFileData == null) {
        	log.info("The result set is null");
        	return iret;
        }
        int intCounter = 1;
        ArrayList<String> columnNames = null;
    	columnNames = objDB.fGetColumnName((String)this.Environment.get("CURRENTEXECUTIONDATASHEET"), "MAIN");
    	int intColCount = columnNames.size();
    	HashMap<Integer, String> Temp = sTemp.get();
    	String Skip = sSkip.get();
    	
        int k = 0;
        
        while (k < calendarFileData.size()) {
        	if (rowID % 2 == 1) {
              Temp.clear();
      		  objGlobalDictOriginal.clear();
              for (int intLoop = 1; intLoop < intColCount && intLoop < calendarFileData.get(k).size(); intLoop++) {
                String temp1 = calendarFileData.get(k).get(intLoop);
                if(temp1 == null || temp1.trim().equalsIgnoreCase("") || temp1.trim().equalsIgnoreCase("x")) {
                  if (columnNames.get(intLoop).contains(this.driverType)) {
                    Skip = "";
                  }
                  temp1 = columnNames.get(intLoop);
                }
                if (temp1 == null) {
                  temp1 = "";
                }
                if(temp1.startsWith("PARAM")){
                	break;
                }
                if (!temp1.equals("") && !temp1.trim().equalsIgnoreCase("x")) {
                  Temp.put(Integer.valueOf(intCounter), temp1);
                  iret = 0;
                  intCounter++;
                }
              }
            }
            else {
              for (int intLoop1 = 1; intLoop1 < intColCount && intLoop1 < calendarFileData.get(k).size(); intLoop1++) {
                String temp1 = calendarFileData.get(k).get(intLoop1);
                if (temp1 == null || temp1.trim().equalsIgnoreCase("")) {
                  temp1 = "";
                  if (columnNames.get(intLoop1).contains(this.driverType)) {
                    Skip = temp1;
                  }
                }
                if (Temp.containsKey(Integer.valueOf(intCounter))) {
                  this.Dictionary.put((String)Temp.get(Integer.valueOf(intCounter)), temp1);              
                  this.objGlobalDictOriginal.put((String)Temp.get(Integer.valueOf(intCounter)), temp1);
                }
                iret = 1;
                intCounter++;
              }
            }
            k++;
          }
        
          sSkip.set(Skip);
          sTemp.set(Temp);
          
          objDB = null;
          return iret;
      }
    
    private String[] getDeviceDetails(String udid) throws Exception{
		String model, manufacturer, operator, version; 
		if(driverType.trim().toUpperCase().contains("IOS")){
			model = BaseUtil.getIOSDeviceDetails("DeviceName", udid);
			manufacturer = BaseUtil.getIOSDeviceDetails("DeviceClass", udid);
			operator = "airtel";
			version = BaseUtil.getIOSDeviceDetails("ProductVersion", udid);
		}
		else{
			model = BaseUtil.runCommandUsingTerminal("adb -s " + udid + " shell getprop ro.product.model", false, "1");
			manufacturer = BaseUtil.runCommandUsingTerminal("adb -s " + udid + " shell getprop ro.product.manufacturer", false, "1");
			operator = BaseUtil.runCommandUsingTerminal("adb -s " + udid + " shell getprop gsm.sim.operator.alpha", false, "1");
			version = BaseUtil.runCommandUsingTerminal("adb -s " + udid + " shell getprop ro.build.version.release", false, "1");
			model = model.trim().split("\n")[0];
			manufacturer = manufacturer.trim().split("\n")[0];
			operator = operator.trim().split("\n")[0];
			version = version.trim().split("\n")[0];
			
			if(model.trim().contains("error:")) {
				System.out.println("SKIPEXCEPTION :: " + "Android device - " + driverType.trim().toUpperCase() + " not found");
				throw new SkipException("Android device - " + driverType.trim().toUpperCase() + " not found");
			}
		}
		
		return new String[]{operator, version, manufacturer, model};
	}
    
    WebDriver initializeIOS(boolean runLocally, String seleniumURI, String dimension) throws Exception{
    	String zaleniumURI = System.getProperty("zaleniumURI") != null && !System.getProperty("zaleniumURI").trim().equalsIgnoreCase("") ? System.getProperty("zaleniumURI").trim() : Environment.get("zaleniumURI").trim();
		if(!zaleniumURI.trim().equalsIgnoreCase("") || !runLocally || (System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")) || (System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase(""))) {
			DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.BROWSER_NAME, "Safari");
            capabilities.setCapability("deviceName", Environment.get("deviceName").trim());
            capabilities.setCapability("platformName", Environment.get("platformName").trim());
            capabilities.setCapability("platformVersion", Environment.get("platformVersion").trim());
            capabilities.setCapability("deviceOrientation", Environment.get("deviceOrientation").trim());
            capabilities.setCapability("deviceType", Environment.get("deviceType").trim());
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("autoAcceptAlerts", "true");
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            // Launch remote browser and set it as the current thread
	        System.out.println("Sauce Labs URI :: " + seleniumURI);
            WebDriver driver = new IOSDriver<MobileElement>(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
		} else {
    		JSONObject json = BaseUtil.readJsonFromUrl((String)Environment.get("ip") + "/status", false);
			if(json != null){
				if(json.has("sessionId")){
					if(!json.get("sessionId").toString().trim().equalsIgnoreCase("") && !json.get("sessionId").toString().trim().equalsIgnoreCase("null")){
						URL url = new URL((String)Environment.get("ip") + "/session/" + json.get("sessionId"));
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("DELETE");
						connection.getResponseCode();
					}
				}
			}
			
			if(Environment.get("udid").trim().equalsIgnoreCase("")){
				if(deviceList.get(driverType.trim().toUpperCase()).trim().equalsIgnoreCase("")) {
					System.out.println("SKIPEXCEPTION :: " + "IOS device - " + driverType.trim().toUpperCase() + " not found");
					throw new SkipException("IOS device - " + driverType.trim().toUpperCase() + " not found");
				}
				Environment.put("udid", deviceList.get(driverType.trim().toUpperCase()).trim());
			}
			
			String[] details = getDeviceDetails(Environment.get("udid").trim());
			Dictionary.put(driverType.trim().toUpperCase() + "_OPERATOR", details.length > 0 ? details[0] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_VERSION", details.length > 1 ? details[1] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_MANUFACTURER", details.length > 2 ? details[2] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_MODEL", details.length > 3 ? details[3] : "");
			
			DesiredCapabilities dc = new DesiredCapabilities();
			if(!Environment.get("bundleId").trim().equalsIgnoreCase("")){
				dc.setCapability("bundleId", Environment.get("bundleId").trim());
				dc.setCapability("app", System.getProperty("user.dir") + OSValidator.delimiter + Environment.get("iosapp").trim());
				dc.setCapability("autoDismissAlerts", Environment.get("autoDismissIOSAlerts").trim().equalsIgnoreCase("") ? "false" : Environment.get("autoDismissIOSAlerts").trim());
				dc.setCapability("autoAcceptAlerts", Environment.get("autoAcceptIOSAlerts").trim().equalsIgnoreCase("") ? "false" : Environment.get("autoAcceptIOSAlerts").trim());
				dc.setCapability("fullReset", Boolean.valueOf(Environment.get("fullReset")));
				dc.setCapability("autoLaunch", true);
				dc.setCapability("noReset", Environment.get("noReset").trim());
			} else {
				dc.setCapability("browserName", "Safari");
				dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			}
			dc.setCapability("deviceName", Environment.get("deviceName"));
			if(!Environment.get("udid").trim().equalsIgnoreCase("")){
				dc.setCapability("udid", Environment.get("udid"));
			}
			dc.setCapability("platformName", Environment.get("platformName"));
			dc.setCapability("newCommandTimeout", Environment.get("newCommandTimeout"));
			dc.setCapability("automationName", "XCUITest");
			LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        dc.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
			
			WebDriver driver = new IOSDriver<MobileElement>(new URL(Environment.get("ip")), dc);
			String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			return driver;
    	}
    }
    
	WebDriver initializeAndroid(boolean runLocally, String seleniumURI, String dimension) throws Exception {
    	String zaleniumURI = System.getProperty("zaleniumURI") != null && !System.getProperty("zaleniumURI").trim().equalsIgnoreCase("") ? System.getProperty("zaleniumURI").trim() : Environment.get("zaleniumURI").trim();
    	if(!zaleniumURI.trim().equalsIgnoreCase("") || !runLocally || (System.getProperty("JOB_NAME") != null && !System.getProperty("JOB_NAME").trim().equalsIgnoreCase("")) || (System.getProperty("gatlingHost") != null && !System.getProperty("gatlingHost").trim().equalsIgnoreCase(""))) {
			DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.BROWSER_NAME, "chrome");
            capabilities.setCapability("deviceName", Environment.get("deviceName").trim());
            capabilities.setCapability("platformName", Environment.get("platformName").trim());
            capabilities.setCapability("platformVersion", Environment.get("platformVersion").trim());
            capabilities.setCapability("deviceOrientation", Environment.get("deviceOrientation").trim());
            capabilities.setCapability("deviceType", Environment.get("deviceType").trim());
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            
            if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
	            ChromeOptions options = new ChromeOptions();
		        options.addArguments("--use-mobile-user-agent");
				options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36 " + Environment.get("USER_AGENT").trim());
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            }
            
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            // Launch remote browser and set it as the current thread
            WebDriver driver = new AndroidDriver<MobileElement>(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
		} else {
    		JSONObject json = BaseUtil.readJsonFromUrl(Environment.get("ip") + "/status", false);
			if(json != null) {
				if(json.has("sessionId")){
					if(!json.get("sessionId").toString().trim().equalsIgnoreCase("") && !json.get("sessionId").toString().trim().equalsIgnoreCase("null")){
						URL url = new URL(Environment.get("ip") + "/session/" + json.get("sessionId"));
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("DELETE");
						connection.getResponseCode();
					}
				}
			}
			
			if(Environment.get("udid").trim().equalsIgnoreCase("")) {
				if(deviceList.get(driverType.trim().toUpperCase()).trim().equalsIgnoreCase("")) {
					System.out.println("SKIPEXCEPTION :: " + "Android device - " + driverType.trim().toUpperCase() + " not found");
					throw new SkipException("Android device - " + driverType.trim().toUpperCase() + " not found");
				}
				Environment.put("udid", deviceList.get(driverType.trim().toUpperCase()).trim());
			}
			
			String[] details = getDeviceDetails(Environment.get("udid").trim());
			Dictionary.put(driverType.trim().toUpperCase() + "_OPERATOR", details.length > 0 ? details[0] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_VERSION", details.length > 1 ? details[1] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_MANUFACTURER", details.length > 2 ? details[2] : "");
			Dictionary.put(driverType.trim().toUpperCase() + "_MODEL", details.length > 3 ? details[3] : "");
			
			DesiredCapabilities dc = DesiredCapabilities.android();
			dc.setCapability("browserName", "chrome");
			dc.setCapability("deviceName", Environment.get("deviceName"));
	        if(!Environment.get("udid").trim().equalsIgnoreCase("")) {
	      	  dc.setCapability("udid", Environment.get("udid"));
	        }
	        
	        if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
		        ChromeOptions options = new ChromeOptions();
		        options.addArguments("--use-mobile-user-agent");
				options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36 " + Environment.get("USER_AGENT").trim());
				dc.setCapability(ChromeOptions.CAPABILITY, options);
	        }
			
	        dc.setCapability("platformName", Environment.get("platformName").trim());
	        dc.setCapability("newCommandTimeout", Environment.get("newCommandTimeout"));
	        LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        dc.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
	        WebDriver driver = new AndroidDriver<MobileElement>(new URL(Environment.get("ip")), dc);
	        String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			return driver;
    	}
    }
    
    @SuppressWarnings("deprecation")
	WebDriver initializeFirefox(String profiles, boolean runLocally, String seleniumURI, String dimension) throws IOException{
    	if(runLocally) {
    		FirefoxProfile profile;
			if(!Environment.get("firefox_profile_name").trim().equalsIgnoreCase("") && profiles.equalsIgnoreCase("custom")) {
				System.setProperty("webdriver.firefox.profile", Environment.get("firefox_profile_name").trim());
				ProfilesIni myprofile = new ProfilesIni();
				profile = myprofile.getProfile(Environment.get("firefox_profile_name").trim());
			} else {
				profile = new FirefoxProfile();
			}
			
			if(!Environment.get("firefox_extension_file_path").trim().equalsIgnoreCase("")){
				File addonpath = new File(System.getProperty("user.dir") + Environment.get("firefox_extension_file_path"));
				profile.addExtension(addonpath);
			}
			
			profile.setAssumeUntrustedCertificateIssuer(false);
			profile.setAcceptUntrustedCertificates(true);
			profile.setPreference("xpinstall.signatures.required", false);
			profile.setPreference("browser.startup.homepage_override.mstone", "ignore"); 
			profile.setPreference("startup.homepage_welcome_url.additional", "about:blank");
			
			if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
				profile.setPreference("general.useragent.override", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36 " + Environment.get("USER_AGENT").trim());
			}
			
			WebDriver driver;
    		String zaleniumURI = System.getProperty("zaleniumURI") != null && !System.getProperty("zaleniumURI").trim().equalsIgnoreCase("") ? System.getProperty("zaleniumURI").trim() : Environment.get("zaleniumURI").trim();
    		if(!zaleniumURI.trim().equalsIgnoreCase("")) {
    			DesiredCapabilities capabilities = new DesiredCapabilities();
    			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        		capabilities.setCapability("acceptSslCerts", true);
    			capabilities.setCapability("handlesAlerts", true);
                capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
                capabilities.setCapability(CapabilityType.PLATFORM, Platform.LINUX);
                capabilities.setCapability("screenResolution", dimension);
                capabilities.setCapability("recordVideo", false);
                capabilities.setCapability("tz", "America/Montreal");
                LoggingPreferences logPrefs = new LoggingPreferences();
		        logPrefs.enable(LogType.BROWSER, Level.ALL);
		        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
		        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    			driver =  new RemoteWebDriver(new URL("http://" + zaleniumURI + "/wd/hub"), capabilities);
    		} else {
    			String firefoxdriver = "geckodriver";
    			if(OSValidator.isWindows(OS))
    				firefoxdriver += ".exe";
    			if(!new File(firefoxdriver).exists()){
    				System.out.println("SKIPEXCEPTION :: " + "Firefoxdriver executable not found in root directory");
    				throw new SkipException("Firefoxdriver executable not found in root directory");
    			}
    			System.setProperty("webdriver.gecko.driver", firefoxdriver);
    			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
    			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
    			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    			capabilities.setCapability("acceptSslCerts", true);
    			capabilities.setCapability("handlesAlerts", true);
    			LoggingPreferences logPrefs = new LoggingPreferences();
		        logPrefs.enable(LogType.BROWSER, Level.ALL);
		        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
		        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
		        
    			if(!Environment.get("firefoxBinaryPath").trim().equalsIgnoreCase("")){
    				if(OSValidator.isUnix(OS)){
    					File pathBinary = new File(Environment.get("firefoxBinaryPath").trim());
    					FirefoxBinary firefoxBinary = new FirefoxBinary(pathBinary);
    					driver = new FirefoxDriver(firefoxBinary, profile, capabilities);
    				} else {
    					driver = new FirefoxDriver(capabilities);
    				}
    			} else {
    				driver = new FirefoxDriver(capabilities);
    			}
    		}
    		String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			
			return driver;
    	} else {
    		DesiredCapabilities capabilities = new DesiredCapabilities();
    		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
            capabilities.setCapability(CapabilityType.VERSION, Environment.get("browserVersion").trim());
            capabilities.setCapability(CapabilityType.PLATFORM, Environment.get("platformName").trim());
            capabilities.setCapability("screenResolution", dimension);
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            
            if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
	            FirefoxProfile profile = new FirefoxProfile();
	            profile.setPreference("general.useragent.override", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36 " + Environment.get("USER_AGENT").trim());
	            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
            }
            
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            
            // Launch remote browser and set it as the current thread
            WebDriver driver = new RemoteWebDriver(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
    	}
    }
    
    private void takeScreenshot(WebDriver driver, String SSPath) {
    	if ((this.driverType.contains("ANDROID")) || (this.driverType.contains("IOS"))) {
    		//Do Nothing
    	} else {
    		driver = new Augmenter().augment(driver);
    	}
    	if(driver != null){
    		try{
    			File scrFile = (File)((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
    			FileUtils.copyFile(scrFile, new File(SSPath));
    			FileUtils.deleteQuietly(scrFile);
    			scrFile = null;
//    			try{
//    				Thread.sleep(1L);
//    			}
//    			catch (InterruptedException e){
//    				log.info("Threw a InterruptedException in Reporting::fTakeScreenshot, full stack trace follows:", e);
//    			}
    		} catch(Exception ex){
    			//Do Nothing
    		}
    	}
    }
    
    private boolean generateScreenshots(WebDriver driver, String SSPath, String dimension) {
    	if(Environment.get("videoGifIntegration").trim().equalsIgnoreCase("true") && dimension.trim().equalsIgnoreCase("")) {
	    	Thread _thread = new Thread(){
				public void run(){
					int index = 1;
					while(!Thread.currentThread().isInterrupted() && driver != null){
						String screenshotPath = SSPath + OSValidator.delimiter + "SS_" + (index++) + ".gif"; 
						takeScreenshot(driver, screenshotPath);
					}
					
				}
	    	};
	    	_thread.start();
	    	sThread.set(_thread);
	    	return true;
    	} else {
    		return false;
    	}
    }
    
    private void generateLogs() {
    	if(Environment.get("consoleLogsIntegration").trim().equalsIgnoreCase("true") && (driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("CHROME")) && sTestDetails != null & sTestDetails.get() != null) {
	    	String logsPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
	    	java.util.Date today = new java.util.Date();
			Timestamp now = new java.sql.Timestamp(today.getTime());
			String tempNow[] = now.toString().split("\\.");
			final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
			String logFile = logsPath + OSValidator.delimiter + "BrowserConsoleLogs_" + sStartTime + ".log";
			String rellogFile = Environment.get("RELLOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME") + OSValidator.delimiter + "BrowserConsoleLogs_" + sStartTime + ".log";
			WebDriver driver = driverFactory.getDriver().get();
			String driverType = driverFactory.getDriverType().get();
			if(driver != null) {
		    	Thread _thread = new Thread("Capturing browser console logs for " + testSuite.getTestSuiteName() + "-" + driverFactory.getDictionary().get().get("TEST_CLASS_NAME") + "-" + sTestDetails.get().get("SCRIPT_NAME")){
	    			public void run() {
	    				LogEntries logEntries;
	    				String logs = "";
	    				try {
		    				if(driverType.trim().toUpperCase().contains("ANDROID") || driverType.trim().toUpperCase().contains("IOS"))
		    					logEntries = driver.manage().logs().get("syslog");
		    				else
		    					logEntries = driver.manage().logs().get(LogType.BROWSER);
		    		    	for (LogEntry entry : logEntries) {
		    		    		logs += new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage() + "\n";
		    		    	}
	    				} catch(Exception ex) {
	    					ex.printStackTrace();
	    				}
	    		    	BufferedWriter bw = null;
	    				FileWriter fw = null;
	    				try {
	    					fw = new FileWriter(logFile);
	    					bw = new BufferedWriter(fw);
	    					bw.write(logs);
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    				} finally {
	    					try {
	    						if (bw != null)
	    							bw.close();
	    						if (fw != null)
	    							fw.close();
	    					} catch (IOException ex) {
	    						ex.printStackTrace();
	    					}
	    				}
	    			}
	    		};
		    	_thread.start();
		    	sThreadGroup.add(_thread);
				sTestDetails.get().put("BROWSER_CONSOLE_LOGS_PATH", rellogFile);
			}
    	}
    }
    
    WebDriver initializeChrome(String profiles, boolean runLocally, String seleniumURI, String dimension) throws MalformedURLException{
    	if(runLocally){
    		ChromeOptions options = new ChromeOptions();
    		options.addArguments("--window-size=1280,960");
//			options.addArguments("start-maximized");
			if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
				String randomUserAgent = RandomUserAgent.getRandomUserAgent() + " " + Math.floor(Math.random() * 100);
				System.out.println("Random user agent :: " + randomUserAgent);
				options.addArguments("--user-agent=" + randomUserAgent + " " + Environment.get("USER_AGENT").trim());
			}
			String chromeArgument = System.getProperty("chromeArgument") != null && !System.getProperty("chromeArgument").trim().equalsIgnoreCase("") ? System.getProperty("chromeArgument").trim() : Environment.get("chromeArgument").trim();
			if(!chromeArgument.trim().equalsIgnoreCase(""))
				options.addArguments(chromeArgument.trim());
			
			if(!Environment.get("chrome_extension_file_path").trim().equalsIgnoreCase("")){
				File addonpath = new File(System.getProperty("user.dir") + Environment.get("chrome_extension_file_path"));
				options.addExtensions(addonpath);
			}
			
			if(!Environment.get("chrome_profile_name").trim().equalsIgnoreCase("") && profiles.equalsIgnoreCase("custom")){
				String path = "";
				if(OSValidator.isMac(OS))
					path = "/Users/" + System.getProperty("user.name") + "/Library/Application Support/Google/Chrome/";
				else if(OSValidator.isWindows(OS))
					path = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Google\\Chrome\\User Data\\";
				options.addArguments("user-data-dir=" + path + Environment.get("chrome_profile_name").trim());
			}
			
			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("profile.default_content_settings.popups", 0);
			options.setExperimentalOption("prefs", prefs);
			
    		String zaleniumURI = System.getProperty("zaleniumURI") != null && !System.getProperty("zaleniumURI").trim().equalsIgnoreCase("") ? System.getProperty("zaleniumURI").trim() : Environment.get("zaleniumURI").trim();
    		WebDriver driver;
    		if(!zaleniumURI.trim().equalsIgnoreCase("")) {
    			DesiredCapabilities capabilities = new DesiredCapabilities();
    			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                capabilities.setCapability(CapabilityType.BROWSER_NAME, "chrome");
                capabilities.setCapability(CapabilityType.PLATFORM, Platform.LINUX);
                capabilities.setCapability("version", Environment.get("linuxBrowserVersion").trim());
                capabilities.setCapability("screenResolution", dimension);
                capabilities.setCapability("recordVideo", false);
                capabilities.setCapability("tz", "America/Montreal");
                LoggingPreferences logPrefs = new LoggingPreferences();
		        logPrefs.enable(LogType.BROWSER, Level.ALL);
		        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
		        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    			driver = new RemoteWebDriver(new URL("http://" + zaleniumURI + "/wd/hub"), capabilities);
    		} else {
    			String chromedriverPath = System.getProperty("chromedriverPath") != null && !System.getProperty("chromedriverPath").trim().equalsIgnoreCase("") ? System.getProperty("chromedriverPath").trim() : Environment.get("chromedriverPath").trim();
    			String chromedriver = chromedriverPath;
				if(OSValidator.isWindows(OS) && !chromedriver.trim().endsWith(".exe"))
					chromedriver += ".exe";
				if(!new File(chromedriver).exists()){
					System.out.println("SKIPEXCEPTION :: " + "Chromedriver executable not found in root directory");
					throw new SkipException("Chromedriver executable not found in root directory");
				}
				System.setProperty("webdriver.chrome.driver", chromedriver);
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				LoggingPreferences logPrefs = new LoggingPreferences();
		        logPrefs.enable(LogType.BROWSER, Level.ALL);
		        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
		        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
				driver = new ChromeDriver(capabilities);
    		}
			String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			return driver;
    	} else {
    		DesiredCapabilities capabilities = new DesiredCapabilities();
    		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(CapabilityType.BROWSER_NAME, "chrome");
            capabilities.setCapability(CapabilityType.VERSION, Environment.get("browserVersion").trim());
            capabilities.setCapability(CapabilityType.PLATFORM, Environment.get("platformName").trim());
            capabilities.setCapability("screenResolution", dimension);
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            
            if(!Environment.get("USER_AGENT").trim().equalsIgnoreCase("")) {
	            ChromeOptions options = new ChromeOptions();
	            options.addArguments("start-maximized");
				options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36 " + Environment.get("USER_AGENT").trim());
				options.addArguments("disable-webgl");
				options.addArguments("blacklist-webgl");
				options.addArguments("blacklist-accelerated-compositing");
				options.addArguments("disable-accelerated-2d-canvas");
				options.addArguments("disable-accelerated-compositing");
				options.addArguments("disable-accelerated-layers");
				options.addArguments("disable-accelerated-plugins");
				options.addArguments("disable-accelerated-video");
				options.addArguments("disable-accelerated-video-decode");
				options.addArguments("disable-gpu");
				options.addArguments("disable-infobars");
				options.addArguments("test-type");
				options.addArguments("--headless");
    			options.addArguments("--no-sandbox");
    			options.addArguments("--disable-dev-shm-usage");
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            }
            
            if(!Environment.get("chromedriverversion").trim().equalsIgnoreCase("")){
            	capabilities.setCapability("chromedriverVersion", Environment.get("chromedriverversion").trim());
            }
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
	        
            // Launch remote browser and set it as the current thread
            WebDriver driver = new RemoteWebDriver(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
    	}
    }
    
    WebDriver initializeSafari(boolean runLocally, String seleniumURI, String dimension) throws MalformedURLException{
    	if(runLocally){
    		DesiredCapabilities capabilities = DesiredCapabilities.safari();
    		SafariOptions options = new SafariOptions();
			options.setUseCleanSession(true);
    		capabilities.setCapability(SafariOptions.CAPABILITY, options);
    		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    		LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    		WebDriver driver = new SafariDriver(capabilities);
    		String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			return driver;
    	} else{
    		DesiredCapabilities capabilities = new DesiredCapabilities();
    		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(CapabilityType.BROWSER_NAME, "safari");
            capabilities.setCapability(CapabilityType.VERSION, Environment.get("browserVersion").trim());
            capabilities.setCapability(CapabilityType.PLATFORM, Environment.get("platformName").trim());
            capabilities.setCapability("screenResolution", dimension);
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            // Launch remote browser and set it as the current thread
            WebDriver driver =  new RemoteWebDriver(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
    	}
    }
    
    WebDriver initializeIE(boolean runLocally, String seleniumURI, String dimension) throws MalformedURLException{
    	if(runLocally) {
    		String iedriver = "IEDriverServer";
			if(OSValidator.isWindows(OS))
				iedriver += ".exe";
			if(!new File(iedriver).exists()){
				System.out.println("SKIPEXCEPTION :: " + "IEDriver executable not found in root directory");
				throw new SkipException("IEDriver executable not found in root directory");
			}
			
			System.setProperty("webdriver.ie.driver", iedriver);
            DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
            dc.setCapability("ensureCleanSession", true);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        dc.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            WebDriver driver = new InternetExplorerDriver(dc);
            String screenShotPath = Environment.get("LOGSFOLDER") + OSValidator.delimiter + sTestDetails.get().get("SCRIPT_NAME");
			boolean status = generateScreenshots(driver, screenShotPath, dimension);
			sTestDetails.get().put("VIDEO_GIF", String.valueOf(status));
			return driver;
    	} else {
    		DesiredCapabilities capabilities = new DesiredCapabilities();
    		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    		
    		String browserVersion = Environment.get("browserVersion").trim();
    		String browserType = "";
    		if(Double.valueOf(browserVersion) > 11){
    			browserType = BrowserType.EDGE;
    		} else {
    			browserType = BrowserType.IE;
    		}
    		
            capabilities.setCapability(CapabilityType.BROWSER_NAME, browserType);
            capabilities.setCapability(CapabilityType.VERSION, browserVersion);
            capabilities.setCapability(CapabilityType.PLATFORM, Environment.get("platformName").trim());
            capabilities.setCapability("screenResolution", dimension);
            capabilities.setCapability("webdriverRemoteQuietExceptions", true);
            capabilities.setCapability("timeZone", Environment.get("timeZone").trim());
            
            if(!Environment.get("iedriverversion").trim().equalsIgnoreCase("")){
            	capabilities.setCapability("iedriverVersion", Environment.get("iedriverversion").trim());
            }
            
            if(!Environment.get("sauceLabsMaxDuration").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("maxDuration", Environment.get("sauceLabsMaxDuration").trim());
            if(!Environment.get("sauceLabsCommandTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("commandTimeout", Environment.get("sauceLabsCommandTimeout").trim());
            if(!Environment.get("sauceLabsIdleTimeout").trim().equalsIgnoreCase(""))
            	capabilities.setCapability("idleTimeout", Environment.get("sauceLabsIdleTimeout").trim());
            String tunnelIdentifier = System.getProperty("tunnelIdentifier") != null && !System.getProperty("tunnelIdentifier").trim().equalsIgnoreCase("") ? System.getProperty("tunnelIdentifier").trim() : Environment.get("tunnelIdentifier").trim();
            if(!tunnelIdentifier.equalsIgnoreCase(""))
            	capabilities.setCapability("tunnelIdentifier", tunnelIdentifier);
            LoggingPreferences logPrefs = new LoggingPreferences();
	        logPrefs.enable(LogType.BROWSER, Level.ALL);
	        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            // Launch remote browser and set it as the current thread
            WebDriver driver = new RemoteWebDriver(new URL("http://"  + Environment.get("sauce_username").trim() + ":" + Environment.get("sauce_accesskey").trim() + "@" + seleniumURI +"/wd/hub"), capabilities);
            SessionId session = ((RemoteWebDriver)driver).getSessionId();
            sTestDetails.get().put("SAUCE_LABS_SESSION_ID", session.toString());
            String authToken = EncryptDecrypt.getSauceAuthToken(Environment.get("sauce_username").trim(), Environment.get("sauce_accesskey").trim(), session.toString());
            sTestDetails.get().put("SAUCE_LABS_AUTH_TOKEN", authToken);
            return driver;
    	}
    }
    
    private String getRelatedEnv(String APP_URL) {
		APP_URL = APP_URL.trim();
		String clientId = APP_URL.substring(APP_URL.lastIndexOf("/") + 1);
		if(clientId.trim().endsWith("/")) {
			clientId = clientId.substring(0, clientId.trim().length() - 1);
		}
		String env = "";
		switch(clientId.trim().toUpperCase()) {
			case "IOMEDIAQAUNITAS" :
			case "TAG7" :
				env = "UNITAS";
				break;
			case "IOMEDIA3" :
				env = "DEMO";
				break;
			case "IOMEDIAQACMS" :
				env = "UNITAS-CMS";
				break;
			default :
				String relatedEnv = System.getProperty("relatedEnv") != null && !System.getProperty("relatedEnv").trim().equalsIgnoreCase("") ? System.getProperty("relatedEnv").trim().toUpperCase() : clientId.trim().toUpperCase();
				env = relatedEnv;
		}
		
		return env;
    }
    
    private String getClientName(String APP_URL) {
    	String appurl = APP_URL.trim();
		if(appurl.trim().endsWith("/"))
			appurl = appurl.trim().substring(0, appurl.trim().length() - 1);
		String clientId = appurl.substring(appurl.lastIndexOf("/") + 1).trim().toUpperCase();
		return clientId;
    }
}