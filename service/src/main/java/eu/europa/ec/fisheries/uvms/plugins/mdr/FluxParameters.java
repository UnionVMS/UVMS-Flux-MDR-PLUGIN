package eu.europa.ec.fisheries.uvms.plugins.mdr;

/**
 * Created by kovian on 26/10/2016.
 */
public class FluxParameters {

    private String providerUrl;
    private String providerId;
    private String providerPwd;


    public void populate(String providerUrl, String providerId, String providerPwd){
        this.providerId = providerId;
        this.providerUrl = providerUrl;
        this.providerPwd = providerPwd;
    }

    public String getProviderUrl() {
        return providerUrl;
    }
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }
    public String getProviderId() {
        return providerId;
    }
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getProviderPwd() {
        return providerPwd;
    }
    public void setProviderPwd(String providerPwd) {
        this.providerPwd = providerPwd;
    }
}
