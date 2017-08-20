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
public class MchInfoTest {

	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		/*MockitoAnnotations.initMocks(this);

		mockMvc = MockMvcBuilders.standaloneSetup(
				new MgrController()
		).build();*/
	}

	@Test
	public void addMchInfo() throws Exception {


		JSONObject params = new JSONObject();
		params.put("mchId", "20001222");
		params.put("name", "骏易科技");
		params.put("type", "1");
		params.put("reqKey", "298332323231231313");
		params.put("resKey", "883435353534543534");




		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/mch/add").param("params", params.toJSONString()))
//				.andExpect(MockMvcResultMatchers.status().isOk())
//				.andExpect(MockMvcResultMatchers.model().attributeExists("user"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

	}

}
