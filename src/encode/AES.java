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
   //把byte[]数组转换为二进制字符串
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
      // System.out.println("this is binstr2");
     //System.out.print(binStr2);
       return binStr2;
       
   }

//byte[]转换字符串
public static String bytes2Hex(byte[] src) {
	 if (src == null || src.length <= 0) { 
	  return null; 
	 } 
	 
	 char[] res = new char[src.length * 2]; // 每个byte对应两个字符
	 final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	 for (int i = 0, j = 0; i < src.length; i++) {
	  res[j++] = hexDigits[src[i] >> 4 & 0x0f]; // 先存byte的高4位
	  res[j++] = hexDigits[src[i] & 0x0f]; // 再存byte的低4位
	 }
	 
	 return new String(res);
	}

//字符串转换byte[]
public static byte[] hexToBytes(String hexString) { 
	 if (hexString == null || hexString.equals("")) { 
	  return null; 
	 } 
	 
	 int length = hexString.length() / 2; 
	 char[] hexChars = hexString.toCharArray(); 
	 byte[] bytes = new byte[length]; 
	 String hexDigits = "0123456789abcdef";
	 for (int i = 0; i < length; i++) { 
	  int pos = i * 2; // 两个字符对应一个byte
	  int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
	  int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
	  if(h == -1 || l == -1) { // 非16进制字符
	   return null;
	  }
	  bytes[i] = (byte) (h | l); 
	 } 
	 return bytes; 
	}




}