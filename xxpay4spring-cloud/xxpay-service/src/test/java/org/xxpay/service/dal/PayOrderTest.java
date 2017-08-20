package org.xxpay.service.dal;

import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class PayOrderTest {

	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		/*MockitoAnnotations.initMocks(this);

		mockMvc = MockMvcBuilders.standaloneSetup(
				new PayOrderController()
		).build();*/
	}

	@Test
	public void payOrder() throws Exception {

//		mockMvc.perform(MockMvcRequestBuilders.post("/secured/resources/incoterms/create").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
//				.content("{\"code\" : \"EXW\", \"description\" : \"code exw\", \"locationQualifier\" : \"DEPARTURE\"}".getBytes()))
//				//.andDo(print())
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("id.value").exists())
//				.andExpect(jsonPath("id.value").value("6305ff33-295e-11e5-ae37-54ee7534021a"))
//				.andExpect(jsonPath("code").value("EXW"));

		//params={mchId:20001222,mchOrderNo:3132121212,channelId:"alipay-app",amount:100,currentcy:"cny",clientIp:"127.0.0.1",device:"app",notifyUrl:"www.baidu.com",sign:1111111}

		JSONObject params = new JSONObject();
		params.put("mchId", "20001222");
		params.put("mchOrderNo", "20001222");
		params.put("channelId", "20001222");
		params.put("amount", "20001222");
		params.put("currentcy", "20001222");
		params.put("clientIp", "20001222");
		params.put("device", "20001222");
		params.put("notifyUrl", "20001222");
		params.put("sign", "20001222");




		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/pay_order").param("params", params.toJSONString()))
//				.andExpect(MockMvcResultMatchers.status().isOk())
//				.andExpect(MockMvcResultMatchers.model().attributeExists("user"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

	}

}
