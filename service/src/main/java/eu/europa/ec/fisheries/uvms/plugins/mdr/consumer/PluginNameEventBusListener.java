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
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.plugins.mdr.OracleMessagePoster;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.MdrPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.FluxBridgeProducer;
import eu.europa.ec.fisheries.uvms.plugins.mdr.service.ExchangePluginServiceBean;
import java.sql.SQLException;
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

import eu.europa.ec.fisheries.uvms.plugins.mdr.service.MdrPluginConfigurationCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR,          propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR,        propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR,            propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
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

    @EJB
    private OracleMessagePoster oracleMsgPoster;

    @EJB
    private ExchangePluginServiceBean exchangeService;

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
        if (StringUtils.isNotEmpty(strRequest)) {
            try {
                final Map<String, String> msgProps = createMessagePropertiesMap();
                if(oracleMsgPoster.isActive()){
                    log.info("[INFO] Going to search for codelist in Oracle DB.");
                    String response = oracleMsgPoster.postMessageToOracleDb(strRequest, mdrPluginConfigurationCache.getNationCode(), msgProps.get(BUSINESS_UUID));
                    exchangeService.sendFLUXMDRResponseMessageToExchange(response);
                } else {
                    bridgeProducer.sendModuleMessageWithProps(strRequest, null, msgProps);
                }
            } catch (MessageException e) {
                log.error("[ERROR] Error while trying to send message to bridge queue : ", e);
            } catch (SQLException e) {
                log.error("[ERROR] Error while trying to call stored procedure in oracle db : ", e);
            }
        } else {
            log.warn("[WARN] The request to be sent to Bridge cannot be empty! Not sending anything..");
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
