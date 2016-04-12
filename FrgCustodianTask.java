package com.zhiye.emaster.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.zhiye.emaster.RefreshListView.PullToRefreshBase;
import com.zhiye.emaster.RefreshListView.PullToRefreshListView;
import com.zhiye.emaster.RefreshListView.PullToRefreshBase.OnRefreshListener;
import com.zhiye.emaster.adapter.TodoTaskAdapter;
import com.zhiye.emaster.base.BaseFragment;
import com.zhiye.emaster.base.C;
import com.zhiye.emaster.model.MapCustodianTask;
import com.zhiye.emaster.model.SubCustodianTask;
import com.zhiye.emaster.model.Url;
import com.zhiye.emaster.ui.R;
import com.zhiye.emaster.ui.UiTaskDetails;

public class FrgCustodianTask extends BaseFragment{
	public static final int CUS_TASK                =3;
	public static final int NON_EDITING          =10;
    /** 分页 */
	private int pageNo = 1;
	/** 上拉刷新 */
	private PullToRefreshListView customListView;
	
	private ListView listView;

    View view;
    
    int taskSum = 0;//任务数量
    TodoTaskAdapter adapter;
    String url;
    Calendar calendar;
    int year,month;
    
    SimpleDateFormat dateFormat;
    String today,nextDay;
    public  String suffix;
    

