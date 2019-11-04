package org.iomedia.framework;

import java.util.Arrays;

import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;

public class Assert extends Assertion {
	
	Reporting Reporter;
	
	public Assert(Reporting Reporter) {
		WebDriverFactory driverFactory = new WebDriverFactory(); 
		this.Reporter = Reporter == null ? driverFactory.getReporting().get() : Reporter;
	}
	
	String getValues(Object data){
		if(data.getClass().isArray()){
        	return Arrays.deepToString((Object[]) data);
		}
		else{
			return String.valueOf(data);
		}
	}
	
	@Override
	public void onAssertSuccess(IAssert assertCommand) {
		if(assertCommand.getMessage() != null){
			String expectedMessage = assertCommand.getExpected() == null ? "NULL" : getValues(assertCommand.getExpected());
			String actualMessage =  assertCommand.getActual() == null ? "NULL" : getValues(assertCommand.getActual());
			Reporter.log(assertCommand.getMessage(), expectedMessage, actualMessage, "Pass");
		}
	}
	 
	@Override
	public void onAssertFailure(IAssert assertCommand, AssertionError ex) {
		if(assertCommand.getMessage() != null){
			String expectedMessage = assertCommand.getExpected() == null ? "NULL" : getValues(assertCommand.getExpected());
			String actualMessage =  assertCommand.getActual() == null ? "NULL" : getValues(assertCommand.getActual());
			if(ex.getCause() == null){
				Reporter.log(assertCommand.getMessage(), expectedMessage, actualMessage + "<Br/>" + ex.toString(), "Fail");
			}
			else{
				Reporter.log(assertCommand.getMessage(), expectedMessage, actualMessage + "<Br/>" + ex.getCause().toString(), "Fail");
			}
		}
	}

	@Override
	public void onAssertFailure(IAssert assertCommand) {
		if(assertCommand.getMessage() != null){
			String expectedMessage = assertCommand.getExpected() == null ? "NULL" : getValues(assertCommand.getExpected());
			String actualMessage =  assertCommand.getActual() == null ? "NULL" : getValues(assertCommand.getActual());
			Reporter.log(assertCommand.getMessage(), expectedMessage, actualMessage , "Fail");
		}
	}
}
