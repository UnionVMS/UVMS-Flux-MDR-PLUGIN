package eu.europa.ec.fisheries.uvms.plugins.flux.mdr.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetMdrPluginRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.producer.FluxMessageProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.producer.PluginMessageProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS, activationConfig = {
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = ExchangeModelConstants.EVENTBUS_NAME)
})
public class PluginNameEventBusListener implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    PluginService service;

    @EJB
    PluginMessageProducer messageProducer;

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
                	LOG.info("Got Request in MDR PLUGIN for : " +fluxMdrRequest.getRequest()+ " Entity.");
                	strRequest = fluxMdrRequest.getRequest();
                	break;
                default:
                    LOG.error("Not supported method : "+" Class : "+request.getClass()+". Method : "+request.getMethod());
                    break;
            }

        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in mdr " + startup.getRegisterClassName() + " ]", e);
        }
        
        // TODO: Test this producer!!
		//fluxMsgProducer.sendMessageToFluxBridge(strRequest);

    }
}
