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

package org.openqa.selenium.testing.drivers;

import static org.openqa.selenium.remote.CapabilityType.HAS_NATIVE_EVENTS;

import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BrowserToCapabilities {
  public static DesiredCapabilities of(Browser browser) {
    if (browser == null) {
      return null;
    }

    DesiredCapabilities caps;

    switch (browser) {
      case chrome:
        caps = new DesiredCapabilities(BrowserType.CHROME,"", Platform.ANY);
        break;

      case ff:
        caps = new DesiredCapabilities(BrowserType.FIREFOX,"", Platform.ANY);
        String property =
          System.getProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
        boolean useMarionette = property != null && Boolean.parseBoolean(property);
        caps.setCapability(FirefoxDriver.MARIONETTE, useMarionette);
        break;

      case htmlunit:
        caps = DesiredCapabilities.htmlUnit();
        break;

      case ie:
        caps = new DesiredCapabilities(BrowserType.IE,"", Platform.WINDOWS);
        break;

      case operablink:
        caps = new DesiredCapabilities(BrowserType.OPERA,"", Platform.ANY);
        break;

      case safari:
        caps = new DesiredCapabilities(BrowserType.SAFARI,"", Platform.MAC);
        break;

      default:
        throw new RuntimeException("Cannot determine browser config to use");
    }

    String version = System.getProperty("selenium.browser.version");
    if (version != null) {
      caps.setVersion(version);
    }

    caps.setCapability(HAS_NATIVE_EVENTS,
        Boolean.getBoolean("selenium.browser.native_events"));

    return caps;
  }
}
