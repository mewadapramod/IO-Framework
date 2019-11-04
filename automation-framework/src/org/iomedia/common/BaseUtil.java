package org.iomedia.common;

import org.iomedia.framework.Assert;
import org.iomedia.framework.SoftAssert;
import org.iomedia.framework.WebDriverFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.iomedia.framework.Driver.HashMapNew;
import org.iomedia.framework.OSValidator;
import org.iomedia.framework.Reporting;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;

public class BaseUtil {
	
	private static long DEFAULT_FIND_ELEMENT_TIMEOUT;
	String OS = System.getProperty("os.name").toLowerCase();
	static File classpathRoot = new File(System.getProperty("user.dir"));
	static Logger log = LoggerFactory.getLogger(BaseUtil.class);
	private String driverType;
	public WebDriverFactory driverFactory;
	public HashMapNew Dictionary;
	public HashMapNew Environment;
	public Reporting Reporter;
	public Assert Assert;
	public SoftAssert SoftAssert;
	public ThreadLocal<HashMapNew> sTestDetails = new ThreadLocal<HashMapNew>(){
		@Override protected HashMapNew initialValue() {
			return null;
		}	
	};
	
	@SafeVarargs
	public BaseUtil(WebDriverFactory driverFactory, HashMapNew Dictionary, HashMapNew Environment, Reporting Reporter, Assert Assert, SoftAssert SoftAssert, ThreadLocal<HashMapNew>... sTestDetails){
		OSValidator.setPropValues(OS);
		this.driverFactory = driverFactory;
		driverType = driverFactory.getDriverType() == null ? null : driverFactory.getDriverType().get();
		this.Dictionary = Dictionary == null || Dictionary.size() == 0 ? (driverFactory.getDictionary() == null ? null : driverFactory.getDictionary().get()) : Dictionary;
		this.Environment = Environment == null || Environment.size() == 0 ? (driverFactory.getEnvironment() == null ? null : driverFactory.getEnvironment().get()) : Environment;
		this.Reporter = Reporter == null ? (driverFactory.getReporting() == null ? null : driverFactory.getReporting().get()) : Reporter;
		this.Assert = Assert == null ? (driverFactory.getAssert() == null ? null : driverFactory.getAssert().get()) : Assert;
		this.SoftAssert = SoftAssert == null ? (driverFactory.getSoftAssert() == null ? null : driverFactory.getSoftAssert().get()) : SoftAssert;
		
		assert sTestDetails.length <= 1;
		this.sTestDetails = sTestDetails.length > 0 ? (sTestDetails[0].get() == null ? driverFactory.getTestDetails() : sTestDetails[0]) : new ThreadLocal<HashMapNew>(){
			@Override protected HashMapNew initialValue() {
				return null;
			}	
		};
		
		DEFAULT_FIND_ELEMENT_TIMEOUT = Environment.get("implicitWait").trim().equalsIgnoreCase("") ? 26 : Long.valueOf(Environment.get("implicitWait")) / 1000;
	}
	
	public void sendKeys(By locator, CharSequence... textToType) throws Exception{
		WebElement we = getElementWhenVisible(locator);
		we.sendKeys(textToType);
	}
	
	public void sendKeys(WebElement we, CharSequence... textToType) throws Exception{
		we.sendKeys(textToType);
	}
	
	public void tap(By locator, String objName, long... waitSeconds) {
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		WebElement we = getElementWhenClickable(locator, waitSeconds);
		while(counter >= 0){
			try{
				if(we != null){
					TouchActions actions = new TouchActions((AppiumDriver<?>)getDriver());
					actions.singleTap(we).perform();
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
					break;
				}
			} catch(Exception ex){
				if(counter == 0){
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
					throw ex;
				}
				sync(500L);
				counter--;
			}
		}
	}
	
	public WebDriver getDriver() {
		return driverFactory.getDriver().get();
	}

	/**
	 * Click on element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 */
	public void click(By locator, String objName, long... waitSeconds){
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		while(counter >= 0){
			try{
				WebElement we = getElementWhenClickable(locator, waitSeconds);
				if(we != null){
					javascriptClick(we, objName);
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
					break;
				}
			} catch(Exception ex){
				if(counter == 0){
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
					throw ex;
				}
				sync(500L);
				counter--;
			}
		}
	}
	
	public void click(By locator, String objName, By androidAppLocator, By iosAppLocator, String objName1, long... waitSeconds) {
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		while(counter >= 0){
			try{
				WebElement we = getElementWhenClickable(locator, waitSeconds);
				if(we != null){
					javascriptClick(we, objName, androidAppLocator, iosAppLocator);
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
					break;
				}
			} catch(Exception ex){
				if(counter == 0){
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
					throw ex;
				}
				sync(500L);
				counter--;
			}
		}
	}
	
	public void click(By locator, String objName, By androidAppLocator, By iosAppLocator, String objName1, boolean screenPrint, long... waitSeconds) {
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		while(counter >= 0){
			try{
				WebElement we = getElementWhenClickable(locator, waitSeconds);
				if(we != null){
					javascriptClick(we, objName, androidAppLocator, iosAppLocator);
					if(screenPrint)
						Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
					break;
				}
			} catch(Exception ex){
				if(counter == 0){
					if(screenPrint)
						Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
					throw ex;
				}
				sync(500L);
				counter--;
			}
		}
	}
	
	/**
	 * Click on element on retry basis. It checks for the expected locator.
	 * 
	 * @param locator
	 * @param objName
	 * @param expectedLocator
	 * @param waitSeconds
	 */
	public void click(By locator, String objName, By expectedLocator, long expectedLocatorWaitSeconds, long... waitSeconds){
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		while(counter >= 0){
			try{
				WebElement we = getElementWhenClickable(locator, waitSeconds);
				if(we != null){
					javascriptClick(we, objName);
					getElementWhenVisible(expectedLocator, expectedLocatorWaitSeconds);
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
					break;
				}
			} catch(Exception ex){
				if(counter == 0){
					Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
					throw ex;
				}
				sync(500L);
				counter--;
			}
		}
	}
	
	/**
	 * Click on element on retry basis. It checks for the existing locator to dismiss.
	 * 
	 * @param locator
	 * @param objName
	 * @param expectedLocator
	 * @param waitSeconds
	 */
	public void clickTillLocatorDismiss(By locator, String objName, By unexpectedLocator, long unexpectedLocatorWaitSeconds, long... waitSeconds){
		int counter = !Environment.get("noOfRetriesForSameOperation").trim().equalsIgnoreCase("") ? Integer.valueOf(Environment.get("noOfRetriesForSameOperation").trim()) : 2;
		do{
			WebElement we = getElementWhenVisible(locator, waitSeconds);
			javascriptClick(we, objName);
			sync(unexpectedLocatorWaitSeconds);
			if(checkIfElementPresent(unexpectedLocator, 1))
				counter--;
			else{
				break;
			}
		}while(counter > 0);
		if(counter == 0){
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
		} else{
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
		}
	}
	
	/**
	 * Click on element on retry basis. It checks for the expected locator.
	 * 
	 * @param we
	 * @param objName
	 * @param expectedLocator
	 * @param expectedLocatorWaitSeconds
	 * @param waitSeconds
	 */
	public void click(WebElement we, String objName, By expectedLocator, long expectedLocatorWaitSeconds, long... waitSeconds){
		int counter = 2;
		do{
			javascriptClick(we, objName);
			try{
				getElementWhenVisible(expectedLocator, expectedLocatorWaitSeconds);
				break;
			}
			catch(Exception ex){
				counter--;
			}
		}while(counter > 0);
		if(counter == 0){
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
		} else{
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
		}
	}
	
	/**
	 * Click on element on retry basis. It checks for the existing locator to get stale.
	 * 
	 * @param we
	 * @param objName
	 * @param locatorToGetStale
	 * @param LocatorToGetStaleSeconds
	 * @param waitSeconds
	 */
	public void click(WebElement we, String objName, WebElement locatorToGetStale, long LocatorToGetStaleSeconds, long... waitSeconds){
		int counter = 2;
		do{
			javascriptClick(we, objName);
			if(!checkIfElementPresent(locatorToGetStale, LocatorToGetStaleSeconds))
				break;
			counter--;
		}while(counter > 0);
		if(counter == 0){
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
		} else{
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
		}
	}
	
	/**
	 * Click on element - overloaded
	 * 
	 * @param we
	 * @param objName
	 */
	public void click(WebElement we, String objName){
		javascriptClick(we, objName);
		Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
	}
	
