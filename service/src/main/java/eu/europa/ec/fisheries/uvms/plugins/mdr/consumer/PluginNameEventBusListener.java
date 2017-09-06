/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.BUSINESS_UUID;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.CONNECTOR_ID;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.CONNECTOR_ID_VAL;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_AD;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_AD_VAL;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_AR;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_AR_VAL;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_DF;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_DF_VAL;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_TO;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_TODT;
import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.FLUX_ENV_TO_VAL;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetMdrPluginRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.MdrPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.FluxBridgeProducer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

@MessageDriven(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS, activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType",          propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = MdrPluginConstants.DURABLE),
        @ActivationConfigProperty(propertyName = "destinationType",        propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = "destination",            propertyValue = ExchangeModelConstants.EVENTBUS_NAME),
        @ActivationConfigProperty(propertyName = "subscriptionName",       propertyValue = MdrPluginConstants.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = "clientId",               propertyValue = MdrPluginConstants.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = "messageSelector",        propertyValue = MdrPluginConstants.MESSAGE_SELECTOR_EV)
})
@Slf4j
public class PluginNameEventBusListener implements MessageListener {

    @EJB
    StartupBean startup;

    @EJB
    FluxBridgeProducer bridgeProducer;


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        log.debug("Eventbus listener for mdr (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        String strRequest = null;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            switch (request.getMethod()) {
                case SET_MDR_REQUEST:
                    SetMdrPluginRequest fluxMdrRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetMdrPluginRequest.class);
                    log.info("\n\nGot Request in MDR PLUGIN : " + fluxMdrRequest.getRequest());
                    strRequest = fluxMdrRequest.getRequest();
                    break;
                default:
                    log.error("Not supported method : " + " Class : " + request.getClass() + ". Method : " + request.getMethod());
                    break;
            }
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            log.error("[ Error when receiving message in mdr plugin" + startup.getRegisterClassName() + " ]", e);
        }

        if (strRequest != null) {
            try {
                bridgeProducer.sendModuleMessage(strRequest, null, createMessagePropertiesMap());
            } catch (MessageException e) {
                log.error("Error while trying to send message to bridge queue : ", e);
            }
        } else {
            log.warn("-->>> The request to be sent to Bridge cannot be empty! Not sending anything..");
        }
    }

    private Map<String, String> createMessagePropertiesMap() {
        return new HashMap<String, String>(){{
            put(CONNECTOR_ID, CONNECTOR_ID_VAL);
            put(FLUX_ENV_AD, FLUX_ENV_AD_VAL);
            put(FLUX_ENV_TO, FLUX_ENV_TO_VAL);
            put(FLUX_ENV_DF, FLUX_ENV_DF_VAL);
            put(BUSINESS_UUID, createBusinessUUID());
            put(FLUX_ENV_TODT, createStringDate());
            put(FLUX_ENV_AR, FLUX_ENV_AR_VAL);
        }};
    }


    private String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        Date newDate = DateUtils.addHours(new Date(), 3);
        gcal.setTime(newDate);
        XMLGregorianCalendar xgcal;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            return xgcal.toString();
        } catch (DatatypeConfigurationException | NullPointerException e) {
            log.error("Error occured while creating newXMLGregorianCalendar", e);
            return null;
        }
    }

    /**
     * BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     *
     * @return randomUUID
     */
    private String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }

/*    private void printMessageProperties(TextMessage fluxMsg) throws JMSException {
        log.info("Prepared message (For Flux TL) with the following properties  : \n\n");
        int i = 0;
        Enumeration propertyNames = fluxMsg.getPropertyNames();
        String propName;
        while (propertyNames.hasMoreElements()) {
            i++;
            propName = (String) propertyNames.nextElement();
            log.info(i + ". " + propName + " : " + fluxMsg.getStringProperty(propName));
        }*/

}
