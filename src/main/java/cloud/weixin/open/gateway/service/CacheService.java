package cloud.weixin.open.gateway.service;

public interface CacheService {
	String getCache(String key);
	void setCache(String key, String val);
	void setCache(String key, String val, long expire, boolean compatible);
}
