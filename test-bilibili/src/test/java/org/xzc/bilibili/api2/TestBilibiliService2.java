package org.xzc.bilibili.api2;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestBilibiliService2.class })
@Configuration
public class TestBilibiliService2 {
	@Bean(name = "bs")
	public BilibiliService2 bs() {
		return new BilibiliService2();
	}

	@Autowired
	private BilibiliService2 bs;

	@Test
	public void testLogin() {
		boolean result = bs.login( "duruofeixh9@163.com", Utils.PASSWORD );
		System.out.println( bs.getDedeUserID() );
		System.out.println( bs.getSESSDATA() );
		System.out.println( result );
	}
}
