// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.Statement;
import org.openqa.selenium.htmlunit.runners.BrowserVersionClassRunner;
import org.openqa.selenium.htmlunit.runners.StandardsMode;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * The custom runner <code>BrowserRunner</code> implements browser parameterized
 * tests. When running a test class, instances are created for the
 * cross-product of the test methods and {@link BrowserVersion}s.
 *
 * For example, write:
 * <pre>
 * &#064;RunWith(BrowserRunner.class)
 * public class SomeTest extends WebTestCase {
 *
 *     &#064;Test
 *     public void test() {
 *         getBrowserVersion();// returns the currently used BrowserVersion
 *     }
 * }
 * </pre>
 */
public class BrowserRunner extends Suite {

    private final ArrayList<Runner> runners_ = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param klass the test case class
     * @throws Throwable If an exception occurs
     */
    public BrowserRunner(final Class<WebTestCase> klass) throws Throwable {
        super(klass, Collections.<Runner>emptyList());

        if (BrowserVersionClassRunner.containsTestMethods(klass)) {
            final Set<String> browsers = WebDriverTestCase.getBrowsersProperties();
            if (WebDriverTestCase.class.isAssignableFrom(klass)) {
                if (browsers.contains("chrome")) {
                    runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.CHROME, true));
                }
                if (browsers.contains("ff78")) {
                    runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.FIREFOX_78, true));
                }
                else if (browsers.contains("ff")) {
                    runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.FIREFOX, true));
                }
                if (browsers.contains("ie")) {
                    runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.INTERNET_EXPLORER, true));
                }
            }

            if (browsers.contains("hu-chrome")) {
                runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.CHROME, false));
            }
            if (browsers.contains("hu-ff78")) {
                runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.FIREFOX_78, false));
            }
            if (browsers.contains("hu-ff")) {
                runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.FIREFOX, false));
            }
            if (browsers.contains("hu-ie")) {
                runners_.add(new BrowserVersionClassRunner(klass, BrowserVersion.INTERNET_EXPLORER, false));
            }
        }
        else {
            throw new IllegalStateException("No @Test method found");
        }
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        return childrenInvoker(notifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final Filter filter) throws NoTestsRemainException {
        boolean atLeastOne = false;
        for (final Runner runner : getChildren()) {
            try {
                if (runner instanceof Filterable) {
                    ((Filterable) runner).filter(filter);
                    atLeastOne = true;
                }
            }
            catch (final NoTestsRemainException e) {
                // nothing
            }
        }

        if (!atLeastOne) {
            throw new NoTestsRemainException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Runner> getChildren() {
        return runners_;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
     */
    public static final String EMPTY_DEFAULT = "~InTerNal_To_BrowSeRRunNer#@$";

    /**
     * Browser.
     */
    public enum Browser {
        /** Latest version of Chrome. */
        CHROME,

        /** Internet Explorer 11. */
        IE,

        /** All versions of Firefox. */
        FF,

        /** Firefox 78. */
        FF78
    }

    /**
     * Allows to express the expected alerts (i.e. the messages passed to the
     * window.alert function) for the different browsers for a unit test.
     * Expected alerts can be retrieved within a unit test with {@link SimpleWebTestCase#getExpectedAlerts()}
     * (resp. {@link WebDriverTestCase#getExpectedAlerts}) to be compared with the actual alerts but most of the time
     * utility functions like {@link SimpleWebTestCase#loadPageWithAlerts(String)}
     * (resp. {@link WebDriverTestCase#loadPageWithAlerts2(String)}) are used which do it
     * after having loaded the page.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Alerts {

        /**
         * Alerts that is used for all browsers (if defined, the other values are ignored).
         * @return the alerts
         */
        String[] value() default { EMPTY_DEFAULT };

        /**
         * Alerts for Internet Explorer 11.
         * @return the alerts
         */
        String[] IE() default { EMPTY_DEFAULT };

        /**
         * Alerts for any Firefox, it can be overridden by specific FF version.
         * @return the alerts
         */
        String[] FF() default { EMPTY_DEFAULT };

        /**
         * Alerts for Firefox 78. If not defined, {@link #FF()} is used.
         * @return the alerts
         */
        String[] FF78() default { EMPTY_DEFAULT };

        /**
         * Alerts for latest Chrome.
         * @return the alerts
         */
        String[] CHROME() default { EMPTY_DEFAULT };

        /**
         * The default alerts, if nothing more specific is defined.
         * @return the alerts
         */
        String[] DEFAULT() default { EMPTY_DEFAULT };
    }

    /**
     * Same as {@link Alerts} but only in {@code Standards Mode}.
     *
     * It is typically used with {@link StandardsMode}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface AlertsStandards {

        /**
         * Alerts that is used for all browsers (if defined, the other values are ignored).
         * @return the alerts
         */
        String[] value() default { EMPTY_DEFAULT };

        /**
         * Alerts for any Internet Explorer, it can be overridden by specific IE version.
         * @return the alerts
         */
        String[] IE() default { EMPTY_DEFAULT };

        /**
         * Alerts for any Firefox, it can be overridden by specific FF version.
         * @return the alerts
         */
        String[] FF() default { EMPTY_DEFAULT };

        /**
         * Alerts for Firefox 78. If not defined, {@link #FF()} is used.
         * @return the alerts
         */
        String[] FF78() default { EMPTY_DEFAULT };

        /**
         * Alerts for latest Chrome.
         * @return the alerts
         */
        String[] CHROME() default { EMPTY_DEFAULT };

        /**
         * The default alerts, if nothing more specific is defined.
         * @return the alerts
         */
        String[] DEFAULT() default { EMPTY_DEFAULT };
    }

    /**
     * Marks a test as not yet working for a particular browser (default value is all).
     * This will cause a failure to be considered as success and a success as failure forcing
     * us to remove this annotation when a feature has been implemented even unintentionally.
     * @see Browser
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface NotYetImplemented {

        /**
         * The browsers with which the case is not yet implemented.
         * @return the browsers
         */
        Browser[] value() default {
          Browser.IE, Browser.FF, Browser.CHROME
        };

        /**
         * @return an optional reason.
         */
        String reason() default "";
    }

    /**
     * Indicates that the test runs manually in a real browser but not when using WebDriver to drive the browser.
     * @see Browser
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface BuggyWebDriver {

        /**
         * The browsers with which the case is failing.
         * @return the browsers
         */
        Browser[] value() default {
          Browser.IE, Browser.FF, Browser.CHROME
        };
    }

    /**
     * The number of tries that test will be executed.
     * The test will fail if and only if all trials failed.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Tries {

        /**
         * The value.
         * @return the value
         */
        int value() default 1;
    }

}
