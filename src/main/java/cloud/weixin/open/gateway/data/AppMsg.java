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
public class AppMsg {

	@XmlElement(name = "ToUserName")
	private String toUserName;
	
	@XmlElement(name = "FromUserName")
	private String fromUserName;
	
	@XmlElement(name = "MsgType")
	private String msgType;
	
	@XmlElement(name = "Content")
	private String content;

	@XmlElement(name = "MsgId")
	private String msgId;

	@XmlElement(name = "CreateTime")
	private int createTime = 0;

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	
	public static AppMsg fromXml(String xmlData) throws JAXBException {
		JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(AppMsg.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		AppMsg ticket = (AppMsg) unmarshaller.unmarshal(new ByteArrayInputStream(xmlData.getBytes()));
		return ticket;
	}
	
}
