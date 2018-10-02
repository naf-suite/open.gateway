package cloud.weixin.open.gateway;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="open.gateway")
public class Configure {
	
    private boolean useRedis;
    private boolean newMode = true;
    private Map<String,String> secret;
    private Map<String,String> appInfo;
    private String authBaseUrl;

	public boolean isUseRedis() {
		return useRedis;
	}

	public void setUseRedis(boolean useRedis) {
		this.useRedis = useRedis;
	}

	public Map<String, String> getSecret() {
		return secret;
	}

	public void setSecret(Map<String, String> secret) {
		this.secret = secret;
	}

	public Map<String, String> getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(Map<String, String> appInfo) {
		this.appInfo = appInfo;
	}

	public String getAuthBaseUrl() {
		return authBaseUrl;
	}

	public void setAuthBaseUrl(String authBaseUrl) {
		this.authBaseUrl = authBaseUrl;
	}

	public boolean isNewMode() {
		return newMode;
	}

	public void setNewMode(boolean newMode) {
		this.newMode = newMode;
	}

}
