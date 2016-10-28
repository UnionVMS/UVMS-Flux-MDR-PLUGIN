package eu.europa.ec.fisheries.uvms.plugins.mdr.constants;

public final class MdrPluginConstants {

	private MdrPluginConstants(){}

	public static final String MDR_GROUP_ID_ARTIFACT_ID    = "eu.europa.ec.fisheries.uvms.plugins.mdr";
	public static final String MDR_GROUP_ID_ARTIFACT_ID_AC = "eu.europa.ec.fisheries.uvms.plugins.mdrPLUGIN_RESPONSE";

	public static final String CLIENT_ID_EV         = MDR_GROUP_ID_ARTIFACT_ID;
	public static final String SUBSCRIPTION_NAME_EV = MDR_GROUP_ID_ARTIFACT_ID;
	public static final String MESSAGE_SELECTOR_EV  = "ServiceName='"+MDR_GROUP_ID_ARTIFACT_ID+"'";

	public static final String CLIENT_ID_AC 		= MDR_GROUP_ID_ARTIFACT_ID_AC;
	public static final String SUBSCRIPTION_NAME_AC = MDR_GROUP_ID_ARTIFACT_ID_AC;
	public static final String MESSAGE_SELECTOR_AC  = "ServiceName='"+MDR_GROUP_ID_ARTIFACT_ID_AC+"'";

    public static final String DURABLE = "Durable";

}
