package cloud.weixin.open.gateway.data;

public class PreAuthCode extends WeixinMsg {
	private String pre_auth_code;
	private int expires_in;
	
	public int getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}
	public String getPre_auth_code() {
		return pre_auth_code;
	}
	public void setPre_auth_code(String pre_auth_code) {
		this.pre_auth_code = pre_auth_code;
	}
}
