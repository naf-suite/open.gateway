package cloud.weixin.open.gateway;

import javax.xml.bind.JAXBException;

import cloud.weixin.open.gateway.aes.AesException;
import cloud.weixin.open.gateway.aes.WXBizMsgCrypt;
import cloud.weixin.open.gateway.data.EncryptMsg;
import cloud.weixin.open.gateway.data.VerifyTicket;

public class XmlTest {

	public static void main(String[] args) throws JAXBException {
		String baseUrl = "http://www.jilinjobswx.cn/";
		System.out.println(baseUrl.substring(0, baseUrl.length()-1));
		// TODO Auto-generated method stub
		String xmlData = "<xml><AppId><![CDATA[wx4b4385cff773f3f9]]></AppId><Encrypt><![CDATA[BEh6x0T1fKKVT4IugdjjOBy+IUXNGk3VyIQatY9CnFO3dA2yvETb27DVrJzCZGbawqB5B39YF6/MeRlMO/0t35tCDe26Xc1nkeAt+DszB/B0kMetdCDnAa28/z/r1lKl+LhaxxC09et+1OfsUikOK5EYNfp/4GafjlhlVKAE4ZIZZD2880BlINPmNBsgNVSMloH+3o3iZk1cKd3aBPBq1EOYF3iazoygzykgWOymIMd5vPCtPrFxJaWGXpQ5pWZEjk38scmbnycOL0rIwjNwfgIOtsey6TZrNWY8NHcL2oNgfrZC4sesh55dm0gsGSYHvdpRH7L2Sic/G7amK6QA3T1qVsh3nxv80SqcrB6yrq5lxQtIDD+xfAQHmbxdJv6QTUTwe/DJe+clKBz/OQpr6Ls7jvtIPclf5ysWQ5EiS1FGWfCzPSf3r1NyatnOXlQ0cROLfYhP88fxSyimOEB0Sg==]]></Encrypt></xml>";
		EncryptMsg msg = EncryptMsg.fromXml(xmlData);
		System.out.println(msg.getAppId());
		System.out.println(msg.getEncrypt());
		
		String appId = msg.getAppId();
		String key = "BojYAIsgTGPFqgtphrqR4j2Lmz1IamBcPJgWGHvwKqw";
		String token = "huiantongxin123123";
		WXBizMsgCrypt pc;
		try {
			pc = new WXBizMsgCrypt(token, key, appId);
			String text = pc.decrypt(msg.getEncrypt());
			System.out.println(text);
			VerifyTicket ticket = VerifyTicket.fromXml(text);
			System.out.println(ticket.getTicket());
		} catch (AesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
