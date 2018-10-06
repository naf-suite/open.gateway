package cloud.weixin.open.gateway.data;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthMsg {

	@XmlElement(name = "AppId")
	private String appId;
	
	@XmlElement(name = "Encrypt")
	private String encrypt;
	
	@XmlElement(name = "InfoType")
	private String infoType;
	
	@XmlElement(name = "AuthorizerAppid")
	private String authorizerAppid;

	@XmlElement(name = "AuthorizationCode")
	private String authorizationCode;

	@XmlElement(name = "AuthorizationCodeExpiredTime")
	private int authorizationCodeExpiredTime = 0;
	
	@XmlElement(name = "PreAuthCode")
	private String preAuthCode;
	

	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}
	public String getInfoType() {
		return infoType;
	}
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	public String getAuthorizerAppid() {
		return authorizerAppid;
	}
	public void setAuthorizerAppid(String authorizerAppid) {
		this.authorizerAppid = authorizerAppid;
	}
	public String getAuthorizationCode() {
		return authorizationCode;
	}
	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
	public int getAuthorizationCodeExpiredTime() {
		return authorizationCodeExpiredTime;
	}
	public void setAuthorizationCodeExpiredTime(int authorizationCodeExpiredTime) {
		this.authorizationCodeExpiredTime = authorizationCodeExpiredTime;
	}
	public String getPreAuthCode() {
		return preAuthCode;
	}
	public void setPreAuthCode(String preAuthCode) {
		this.preAuthCode = preAuthCode;
	}
	
	public static AuthMsg fromXml(String xmlData) throws JAXBException {
		JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(AuthMsg.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		AuthMsg ticket = (AuthMsg) unmarshaller.unmarshal(new ByteArrayInputStream(xmlData.getBytes()));
		return ticket;
	}
	
}
