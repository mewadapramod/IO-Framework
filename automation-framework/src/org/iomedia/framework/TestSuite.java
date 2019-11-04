package org.iomedia.framework;

public class TestSuite {
	public static ThreadLocal<String> sTestSuiteName = new ThreadLocal<String>(){
		@Override public String initialValue() {
			return null;
		}	
	};
	
	public String getTestSuiteName() {
		return sTestSuiteName.get();
	}

	public void setTestSuiteName(String driverType) {
		sTestSuiteName.set(driverType);
	}
}