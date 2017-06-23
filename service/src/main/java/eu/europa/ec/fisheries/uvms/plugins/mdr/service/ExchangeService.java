/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.mdr.service;

import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.plugins.mdr.producer.PluginMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;

/**
 * EJB needed for sending messages to Exchange queue
 *
 * @author akovi
 */
@LocalBean
@Stateless
public class ExchangeService {

	final static Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

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
