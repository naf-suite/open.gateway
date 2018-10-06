package cloud.weixin.open.gateway.service;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cloud.weixin.open.gateway.Configure;
import io.netty.util.internal.StringUtil;

@Service
public class CacheService {
	// inject the actual template
    @Autowired
    private RedisTemplate<String, String> template;
    
    private final static Map<String, Token> memCache = new ConcurrentHashMap<String, Token>();
    
    //@Autowired
    private Configure config;

    @Autowired
    public CacheService(Configure config){
    	this.config = config;
    }

	public String getCache(String key) {
		if(config.isUseRedis()){
			String val = template.opsForValue().get(key);
			if(val != null && isExpireInRedis(key))//兼容已有使用模式
				return null;
			return val;
		}else{
			Token t = memCache.get(key);
			if(t != null && t.expire >= Calendar.getInstance().getTimeInMillis())
				return t.token;
			if(t != null)
				memCache.remove(key);
			return null;
		}
	}

	public void setCache(String key, String val) {
    	setCache(key, val, 0, false);
	}

	public void setCache(String key, String val, int expire_in) {
    	setCache(key, val, expire_in, false);
	}

	public void setCache(String key, String val, int expire, boolean compatible) {
		if(config.isUseRedis()){
			if(expire > 0){
				template.opsForValue().set(key, val, expire, TimeUnit.SECONDS);
				if(compatible)
					template.opsForValue().set(key+"time", String.valueOf(Calendar.getInstance().getTimeInMillis()));
				//template.expire(key, expire, TimeUnit.MILLISECONDS);
			}else{
				template.opsForValue().set(key, val);
			}
		}else{
			if(expire <= 0)
				expire = Integer.MAX_VALUE;
			else
				expire = expire*1000;
			Token t = new Token(key, val, Calendar.getInstance().getTimeInMillis() + expire);
			memCache.put(key, t);
		}
	}

	boolean isExpireInRedis(String key){
		if(config.isNewMode()) return false;
		
		//TODO: 兼容已有使用模式
		String time = template.opsForValue().get(key+"time");
		if(StringUtil.isNullOrEmpty(time) || !time.matches("\\d+"))
			return false;
		
		long now = Calendar.getInstance().getTimeInMillis();
		return now - Long.parseLong(time) > 7000*1000;
	}

	public static class Token{
		String appid;
		String token;
		long expire;
		
		Token(String appid, String token, long expire){
			this.appid = appid;
			this.token = token;
			this.expire = expire;
		}
	}

}
