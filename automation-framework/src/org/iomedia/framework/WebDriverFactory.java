package org.iomedia.framework;

import org.iomedia.framework.Driver.HashMapNew;
import org.openqa.selenium.WebDriver;

public class WebDriverFactory {
	public static ThreadLocal<ThreadLocal<String>> sDriverType = new ThreadLocal<ThreadLocal<String>>(){
		@Override public ThreadLocal<String> initialValue() {
			return new ThreadLocal<String>() {
				@Override public String initialValue() {
					return null;
				}
			};
		}	
	};
	
	public static ThreadLocal<ThreadLocal<WebDriver>> sDriver = new ThreadLocal<ThreadLocal<WebDriver>>() {
		@Override public ThreadLocal<WebDriver> initialValue() {
			return new ThreadLocal<WebDriver>() {
				@Override public WebDriver initialValue() {
					return null;
				}
			};
		}
	};
	
	public static ThreadLocal<ThreadLocal<HashMapNew>> sDict = new ThreadLocal<ThreadLocal<HashMapNew>>() {
		@Override public ThreadLocal<HashMapNew> initialValue() {
			return new ThreadLocal<HashMapNew>() {
				@Override public HashMapNew initialValue() {
					return null;
				}
			};
		}
	};
	
	public static ThreadLocal<ThreadLocal<HashMapNew>> sEnv = new ThreadLocal<ThreadLocal<HashMapNew>>(){
		@Override public ThreadLocal<HashMapNew> initialValue() {
			return new ThreadLocal<HashMapNew>() {
				@Override public HashMapNew initialValue() {
					return null;
				}
			};
		}
	};
	
	public static ThreadLocal<ThreadLocal<HashMapNew>> sTestDetails = new ThreadLocal<ThreadLocal<HashMapNew>>(){
		@Override public ThreadLocal<HashMapNew> initialValue() {
			return new ThreadLocal<HashMapNew>(){
				@Override public HashMapNew initialValue() {
					return null;
				}	
			};
		}	
	};
	
	public static ThreadLocal<ThreadLocal<Reporting>> sReporting = new ThreadLocal<ThreadLocal<Reporting>>(){
		@Override public ThreadLocal<Reporting> initialValue() {
			return new ThreadLocal<Reporting>(){
				@Override public Reporting initialValue() {
					return null;
				}
			};
		}
	};
	
	public static ThreadLocal<ThreadLocal<Assert>> sAssert = new ThreadLocal<ThreadLocal<Assert>>(){
		@Override public ThreadLocal<Assert> initialValue() {
			return new ThreadLocal<Assert>(){
				@Override public Assert initialValue() {
					return null;
				}
			};
		}
	};
	
	public static ThreadLocal<ThreadLocal<SoftAssert>> sSoftAssert = new ThreadLocal<ThreadLocal<SoftAssert>>(){
		@Override public ThreadLocal<SoftAssert> initialValue() {
			return new ThreadLocal<SoftAssert>() {
				@Override public SoftAssert initialValue() {
					return null;
				}
			};
		}
	};
	
	public ThreadLocal<WebDriver> getDriver() {
		return sDriver.get();
	}

	public void setDriver(ThreadLocal<WebDriver> driver) {
		sDriver.set(driver);
	}
	
	public ThreadLocal<HashMapNew> getDictionary() {
		return sDict.get();
	}

	public void setDictionary(ThreadLocal<HashMapNew> dict) {
		sDict.set(dict);
	}
	
	public ThreadLocal<HashMapNew> getEnvironment() {
		return sEnv.get();
	}

	public void setEnvironment(ThreadLocal<HashMapNew> env) {
		sEnv.set(env);
	}
	
	public ThreadLocal<HashMapNew> getTestDetails() {
		return sTestDetails.get();
	}

	public void setTestDetails(ThreadLocal<HashMapNew> testDetails) {
		sTestDetails.set(testDetails);
	}
	
	public ThreadLocal<Reporting> getReporting() {
		return sReporting.get();
	}

	public void setReporting(ThreadLocal<Reporting> reporting) {
		sReporting.set(reporting);
	}
	
	public ThreadLocal<Assert> getAssert() {
		return sAssert.get();
	}

	public void setAssert(ThreadLocal<Assert> _assert) {
		sAssert.set(_assert);
	}
	
	public ThreadLocal<SoftAssert> getSoftAssert() {
		return sSoftAssert.get();
	}

	public void setSoftAssert(ThreadLocal<SoftAssert> _assert) {
		sSoftAssert.set(_assert);
	}
	
	public ThreadLocal<String> getDriverType() {
		return sDriverType.get();
	}

	public void setDriverType(ThreadLocal<String> driverType) {
		sDriverType.set(driverType);
	}
}