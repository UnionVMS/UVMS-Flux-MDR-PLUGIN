package eu.europa.ec.fisheries.uvms.plugins.flux.mdr.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginFault;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.constants.MdrPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.service.PluginService;

@MessageDriven(mappedName = MdrPluginConstants.FLUX_MESSAGE_IN_QUEUE, activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = MdrPluginConstants.DESTINATION_TYPE_QUEUE),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = MdrPluginConstants.QUEUE_FLUX_RECEIVER_NAME) })
public class FluxMdrMessageListener implements MessageListener {

	final static Logger LOG = LoggerFactory.getLogger(FluxMdrMessageListener.class);

	@EJB
	ExchangeService exchangeService;
	@EJB
	StartupBean startupService;

	@EJB
	PluginService fluxService;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onMessage(Message inMessage) {

		LOG.info(">>>>>>>>>>>>>>> Got message in Flux MDR plugin queue.");

		TextMessage textMessage = (TextMessage) inMessage;

		try {
			LOG.info("Sending Message -  Response from Flux - to MDR Plugin.");
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