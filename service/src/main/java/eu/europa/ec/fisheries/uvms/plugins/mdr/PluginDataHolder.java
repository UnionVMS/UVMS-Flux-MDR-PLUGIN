/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.mdr;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author jojoha
 */
public abstract class PluginDataHolder {

    public static final String PLUGIN_PROPERTIES_KEY = "mdr.properties";
    public static final String PROPERTIES_KEY        = "settings.properties";
    public static final String CAPABILITIES_KEY      = "capabilities.properties";

    private Properties mdrApplicaitonProperties;
    private Properties mdrProperties;
    private Properties mdrCapabilities;

    private final ConcurrentMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SetReportMovementType> cachedMovement = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getSettings() {
        return settings;
    }
    public ConcurrentMap<String, String> getCapabilities() {
        return capabilities;
    }
    public ConcurrentMap<String, SetReportMovementType> getCachedMovement() {
        return cachedMovement;
    }
    public Properties getPluginApplicaitonProperties() {
        return mdrApplicaitonProperties;
    }
    public void setPluginApplicaitonProperties(Properties mdrApplicaitonProperties) {
        this.mdrApplicaitonProperties = mdrApplicaitonProperties;
    }
    public Properties getPluginProperties() {
        return mdrProperties;
    }
    public void setPluginProperties(Properties mdrProperties) {
        this.mdrProperties = mdrProperties;
    }
    public Properties getPluginCapabilities() {
        return mdrCapabilities;
    }
    public void setPluginCapabilities(Properties mdrCapabilities) {
        this.mdrCapabilities = mdrCapabilities;
    }

}
