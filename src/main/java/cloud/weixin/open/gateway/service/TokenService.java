package cloud.weixin.open.gateway.service;

public interface TokenService {
	String fetchToken(String appid, String orginfo, boolean force);
	String fetchTicket(String appid, String orginfo);
	String fetchOpenid(String appid, String code);
}
