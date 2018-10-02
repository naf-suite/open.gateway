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
public class VerifyTicket {

	@XmlElement(name = "AppId")
	private String appId;
	
	@XmlElement(name = "CreateTime")
	private String createTime;
	
	@XmlElement(name = "InfoType")
	private String infoType;

	@XmlElement(name = "ComponentVerifyTicket")
	private String ticket;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getInfoType() {
		return infoType;
	}
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	
	public static VerifyTicket fromXml(String xmlData) throws JAXBException {
		JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(VerifyTicket.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		VerifyTicket ticket = (VerifyTicket) unmarshaller.unmarshal(new ByteArrayInputStream(xmlData.getBytes()));
		return ticket;
	}

}
