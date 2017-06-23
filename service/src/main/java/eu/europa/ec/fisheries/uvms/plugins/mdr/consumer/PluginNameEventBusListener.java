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
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.MdrPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.FluxMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS, activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType",          propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = MdrPluginConstants.DURABLE),
        @ActivationConfigProperty(propertyName = "destinationType",        propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = "destination",            propertyValue = ExchangeModelConstants.EVENTBUS_NAME),
        @ActivationConfigProperty(propertyName = "subscriptionName",       propertyValue = MdrPluginConstants.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = "clientId",               propertyValue = MdrPluginConstants.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = "messageSelector",        propertyValue = MdrPluginConstants.MESSAGE_SELECTOR_EV)
})
public class PluginNameEventBusListener implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    StartupBean startup;

    @EJB
    FluxMessageProducer fluxMsgProducer;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.debug("Eventbus listener for mdr (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        String strRequest = null;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            switch (request.getMethod()) {
                case SET_MDR_REQUEST:
                    SetMdrPluginRequest fluxMdrRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetMdrPluginRequest.class);
                    LOG.info("\n\nGot Request in MDR PLUGIN : " + fluxMdrRequest.getRequest());
                    strRequest = fluxMdrRequest.getRequest();
                    break;
                default:
                    LOG.error("Not supported method : " + " Class : " + request.getClass() + ". Method : " + request.getMethod());
                    break;
            }
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in mdr plugin" + startup.getRegisterClassName() + " ]", e);
        }

        if (strRequest != null) {
            fluxMsgProducer.sendMessageToFluxBridge(strRequest);
        } else {
            LOG.warn("-->>> The request to be sent to Bridge cannot be empty! Not sending anything..");
        }
    }

}
