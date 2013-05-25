package de.nware.custompreferences;


import de.olei.custompref.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * A Preference that show directly a SeekBar in the Preference Screen without the need of an {@link DialogPreference}
 * inspired by Robobunnys <a href="http://robobunny.com/wp/2011/08/13/android-seekbar-preference/">SeekBarPreference</a>
 * based on CheckBoxPreference.java from AOSP (4.2)
 * 
 * @author Oliver Eichner n0izeland@{at}gmail.com
 *
 */
public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {

	final String TAG = SeekBarPreference.class.getSimpleName();

	SeekBar mSeekBar;
	TextView mWidgetValueText;

	private int mMaxValue = 100;
	private int mMinValue = 0;
	
	String mUnitSign;

	private int mCurrentValue;


	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.de_nware_custompreferences_SeekBarPreference);
		mUnitSign = a.getString(R.styleable.de_nware_custompreferences_SeekBarPreference_unit_sign);
		if(mUnitSign == null) {
			mUnitSign=""; //if no unit sign is selected set it to nothing
		}
		mMaxValue = a.getInt(R.styleable.de_nware_custompreferences_SeekBarPreference_max_value, mMaxValue);
		mMinValue = a.getInt(R.styleable.de_nware_custompreferences_SeekBarPreference_min_value, mMinValue);

		a.recycle();

	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		//TODO defstyle shouldn't 0
		this(context,attrs,0);
	}

	public SeekBarPreference(Context context) {
		this(context,null);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		//Create new LinearLayout and fill in the Preference default layout and the SeekBar
		LinearLayout layout = new LinearLayout(getContext());
		//XXX ListView.LayoutParameter instead of LinearLayout.LayoutParameter, due to backward compatibility with API8
		layout.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View childLayout = inflater.inflate(getLayoutResource(),
	             parent,false);
		//Place the current Value TexzView in the preference widget area
		LinearLayout widgetView =(LinearLayout) childLayout.findViewById(android.R.id.widget_frame);
		mWidgetValueText = new TextView(getContext());
		mWidgetValueText.setGravity(Gravity.RIGHT);
		//TODO assign proper id, but how?
		mWidgetValueText.setId(99999);
		widgetView.addView(mWidgetValueText);
		
		layout.addView(childLayout);		
		layout.addView(inflater.inflate(R.layout.seekbar_pref, parent,false));

		return (ViewGroup)layout;
	}
	
	
	@Override
	protected void onBindView(View view) {
		//TODO id
		mWidgetValueText = (TextView) ((ViewGroup)view).findViewById(99999);
		if(mWidgetValueText==null) Log.e(TAG, "text widget null");

		mWidgetValueText.setText(mCurrentValue + mUnitSign);

		mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
		int max;
		int currentProgress;
		if(mMinValue<0){
			max=mMaxValue + Math.abs(mMinValue);
			currentProgress = mCurrentValue +  Math.abs(mMinValue); 
		} else {
			max=mMaxValue - mMinValue;
			currentProgress = mCurrentValue - mMinValue;
		}
	
		mSeekBar.setMax(max);
		mSeekBar.setOnSeekBarChangeListener(this);

		mSeekBar.setProgress(currentProgress);

		super.onBindView(view);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
		int newValue;
		
		newValue=mMinValue+i;
		
		if (!callChangeListener(newValue)) {
			return;
		}
		
		mWidgetValueText.setText(newValue + mUnitSign);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		setCurrentValue(mMinValue+seekBar.getProgress());
		
	}

	/**
	 * Set the current Value persist it and notifies about the changes
	 * @param currentValue the value to be set
	 */
	private void setCurrentValue(int currentValue) {
		mCurrentValue = currentValue;
		persistInt(currentValue);
		notifyChanged();
	}

	/**
	 * The current SeekBarPreference value.
	 * @return value
	 */
	private int getCurrentValue() {
		return mCurrentValue;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 50);

	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {

		setCurrentValue(restorePersistedValue ? getPersistedInt(mCurrentValue)
				: (Integer) defaultValue);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.currentValue = getCurrentValue();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setCurrentValue(myState.currentValue);
	}

	private static class SavedState extends BaseSavedState {
		int currentValue;

		public SavedState(Parcel source) {
			super(source);
			currentValue = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentValue);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

	}
}
