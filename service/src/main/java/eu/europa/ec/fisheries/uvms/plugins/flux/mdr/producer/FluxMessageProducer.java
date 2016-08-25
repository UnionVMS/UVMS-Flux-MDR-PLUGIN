package eu.europa.ec.fisheries.uvms.plugins.flux.mdr.producer;

import eu.europa.ec.fisheries.uvms.plugins.flux.mdr.constants.FluxConnectionConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Queue             bridgeQueue              = null;
    private HornetQConnectionFactory connectionFactory = null;
    private Connection        connection               = null;
    private Session           session                  = null;

    private Random randomGenerator = new Random();

    final static Logger LOG = LoggerFactory.getLogger(PluginMessageProducer.class);

    /**
     * Sends a message from this Module to the flux Bridge queue.
     *
     * @param textMessage
     */
    public void sendMessageToFluxBridge(String textMessage){
        try {
        	loadRemoteQueueProperties();
            connectQueue();
            TextMessage fluxMsgToSend = prepareMessage(textMessage);
            MessageProducer producer  = session.createProducer(bridgeQueue);
            System.out.println("Sending a message -to flux XEu Node- with ID : " + fluxMsgToSend.getStringProperty("BUSINESS_UUID"));
            producer.send(fluxMsgToSend);

            // TODO : Create test consumer; take this off when not needed!! MDB should be configured instead
            /*JMSContext createContext = connectionFactory.createContext(FluxConnectionConstants.SECURITY_PRINCIPAL_ID, FluxConnectionConstants.SECURITY_PRINCIPAL_PWD,
                    JMSContext.AUTO_ACKNOWLEDGE);
            JMSConsumer testConsumer = createContext.createConsumer(bridgeQueue);
            Message message = testConsumer.receive(5000);*/
            System.out.println("Receiving message from bridge.....");

        } catch(Exception ex){
        	LOG.error("Error while trying to send message to FLUX node.",ex);
		} finally {
        	disconnectQueue();
        }
    }

    /**
     * Creates the initial context (with the remote flux queue properties) and initializes the connectionFactory.
     *
     * @throws NamingException
     * @throws JMSException
     */
    private void loadRemoteQueueProperties() throws NamingException, JMSException {
        Properties contextProps = new Properties ();
        contextProps.put(Context.INITIAL_CONTEXT_FACTORY, FluxConnectionConstants.INITIAL_CONTEXT_FACTORY);
        contextProps.put(Context.PROVIDER_URL,            FluxConnectionConstants.PROVIDER_URL);
        contextProps.put(Context.SECURITY_PRINCIPAL,      FluxConnectionConstants.SECURITY_PRINCIPAL_ID);
        contextProps.put(Context.SECURITY_CREDENTIALS,    FluxConnectionConstants.SECURITY_PRINCIPAL_PWD);
        //contextProps.put(Context.URL_PKG_PREFIXES,        FluxConnectionConstants.URL_PKG_PREFIXES);
        //contextProps.put(FluxConnectionConstants.CLIENT_EJB_CONTEXT, true);
        Context context   = new InitialContext(contextProps);
        connectionFactory = (HornetQConnectionFactory) context.lookup(FluxConnectionConstants.REMOTE_CONNECTION_FACTORY);
        bridgeQueue       = (Queue) context.lookup(FluxConnectionConstants.JMS_JNDI_QUEUE);
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
    private TextMessage prepareMessage(String textMessage)
            throws JMSException, DatatypeConfigurationException {
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

    private String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        gcal.setTime(new Date(System.currentTimeMillis() + 1000000));
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return xgcal.toString();
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

    private void connectQueue() throws JMSException {
        connection = connectionFactory.createConnection(FluxConnectionConstants.SECURITY_PRINCIPAL_ID, FluxConnectionConstants.SECURITY_PRINCIPAL_PWD);
        session    = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

    private void disconnectQueue() {
        try {
            connection.stop();
            connection.close();
        } catch (JMSException | NullPointerException e) {
            LOG.error("[ Error when stopping or closing JMS queue ] {}", e);
        }
    }
}
