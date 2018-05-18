package eu.europa.ec.fisheries.uvms.plugins.mdr.service;

import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractConfigSettingsBean;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.plugins.mdr.consumer.ConfigOutQueueConsumer;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;


@Singleton
@Startup
@Slf4j
public class MdrPluginConfigurationCache extends AbstractConfigSettingsBean {

    @EJB
    private ConfigOutQueueConsumer consumer;

    @EJB
    private PluginToExchangeProducer producer;

    @Override
    protected ConfigOutQueueConsumer getConsumer() {
        return consumer;
    }

    @Override
    protected AbstractProducer getProducer() {
        return producer;
    }

    @Override
    protected String getModuleName() {
        return "mdr-plugin";
    }

    public String getNationCode() {
        String fluxNationCode = getSingleConfig("flux_local_nation_code");
        fluxNationCode = StringUtils.isNotEmpty(fluxNationCode) ? fluxNationCode : "please_set_flux_local_nation_code_in_config_settings_table";
        return fluxNationCode;
    }

}