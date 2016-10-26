package eu.europa.ec.fisheries.uvms.plugins.mdr.constants;

public class FluxConnectionConstants {

    private FluxConnectionConstants(){}

    public static final String DESTINATION_TYPE_QUEUE              = "javax.jms.Queue";
    public static final String FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE    = "jms/queue/mdrin";
    public static final String FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME    = "mdrin";
    public static final String NETTY_CONNECTOR_CLASS_NAME          = "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory";

    public static final String FLUX_CONNECTION_FACTORY = "java:/FluxFactory";

	// ConnectionFactory details
    public static final String REMOTE_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";

    public static final String AUCNOWLEDGE_TYPE_AUTO = "Auto-acknowledge";
    public static final String URL_PKG_PREFIXES        = "org.jboss.naming.remote.client";
    public static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    public static final String PROVIDER_URL_WITH_PORT = "http-remoting://10.155.0.10:8180";
    public static final String PROVIDER_URL_WITH_PORT_VERS_2 = "host=http-remoting://10.155.0.10;port=8180";
    public static final String SECURITY_PRINCIPAL_ID   = "fluxq";
    public static final String SECURITY_PRINCIPAL_PWD  = "testpassword";

    public static final String CLIENT_EJB_CONTEXT = "jboss.naming.client.ejb.context";

    // Queue details
    public static final String JMS_QUEUE_BRIDGE = "jms/queue/bridge";

    // Message details
    public static final int NUMBER_OF_MESSAGES = 10;
    public static final String CONNECTOR_ID     = "CONNECTOR_ID";
    public static final String CONNECTOR_ID_VAL = "JMS Business AP1";
    public static final String FLUX_ENV_AD      = "AD";
    public static final String FLUX_ENV_AD_VAL  = "XEU";
    public static final String FLUX_ENV_DF      = "DF";
    public static final String FLUX_ENV_DF_VAL  = "urn:xeu:ec:fisheries:flux-bl:mdm-list:1";
    public static final String FLUX_ENV_TO      = "TO";
    public static final int FLUX_ENV_TO_VAL     = 60;
    public static final String FLUX_ENV_TODT    = "TODT";
    // Business procedure signature
    public static final String BUSINESS_PROCEDURE_PREFIX = "BP";
    public static final String BUSINESS_UUID             = "BUSINESS_UUID";

    public static final String ALL_CONFIG = "java.naming.factory.initial="+INITIAL_CONTEXT_FACTORY+
            ";java.naming.provider.url="+ PROVIDER_URL_WITH_PORT +
            ";java.naming.security.principal="+SECURITY_PRINCIPAL_ID+
            ";java.naming.security.credentials="+SECURITY_PRINCIPAL_PWD;


}