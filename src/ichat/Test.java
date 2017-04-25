package ichat;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Test {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		JSONObject jsonReply = new JSONObject();
		ArrayList<Map<String,String>> friendList = new ArrayList<>();//在线联系人列表
		JSONArray jsonA = new JSONArray();
	    
		Map<String, String> list = new HashMap<>();
		list.put("1", "hello");
		list.put("2", "world");
		list.put("3", "!");
		System.out.println("list:"+list);
		
		Iterator<String> friend = list.keySet().iterator();
		while(friend.hasNext()){
			Map<String,String> friendData = new HashMap<>();
			friendData.put("text", friend.next());
			System.out.println(friendData);
			jsonA.add(friendData);
			
		}
		
		System.out.println("frendList:"+friendList);
    	jsonReply.put("FriendList", jsonA);
		System.out.println(jsonA);
		System.out.println(jsonReply);
		
		JSONArray f = jsonReply.getJSONArray("FriendList");		
		Map<String, String> map = f.getJSONObject(0);
		System.out.println(map);
		System.out.println(map.get("text"));
		
	}
	
}
