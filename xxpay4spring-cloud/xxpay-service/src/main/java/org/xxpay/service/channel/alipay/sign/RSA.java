
package org.xxpay.service.channel.alipay.sign;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA{
	
	public static final String  SIGN_ALGORITHMS = "SHA1WithRSA";
	
	/**
	* RSA签名
	* @param content 待签名数据
	* @param privateKey 商户私钥
	* @param input_charset 编码格式
	* @return 签名值
	*/
	public static String sign(String content, String privateKey, String input_charset)
	{
        try 
        {
        	PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec(Base64.decode(privateKey) );
        	KeyFactory keyf 				= KeyFactory.getInstance("RSA");
        	PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update( content.getBytes(input_charset) );

            byte[] signed = signature.sign();
            
            return Base64.encode(signed);
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        }
        
        return null;
    }
	
	/**
	* RSA验签名检查
	* @param content 待签名数据
	* @param sign 签名值
	* @param ali_public_key 支付宝公钥
	* @param input_charset 编码格式
	* @return 布尔值
	*/
	public static boolean verify(String content, String sign, String ali_public_key, String input_charset)
	{
		try 
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] encodedKey = Base64.decode(ali_public_key);
	        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

		
			java.security.Signature signature = java.security.Signature
			.getInstance(SIGN_ALGORITHMS);
		
			signature.initVerify(pubKey);
			signature.update(content.getBytes(input_charset) );
		
			boolean bverify = signature.verify(Base64.decode(sign) );
			return bverify;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	* 解密
	* @param content 密文
	* @param private_key 商户私钥
	* @param input_charset 编码格式
	* @return 解密后的字符串
	*/
	public static String decrypt(String content, String private_key, String input_charset) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);

        InputStream ins = new ByteArrayInputStream(Base64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        //rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;

            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }

            writer.write(cipher.doFinal(block));
        }

        return new String(writer.toByteArray(), input_charset);
    }

	
	/**
	* 得到私钥
	* @param key 密钥字符串（经过base64编码）
	* @throws Exception
	*/
	public static PrivateKey getPrivateKey(String key) throws Exception {

		byte[] keyBytes;
		
		keyBytes = Base64.decode(key);
		
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		
		return privateKey;
	}

	/*public static void main(String[] args) {
		System.out.println(RSA.sign(
				"_input_charset=utf-8&app_pay=Y&body=VV直播下单测试&it_b_pay=60m&notify_url=${pay.url}/notify/aliPayNotifyRes.htm&out_trade_no=1494684553763&partner=2088521108562983&payment_type=1&seller_id=2088521108562983&service=alipay.wap.create.direct.pay.by.user&show_url=http://www.xxpay.org&subject=null&total_fee=0.01",
				"MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKOuhY6LYZBMiRrvRw2s71CjPW7OPCvapjZdJJNPt5x471E3kDCb9A7kQTyqFIIVcUNKDlKRNztKBhhlxAiI7d95UkN5pAMK+XUItjyA9nj9cqK/ajHjwC4AlIRUZhlsPvj6lt1Oj1Kf1sNDJMM/NZL9IR8EXr7HlIsCjJNVHFPvAgMBAAECgYBY1S7G3f5lQiRm6dW2JlT7fpyotmURp+jtOD/Rc0JDOZ8ohO9McldSfa6qLeRTdS+zRU3goc9H7jTAqPprZ2UxNTUwJ4uMh+2bCtXkvUPwoWF4fb095xGtEUdbKMFkv+yKpCQASrjDhqzVq5xD/uc796wd7HOHwr8xPNOrKKSGAQJBANfgcaiIyFeo8KK4vIUWtqSiqLgG6gp7ABx2WpMWX3wsjbiCBQGVbJbnFcCkB+bofCuKYj7BGLjEEqc3c6y+Ph8CQQDCGprovroKw09dOzqFFPpkMrZvkOpO2e+RhDhhLYq2e5lRLVePtB/ZX2iy2yKQEp/7VWbNFzobqYR6KPXEH5AxAkBx3oD1XhkXLBSqMHm4Ve/HTcljMLp5BsJbQQ6rsUxyimnC3kpXuILL4l61+4/ze8Qrj1YdNeudYkdYjsZkYwEPAkBoWedIEylvmdqz86CdZU7LyVu9FPpyk8WwxJWO4O3+9unQ84BseFjbAukFprupGuo5M4uF3OPXdUYMarLd0l4xAkAMjec0KXp15a93I5y/vfIEAMQ+CQj/LwOxyAM6tTSPVGJHu70pXFQVWtwY+ycMuzbxTdSLuQYxITstHnV3mu76", "utf-8"));
	}*/

}
