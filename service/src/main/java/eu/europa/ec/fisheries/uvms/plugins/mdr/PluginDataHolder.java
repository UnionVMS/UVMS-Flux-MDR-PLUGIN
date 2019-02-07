/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.mdr;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author ankovi
 */
public abstract class PluginDataHolder {

    static final String PLUGIN_PROPERTIES_KEY = "mdr.properties";
    static final String PROPERTIES_KEY        = "settings.properties";
    static final String CAPABILITIES_KEY      = "capabilities.properties";

    private Properties mdrApplicaitonProperties;
    private Properties mdrProperties;
    private Properties mdrCapabilities;

    private final ConcurrentMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> capabilities = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getSettings() {
        return settings;
    }
    ConcurrentMap<String, String> getCapabilities() {
        return capabilities;
    }
    Properties getPluginApplicaitonProperties() {
        return mdrApplicaitonProperties;
    }
    void setPluginApplicaitonProperties(Properties mdrApplicaitonProperties) {
        this.mdrApplicaitonProperties = mdrApplicaitonProperties;
    }
    Properties getPluginProperties() {
        return mdrProperties;
    }
    void setPluginProperties(Properties mdrProperties) {
        this.mdrProperties = mdrProperties;
    }
    Properties getPluginCapabilities() {
        return mdrCapabilities;
    }
    void setPluginCapabilities(Properties mdrCapabilities) {
        this.mdrCapabilities = mdrCapabilities;
    }

}