    LinearLayout  noMsgView;
    TextView noMsgIc,noMsgText;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
	@SuppressWarnings("static-access")
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle bundle){
		//view = inflater.inflate(R.layout.frag_task, container,false);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.frag_task, null);

        /*缺省提示*/
        noMsgView = (LinearLayout)view.findViewById(R.id.task_no_msg);
        noMsgIc = (TextView)view.findViewById(R.id.no_msg_ic);
        noMsgIc.setTypeface(gettypeface());
        noMsgText = (TextView)view.findViewById(R.id.no_msg_text);
        noMsgText.setText("无监督任务");
        
        MapCustodianTask.getInstance().clear();
        
    	calendar = Calendar.getInstance();
    	dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	calendar.add(calendar.DATE, -3);
    	today = dateFormat.format(calendar.getTime());
    	calendar.add(calendar.DATE, 10);
    	nextDay = dateFormat.format(calendar.getTime());
    	Url.getInstance().setState(5);//监督任务标识
    	suffix = "?start="+today+"&end="+nextDay+"&state=5&size=200000&page=";
    	url = C.api.memberInfo+Url.getInstance().getUserId()+ suffix;
    	
 		//customListView = new PullToRefreshListView(view.getContext());
    	customListView = (PullToRefreshListView)view.findViewById(R.id.task_list);
 		customListView.setPullLoadEnabled(false);
 		customListView.setScrollLoadEnabled(true);
 		listView = customListView.getRefreshableView();
 		customListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

 			public void onPullDownToRefresh(
 					PullToRefreshBase<ListView> refreshView) {
 				// TODO Auto-generated method stub
 				if (isOnLine()) {
 					pageNo = 1;
 					taskSum = 0;
 					doTaskAsync(C.task.custodianTask, url+pageNo,C.http.get);
 				} else {
 					noNetWork();
 				}
 			}

 			@Override
 			public void onPullUpToRefresh(
 					PullToRefreshBase<ListView> refreshView) {
 				// TODO Auto-generated method stub
 				if (isOnLine()) {
 					pageNo++;
 					taskSum = MapCustodianTask.getInstance().size();
 					//toast("下一页");
 					doTaskAsync(C.task.custodianTask, url+pageNo,C.http.get);
 					 
 				} else {
 					noNetWork();
 				}
 			}
 		});
 		
 		customListView.doPullRefreshing(true, 500);//第一次进入页面 主动刷新
	    view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    	
	    //rootLayout.addView(customListView);
		return view;
	}
	public void onTaskComplete(int taskId,String result){
		super.onTaskComplete(taskId, result);
		JSONObject jsonObj = null;
		JSONArray jsonArray = null;
		switch (taskId) {
		case C.task.custodianTask:
			try {
				jsonObj = new JSONObject(result);
				if (jsonObj.length() <= 0) {
					toast("解析为空");
					return;
				}
				boolean flag = jsonObj.getBoolean("Flag");
				if (flag == false) {
					toast("请求出错");
					return;
				}
				jsonArray = jsonObj.getJSONArray("Content");
				if (flag && jsonArray.length() == 0) {
					if (pageNo == 1) {
						customListView.setVisibility(View.GONE);
						noMsgView.setVisibility(View.VISIBLE);
						//customListView.onPullUpRefreshComplete();
					}
					
					customListView.onPullDownRefreshComplete();
					customListView.setHasMoreData(false);
					customListView.setPullLoadEnabled(false);
					return;
				}
			
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject json = jsonArray.getJSONObject(i);
					SubCustodianTask sub = new SubCustodianTask();
					
					String id = json.getString("Id");
					sub.setId(id);
					String content = json.getString("Content");
					sub.setcontent(content);
					int level = json.getInt("Level");
					sub.setlevel(level);
					String ownerId = json.getString("Owner");
					sub.setownerId(ownerId);
					String ownerName = json.getString("OwnerName");
					sub.setownerName(ownerName);
					String custodianId = json.getString("Custodian");
					sub.setcustodianId(custodianId);
					String custodianName = json.getString("CustodianName");
					sub.setcustodianName(custodianName);
					String executor = json.getString("Executor");
					sub.setexecutor(executor);
					String executorName = json.getString("ExecutorName");
					sub.setexecutorName(executorName);
					String startTime = json.getString("StartTime");
					sub.setstartTime(startTime);
					String endTime = json.getString("EndTime");
					sub.setEndTime(endTime);
					int progress = json.getInt("Progress");
					sub.setprogress(progress);
					int commentNum = json.getInt("CommentCount");
		            sub.setCommentCount(commentNum);
		            String completeTime = json.getString("CompleteTime");
					sub.setcompleteTime(completeTime);
					
					String warningTime = json.getString("WarningTime");
					sub.setwarningTime(warningTime);
					
					try {
						 JSONArray att = json.getJSONArray("Attachments");
				            sub.setAtts(att.length());
					} catch (Exception e) {
						// TODO: handle exception
					}
		           
		            sub.setType(json.getInt("Type"));
					MapCustodianTask.getInstance().put(taskSum+i, sub);
				}
				
				adapter = new TodoTaskAdapter(getFraContext(),CUS_TASK);
				
				if (pageNo == 1) {
					
					listView.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}
				//不足一页
				if (pageNo == 1 && jsonArray.length() <7  && jsonArray.length() > 0) {
					customListView.getFooterLoadingLayout().setVisibility(View.VISIBLE);
			 		customListView.setHasMoreData(false);
				}
		 		customListView.onPullDownRefreshComplete();
		 		customListView.onPullUpRefreshComplete();
		 		customListView.setHasMoreData(true);
		 		setLastUpdateTime();
		 		listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position,
							long arg3) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(),UiTaskDetails.class);
						intent.putExtra("editFlag", NON_EDITING);
						intent.putExtra("position", position);
						intent.putExtra("taskId", MapCustodianTask.getInstance().get(position).getId());
						intent.putExtra("tag", 2);
						startActivity(intent);
						getActivity().finish();
					}
				});
			} catch (JSONException e) {
				// TODO: handle exception
			}
			break;
			
		default:
			break;
		}
	}
	private void setLastUpdateTime() {
		// TODO Auto-generated method stub
		String text = formatDateTime(System.currentTimeMillis());
		customListView.setLastUpdatedLabel(text);
	}
	private String formatDateTime(long time) {
		if (0 == time) {
			return "";
		}
		return mDateFormat.format(new Date(time));
	}
	public boolean isOnLine(){
		ConnectivityManager connManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
   public void noNetWork(){
	   toast("无网络");
	   customListView.onPullDownRefreshComplete();
	   customListView.onPullUpRefreshComplete();
	   customListView.setHasMoreData(true);
   }
}
