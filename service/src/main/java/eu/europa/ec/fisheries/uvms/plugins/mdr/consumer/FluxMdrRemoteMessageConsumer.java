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
import java.io.StringWriter;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

@MessageDriven(mappedName = MessageConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME)
})
@Slf4j
public class FluxMdrRemoteMessageConsumer implements MessageListener {

    @EJB
    private ExchangeService exchangeService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        log.info("\n\n\t[[NEW MESSAGE]] Got message (from Flux) in Flux MDR plugin queue! \n\n");
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            log.info("[START] Sending Message to Exchange Module..");
            exchangeService.sendFLUXMDRResponseMessageToExchange(textMessage.getText());
            log.info("[END] Message sent successfully back to Exchange Module..");
            log.debug("\nMESSAGE CONTENT : \n\n " + prettyPrintXml(textMessage.getText()) + "\n\n");
        } catch (JMSException e1) {
            log.error("[ERROR] Error while marshalling Flux Response.", e1);
        }
    }

    /**
     * Pretty Print XML String
     *
     * @param xml
     * @return formattedXml
     */
    public static String prettyPrintXml(String xml) {
        StringWriter sw = new StringWriter();
        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
        } catch (Exception e) {
            log.error("Error pretty printing xml:\n" + xml, e);
        }
        String formattedStr = sw.toString();
        return formattedStr.substring(0, formattedStr.length() > 10000 ? 10000 : formattedStr.length()-1) + ".......";
    }
}