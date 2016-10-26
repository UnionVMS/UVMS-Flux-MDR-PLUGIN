package eu.europa.ec.fisheries.uvms.plugins.mdr.constants;

public class FluxConnectionConstants {

    private FluxConnectionConstants(){}

    public static final String DESTINATION_TYPE_QUEUE                = "javax.jms.Queue";
    public static final String FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE      = "jms/queue/mdrin";
    public static final String FLUX_MDR_REMOTE_MESSAGE_IN_QUEUE_NAME = "mdrin";
    public static final String FLUX_CONNECTION_FACTORY               = "java:/FluxFactory";

	// ConnectionFactory details
    public static final String REMOTE_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    public static final String INITIAL_CONTEXT_FACTORY   = "org.jboss.naming.remote.client.InitialContextFactory";

    // Queue details
    public static final String JMS_QUEUE_BRIDGE = "jms/queue/bridge";

    // Message details
    public static final int NUMBER_OF_MESSAGES  = 10;
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



}