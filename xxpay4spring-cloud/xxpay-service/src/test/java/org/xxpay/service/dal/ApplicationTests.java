package org.xxpay.service.dal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ApplicationTests {

	private MockMvc mvc;

	@Before
	/*public void setUp() throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(new ComputeController()).build();
	}*/

	@Test
	public void getHello() throws Exception {
	}

}
