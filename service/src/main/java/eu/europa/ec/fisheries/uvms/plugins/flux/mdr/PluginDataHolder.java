/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.flux.mdr;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jojoha
 */
public abstract class PluginDataHolder {

    public final static String PLUGIN_PROPERTIES = "mdr.properties";
    public final static String PROPERTIES = "settings.properties";
    public final static String CAPABILITIES = "capabilities.properties";

    private Properties mdrApplicaitonProperties;
    private Properties mdrProperties;
    private Properties mdrCapabilities;

    private final ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SetReportMovementType> cachedMovement = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, String> getSettings() {
        return settings;
    }

    public ConcurrentHashMap<String, String> getCapabilities() {
        return capabilities;
    }

    public ConcurrentHashMap<String, SetReportMovementType> getCachedMovement() {
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
