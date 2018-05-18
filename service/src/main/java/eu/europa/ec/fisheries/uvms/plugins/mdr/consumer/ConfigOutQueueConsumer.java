package eu.europa.ec.fisheries.uvms.plugins.mdr.consumer;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractConsumer;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local
public class ConfigOutQueueConsumer extends AbstractConsumer {

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_CONFIG_RESPONSE;
    }
}