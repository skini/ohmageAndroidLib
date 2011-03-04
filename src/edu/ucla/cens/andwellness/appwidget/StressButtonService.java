package edu.ucla.cens.andwellness.appwidget;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import edu.ucla.cens.andwellness.PromptXmlParser;
import edu.ucla.cens.andwellness.R;
import edu.ucla.cens.andwellness.SharedPreferencesHelper;
import edu.ucla.cens.andwellness.R.raw;
import edu.ucla.cens.andwellness.db.DbHelper;
import edu.ucla.cens.andwellness.prompts.AbstractPrompt;
import edu.ucla.cens.andwellness.prompts.Prompt;
import edu.ucla.cens.andwellness.service.SurveyGeotagService;
import edu.ucla.cens.andwellness.triggers.glue.TriggerFramework;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class StressButtonService extends IntentService {
	
	private static final String TAG = "StressButtonService";
	
	private Handler mHandler;
	
	public StressButtonService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar now = Calendar.getInstance();
		String launchTime = dateFormat.format(now.getTime());
		
		List<Prompt> prompts = null;
		
		String surveyId = "stressButton";
        String surveyTitle = "Stress";
        
        try {
			prompts = PromptXmlParser.parsePrompts(getResources().openRawResource(SharedPreferencesHelper.CAMPAIGN_XML_RESOURCE_ID), surveyId);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startService(new Intent(this, SurveyGeotagService.class));

		if (((AbstractPrompt)prompts.get(0)).getResponseObject() == null) {
			Toast.makeText(this, "There is a bug: default value not being set!", Toast.LENGTH_SHORT).show();
		} else {
			((AbstractPrompt)prompts.get(0)).setDisplayed(true);
			((AbstractPrompt)prompts.get(0)).setSkipped(false);
			Log.i(TAG, prompts.get(0).getResponseJson());
			storeResponse(surveyId, surveyTitle, launchTime, prompts);
			//Toast.makeText(this, "Registered stressful event.", Toast.LENGTH_SHORT).show();
			mHandler.post(new DisplayToast("Registered stressful event."));
		}
		return;
	}
	
	private class DisplayToast implements Runnable{
		String mText;

		public DisplayToast(String text){
			mText = text;
		}

		public void run(){
			Toast.makeText(StressButtonService.this, mText, Toast.LENGTH_SHORT).show();
		}
	}

	private void storeResponse(String surveyId, String surveyTitle, String launchTime, List<Prompt> prompts) {
		
		SharedPreferencesHelper helper = new SharedPreferencesHelper(this);
		String campaign = helper.getCampaignName();
		String campaignVersion = helper.getCampaignVersion();
		String username = helper.getUsername();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar now = Calendar.getInstance();
		String date = dateFormat.format(now.getTime());
		long time = now.getTimeInMillis();
		String timezone = TimeZone.getDefault().getID();
		
		LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc == null || System.currentTimeMillis() - loc.getTime() > SurveyGeotagService.LOCATION_STALENESS_LIMIT || loc.getAccuracy() > SurveyGeotagService.LOCATION_ACCURACY_THRESHOLD) {
			Log.w(TAG, "gps provider disabled or location stale or inaccurate");
			loc = null;
		}
		
		//get launch context from trigger glue
		JSONObject surveyLaunchContextJson = new JSONObject();
		try {
			surveyLaunchContextJson.put("launch_time", launchTime);
			surveyLaunchContextJson.put("active_triggers", TriggerFramework.getActiveTriggerInfo(this, surveyTitle));
		} catch (JSONException e1) {
			throw new RuntimeException(e1);
		}
		String surveyLaunchContext = surveyLaunchContextJson.toString();
		
		JSONArray responseJson = new JSONArray();
		for (int i = 0; i < prompts.size(); i++) {
			JSONObject itemJson = new JSONObject();
			try {
				itemJson.put("prompt_id", ((AbstractPrompt)prompts.get(i)).getId());
				itemJson.put("value", ((AbstractPrompt)prompts.get(i)).getResponseObject());
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			responseJson.put(itemJson);
		}
		String response = responseJson.toString();
		
		DbHelper dbHelper = new DbHelper(this);
		if (loc != null) {
			dbHelper.addResponseRow(campaign, campaignVersion, username, date, time, timezone, SurveyGeotagService.LOCATION_VALID, loc.getLatitude(), loc.getLongitude(), loc.getProvider(), loc.getAccuracy(), loc.getTime(), surveyId, surveyLaunchContext, response);
		} else {
			dbHelper.addResponseRowWithoutLocation(campaign, campaignVersion, username, date, time, timezone, surveyId, surveyLaunchContext, response);
		}
	}
}