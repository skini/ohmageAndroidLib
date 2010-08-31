package edu.ucla.cens.andwellness;

import java.util.HashMap;

import android.content.Context;
import android.view.View;

public abstract class AbstractPrompt implements Prompt {

	// TODO change private to protected
	private String mId;
	private String mDisplayType;
	private String mDisplayLabel;
	private String mPromptText;
	private String mAbbreviatedText;
	private String mExplanationText;
	private String mDefaultValue;
	private String mCondition;
	private String mSkippable;
	private String mSkipLabel;
	
	public AbstractPrompt() {};
	
	/*public AbstractPrompt(	String id, String displayType, String displayLabel,
					String promptText, String abbreviatedText, String explanationText, 
					String defaultValue, String condition, 
					String skippable, String skipLabel) {
		
		this.mId = id;
		this.mDisplayType = displayType;
		this.mDisplayLabel = displayLabel;
		this.mPromptText = promptText;
		this.mAbbreviatedText = abbreviatedText;
		this.mExplanationText = explanationText;
		this.mDefaultValue = defaultValue;
		this.mCondition = condition;
		this.mSkippable = skippable;
		this.mSkipLabel = skipLabel;
	}*/
	
	public String getId() {
		return mId;
	}
	
	public String getDisplayType() {
		return mDisplayType;
	}
	
	public String getDisplayLabel() {
		return mDisplayLabel;
	}
	
	public String getPromptText() {
		return mPromptText;
	}
	
	public String getAbbreviatedText() {
		return mAbbreviatedText;
	}
	
	public String getExplanationText() {
		return mExplanationText;
	}
	
	public String getDefaultValue() {
		return mDefaultValue;
	}
	
	public String getCondition() {
		return mCondition;
	}
	
	public String getSkippable() {
		return mSkippable;
	}
	
	public String getSkipLabel() {
		return mSkipLabel;
	}
	
	public void setId(String id) {
		this.mId = id;
	}

	public void setDisplayType(String displayType) {
		this.mDisplayType = displayType;
	}

	public void setDisplayLabel(String displayLabel) {
		this.mDisplayLabel = displayLabel;
	}

	public void setPromptText(String promptText) {
		this.mPromptText = promptText;
	}

	public void setAbbreviatedText(String abbreviatedText) {
		this.mAbbreviatedText = abbreviatedText;
	}

	public void setExplanationText(String explanationText) {
		this.mExplanationText = explanationText;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.mDefaultValue = defaultValue;
	}

	public void setCondition(String condition) {
		this.mCondition = condition;
	}

	public void setSkippable(String skippable) {
		this.mSkippable = skippable;
	}

	public void setSkipLabel(String skipLabel) {
		this.mSkipLabel = skipLabel;
	}
	
}