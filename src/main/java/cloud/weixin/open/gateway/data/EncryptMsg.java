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
public class EncryptMsg {

	@XmlElement(name = "AppId")
	private String appId;
	
	@XmlElement(name = "Encrypt")
	private String encrypt;
	
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
	
	public static EncryptMsg fromXml(String xmlData) throws JAXBException {
		JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(EncryptMsg.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		EncryptMsg ticket = (EncryptMsg) unmarshaller.unmarshal(new ByteArrayInputStream(xmlData.getBytes()));
		return ticket;
	}
	
}
