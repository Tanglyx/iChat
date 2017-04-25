package encode;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

   public static final String KEY_ALGORITHM = "AES";
   public static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//默认的加密算法

   public static byte[] initSecretKey() {

       //返回生成指定算法密钥生成器的 KeyGenerator 对象
       KeyGenerator kg = null;
       try {
           kg = KeyGenerator.getInstance(KEY_ALGORITHM);
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
           return new byte[0];
       }
       //初始化此密钥生成器，使其具有确定的密钥大小
       //AES 要求密钥长度为 128
       kg.init(128);
       //生成一个密钥
       SecretKey  secretKey = kg.generateKey();
       return secretKey.getEncoded();
   }

   public static Key toKey(byte[] key){
       //生成密钥
       return new SecretKeySpec(key, KEY_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,Key key) throws Exception{
       return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
       return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
       //还原密钥
       Key k = toKey(key);
       return encrypt(data, k, cipherAlgorithm);
   }

   public static byte[] encrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
       //实例化
       Cipher cipher = Cipher.getInstance(cipherAlgorithm);
       //使用密钥初始化，设置为加密模式
       cipher.init(Cipher.ENCRYPT_MODE, key);
       //执行操作
       return cipher.doFinal(data);
   }

   public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
       return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] decrypt(byte[] data,Key key) throws Exception{
       return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
       //还原密钥
       Key k = toKey(key);
       return decrypt(data, k, cipherAlgorithm);
   }

   public static byte[] decrypt( byte[] data,Key key,String cipherAlgorithm) throws Exception{
       //实例化
       Cipher cipher = Cipher.getInstance(cipherAlgorithm);
       //使用密钥初始化，设置为解密模式
       cipher.init(Cipher.DECRYPT_MODE, key);
       //执行操作
       return cipher.doFinal(data);
   }

   public static String  showByteArray(byte[] data){
	   
	   if(null == data){
           return null;
       }
       StringBuilder sb = new StringBuilder("{");
       String binStr2="";
       for(byte b:data){
           sb.append(b).append(",");
       }
       String binStr3=binStr2;
       System.out.print(binStr3);
       sb.deleteCharAt(sb.length()-1);
       sb.append("}");
       return sb.toString();
       
   }
   //把数组转换为二进制字符串
public static String  toBinary(byte[] data){
	   
	   if(null == data){
           return null;
       }
      
       String binStr2="";
       for(byte b:data){  
    	   if(b>=0)
    	   {
    		   String binStr = Integer.toBinaryString(b);
    		   if(binStr.length()==8)
    			   binStr2=binStr2+binStr;
    		   else
    		   {
    			   int k=8-binStr.length();
    			   for(;k>0;k--)
    			   {
    				   binStr='0'+binStr;
    			   }
    			   binStr2=binStr2+binStr;
    		   }
    		  System.out.println(binStr);
    	   }
    	   else
    	   {
    		   String binStr = Integer.toBinaryString(b);
    		   //取后8位
    		   binStr=binStr.substring(24, 32);
    		   binStr2=binStr2+binStr;
    		  System.out.println(binStr);
    	   }
       }
      //System.out.print(binStr2);
       return binStr2;
       
   }

}