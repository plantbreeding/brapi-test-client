package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;

public interface TestSuiteRunner {

    /**
     * Run the tests
     *
     * @return Test report
     */
    public TestSuiteReport runTests(boolean allowAdditional, Boolean singleTest);

    /**
     * @return the id
     */
    public String getId();

    /**
     * @param id the id to set
     */
    public void setId(String id);
    /**
     * @return the url
     */
    public String getUrl();

    /**
     * @param url the url to set
     */
    public void setEp(Resource ep);

    void setTestCollection(TestCollection tc);

}
