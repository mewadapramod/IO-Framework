package org.iomedia.framework;

import java.util.ArrayList;
import java.util.List;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.xml.XmlClass;

public class MyInterceptor implements IMethodInterceptor {
	
	ThreadLocal<List<IMethodInstance>> tNewList = new ThreadLocal<List<IMethodInstance>>(){
		@Override protected List<IMethodInstance> initialValue() {
			return new ArrayList<IMethodInstance>();
		}
	};
	ThreadLocal<Integer> tCount = new ThreadLocal<Integer>(){
		@Override protected Integer initialValue() {
			return 0;
		}
	};
	ThreadLocal<String> tSuiteName = new ThreadLocal<String>(){
		@Override protected String initialValue() {
			return "";
		}
	};
	ThreadLocal<String> tTestName = new ThreadLocal<String>(){
		@Override protected String initialValue() {
			return "";
		}
	};
	
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    	List<IMethodInstance> newList = tNewList.get();
    	int count = tCount.get();
    	String suiteName = tSuiteName.get();
    	String testName = tTestName.get();
    	if(!suiteName.trim().equals(context.getSuite().getName()) || ! testName.trim().equals(context.getCurrentXmlTest().getName())){
    		count = 0;
    		suiteName = context.getSuite().getName();
    		testName = context.getCurrentXmlTest().getName();
	    	List<XmlClass> xmlClasses = context.getCurrentXmlTest().getXmlClasses();
	    	for(XmlClass xmlClass : xmlClasses){
	    		String xmlClassName = xmlClass.getName();
	    		for(IMethodInstance m : methods) {
	        		String className = m.getInstance().getClass().getName();
	        		if(className.trim().equals(xmlClassName.trim())){
	        			newList.add(m);
	        		}
	        	}
	    	}
	    	
	    	tCount.set(count);
	    	tSuiteName.set(suiteName);
	    	tTestName.set(testName);
	    	tNewList.set(newList);
    	}
    	count++;
    	tCount.set(count);
    	if(tCount.get() > 2){
    		return new ArrayList<IMethodInstance>();
    	}
        return tNewList.get();
    }
}