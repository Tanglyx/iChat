package encode;
import java.io.BufferedReader;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class Kmain {
	public static void main(String[] args) throws Exception {
			// TODO Auto-generated method stub
			HashMap<String, Object> map = RSA.getKeys();
			//生成RSA公钥和私钥
			RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
			RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
			
			//模
			String modulus = publicKey.getModulus().toString();
			//公钥指数
			String public_exponent = publicKey.getPublicExponent().toString();
			//私钥指数
			String private_exponent = privateKey.getPrivateExponent().toString();
			//生产AES秘钥
			byte[] key = AES.initSecretKey();
		    System.out.println("AES的key："+AES.showByteArray(key));
		    Key k = AES.toKey(key); //生成秘钥
		       
		    //将AES秘钥转化为二进制   
		    String binKey=AES.toBinary(key);
		    int binLen=binKey.length();
		    System.out.println("生成的aes二进制秘钥"+binKey);
		    System.out.println("生成的aes二进制秘钥长度"+binLen);
					       
		     String mingorigin=binKey;  //AES的二进制表示
			//String mingorigin = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678";
			//未使用的temp变量用来对比解密后的数据文件
			//String temp     = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678";
			//切分要传输的秘钥,把128位秘钥切分为两个秘钥
			int len=mingorigin.length();
			//显示原始数据的长度
			System.out.println("要加密字符串长度为："+len);
			String s1="";
			String s2="";
			if (len>117)
			{
				s1 = mingorigin.substring(0,117);
				s2 = mingorigin.substring(117,len);
				//使用模和指数生成公钥和私钥
				RSAPublicKey pubKey = RSA.getPublicKey(modulus, public_exponent);
				RSAPrivateKey priKey = RSA.getPrivateKey(modulus, private_exponent);
				System.out.println("RSA公钥："+pubKey);
				
				//RSA公钥对AES秘钥key加密后的密文
				String mi1 = RSA.encryptByPublicKey(s1, pubKey);
				String mi2 = RSA.encryptByPublicKey(s2, pubKey);
				System.err.println("RSA公钥加密AES秘钥后："+mi1+mi2);
				//用RSA私钥解密后的明文
				String mingsecond1 = RSA.decryptByPrivateKey(mi1, priKey);
				String mingsecond2 = RSA.decryptByPrivateKey(mi2, priKey);
				String mingsecond=mingsecond1+mingsecond2;
				System.err.println("用RSA私钥解密后的数据为："+mingsecond);
			}
			//输入秘钥小于128位的处理方式输出错误
			else 
			{
				System.out.println("要加密字符串长度为："+len);
				System.err.println("秘钥小于128位");
			}
				
			
			
			//AES加密解密
//			   byte[] key = AES.initSecretKey();
//		       System.out.println("key："+AES.showByteArray(key));
//		       Key k = AES.toKey(key); //生成秘钥
//		       
//		       
//		       String binKey=AES.toBinary(key);
//		       int binLen=binKey.length();
//		       System.out.println("生成的aes二进制秘钥"+binKey);
//		       System.out.println("生成的aes二进制秘钥长度"+binLen);
		       
		       
		       System.out.println("****************对数据AES加密*****************");
		       String data ="要加密的数据";
		       System.out.println("加密前数据: string:"+data);
		       System.out.println("加密前数据: byte[]:"+AES.showByteArray(data.getBytes()));
		       System.out.println();
		       byte[] encryptData = AES.encrypt(data.getBytes(), k);//数据加密
		       System.out.println("加密后数据: byte[]:"+AES.showByteArray(encryptData));
//		       System.out.println("加密后数据: hexStr:"+Hex.encodeHexStr(encryptData));
		       System.out.println();
		       byte[] decryptData = AES.decrypt(encryptData, k);//数据解密
		       System.out.println("解密后数据: byte[]:"+AES.showByteArray(decryptData));
		       System.out.println("解密后数据: string:"+new String(decryptData));
		}
}

