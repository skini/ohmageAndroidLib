package org.ohmage.activity;

import org.ohmage.R;
import org.ohmage.controls.ActionBarControl;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A base activity for entity info screens that includes the entity info header, and provides a view
 * below the header that scrolls independently.
 * 
 * @author faisal
 *
 */
public abstract class BaseInfoActivity extends FragmentActivity {
	// fields in the entity info header, populated by onContentChanged()
	protected View mEntityHeader;
	protected TextView mHeadertext;
	protected TextView mSubtext;
	protected TextView mNotetext;
	protected ImageView mIconView;
	protected LinearLayout mButtonTray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.base_info_activity);
	}
	
	/**
	 * Returns the scrollable content area which should be populated with the details pertaining to this entity.
	 * 
	 * @return a FrameLayout to which other layouts (e.g. a linearlayout) can be added
	 */
	protected FrameLayout getContentArea() {
		return (FrameLayout)findViewById(R.id.root_container);
	}
	
	protected ActionBarControl getActionBar() {
		return (ActionBarControl)findViewById(R.id.action_bar);
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();

		mEntityHeader = findViewById(R.id.entity_header_content);
		// mEntityHeader.setVisibility(View.GONE);
		mIconView = (ImageView) findViewById(R.id.entity_icon);
		mHeadertext = (TextView) findViewById(R.id.entity_header);
		mSubtext = (TextView) findViewById(R.id.entity_header_sub1);
		mNotetext = (TextView) findViewById(R.id.entity_header_sub2);
		mButtonTray = (LinearLayout) findViewById(R.id.entity_header_tray);
	}
	
	// utility functions and classes to implement togglable views
	
	/**
	 * Attaches a handler to the parent view's onclick event that causes the
	 * child view to toggle its appearance whenever the parent is clicked.
	 * 
	 * @param parent the view which will be clicked to toggle the child
	 * @param child the view which will toggle when the parent is clicked
	 */
	protected void setDetailsExpansionHandler(View parent, final View child) {
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int currentVis = child.getVisibility();
				child.setVisibility((currentVis == View.INVISIBLE || currentVis == View.GONE)?View.VISIBLE:View.GONE);
			}
		});
	}
	
/*    protected class DropDownAnim extends Animation {
        int targetHeight;
        View v;
        boolean down;

        public DropDownAnim(View wv, boolean d){
            v = wv;
            targetHeight = 100;
            down = d;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t){
            int newHeight;
            if(down){
                  newHeight = (int)(targetHeight*interpolatedTime);
            }
            else{
                  newHeight = (int)(targetHeight*(1-interpolatedTime));
            }
            v.getLayoutParams().height = newHeight;
            v.requestLayout();
        }

        public void initalize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width,height,parentWidth,parentHeight);
        }

        public boolean willChangeBounds() {
            return true;
        }
    }*/
}
