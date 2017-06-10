package encode;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

   public static final String KEY_ALGORITHM = "AES";
   public static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//Ĭ�ϵļ����㷨

   public static byte[] initSecretKey() {

       //��������ָ���㷨��Կ�������� KeyGenerator ����
       KeyGenerator kg = null;
       try {
           kg = KeyGenerator.getInstance(KEY_ALGORITHM);
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
           return new byte[0];
       }
       //��ʼ������Կ��������ʹ�����ȷ������Կ��С
       //AES Ҫ����Կ����Ϊ 128
       kg.init(128);
       //����һ����Կ
       SecretKey  secretKey = kg.generateKey();
       return secretKey.getEncoded();
   }

   public static Key toKey(byte[] key){
       //������Կ
       return new SecretKeySpec(key, KEY_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,Key key) throws Exception{
       return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
       return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] encrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
       //��ԭ��Կ
       Key k = toKey(key);
       return encrypt(data, k, cipherAlgorithm);
   }

   public static byte[] encrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
       //ʵ����
       Cipher cipher = Cipher.getInstance(cipherAlgorithm);
       //ʹ����Կ��ʼ��������Ϊ����ģʽ
       cipher.init(Cipher.ENCRYPT_MODE, key);
       //ִ�в���
       return cipher.doFinal(data);
   }

   public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
       return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] decrypt(byte[] data,Key key) throws Exception{
       return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
   }

   public static byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
       //��ԭ��Կ
       Key k = toKey(key);
       return decrypt(data, k, cipherAlgorithm);
   }

   public static byte[] decrypt( byte[] data,Key key,String cipherAlgorithm) throws Exception{
       //ʵ����
       Cipher cipher = Cipher.getInstance(cipherAlgorithm);
       //ʹ����Կ��ʼ��������Ϊ����ģʽ
       cipher.init(Cipher.DECRYPT_MODE, key);
       //ִ�в���
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
   //��byte[]����ת��Ϊ�������ַ���
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
    		   //ȡ��8λ
    		   binStr=binStr.substring(24, 32);
    		   binStr2=binStr2+binStr;
    		  System.out.println(binStr);
    	   }
       }
      // System.out.println("this is binstr2");
     //System.out.print(binStr2);
       return binStr2;
       
   }

//byte[]ת���ַ���
public static String bytes2Hex(byte[] src) {
	 if (src == null || src.length <= 0) { 
	  return null; 
	 } 
	 
	 char[] res = new char[src.length * 2]; // ÿ��byte��Ӧ�����ַ�
	 final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	 for (int i = 0, j = 0; i < src.length; i++) {
	  res[j++] = hexDigits[src[i] >> 4 & 0x0f]; // �ȴ�byte�ĸ�4λ
	  res[j++] = hexDigits[src[i] & 0x0f]; // �ٴ�byte�ĵ�4λ
	 }
	 
	 return new String(res);
	}

//�ַ���ת��byte[]
public static byte[] hexToBytes(String hexString) { 
	 if (hexString == null || hexString.equals("")) { 
	  return null; 
	 } 
	 
	 int length = hexString.length() / 2; 
	 char[] hexChars = hexString.toCharArray(); 
	 byte[] bytes = new byte[length]; 
	 String hexDigits = "0123456789abcdef";
	 for (int i = 0; i < length; i++) { 
	  int pos = i * 2; // �����ַ���Ӧһ��byte
	  int h = hexDigits.indexOf(hexChars[pos]) << 4; // ע1
	  int l = hexDigits.indexOf(hexChars[pos + 1]); // ע2
	  if(h == -1 || l == -1) { // ��16�����ַ�
	   return null;
	  }
	  bytes[i] = (byte) (h | l); 
	 } 
	 return bytes; 
	}




}