/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;


import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.service.ExchangePluginServiceBean;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = MessageConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME)
})
@Slf4j
public class FluxMdrRemoteMessageConsumer implements MessageListener {

    @EJB
    private ExchangePluginServiceBean exchangeService;

    @Override
    public void onMessage(Message inMessage) {
        log.info("\n\n\t[[NEW MESSAGE]] Got message (from Flux) in Flux MDR plugin queue! \n\n");
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            log.info("[START] Sending Message to Exchange Module..");
            exchangeService.sendFLUXMDRResponseMessageToExchange(textMessage.getText());
            log.info("[END] Message sent successfully back to Exchange Module..");
        } catch (JMSException e1) {
            log.error("[ERROR] Error while marshalling Flux Response.", e1);
        }
    }
}