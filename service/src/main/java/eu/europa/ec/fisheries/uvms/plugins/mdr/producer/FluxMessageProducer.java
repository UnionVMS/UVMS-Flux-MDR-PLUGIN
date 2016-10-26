package eu.europa.ec.fisheries.uvms.plugins.mdr.producer;

import eu.europa.ec.fisheries.uvms.plugins.mdr.FluxParameters;
import eu.europa.ec.fisheries.uvms.plugins.mdr.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.constants.FluxConnectionConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Random;

@Stateless
@LocalBean
public class FluxMessageProducer {

    @EJB
    private StartupBean startUpBean;

    private Queue bridgeQueue                          = null;
    private HornetQConnectionFactory connectionFactory = null;
    private Connection connection                      = null;
    private Random randomGenerator                     = new Random();

    private static final Logger LOG = LoggerFactory.getLogger(PluginMessageProducer.class);

    /**
     * Sends a message from this Module to the flux Bridge queue.
     *
     * @param textMessage
     */
    public void sendMessageToFluxBridge(String textMessage){
        try {
            Session session           = getNewSession();
            TextMessage fluxMsgToSend = prepareMessage(textMessage, session);
            LOG.debug("-- Sending a message -to flux XEU Node- with ID : " + fluxMsgToSend.getStringProperty("BUSINESS_UUID"));
            getProducer(session, bridgeQueue).send(fluxMsgToSend);
            LOG.debug("-- Message sent succesfully.. OK..");
        } catch(Exception ex){
            LOG.error("Error while trying to send message to FLUX node.",ex);
        } finally {
            disconnectQueue();
        }
    }

    /**
     * Creates a new JMS Session and returns it;
     *
     * @return Session
     * @throws JMSException
     */
    private Session getNewSession() throws JMSException {
        try {
            loadRemoteQueueProperties();
        } catch (NamingException ex) {
            LOG.error("Error when open connection to JMS broker", ex);
            throw new JMSException(ex.getMessage());
        }
        if (connection == null) {
            LOG.debug("Opening connection to JMS broker");
            try {
                final FluxParameters fluxParameters = startUpBean.getFluxParameters();
                connection = connectionFactory.createConnection(fluxParameters.getProviderId(), fluxParameters.getProviderPwd());
                connection.start();
                return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException ex) {
                LOG.error("Error when open connection to JMS broker", ex);
                throw ex;
            }
        }
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Creates a MessageProducer for the given destination;
     *
     * @param session
     * @param destination
     * @return MessageProducer
     * @throws JMSException
     */
    private MessageProducer getProducer(Session session, Destination destination) throws JMSException {
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(60000L);
        return producer;
    }

    /**
     * Prepare the message for sending and set minimal set of attributes, required by FLUX TL JMS;
     *
     * @param textMessage
     * @return fluxMsg
     *
     * @throws JMSException
     * @throws DatatypeConfigurationException
     */
    private TextMessage prepareMessage(String textMessage, Session session) throws JMSException {
        TextMessage fluxMsg = session.createTextMessage();
        fluxMsg.setText(textMessage);
        fluxMsg.setStringProperty(FluxConnectionConstants.CONNECTOR_ID,  FluxConnectionConstants.CONNECTOR_ID_VAL);
        fluxMsg.setStringProperty(FluxConnectionConstants.FLUX_ENV_AD,   FluxConnectionConstants.FLUX_ENV_AD_VAL);
        fluxMsg.setIntProperty(FluxConnectionConstants.FLUX_ENV_TO,      FluxConnectionConstants.FLUX_ENV_TO_VAL);
        fluxMsg.setStringProperty(FluxConnectionConstants.FLUX_ENV_DF,   FluxConnectionConstants.FLUX_ENV_DF_VAL);
        fluxMsg.setStringProperty(FluxConnectionConstants.BUSINESS_UUID, createBusinessUUID());
        fluxMsg.setStringProperty(FluxConnectionConstants.FLUX_ENV_TODT, createStringDate());
        return fluxMsg;
    }

    /**
     * Creates the initial context (with the remote flux queue properties) and initializes the connectionFactory.
     *
     * @throws NamingException
     * @throws JMSException
     */
    private void loadRemoteQueueProperties() throws NamingException, JMSException {
        Properties contextProps = new Properties ();
        final FluxParameters fluxParameters = startUpBean.getFluxParameters();
        contextProps.put(Context.INITIAL_CONTEXT_FACTORY, FluxConnectionConstants.INITIAL_CONTEXT_FACTORY);
        contextProps.put(Context.PROVIDER_URL,            fluxParameters.getProviderUrl());
        contextProps.put(Context.SECURITY_PRINCIPAL,      fluxParameters.getProviderId());
        contextProps.put(Context.SECURITY_CREDENTIALS,    fluxParameters.getProviderPwd());
        Context context   = new InitialContext(contextProps);
        connectionFactory = (HornetQConnectionFactory) context.lookup(FluxConnectionConstants.REMOTE_CONNECTION_FACTORY);
        bridgeQueue       = (Queue) context.lookup(FluxConnectionConstants.JMS_QUEUE_BRIDGE);
    }

    private String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        gcal.setTime(new Date(System.currentTimeMillis() + 1000000));
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            return xgcal.toString();
        } catch (DatatypeConfigurationException | NullPointerException e) {
            LOG.error("Error occured while creating newXMLGregorianCalendar",e);
            return null;
        }
    }

    /**
     *  BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     * @return String
     */
    private String createBusinessUUID(){
        // Prepare unique Business Process ID
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("ddHHmmss");
        return FluxConnectionConstants.BUSINESS_PROCEDURE_PREFIX + format.format(curDate) + String.format("%02d", randomGenerator.nextInt(100));
    }

    /**
     * Disconnects from the queue this Producer is connected to.
     *
     */
    private void disconnectQueue() {
        try {
            connection.stop();
            connection.close();
            LOG.debug("Succesfully disconnected from FLUX BRIDGE Remote queue.");
        } catch (JMSException | NullPointerException e) {
            LOG.error("[ Error when stopping or closing JMS queue ] {}", e);
        }
    }

}
