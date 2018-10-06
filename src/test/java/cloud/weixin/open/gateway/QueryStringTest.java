package cloud.weixin.open.gateway;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

public class QueryStringTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String,String> query = new LinkedHashMap<>();
		query.put("access_token", "tttttt");
		query.put("appid", "iiii");
		query.put("next_openid", "nnnn");

	    String uri = query.entrySet().stream().map(entry->{
    		try {
				return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("URL编码错误");
				return entry.getKey() + "=" + entry.getValue();
			}
    	}).reduce(new BinaryOperator<String>() {
			
			@Override
			public String apply(String a, String b) {
				// TODO Auto-generated method stub
				return a + "&" + b;
			}
		}).get();
	    
	    System.out.println(uri);

	}

}
