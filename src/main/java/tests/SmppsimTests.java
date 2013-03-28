package tests;
import junit.framework.*;

public class SmppsimTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(SmppsimBindTests.class);
		suite.addTestSuite(SmppsimSubmitSmTests.class);
		suite.addTestSuite(SmppsimDeliverSmTests.class);
		suite.addTestSuite(SmppsimEnquireLinkTests.class);
		suite.addTestSuite(SmppsimQuerySmTests.class);
		suite.addTestSuite(SmppsimCancelSmTests.class);
		suite.addTestSuite(SmppsimReplaceSmTests.class);
		suite.addTestSuite(SmppsimSubmitMultiTests.class);
		return suite;
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}