	/**
	 * Send keys into textbox
	 * 
	 * @param locator
	 * @param objName
	 * @param textToType
	 * @param waitSeconds
	 * @throws Exception 
	 */
	public void type(By locator, String objName, String textToType, long... waitSeconds) throws Exception{
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				clear(we);
				sendKeys(we, textToType);
				if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				we = getElementWhenVisible(locator, waitSeconds);
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	private MobileElement goToElement(By iosNativeAppLocator, By webLocator) {
		MobileElement me;
		try {
			me = (MobileElement) getElementWhenVisible(iosNativeAppLocator);
		} catch(Exception ex) {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
			scrollingToElementofAPage(webLocator);
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			me = (MobileElement) getElementWhenVisible(iosNativeAppLocator);
		}
		return me;
	}
	
	private MobileElement goToElement(By iosNativeAppLocator, WebElement we) {
		MobileElement me;
		try {
			me = (MobileElement) getElementWhenVisible(iosNativeAppLocator);
		} catch(Exception ex) {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
			scrollingToElementofAPage(we);
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			me = (MobileElement) getElementWhenVisible(iosNativeAppLocator);
		}
		return me;
	}
	
	private void typeInIOS(By iosNativeAppLocator, String text, By webLocator) {
		try{
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			MobileElement me = goToElement(iosNativeAppLocator, webLocator);
			String mtext = me.getText();
			String value = me.getAttribute("value");
			if(mtext != null && mtext.trim().equalsIgnoreCase(text.trim())) {
				return;
			}
			if(value != null && value.trim().equalsIgnoreCase(text.trim())) {
				return;
			}
			try {
//				me.click();
				int counter = 10;
				do {
					me.clear();
					mtext = me.getText();
					value = me.getAttribute("value");
					counter--;
				} while(counter > 0 && ((mtext != null && !mtext.trim().equalsIgnoreCase("")) || (value != null && !value.trim().equalsIgnoreCase(""))));
			} catch(Exception ex) {
				//Do Nothing
			}
			me.setValue(text);
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
    }
	
	private void typeInIOS(By iosNativeAppLocator, String text, WebElement we) {
		try{
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			MobileElement me = goToElement(iosNativeAppLocator, we);
			String mtext = me.getText();
			String value = me.getAttribute("value");
			if(mtext != null && mtext.trim().equalsIgnoreCase(text.trim())) {
				return;
			}
			if(value != null && value.trim().equalsIgnoreCase(text.trim())) {
				return;
			}
			try {
//				me.click();
				int counter = 10;
				do {
					me.clear();
					mtext = me.getText();
					value = me.getAttribute("value");
					counter--;
				} while(counter > 0 && ((mtext != null && !mtext.trim().equalsIgnoreCase("")) || (value != null && !value.trim().equalsIgnoreCase(""))));
			} catch(Exception ex) {
				//Do Nothing
			}
			me.setValue(text);
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
    }
	
	public void swipe(AndroidDriver<?> android, int startx, int starty, int endx, int endy, Duration duration) {
		try {
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
		//	new TouchAction(android).press(startx, starty).waitAction(duration).moveTo(endx, endy).release().perform();
			 new TouchAction(android).press(PointOption.point(startx, starty)).waitAction(WaitOptions.waitOptions(duration)).moveTo(PointOption.point(endx, endy)).release().perform(); 
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
	}
	
	public void swipe(IOSDriver<?> ios, int startx, int starty, int endx, int endy, Duration duration) {
	    int xOffset = endx - startx;
	    int yOffset = endy - starty;
	    new TouchAction(ios).press(PointOption.point(startx, starty)).waitAction(WaitOptions.waitOptions(duration)).moveTo(PointOption.point(xOffset, yOffset)).release().perform(); 
	}
	
	public void swipe(By webElemProp, String SwipeDirection, Object[] coords) { 
		WebElement we = getElementWhenVisible(webElemProp);
		Dimension size = we.getSize();
		
		MobileElement me = ((MobileElement) we);
		AppiumDriver<?> appium = (AppiumDriver<?>)getDriver();
		
		if(driverType.trim().toUpperCase().contains("IOS")) {
			IOSDriver<?> ios = (IOSDriver<?>)appium;
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				swipe(ios, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, me.getCenter().getX(), me.getLocation().getY() + 1, Duration.ofMillis(1));
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				swipe(ios, me.getCenter().getX(), me.getLocation().getY() + 1, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, Duration.ofMillis(1));
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				swipe(ios, me.getLocation().getX() + 1, me.getCenter().getY(), me.getLocation().getX() + size.width - 1, me.getCenter().getY(), Duration.ofMillis(1));
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				swipe(ios, me.getLocation().getX() + size.width - 1, me.getCenter().getY(), me.getLocation().getX() + 1, me.getCenter().getY(), Duration.ofMillis(1));
			}
			else {
				System.out.println("Not a valid direction passed");
			}
		} else {
			AndroidDriver<?> android = (AndroidDriver<?>)appium;
			Point pt = (Point) coords[0];
			Point loc = (Point) coords[1];
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				swipe(android, pt.getX(), loc.getY() + size.height - 1, pt.getX(), loc.getY() + 1, Duration.ofMillis(1000));
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				swipe(android, pt.getX(), loc.getY() + 1, pt.getX(), loc.getY() + size.height - 1, Duration.ofMillis(1000));
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				swipe(android, loc.getX() + 1, pt.getY(), loc.getX() + size.width - 1, pt.getY(), Duration.ofMillis(500));
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				swipe(android, loc.getX() + size.width - 1, pt.getY(), loc.getX() + 1, pt.getY(), Duration.ofMillis(500));
			}
			else {
				System.out.println("Not a valid direction passed");
			}
		}
		Reporter.log("Swipe", "Done", "Done", "Pass");
	}
	
	public Object[] getCoordinates(By locator) {
		try {
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			MobileElement me = (MobileElement) getElementWhenVisible(locator);
			return new Object[]{me.getCenter(), me.getLocation()};
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
	}
	
	/**
	 * Send keys into textbox
	 * 
	 * @param locator
	 * @param objName
	 * @param textToType
	 * @param waitSeconds
	 * @throws Exception 
	 */
	public void type(By locator, String objName, String textToType, boolean skipValueCheck, long... waitSeconds) throws Exception{
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				clear(we);
				sendKeys(we, textToType);
				if(skipValueCheck)
					break;
				if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				we = getElementWhenVisible(locator, waitSeconds);
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	public void type(By locator, String objName, String textToType, boolean skipValueCheck, By iosNativeAppLocator, long... waitSeconds) throws Exception{
		if(driverType.trim().toUpperCase().contains("IOS")) {
			typeInIOS(iosNativeAppLocator, textToType, locator);
		} else {
			WebElement we = getElementWhenVisible(locator, waitSeconds);
			int intCount = 1;        
			while (intCount <= 4){
				try {	        		
					clear(we);
					sendKeys(we, textToType);
					if(skipValueCheck)
						break;
					if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
						break;
				}catch (Exception e){	
					we = getElementWhenVisible(locator, waitSeconds);
				}
				if(intCount==4){
					Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
					throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
				}
				intCount++;
			}
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Send keys into textbox - overloaded
	 * 
	 * @param we
	 * @param objName
	 * @param textToType
	 * @throws Exception 
	 */
	public void type(WebElement we, String objName, String textToType) throws Exception{
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				clear(we);
				sendKeys(we, textToType);
				if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				//Do Nothing
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Send keys into textbox - overloaded
	 * 
	 * @param we
	 * @param objName
	 * @param textToType
	 * @throws Exception 
	 */
	public void type(WebElement we, String objName, String textToType, boolean skipValueCheck, By iosNativeAppLocator) throws Exception{
		if(driverType.trim().toUpperCase().contains("IOS")) {
			typeInIOS(iosNativeAppLocator, textToType, we);
		} else {
			int intCount = 1;        
			while (intCount <= 4){
				try {	        		
					clear(we);
					sendKeys(we, textToType);
					if(skipValueCheck)
						break;
					if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
						break;
				}catch (Exception e){	
					//Do Nothing
				}
				if(intCount==4){
					Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
					throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
				}
				intCount++;
			}
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	public void type(WebElement we, String objName, String textToType, boolean skipValueCheck) throws Exception{
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				clear(we);
				sendKeys(we, textToType);
				if(skipValueCheck)
					break;
				if(((driverType.trim().toUpperCase().contains("IOS") || driverType.trim().toUpperCase().contains("ANDROID")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || ((driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("FIREFOX") || driverType.trim().toUpperCase().contains("SAFARI") || driverType.trim().toUpperCase().contains("IE")) && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				//Do Nothing
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				throw new Exception("Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase());
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Get text attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public String getText(By locator, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we == null)
			return null;
		return we.getText();
	}
	
	/**
	 * Get text attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public String getText(WebElement we, long... waitSeconds){
		if(we == null)
			return null;
		return we.getText();
	}
	
	/**
	 * Get attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param attribute
	 * @param waitSeconds
	 * @return
	 */
	public String getAttribute(By locator, String attribute, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we == null)
			return null;
		return we.getAttribute(attribute);
	}
	
	/**
	 * Get attribute from element - overloaded
	 * 
	 * @param we
	 * @param attribute
	 * @return
	 */
	public String getAttribute(WebElement we, String attribute){
		return we.getAttribute(attribute);
	}

	/**
	 * Navigate back
	 */
	public void navigateBack(){
		if(driverType.trim().toUpperCase().contains("SAFARI")) {
			((JavascriptExecutor) getDriver()).executeScript("history.go(-1)");
		} else {
			getDriver().navigate().back();
		}
	}
	
	/**
	 * Get element when visible
	 * 
	 * @param locater
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenVisible(By locater, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement element = null;
		
		waitForJStoLoad();
		
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(locater));
				break;
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						element = wait.until(ExpectedConditions.visibilityOfElementLocated(locater));
						break;
					} catch(Exception ex1){
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20)
						throw ex;
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return element;
	}
	
	/**
	 * Get element when invisible
	 * 
	 * @param locater
	 * @param waitSeconds
	 * @return
	 */
	public Boolean getElementWhenInVisible(final By locater, long... waitSeconds) {
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		long counter = seconds;
		do{
			try{
				getElementWhenVisible(locater, 1);
				sync(100L);
				counter--;
			} catch(Exception ex){
				break;
			}
		}while(counter > 0);
		
		if(counter == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * Get element when visible
	 * 
	 * @param locater
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenVisibleWithoutHandlePopups(By locater, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement element = null;
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(locater));
				break;
			}
			catch(Exception ex){
				if(counter >= seconds || seconds <= 20)
					throw ex;
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return element;
	}
	
	/**
	 * Get element when clickable
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenClickable(By locator, long...waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement element = null;
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				element = wait.until(ExpectedConditions.elementToBeClickable(locator));
				break;
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						element = wait.until(ExpectedConditions.elementToBeClickable(locator));
						break;
					} catch(Exception ex1){
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20)
						throw ex;
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return element;
	}

	/**
	 * Get element when text is present
	 * 
	 * @param locater
	 * @param objName
	 * @param text
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenTextIsPresent(By locater, String text, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;

		WebElement element =null;
		
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				boolean val = wait.until(ExpectedConditions.textToBePresentInElementLocated(locater, text));
				if(val){
					element = getDriver().findElement(locater);
					break;
				}
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						boolean val = wait.until(ExpectedConditions.textToBePresentInElementLocated(locater, text));
						if(val){
							element = getDriver().findElement(locater);
							break;
						}
					} catch(Exception ex1){
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20)
						throw ex;
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return element;
	}
	
	/**
	 * Get element when refreshed
	 * 
	 * @param locater
	 * @param objName
	 * @param text
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenRefreshed(final By locater, final String attribute, final String text, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement we = null;
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				Boolean val = wait.until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>(){
					@Override
					public Boolean apply(WebDriver driver) {
						String value = "";
						if(driverType.trim().toUpperCase().contains("FIREFOX") && attribute.trim().equalsIgnoreCase("innerHTML")) {
							value = getDriver().findElement(locater).getText();
						} else {
							value = getDriver().findElement(locater).getAttribute(attribute);
						}
						if(attribute.trim().equalsIgnoreCase("disabled"))
							return value == null ? true : value.trim().equalsIgnoreCase(text);
						else
							return value == null ? false : value.trim().equalsIgnoreCase(text);
					}
					
				}));
				if(val){
					we = getDriver().findElement(locater);
					break;
				}
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						Boolean val = wait.until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>(){
							@Override
							public Boolean apply(WebDriver driver) {
								String value = "";
								if(driverType.trim().toUpperCase().contains("FIREFOX") && attribute.trim().equalsIgnoreCase("innerHTML")) {
									value = getDriver().findElement(locater).getText();
								} else {
									value = getDriver().findElement(locater).getAttribute(attribute);
								}
								if(attribute.trim().equalsIgnoreCase("disabled"))
									return value == null ? true : value.trim().equalsIgnoreCase(text);
								else
									return value == null ? false : value.trim().equalsIgnoreCase(text);
							}
							
						}));
						if(val){
							we = getDriver().findElement(locater);
							break;
						}
					} catch(Exception ex1){
						String value = "";
						if(driverType.trim().toUpperCase().contains("FIREFOX") && attribute.trim().equalsIgnoreCase("innerHTML")) {
							value = getDriver().findElement(locater).getText();
						} else {
							value = getDriver().findElement(locater).getAttribute(attribute);
						}
						Reporter.log(locater.toString() + " - " + attribute, text, value, "Done");
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20) {
						String value = "";
						if(driverType.trim().toUpperCase().contains("FIREFOX") && attribute.trim().equalsIgnoreCase("innerHTML")) {
							value = getDriver().findElement(locater).getText();
						} else {
							value = getDriver().findElement(locater).getAttribute(attribute);
						}
						Reporter.log(locater.toString() + " - " + attribute, text, value, "Done");
						throw ex;
					}
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return we;
	}
	
	/**
	 * Get element when rendered
	 * 
	 * @param locater
	 * @param objName
	 * @param text
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenRendered(final By locater, final int height, final int width, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement we = null;
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				Boolean val = wait.until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>(){
					@Override
					public Boolean apply(WebDriver driver) {
						int h = getDriver().findElement(locater).getSize().getHeight();
						int w = getDriver().findElement(locater).getSize().getWidth();
						return h >= height && w >= width;
					}
					
				}));
				if(val){
					we = getDriver().findElement(locater);
					break;
				}
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						Boolean val = wait.until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>(){
							@Override
							public Boolean apply(WebDriver driver) {
								int h = getDriver().findElement(locater).getSize().getHeight();
								int w = getDriver().findElement(locater).getSize().getWidth();
								return h >= height && w >= width;
							}
							
						}));
						if(val){
							we = getDriver().findElement(locater);
							break;
						}
					} catch(Exception ex1){
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20)
						throw ex;
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return we;
	}
	
	/**
	 * Check if element is present
	 * 
	 * @param locator
	 * @param waitSeconds
	 * @return
	 */
	
	public boolean checkElementPresent(By locator, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : 20;

		if((driverType.trim().toUpperCase().contains("IE") && Environment.get("browserVersion").trim().equalsIgnoreCase("11.0")) || driverType.trim().toUpperCase().contains("SAFARI")) {
			WebDriverWait wait  = new WebDriverWait(getDriver(), seconds);
			try{
				wait.until(ExpectedConditions.presenceOfElementLocated(locator));
				return true;
			} catch(Exception ex) {
				return false;
			}
		} else {
			try{
				getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
				if(driverType.trim().toUpperCase().contains("IOS")){
					Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver())
				       .withTimeout(seconds, TimeUnit.SECONDS)
				       .pollingEvery(20000, TimeUnit.MILLISECONDS);
					
					try{
						wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
						return true;
					}
					catch(Exception ex){
						return false;
					}
					finally{
						getDriver().manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
					}
				}
				else{
					List<WebElement> obj = getDriver().findElements(locator);
					Boolean isPresent = obj.size() > 0;
					return isPresent;
				}
			}
			catch(Exception e){
				//Do Nothing
			}
			finally{
				getDriver().manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
			}
		}
		return false;
	}
	
	
	public boolean checkIfElementPresent(By locator, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : 5;
		if((driverType.trim().toUpperCase().contains("IE") && Environment.get("browserVersion").trim().equalsIgnoreCase("11.0")) || driverType.trim().toUpperCase().contains("SAFARI")) {
			WebDriverWait wait  = new WebDriverWait(getDriver(), seconds);
			try{
				wait.until(ExpectedConditions.presenceOfElementLocated(locator));
				return true;
			} catch(Exception ex) {
				return false;
			}
		} else {
			try{
				getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
				if(driverType.trim().toUpperCase().contains("IOS")){
					Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver())
				       .withTimeout(seconds, TimeUnit.SECONDS)
				       .pollingEvery(100, TimeUnit.MILLISECONDS);
					
					try{
						wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
						return true;
					}
					catch(Exception ex){
						return false;
					}
					finally{
						getDriver().manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
					}
				}
				else{
					List<WebElement> obj = getDriver().findElements(locator);
					Boolean isPresent = obj.size() > 0;
					return isPresent;
				}
			}
			catch(Exception e){
				//Do Nothing
			}
			finally{
				getDriver().manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
			}
		}
		return false;
	}
	
	/**
	 * Check if element is present
	 * 
	 * @param locator
	 * @param waitSeconds
	 * @return
	 */
	public boolean checkIfElementPresent(WebElement we, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : 5;
		try{
			getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
			Boolean isPresent = we.isDisplayed();
			return isPresent;
		}
		catch(Exception e){
			//Do Nothing
		}
		finally{
			getDriver().manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		return false;
	}
	
	/**
	 * Verify is element displayed
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementDisplayed(By locator, String objName, boolean screenPrint){
		boolean isDisplayed;
		WebElement we = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is not displayed", "Fail");
			return false;
		}
		isDisplayed = we.isDisplayed();
		if(screenPrint){
			if(isDisplayed){
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is displayed successfully", "Pass");
			}
			else{
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is not displayed", "Fail");
			}
		}
		return isDisplayed;
	}
	
	/**
	 * Verify is element enabled
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementEnabled(By locator, String objName, boolean... screenPrint){
		boolean isEnabled;
		WebElement we = getElementWhenVisible(locator);
		boolean print = screenPrint.length > 0 ? screenPrint[0] : false;
		if(we == null){
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is not enabled", "Fail");
			return false;
		}
		isEnabled = we.getAttribute("enabled").trim().equalsIgnoreCase("true") ? true : false;
		if(isEnabled)
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is enabled successfully", "Pass");
		else
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is not enabled", "Fail");
		
		return isEnabled;
	}
	
	/**
	 * Verify is element selected
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementSelected(By locator, String objName){
		WebElement we  = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is not selected", "Fail");
			return false;
		}
		boolean isSelected = we.isSelected();
		
		if(isSelected)
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is selected successfully", "Pass");
		else
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is not selected", "Fail");
		
		return isSelected;
	}
	
	/**
	 * Check if the object is checked or not
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isChecked(By locator, String objName){
		WebElement we  = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is  unchecked", "Fail");
			return false;
		}
		boolean isChecked = Boolean.valueOf(getAttribute(locator, "checked"));
		
		if(isChecked)
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is checked successfully", "Pass");
		else
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is unchecked", "Fail");
		
		return isChecked;
	}
	
	/**
	 * Select by value
	 * 
	 * @param locator
	 * @param objName
	 * @param selText
	 */
	public void selectByValue(By locator, String objName, String selText){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByValue(selText);
		if(select.getFirstSelectedOption().getAttribute("value").equals(selText))
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
		else
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is not selected from the list - " + objName.toLowerCase(), "Fail");
	}
	
	/**
	 * Select by index
	 * 
	 * @param locator
	 * @param objName
	 * @param index
	 */
	public void selectByIndex(By locator, String objName, int index){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByIndex(index);
		Reporter.log("Validate value at index - " + index + " is selected from the list - " + objName.toLowerCase(), "Value at index - " + index + " should be selected from the list - " + objName.toLowerCase(), "Value at index - " + index + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
	}
	
	/**
	 * Select by visible text
	 * 
	 * @param locator
	 * @param objName
	 * @param selText
	 */
	public void selectByVisibleText(By locator, String objName, String selText){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByVisibleText(selText);
		if(select.getFirstSelectedOption().getText().equals(selText))
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
		else
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is not selected from the list - " + objName.toLowerCase(), "Fail");
	}
	
	/**
	 * Get elements list
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public List<WebElement> getWebElementsList(By locator){
		return getDriver().findElements(locator);
	}
	
	public List<WebElement> getIosMobileElementsList(By locator){
		try{
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			getElementWhenPresent(locator);
			return getWebElementsList(locator);
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
	}
	
	/**
	 * Get text of all elements
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public ArrayList<String> getTextOfAllWebElements(By locator, String objName){
		ArrayList<String> webElementsTextList = new ArrayList<String>();
		List<WebElement> webElementsList = getWebElementsList(locator);
		if(webElementsList != null){
			for(int i = 0; i< webElementsList.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					webElementsTextList.add(webElementsList.get(i).getText());
				}
				else{
					webElementsTextList.add(webElementsList.get(i).getAttribute("text"));
				}
			}
		}
		
		return webElementsTextList;
		
	}
	
	/**
	 * Get attributeValue of all elements
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public ArrayList<String> getTextOfAllWebElements(By locator, String objName, String attributeName){
		ArrayList<String> webElementsTextList = new ArrayList<String>();
		List<WebElement> webElementsList = getWebElementsList(locator);
		if(webElementsList != null){
			for(int i = 0; i< webElementsList.size(); i++){
				webElementsTextList.add(webElementsList.get(i).getAttribute(attributeName));
			}
		}
		
		return webElementsTextList;
	}
		
	/**
	 * get command
	 * 
	 * @param _command
	 * @param arguments
	 * @param flagHandleQuoting
	 * @return
	 */
	public CommandLine getCommand(String _command, String[] arguments, boolean[] flagHandleQuoting){
		CommandLine command = new CommandLine(_command);
		for(int i = 0; i < arguments.length; i++){
			command.addArgument(arguments[i], flagHandleQuoting[i]);
		}
	  
		return command;
	}
	
	/**
	 * Run command on terminal
	 * 
	 * @param _command
	 * @param arguments
	 * @param flagHandleQuoting
	 * @param wait
	 * @return
	 */
	public String runCommand(String strCommand, long wait, boolean printToConsole, String... condition){
		CommandLine command = new CommandLine(OSValidator.shellType);
		if(OSValidator.shellType.trim().equalsIgnoreCase("cmd"))
			command.addArgument("/c", false);
		else{
			command.addArgument("-l", false);
			command.addArgument("-c", false);
		}
		command.addArgument(strCommand, false);
	  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			assert condition.length <= 1;
			String finalvalue = condition.length > 0 ? condition[0] : "NOT NULL";
			switch(finalvalue.trim().toUpperCase()){
			case "NOT NULL":
				while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
					sync(1L);
					wait--;
				} 
				break;
			case "WAIT":
				sync(wait);
				break;
			default:
				while(!stdout.toString().trim().contains(finalvalue.trim()) && wait > 0){
					sync(1L);
					wait--;
				}
			}
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::runCommand, full stack trace follows:", e1);
		}
		
		if(printToConsole)
			System.out.println(stdout.toString());
		return stdout.toString();
	}

	
	public Object[] runtimeCommand(String strCommand, int counter, boolean printToConsole, boolean waitFor, long... timeout){
		assert timeout.length <= 1;
		long waitTime = timeout.length > 0 ? timeout[0] : (1 * 60 * 1000);
		
		String output = "";
		int exitValue = -1;
		try{
			CommandLine command = new CommandLine(OSValidator.shellType);
			if(OSValidator.shellType.trim().equalsIgnoreCase("cmd"))
				command.addArgument("/c", false);
			else{
				command.addArgument("-l", false);
				command.addArgument("-c", false);
			}
			command.addArgument(strCommand, false);
		  
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(psh);
			try {
				executor.execute(command, resultHandler);
				if(waitFor)
					resultHandler.waitFor(waitTime);
				exitValue = resultHandler.getExitValue();
				if(printToConsole)
					System.out.println(stdout);
				output = stdout.toString();
			} catch (IOException | InterruptedException e1) {
				log.info("Threw a Exception in BaseUtil::runtimeCommand, full stack trace follows:", e1);
			}
		}
		catch(Exception ex){
			//Do Nothing
		} 
		finally{
			counter--;
			sync(1000L);
		}
		
		return new Object[]{output, counter, exitValue};
	}
	
	/**
	 * Run command on terminal
	 * 
	 * @param _command
	 * @param arguments
	 * @param flagHandleQuoting
	 * @param wait
	 * @return
	 */
	public String runCommandUsingTerminal(String command, boolean printToConsole, String... params) {
		assert params.length <= 3;
		String output = "";
		
		String strCommand = command;
		long timeout = Long.valueOf(params.length > 2 ? params[2] : String.valueOf(1 * 60 * 1000));
		String finalvalue = params.length > 1 ? params[1] : "NOT NULL";
		int counter = Integer.valueOf(params.length > 0 ? params[0] : !Environment.get("noOfRetriesForADBLogs").trim().equalsIgnoreCase("") ? Environment.get("noOfRetriesForADBLogs").trim() : "5");
		
		switch(finalvalue.trim().toUpperCase()){
		case "NOT NULL":
			do{
				Object[] obj = runtimeCommand(strCommand, counter, printToConsole, true, timeout);
				output = (String)obj[0];
				counter = (int)obj[1];
				int exitValue = (int)obj[2];
				if(exitValue != 0)
					break;
			}while(output.trim().equalsIgnoreCase("") && counter > 0); 
			break;
		case "WAIT":
			do{
				Object[] obj = runtimeCommand(strCommand, counter, printToConsole, true, timeout);
				output = (String)obj[0];
				counter = (int)obj[1];
				int exitValue = (int)obj[2];
				if(exitValue != 0)
					break;
			}while(counter > 0);
			break;
		default:
			do{
				Object[] obj = runtimeCommand(strCommand, counter, printToConsole, true, timeout);
				output = (String)obj[0];
				counter = (int)obj[1];
				int exitValue = (int)obj[2];
				if(exitValue != 0)
					break;
			}while(!output.trim().contains(finalvalue.trim()) && counter > 0);
		}
		
		return output;
	}
	
	/**
	 * Get XML node value
	 * 
	 * @param path
	 * @param parentNode
	 * @param index
	 * @return
	 */
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
	    	log.info("Threw a Exception in BaseUtil::GetXMLNodeValue, full stack trace follows:", excep);
	    }
	    
	    return dict;
	}
	
	/**
	 * Function to read jsonObject from given URL(Http(s))
	 * 
	 * @param url
	 * @param sslSecurity
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject readJsonFromUrl(String url, boolean sslSecurity) throws IOException, JSONException {
		InputStream is = null;
		JSONObject json = null;
		
		try {            
	      final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	          public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
	          }
	          public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
	          }
	          public X509Certificate[] getAcceptedIssuers() {
	              return null;
	          }
	      } };
	      
	      final SSLContext sslContext = SSLContext.getInstance( "SSL" );
	      sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
	      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();            
	      
	      final URLConnection urlCon = new URL(url).openConnection();            
	      urlCon.setRequestProperty("Request Method", "GET");
	      urlCon.setRequestProperty("Accept", "application/json"); 
	      
	      if(sslSecurity){
	    	  ( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );
	      }
	      is = urlCon.getInputStream();
	              	
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	   	  String jsonText = readAll(rd);
		  json = new JSONObject(jsonText);  	    
	      
	  } catch(ConnectException e){
		  //Do Nothing
	  } catch ( final Exception e ) {
		  log.info("Threw a Exception in BaseUtil::readJsonFromUrl, full stack trace follows:", e);
	  } finally {
		  if(is != null)
			  is.close();
	  }
		return json;
	}
  
	/**
	 * Read text from file reader
	 * 
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	public String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	/**
	 * Launch env for any URL
	 * 
	 * @param strUrl
	 * @return
	 */
	public boolean launchUrl(String strUrl, boolean clearCookie){
		try {
			if(driverType.trim().toUpperCase().contains("IOS")){
				try{
					if(clearCookie)
						((JavascriptExecutor) getDriver()).executeScript("window.sessionStorage.clear();");
				}catch(Exception e){
					log.info("Threw a Exception in BaseUtil::launchUrl, full stack trace follows:", e);
				}							
			}
			
			if(clearCookie)
				getDriver().manage().deleteAllCookies();
			
			//open env according to given URL
			getDriver().get(strUrl);
			        
			if(!driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")){
				getDriver().manage().window().maximize();
			}		

			Reporter.log("Launch: "+strUrl, strUrl+" should be launched", strUrl+ " is launched successfully", "Pass");
			return true;

		} catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::launchUrl, full stack trace follows:", e);
			Reporter.log("Launch: "+strUrl, "Exception occurred","Exception: " + e, "Fail");
			return false;
		}       
	}
	
	/**
	 * Function to validate the selected option in the list
	 * 
	 * @param webElmtProp
	 * @param strObjName
	 * @param expectedValue
	 * @return
	 */
	public boolean validateSelectedOptionFromList(By webElmtProp, String strObjName, String expectedValue){
		try{
			//Get WebElement
			WebElement objList = getElementWhenVisible(webElmtProp);

			//Set Select Element
			Select select = new Select(objList);
			//Get the selected value from the drop down
			String actualValue = select.getFirstSelectedOption().getText().trim();
			log.info("actual value: " + actualValue);
			log.info("expected value: " + expectedValue.trim());

			//Check if actual selected value is equal to expected value
			if(actualValue.trim().equalsIgnoreCase(expectedValue.trim())){
				Reporter.log("Validate option - " + expectedValue.toLowerCase() + " is selected from the list - " + strObjName.toLowerCase(), "Option - " + expectedValue.toLowerCase() + " should be selected from the list - " + strObjName.toLowerCase(), "Expected value matches actual value - " + actualValue.toLowerCase(), "Pass");
				return true;        		
			}else{
				Reporter.log("Validate option - " + expectedValue.toLowerCase() + " is selected from the list - " + strObjName.toLowerCase(), "Option - " + expectedValue.toLowerCase() + " should be selected from the list - " + strObjName.toLowerCase(), "Actual value selected is - " + actualValue.toLowerCase(), "Fail");
				return false;
			}
		} catch (Exception e){
			log.info("Threw a Exception in BaseUtil::validateSelectedOptionFromList, full stack trace follows:", e);
			Reporter.log("Weblist: "+strObjName, "Exception occurred","Exception: " + e, "Fail");
			return false;
		}
	}
	
	/**
	 * waiting the specified time
	 * 
	 * @param sTime
	 */
	public void sync(Long sTime)
	{
		try {
			Thread.sleep(sTime);
		} catch (InterruptedException e) {			
			log.info("Threw a Exception in BaseUtil::sync, full stack trace follows:", e);
		}
	}
	
	/**
	 * check if Alert popup is coming and click on OK (accept) button
	 * 
	 * @param sAction
	 */
	public void checkAlert(String sAction)
	{
		try{
			WebDriverWait wait = new WebDriverWait(getDriver(),1);
			wait.until(ExpectedConditions.alertIsPresent());

			Alert alert = getDriver().switchTo().alert();
			if(sAction.equalsIgnoreCase("accept"))
				alert.accept();
			else if(sAction.equalsIgnoreCase("decline"))
				alert.dismiss();
		}
		catch (Exception e){
			log.info("Threw a Exception in BaseUtil::checkAlert, full stack trace follows:", e);
		}
	}	
	
	/**
	 * navigate back to previous page
	 */
	public void browserBackButton() {			
		if(driverType.trim().toUpperCase().contains("SAFARI")) {
			((JavascriptExecutor) getDriver()).executeScript("history.go(-1)");
		} else {
			getDriver().navigate().back();
		}
	}
	
	/**
	 * switch to window based on index
	 * 
	 * @param iIndex
	 */
	public void switchToWindow(Integer iIndex){
		Set<String> collWindowHandles = getDriver().getWindowHandles();
		if(collWindowHandles.size() < iIndex + 1){
			Reporter.log("SwitchToWindow", "Specified index out of range.", "Available Windows: " + collWindowHandles.size() + "Specified Index: " + iIndex , "Fail");
		}
		else{
			Iterator<String> iter = collWindowHandles.iterator();
			for(int i=0;i<collWindowHandles.size();i++){    			
				String sWindowHandle = iter.next();
				if(i == iIndex){
					getDriver().switchTo().window(sWindowHandle);
					break;
				}
			}
		}
	}
	
	/**
	 * switch to window based on window name
	 * 
	 * @param windowName
	 */
	public void switchToWindow(String windowName){
		Set<String> collWindowHandles = getDriver().getWindowHandles();
		boolean flag = false;
		Iterator<String> iter = collWindowHandles.iterator();
		for(int i=0;i<collWindowHandles.size();i++){    			
			String sWindowHandle = iter.next();
			if(sWindowHandle.trim().equalsIgnoreCase(windowName) || sWindowHandle.trim().toLowerCase().contains(windowName.trim().toLowerCase())){
				flag = true;
				getDriver().switchTo().window(sWindowHandle);
				break;
			}
		}
		
		if(flag == false){
			Reporter.log("Switch to window", "Specified window should be found", "Specified window not found", "Fail");
		}
	}
	
	/**
	 * switch to window based on url
	 * 
	 * @param windowName
	 */
	public void switchToWindowBasedOnUrl(String url){
		Set<String> collWindowHandles = getDriver().getWindowHandles();
		boolean flag = false;
		Iterator<String> iter = collWindowHandles.iterator();
		String currentUrl = getDriver().getCurrentUrl();
		for(int i=0; i<collWindowHandles.size(); i++){    			
			String sWindowHandle = iter.next();
			getDriver().switchTo().window(sWindowHandle);
			if(getDriver().getCurrentUrl().trim().equalsIgnoreCase(url.trim())){
				flag = true;
				break;
			}
		}
		
		if(flag == false){
			switchToWindowBasedOnUrl(currentUrl);
			Reporter.log("Switch to window", "Specified window should be found", "Specified window not found", "Fail");
		}
	}
	
	/**
	 * Swipe function for swiping the entire page
	 * 
	 * @param SwipeDirection
	 */
		
	public String getLocatorValue(By element){
		
		String val = null;
		
		if (element instanceof By.ByXPath){
			val = element.toString().replaceFirst("By.xpath: ", "");
		}
		else if (element instanceof By.ByName){
			val = element.toString().replaceFirst("By.name: ", "");
		}
		else if (element instanceof By.ById){
			val = element.toString().replaceFirst("By.id: ", "");
		}
		else if (element instanceof By.ByClassName){
			val = element.toString().replaceFirst("By.className: ", "");
		}
		else if (element instanceof By.ByTagName){
			val = element.toString().replaceFirst("By.tagName: ", "");
		}
		else if(element instanceof By.ByCssSelector){
			val = element.toString().replaceFirst("By.selector: ", "");
		}
		else if(element instanceof By.ByLinkText){
			val = element.toString().replaceFirst("By.linkText: ", "");
		}
		else{
			val = element.toString().replaceFirst("By.partialLinkText: ", "");
		}
		
		return val;
	}
	
	public String getXpath(By childElement, String objName, String objType, int index){
		
		String Xpath = null;
		String FindBy = "";
		String val = getLocatorValue(childElement);
		if (childElement instanceof By.ByXPath){
			FindBy = "Xpath";
			if(index > 0){
				Xpath = "(" + val + ")[" + index + "]";
			}
			else{
				Xpath = val;
			}
		}
		else if (childElement instanceof By.ByName){
			FindBy = "Name";
			if(index > 0){
				Xpath = "(//" + objType + "[@name='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@name='" + val + "']";
			}
		}
		else if (childElement instanceof By.ById){
			FindBy = "Id";
			String id = "id";
			if(index > 0){
				Xpath = "(//" + objType + "[@" + id + "='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@" + id + "='" + val + "']";
			}
		}
		else if (childElement instanceof By.ByClassName){
			FindBy = "ClassName";
			if(index > 0){
				Xpath = "(//" + objType + "[@class='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@class='" + val + "']";
			}
		}
		else if (childElement instanceof By.ByTagName){
			FindBy = "TagName";
			if(index > 0){
				Xpath = "(//" + val + ")[" + index + "]";
			}
			else{
				Xpath = "//" + val;
			}
		}
		else{
			Reporter.log("Object Identification", "Property name :" + FindBy,"Property name specified for object " + objName + " is invalid", "Fail");
			return null;
		}
		
		return Xpath;
	}
	
	/**
	 * Function to get Parent Web Element
	 * 
	 * @param childElement
	 * @param objType
	 * @param objName
	 * @param ParentLevel
	 * @param index
	 * @return
	 */
	public String getParentElement(By childElement, String objType, String objName, int ParentLevel, int index){
		try{
			String Xpath = getXpath(childElement, objName, objType, index);
			//Define Parent xpath
			String strParentXpath = "";
			if(ParentLevel > 0){
				for(int count=1; count<=ParentLevel; count++){
					strParentXpath = strParentXpath + "/..";
				}	
			}
			//Get Parent WebElement
			return Xpath + strParentXpath;			
		}catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getParentElement, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		} 
	}
	
	/**
	 * Function to get Parent Web Element - Overloaded function
	 * 
	 * @param childElement
	 * @param objName
	 * @param ParentLevel
	 * @return
	 */
	public WebElement getParentElement(By childElement, String objName, int ParentLevel){
		try{
			//get the object 
			WebElement childObject = getElementWhenVisible(childElement);
			if(childObject==null)
				return null;
			
			//Define Parent xpath
			String strParentXpath = "..";
			if(ParentLevel > 1){
				for(int count=2; count<=ParentLevel; count++){
					strParentXpath = strParentXpath + "/..";
				}	
			}
			//Get Parent WebElement
			WebElement parentElement = childObject.findElement(By.xpath(strParentXpath));   		
			return parentElement;			
		}catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getParentElement, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		} 
	}
	
	/**
	 * Function to get Sibling Web Element
	 * 
	 * @param strChildElement
	 * @param siblingDesc
	 * @return
	 */
	public List<WebElement> getSiblingElements(By childElement,String objName){
		try{

			WebElement child = getElementWhenVisible(childElement);
			WebElement parent = child.findElement(By.xpath(".."));
			List<WebElement> list= getMultipleChildObjects(parent, childElement, objName);
			return list;
		}
		catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getSiblingElements, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		}
	}
	
	/**
	 * Get child webelements under parent webelement
	 * 
	 * @param Parent
	 * @param childElmtProp
	 * @param childName
	 * @return
	 */
	public List<WebElement> getChildWebElementsList(WebElement Parent, By childElmtProp, String childName){
		try{
			//Get WebElement    		
			List<WebElement> childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);

			//Check if the WebElement is enabled or displayed    		
			boolean bIsDisplayed = false;
			boolean bIsEnabled = false;

			int intCount = 1;        
			while (!(bIsDisplayed || bIsEnabled) && (intCount <=3)){
				try {	        					
					if(childWebElements.size() != 0){
						bIsDisplayed = childWebElements.get(0).isDisplayed();
						bIsEnabled = childWebElements.get(0).getAttribute("enabled").trim().equalsIgnoreCase("true") ? true : false;
						for(int i = 0; i < childWebElements.size(); i++){
							if(!childWebElements.get(i).isDisplayed()){
								childWebElements.remove(i);
							}
						}
					}					
				}catch (StaleElementReferenceException e){	
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
				}catch (WebDriverException e){	    
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
				}catch (NullPointerException e){	    
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
					if(childWebElements == null){
						break;
					}
				}	    	    
				intCount++;			
			}

			//Validate if the element is displayed
			if (!(bIsDisplayed || bIsEnabled)){	        	
				return null;
			}	        
			return childWebElements;
		}catch(Exception e){
			log.info("Threw a Exception in Baseutil::getChildWebElementsList, full stack trace follows:", e);
			Reporter.log(childName, "Exception occurred","Exception: " + e, "Fail");			
			return null;    		
		}
	}
	
	/**
	 * Get child webelements under parent webelement
	 * 
	 * @param Parent
	 * @param childElmtProp
	 * @param childName
	 * @param objType
	 * @return
	 */
	public List<WebElement> getChildWebElementsList(By Parent, By childElmtProp, String childName, String objType){
		try{
			//Get WebElement    		
			List<WebElement> childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
			
			 if(!driverType.trim().toUpperCase().contains("IOS")){
				//Check if the WebElement is enabled or displayed    		
				boolean bIsDisplayed = false;
				boolean bIsEnabled = false;
	
				int intCount = 1;        
				while (!(bIsDisplayed || bIsEnabled) && (intCount <=3)){
					try {	        					
						if(childWebElements.size() != 0){
							bIsDisplayed = childWebElements.get(0).isDisplayed();
							bIsEnabled = childWebElements.get(0).getAttribute("enabled").trim().equalsIgnoreCase("true") ? true : false;
							for(int i = 0; i < childWebElements.size(); i++){
								if(!childWebElements.get(i).isDisplayed()){
									childWebElements.remove(i);
								}
							}
						}					
					}catch (StaleElementReferenceException e){	
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
					}catch (WebDriverException e){	    
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
					}catch (NullPointerException e){	    
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
						if(childWebElements == null){
							break;
						}
					}	    	    
					intCount++;			
				}
	
				//Validate if the element is displayed
				if (!(bIsDisplayed || bIsEnabled)){	        	
					return null;
				}	 
			 }
			return childWebElements;
		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::getChildWebElementsList, full stack trace follows:", e);
			Reporter.log(childName, "Exception occurred","Exception: " + e, "Fail");			
			return null;    		
		}
	}
	
	/**
	 * Method to get single child object under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objType
	 * @param objName
	 * @return
	 */
	public WebElement getSingleChildObject(String parent, By objDesc, String objType, String objName){
		if(parent == null){
			return null;
		}
		String Xpath = getXpath(objDesc, objName, objType, -1);
		return getElementWhenVisible(By.xpath(parent + Xpath));       
	}
	
	/**
	 * Method to get multiple child objects under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objName
	 * @return
	 */
	public List<WebElement> getMultipleChildObjects(WebElement parent, By objDesc, String objName){
		//Verify parent element
		if(parent == null){ 
			return null;
		}
		
		String val = getLocatorValue(objDesc);	            
		String FindBy = "";
		int intcount = 1;	            
		while (intcount <= 2){	            	
			try{
				//Handle all FindBy cases
				if (objDesc instanceof By.ByLinkText){
					FindBy = "LinkText";
					return parent.findElements(By.linkText(val));
				}
				else if (objDesc instanceof By.ByXPath){
					FindBy = "Xpath";
					return parent.findElements(By.xpath(val));
				}
				else if (objDesc instanceof By.ByName){
					FindBy = "Name";
					return parent.findElements(By.name(val));
				}
				else if (objDesc instanceof By.ById){
					FindBy = "Id";
					return parent.findElements(By.id(val));
				}
				else if (objDesc instanceof By.ByClassName){
					FindBy = "Classname";
					return parent.findElements(By.className(val));
				}
				else if (objDesc instanceof By.ByCssSelector){
					FindBy = "CssSelector";
					return parent.findElements(By.cssSelector(val));
				}
				else if (objDesc instanceof By.ByTagName){
					FindBy = "TagName";
					return parent.findElements(By.tagName(val));
				}
				else{
					Reporter.log("Object Identification", "Property name :" + FindBy,"Property name specified for object " + objName + " is invalid", "Fail");
					return null;
				}		            	
			}
			catch(Exception e){		            	
				if (intcount == 2){
					log.info("Threw a Exception in BaseUtil::getMultipleChildObjects, full stack trace follows:", e);
					Reporter.log("Object : " + objName, objName + " is not identified", "Exception :" + e.toString(), "Fail");
					return null;
				}		            	
				intcount = intcount + 1;
			}		            
		}
		return null;	           
	}
	
	/**
	 * Method to get multiple child objects under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objName
	 * @param objType
	 * @return
	 */
	public List<WebElement> getMultipleChildObjects(By parent, By objDesc, String objName, String objType){
		//Verify parent element
		if(parent == null){ 
			return null;
		}
		
		String Xpath = getXpath(objDesc, objName, objType, -1);
		String parentXpath = getXpath(parent, "Parent", objType, -1);
		int intcount = 1;	            
		while (intcount <= 2){	            	
			try{
				return getDriver().findElements(By.xpath(parentXpath + Xpath));
			}
			catch(Exception e){		            	
				if (intcount == 2){
					log.info("Threw a Exception in BaseUtil::getMultipleChildObjects, full stack trace follows:", e);
					Reporter.log("Object :" + objName, objName + " is not identified", "Exception :" + e.toString(), "Fail");
					return null;
				}		            	
				intcount = intcount + 1;
			}		            
		}
		return null;	           
	}
		
	/**
	 * Retrives all xml nodes for a xml path
	 * 
	 * @param xmlFile
	 * @param strXPath
	 * @return
	 */
	public NodeList getXMLNodes(File xmlFile, String strXPath){
		try{
			//Create Document Object
			DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
			Document xmldoc = docBuilder.parse( xmlFile );

			//Create xPath object
			XPathFactory xPathfac = XPathFactory.newInstance();
			XPath objXpath = xPathfac.newXPath();			
			XPathExpression xpathExpr = objXpath.compile( strXPath );

			//Get List of nodes
			NodeList objNodeList = (NodeList)xpathExpr.evaluate(xmldoc, XPathConstants.NODESET);

			return objNodeList;    		
		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::getXMLNodes, full stack trace follows:", e);
			Reporter.log("getXMLNodes", "Exception occurred","Exception : " + e, "Fail");			
			return null;		
		}  	
	}
	
	/**
     * Click on the webelement
     * 
     * @param webElement
     * @param strObjName
     * @return
     */
    public boolean javascriptClick(WebElement webElement, String strObjName){   	 		
        //Click on the WebElement    		
        int intCount = 1;        
        while (intCount<=4){
        	try {
        		if(driverType.trim().toUpperCase().contains("ANDROID") || driverType.trim().toUpperCase().contains("IOS")) {
        			try {
        				webElement.click();
        			} catch(Exception ex) {
        				scrollingToElementofAPage(webElement);
        				webElement.click();
        			}
	        	}else{
	        		try {
	        			((JavascriptExecutor) getDriver()).executeScript("return arguments[0].click()", webElement);
	        		} catch(WebDriverException we) {
	        			webElement.click();
	        		}
	        	}
        		break;
	        }catch (Exception e){
	        	sync(500L);
	        	if(intCount==4){
	        		log.info("Threw a Exception in BaseUtil::javascriptClick, full stack trace follows:", e);
	    	    	Reporter.log("Click: " + strObjName, "Exception occurred","Exception: " + e, "Fail");
	    	    	throw e;
	        	}
    	    }  	    
    	    intCount++;
        }	        
        return true;    	       
    }
    
    public boolean javascriptClick(WebElement webElement, String strObjName, By androidAppLocator, By iosAppLocator){   	 		
        //Click on the WebElement    		
        int intCount = 1;        
        while (intCount<=4){
        	try {
        		if(driverType.trim().toUpperCase().contains("ANDROID") || driverType.trim().toUpperCase().contains("IOS")) {
        			if(driverType.trim().toUpperCase().contains("ANDROID") && androidAppLocator != null) {
        				clickAppElement(androidAppLocator, webElement);
        			} else if(driverType.trim().toUpperCase().contains("IOS") && iosAppLocator != null){
        				clickAppElement(iosAppLocator, webElement);
        			} else {
        				try {
            				webElement.click();
            			} catch(Exception ex) {
            				scrollingToElementofAPage(webElement);
            				webElement.click();
            			}
        			}
	        	}else{
	        		try {
	        			((JavascriptExecutor) getDriver()).executeScript("return arguments[0].click()", webElement);
	        		} catch(WebDriverException we) {
	        			webElement.click();
	        		}
	        	}
        		break;
	        }catch (Exception e){    
	        	sync(500L);
	        	if(intCount==4){
	        		log.info("Threw a Exception in BaseUtil::javascriptClick, full stack trace follows:", e);
	    	    	Reporter.log("Click: " + strObjName, "Exception occurred","Exception: " + e, "Fail");
	    			throw e;
	        	}
    	    }  	    
    	    intCount++;
        }	        
        return true;    	       
    }
    
    private void clickAppElement(By appLocator, WebElement we) {
		try{
			((AppiumDriver<?>)getDriver()).context("NATIVE_APP");
			MobileElement me = goToElement(appLocator, we);
			me.click();
		} finally {
			Set<String> contextHandles = ((AppiumDriver<?>)getDriver()).getContextHandles();
			Iterator<String> iter = contextHandles.iterator();
			String contextName = "";
			while(iter.hasNext()) {
				String names = iter.next();
				if(!names.trim().toUpperCase().contains("NATIVE_APP")) {
					contextName = names;
					break;
				}
			}
			((AppiumDriver<?>)getDriver()).context(contextName);
		}
    }
	
	/**
	 * Deletes a folder after deleting all its sub-folders and files
	 * 
	 * @param FolderPath
	 * @return
	 */
	public boolean deleteFolder(File FolderPath) {
		try{		
			if (FolderPath.isDirectory()) {
				String[] arrChildNodes = FolderPath.list();
				for (int i=0; i<arrChildNodes.length; i++) {
					deleteFolder(new File(FolderPath, arrChildNodes[i]));
				}
			}
			FolderPath.delete();
			return true;

		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::DeleteFolder, full stack trace follows:", e);
			Reporter.log("DeleteFolder", "Exception occurred" ,"Exception :" + e, "Fail");
			return false;
		}
	}
	
	/**
	 * Epoch time converter
	 * 
	 * @param value
	 * @param type : EPOCH or any value
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public String epochTimeConverter(String value, String type) throws ParseException{
		String output = null;
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
		if(type.trim().equalsIgnoreCase("EPOCH")){
	        output = formatter.format(new java.util.Date(Long.parseLong(value)));
		}
		else{
			Date _date = null;
			if(!value.trim().equalsIgnoreCase(""))
				_date = formatter.parse((formatter.format(new Date(value))));
			else
				_date = formatter.parse((formatter.format(new Date())));
				output = String.valueOf(_date.getTime());
		}
		
		return output;
	}
	
	/**
	 * Fetch file from remote server and store it locally
	 * 
	 * @param host
	 * @param username
	 * @param remotePath
	 * @param remoteFileName
	 * @param localPath
	 * @param localFileName
	 * @return
	 * @throws Exception
	 */
	public String fetchLogFile(String host, String username, String remotePath, String remoteFileName, String localPath, String localFileName) throws Exception{
		if(localPath == null || localPath.trim().equalsIgnoreCase("")){
	        if(!new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS").exists()){
	        	new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS").mkdirs();	                
	        }
	        
	        localPath = Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS" + OSValidator.delimiter;
		}
		
		if(localFileName == null || localFileName.trim().equalsIgnoreCase("")){
			java.util.Date today = new java.util.Date();
			Timestamp now = new java.sql.Timestamp(today.getTime());
			String tempNow[] = now.toString().split("\\.");
			final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
			localFileName = localPath + "Analytics_" + sStartTime + ".log";
		}
		else{
			localFileName = localPath + localFileName + ".log";
		}
		
		String output = "";
		Object[] obj = runtimeCommand("perl " + System.getProperty("user.dir") + Environment.get("perlScriptPath") + "FetchLogFile.pl" + " " + host + " " + username + " " + remotePath + " " + remoteFileName + " " + localFileName, 1, true, true, 60*1000);
		output = (String)obj[0];
		if(output.trim().contains("copied from " + host + " successfully!")){
			log.info("Log file fetched successfully and stored in " + localFileName);
			return localFileName;
		}
		else{
			log.info("Log file not found");
			return null;
		}
	}
	
	/**
	 * Run command on remote server
	 * 
	 * @param host
	 * @param username
	 * @param command
	 * @throws Exception 
	 */
	public String runRemoteCommand(String host, String username, String command) throws Exception{
		Object[] obj = runtimeCommand("perl " + System.getProperty("user.dir") + Environment.get("perlScriptPath") + "RunRemoteCommand.pl" + " " + host + " " + username + " \"" + command + "\"", 1, true, true, 60*1000);
		String output = (String)obj[0];
		return output;
	}
	
	/**
	 * Get current time in seconds on android device
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getCurrentTimeInSecondsOnAndroidDevice() throws Exception{
		return runCommandUsingTerminal("adb -s " + Environment.get("udid").trim() + " shell date +%s", false, "1");
	}

	/**
	 * Get current time in milliseconds on android device
	 * 
	 * @return
	 * @throws Exception
	 */
	public long getCurrentTimeInMillisecondsOnAndroidDevice() throws Exception{
		String timeinsecondsOnDevice = getCurrentTimeInSecondsOnAndroidDevice();
		long timeinMillisecondsOnDeviceL = Long.valueOf(timeinsecondsOnDevice.trim())*1000;
		return timeinMillisecondsOnDeviceL;
	}
	
	/**
	 * Convert locators based on automation tool - appium or selendroid
	 * By default "Appium" is the automation name
	 * 
	 * @return
	 */
	public By convertLocator(By locator){
		String val = null;
		
		if(Environment.get("automationName").trim().equalsIgnoreCase("Selendroid")){
			if (locator instanceof By.ByXPath){
				val = locator.toString().replaceFirst("By.xpath: ", "").trim();
				boolean flag = true;
				while(flag){
					if(val.trim().contains("android.view.View")){
						val = val.replace("android.view.View", "Toolbar");
					}
					else if(val.trim().contains("resource-id")){
						val = val.replace("resource-id", "id");
					}
					else if(val.trim().contains("android.widget.TextView")){
						val = val.replace("android.widget.TextView", "TextView");
					}
					else{
						flag = false;
					}
				}
				
				val = val.replace(Environment.get("appPackage") + ":id/", "");
				return By.xpath(val);
			}
			else if (locator instanceof By.ById){
				val = locator.toString().replaceFirst("By.id: ", "");
				String[] split = val.split(":id/");
				if(split.length > 1){
					return By.id(split[1]);
				}
				else{
					return locator;
				}
			}
			else if (locator instanceof By.ByClassName){
				//TBD
			}
			else{
				return locator;
			}
		}
		
		return locator;
	}
	
	public void scrollingToElementofAPage(WebElement we) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", we);
		sync(1000L);
	}
	
	public void scrollingToElementofAPage(By locator) {
		WebElement webElement = getElementWhenVisible(locator);		
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", webElement);
		sync(1000L);
	}
	
	public void scrollingByCoordinatesofAPage(int x, int y) {
		((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(" + x + "," + y + ")");
		sync(1000L);
	}
	
	public void scrollingToBottomofAPage() {
		 ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		 sync(1000L);
	}
	
	public void scrollingToTopofAPage() {
		 ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, 0)");
		 sync(1000L);
	}
	
	public boolean waitForJStoLoad() {
//		if(!driverType.trim().toUpperCase().contains("CHROME") && !driverType.trim().toUpperCase().contains("SAFARI") && !driverType.trim().toUpperCase().contains("FIREFOX") && !driverType.trim().toUpperCase().contains("IE"))
//			return true;
//		
//	    WebDriverWait wait = new WebDriverWait(getDriver(), 30);
//
//	    // wait for jQuery to load
//	    ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
//	      @Override
//	      public Boolean apply(WebDriver driver) {
//	        try {
//	          return ((JavascriptExecutor) driver).executeScript("return jQuery.active").toString().equals("0");
//	        }
//	        catch (Exception e) {
//	          return true;
//	        }
//	      }
//	    };
//
//	    // wait for Javascript to load
//	    ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
//	      @Override
//	      public Boolean apply(WebDriver driver) {
//	        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
//	      }
//	    };
//
//	  return wait.until(jQueryLoad) && wait.until(jsLoad);
		return true;
	}
	
	public String getIOSDeviceDetails(String prop, String udid){
		String output = "";
		if(!udid.trim().equalsIgnoreCase("")){
			output = runCommandUsingTerminal("ideviceinfo -u " + udid + " -k " + prop, false, "1");
		} else{
			output = runCommandUsingTerminal("ideviceinfo -k " + prop, false, "1");
		}	
		return output.trim();
	}
	
	public void load(String uri) {
		getDriver().get(Environment.get("APP_URL").trim() + uri);
		boolean runLocally = System.getProperty("runLocally") != null && !System.getProperty("runLocally").trim().equalsIgnoreCase("") ? Boolean.valueOf(System.getProperty("runLocally").trim().toLowerCase()) : Boolean.valueOf(Environment.get("runLocally").trim().toLowerCase());
    	if(driverType.trim().toUpperCase().contains("SAFARI") && runLocally) {
    		getDriver().get(Environment.get("APP_URL").trim() + "/user/logout");
    	}
    	getDriver().get(Environment.get("APP_URL").trim() + uri);
    	
//    	if(!driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")) {
    		try {
    			Object obj = ((JavascriptExecutor) getDriver()).executeScript("var obj = drupalSettings.componentConfigData.siteconfig;return JSON.stringify(obj);");
    			JSONObject json = new JSONObject(obj.toString());
    			Environment.put("currency", json.has("currency")? json.getString("currency") : "$");
    			((JavascriptExecutor) getDriver()).executeScript("$('#doorbell-button').remove()");
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
	
	public WebElement getElementWhenPresent(By locater, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement element = null;
		
		waitForJStoLoad();
		
		int counter = 0;
		do{
			long time = 20;
			if(seconds <= 20)
				time = seconds;
			WebDriverWait wait  = new WebDriverWait(getDriver(), time);
			try{
				element = wait.until(ExpectedConditions.presenceOfElementLocated(locater));
				break;
			}
			catch(Exception ex){
				boolean flag = false;
				if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
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
					
				}
				if(flag && seconds <= 20){
					try{
						element = wait.until(ExpectedConditions.presenceOfElementLocated(locater));
						break;
					} catch(Exception ex1){
						throw ex1;
					}
				}
				else{
					if(counter >= seconds || seconds <= 20)
						throw ex;
				}
			}
			finally{
				counter += time;
			}
		}while(true);
		
		return element;
	}
	
	public Object getGDValue(Object value) {
		Object newValue = null;
		if(value instanceof String){
			if(((String)value).trim().endsWith("L") && StringUtils.isNumeric(((String)value).trim().substring(0, ((String)value).trim().length() - 1)))
				return Long.valueOf(((String)value).trim().substring(0, ((String)value).trim().length() - 1));
			value = getComplexValue((String) value, "%{GD_");
			newValue = getComplexValue((String) value, "%{ENV_");
		}
		else{
			newValue = value;
		}
		if(((String)newValue).trim().startsWith("\""))
			newValue = ((String)newValue).trim().substring(1);
		if(((String)newValue).trim().endsWith("\""))
			newValue = ((String)newValue).trim().substring(0, ((String)newValue).trim().length() - 1);
		return newValue;
	}
	
	public String getComplexValue(String value, String prefix) {
		Stack<String> pos = new Stack<String>();
		while(value.contains(prefix)){
			String initialString = value.substring(0, value.indexOf(prefix));
			pos.push(initialString);
			pos.push(prefix);
			int startindex = value.indexOf(prefix) + 5;
			int endindex =  value.indexOf("}");
			String nextValue = value.substring(startindex);
			int firstendindex = nextValue.indexOf("}");
			int lastendindex = nextValue.indexOf("%");
			if(lastendindex != -1 && lastendindex < firstendindex) {
				value = value.substring(startindex);
				continue;
			} else {
				String key = value.trim().substring(startindex, endindex).trim();
				value = value.substring(endindex + 1);
				pos.push(key);
				String _value;
				if(prefix.trim().equalsIgnoreCase(prefix))
					_value = Dictionary.containsKey(key) ? Dictionary.get(key).trim() : Environment.get(key).trim();
				else
					_value = Environment.containsKey(key) ? Environment.get(key).trim() : Dictionary.get(key).trim();
				pos.pop();
				pos.pop();
				value = pos.pop() + _value + value;
			}
		}
		if(!pos.empty()) {
			while(!pos.empty()) {
				value = pos.pop() + value;
			}
			value = getComplexValue(value, prefix);
		}
		return value;
	}
	
	public String getSessionId() {
		Set<Cookie> cookies = getDriver().manage().getCookies();
		Iterator<Cookie> iter = cookies.iterator();
		String _cookieValue = "";
		while(iter.hasNext()) {
			Cookie cookie = iter.next();
			if(cookie.getName().trim().startsWith("SSESS") || cookie.getName().trim().startsWith("SESS")) {
				_cookieValue = cookie.getValue().trim();
				break;
			}
		}
		return _cookieValue;
	}
	
	public void clear(WebElement we) {
		if(driverType.trim().toUpperCase().contains("CHROME")) {
//			Keys[] keys = new Keys[we.getAttribute("value").length()];
//            for (int i = 0; i < keys.length; i++)
//                keys[i] = Keys.BACK_SPACE;
//            we.sendKeys(Keys.chord(keys));
			Actions navigator = new Actions(getDriver());
		    navigator.click(we)
		        .sendKeys(Keys.END)
		        .keyDown(Keys.SHIFT)
		        .sendKeys(Keys.HOME)
		        .keyUp(Keys.SHIFT)
		        .sendKeys(Keys.BACK_SPACE)
		        .perform();
		} else
			we.clear();
	}
}
