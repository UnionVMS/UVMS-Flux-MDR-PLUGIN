/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.mdr.service;

import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.PluginMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;

/**
 *
 * @author akovi
 */
@LocalBean
@Stateless
public class ExchangeService {

	final static Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

	@EJB
	StartupBean startupBean;

	@EJB
	PluginMessageProducer producer;

	public void sendFLUXMDRResponseMessageToExchange(String fluxMdrResponseText) {
		try {
			String text = ExchangeModuleRequestMapper.createFluxMdrSyncEntityResponse(fluxMdrResponseText, "flux");
			String messageId = producer.sendModuleMessage(text, ModuleQueue.EXCHANGE);
			LOG.info("FluxMdrResponse Sent to Exchange module. MessageID :" + messageId);
		} catch (ExchangeModelMarshallException e) {
			LOG.error("Couldn't map Mdr Entity to SetFLUXMDRSyncMessageResponse.", e);
		} catch (JMSException e) {
			LOG.error("Couldn't send SetFLUXMDRSyncMessageResponse to Exchange module", e);
		}
	}
}
