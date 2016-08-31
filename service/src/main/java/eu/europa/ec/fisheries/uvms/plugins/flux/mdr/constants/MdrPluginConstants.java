package eu.europa.ec.fisheries.uvms.plugins.flux.mdr.constants;

public final class MdrPluginConstants {

	private MdrPluginConstants(){}

	public static final String FLUX_MESSAGE_IN_QUEUE    = "java:/jms/queue/ERSMDRPlugin";
	public static final String CONNECTION_TYPE          = "javax.jms.MessageListener";
	public static final String DESTINATION_TYPE_QUEUE   = "javax.jms.Queue";
	public static final String QUEUE_FLUX_RECEIVER_NAME = "ERSMDRPlugin";
	public static final String CONNECTION_FACTORY       = "jms/RemoteConnectionFactory";
}
