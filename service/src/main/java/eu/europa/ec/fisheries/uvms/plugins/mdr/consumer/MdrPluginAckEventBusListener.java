/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginFault;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.RegisterServiceResponse;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.UnregisterServiceResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.MdrPluginConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR,  propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR, propertyValue = MdrPluginConstants.SUBSCRIPTION_NAME_AC),
        @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR, propertyValue = MdrPluginConstants.CLIENT_ID_AC),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR, propertyValue = MdrPluginConstants.MESSAGE_SELECTOR_AC)
})
public class MdrPluginAckEventBusListener implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(MdrPluginAckEventBusListener.class);

    @EJB
    private StartupBean startupService;

    @Override
    public void onMessage(Message inMessage) {
        LOG.info("Eventbus listener for mdr at selector: {} got a message", startupService.getPluginResponseSubscriptionName());
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            ExchangeRegistryBaseRequest request = tryConsumeRegistryBaseRequest(textMessage);
            if (request == null) {
                PluginFault fault = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginFault.class);
                handlePluginFault(fault);
                return;
            }
            switch (request.getMethod()) {
                case REGISTER_SERVICE:
                    RegisterServiceResponse registerResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, RegisterServiceResponse.class);
                    startupService.setWaitingForResponse(Boolean.FALSE);
                    setRegistrationResponse(request, registerResponse);
                    break;
                case UNREGISTER_SERVICE:
                    UnregisterServiceResponse unregisterResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, UnregisterServiceResponse.class);
                    setUnRegistrationResponse(unregisterResponse);
                    break;
                default:
                    LOG.error("Not supported method");
                    break;
            }

        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("Error when receiving message in mdr ", e);
        }
    }

    private void setUnRegistrationResponse(UnregisterServiceResponse unregisterResponse) {
        switch (unregisterResponse.getAck().getType()) {
            case OK:
                LOG.info("Unregister OK");
                break;
            case NOK:
                LOG.info("Unregister NOK");
                break;
            default:
                LOG.error("[ Ack type not supported ] ");
                break;
        }
    }

    private void setRegistrationResponse(ExchangeRegistryBaseRequest request, RegisterServiceResponse registerResponse) {
        switch (registerResponse.getAck().getType()) {
            case OK:
                LOG.info("Register OK");
                startupService.setIsRegistered(Boolean.TRUE);
                break;
            case NOK:
                LOG.info("Register NOK: " + registerResponse.getAck().getMessage());
                startupService.setIsRegistered(Boolean.FALSE);
                break;
            default:
                LOG.error("[ Type not supperted: ]" + request.getMethod());
        }
    }

    private void handlePluginFault(PluginFault fault) {
        LOG.error(startupService.getPluginResponseSubscriptionName() + " received fault " + fault.getCode() + " : " + fault.getMessage());
    }

    private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
        try {
            return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Error trying to consume BaseRequest",e);
            return null;
        }
    }
}
