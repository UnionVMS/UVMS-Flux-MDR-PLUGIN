/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetMdrPluginRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.MdrPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.FluxBridgeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

import static eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants.*;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR,          propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR,        propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR,             propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR,       propertyValue = MdrPluginConstants.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR,               propertyValue = MdrPluginConstants.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR,        propertyValue = MdrPluginConstants.MESSAGE_SELECTOR_EV)
})
@Slf4j
public class PluginNameEventBusListener implements MessageListener {

    @EJB
    private StartupBean startup;

    @EJB
    private FluxBridgeProducer bridgeProducer;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        log.debug("Eventbus listener for mdr (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        String strRequest = null;
        SetMdrPluginRequest fluxMdrRequest = null;
        try {
            PluginBaseRequest request = JAXBUtils.unMarshallMessage(textMessage.getText(), PluginBaseRequest.class);
            switch (request.getMethod()) {
                case SET_MDR_REQUEST:
                    fluxMdrRequest = JAXBUtils.unMarshallMessage(textMessage.getText(), SetMdrPluginRequest.class);
                    log.debug("\n [INFO] Got Request in MDR PLUGIN : " + fluxMdrRequest.getRequest());
                    log.info("[INFO] Going to send sync request to : {}", fluxMdrRequest.getFr());
                    strRequest = fluxMdrRequest.getRequest();
                    break;
                default:
                    log.error("Not supported method : " + " Class : " + request.getClass() + ". Method : " + request.getMethod());
                    break;
            }
        } catch (NullPointerException | JMSException | JAXBException e) {
            log.error("[ Error when receiving message in mdr plugin" + startup.getRegisterClassName() + " ]", e);
        }
        if (strRequest != null) {
            try {
                bridgeProducer.sendModuleMessageWithProps(strRequest, null, createMessagePropertiesMap(fluxMdrRequest.getFr()));
            } catch (MessageException e) {
                log.error("Error while trying to send message to bridge queue : ", e);
            }
        } else {
            log.warn("-->>> The request to be sent to Bridge cannot be empty! Not sending anything..");
        }
    }

    private Map<String, String> createMessagePropertiesMap(final String fr) {
        return new HashMap<String, String>(){{
            put(CONNECTOR_ID, CONNECTOR_ID_VAL);
            put(FLUX_ENV_AD, FLUX_ENV_AD_VAL);
            put(FLUX_ENV_TO, FLUX_ENV_TO_VAL);
            put(FLUX_ENV_DF, FLUX_ENV_DF_VAL);
            put(BUSINESS_UUID, createBusinessUUID());
            put(FLUX_ENV_TODT, createStringDate());
            put(FLUX_ENV_AR, FLUX_ENV_AR_VAL);
            put(FLUX_ENV_FR, fr);
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
