package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginFault;
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

	final static Logger LOG = LoggerFactory.getLogger(FluxMdrRemoteMessageConsumer.class);

	@EJB
    ExchangeService exchangeService;

	@EJB
	StartupBean startupService;

	@EJB
    PluginService fluxService;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onMessage(Message inMessage) {

		LOG.info("\n\n>>>>>>>>>>>>>>> Got message in Flux MDR plugin queue <<<<<<<<<<<<<<<<<<<\n\n");
		TextMessage textMessage = (TextMessage) inMessage;

		try {
			LOG.info("Sending Message -  Response from Flux - to Exchange Module.");
			exchangeService.sendFLUXMDRResponseMessageToExchange(textMessage.getText());
			LOG.info(">>>>>>>>>>>>>>> Message sent successfully back to Exchange Module.");
		} catch (JMSException e1) {
			LOG.error("Error while marshalling Flux Response.");
			e1.printStackTrace();
		}

	
	}

	private void handlePluginFault(PluginFault fault) {
		LOG.error(startupService.getPluginResponseSubscriptionName() + " received fault " + fault.getCode() + " : "
				+ fault.getMessage());
	}

	private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
		try {
			return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
		} catch (ExchangeModelMarshallException e) {
			return null;
		}
	}
}