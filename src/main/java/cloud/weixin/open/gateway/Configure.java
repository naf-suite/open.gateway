package cloud.weixin.open.gateway;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="open.gateway")
public class Configure {
	
    private boolean useRedis;
    private boolean newMode = true;
    private Map<String,AppInfo> apps;
    private String authBaseUrl;

	public boolean isUseRedis() {
		return useRedis;
	}

	public void setUseRedis(boolean useRedis) {
		this.useRedis = useRedis;
	}

	public Map<String, AppInfo> getApps() {
		return apps;
	}

	public void setApps(Map<String, AppInfo> apps) {
		this.apps = apps;
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
	
	public String getAppSecret(String appid) {
		if(apps != null && apps.containsKey(appid)) {
			return apps.get(appid).getSecret();
		}
		return null;
	}
	public String getAppKey(String appid) {
		if(apps != null && apps.containsKey(appid)) {
			return apps.get(appid).getSecret();
		}
		return null;
	}
	
	public static class AppInfo {
		private String secret;
		private String key;
		private String token;
		
		public String getSecret() {
			return secret;
		}
		public void setSecret(String secret) {
			this.secret = secret;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		
	}

}
