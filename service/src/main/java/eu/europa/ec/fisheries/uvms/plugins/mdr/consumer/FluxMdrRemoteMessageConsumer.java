/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants;
import eu.europa.ec.fisheries.uvms.plugins.mdr.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.plugins.mdr.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = FluxConnectionConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME,  activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = FluxConnectionConstants.DESTINATION_TYPE_QUEUE),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = FluxConnectionConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE),
		@ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = FluxConnectionConstants.FLUX_CONNECTION_FACTORY)
})
public class FluxMdrRemoteMessageConsumer implements MessageListener {

	private static final Logger LOG = LoggerFactory.getLogger(FluxMdrRemoteMessageConsumer.class);

	@EJB
    ExchangeService exchangeService;

	@EJB
	StartupBean startupService;

	@EJB
    PluginService fluxService;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onMessage(Message inMessage) {

		LOG.trace("\n\n3.9.\tEFFORT_ZONE>>>>>>>>>>>>>>> Got message (from Flux) in Flux MDR plugin queue <<<<<<<<<<<<<<<<<<<\n\n");
	    TextMessage textMessage = (TextMessage) inMessage;
		try {
			LOG.trace("Sending Message [Response from Flux]  to Exchange Module.");
			if(LOG.isDebugEnabled()){
				LOG.debug("\n\n\nMESSAGE CONTENT : \n\n "+ textMessage.getText() + "\n\n\n");
			}
			exchangeService.sendFLUXMDRResponseMessageToExchange(textMessage.getText());
			LOG.trace(">>>>>>>>>>>>>>> Message sent successfully back to Exchange Module.");
		} catch (JMSException e1) {
			LOG.error("Error while marshalling Flux Response.",e1);
		}
	}


	private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
		try {
			return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
		} catch (ExchangeModelMarshallException e) {
			LOG.error("Errorr when tryConsumeRegistryBaseRequest",e);
			return null;
		}
	}
}