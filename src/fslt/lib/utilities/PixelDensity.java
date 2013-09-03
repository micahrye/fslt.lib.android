package fslt.lib.utilities;

import android.content.Context;
import android.util.DisplayMetrics;

public class PixelDensity {

	private DisplayMetrics mDisplayMetrics; 
	
	public PixelDensity(Context ctx){
		mDisplayMetrics = ctx.getResources().getDisplayMetrics(); 
	}
	
	public int pixelFromDensityPixel(int dp){
		//px = dp * (dpi / 160)		
		float f = dp * mDisplayMetrics.density; 
		return (int)(Math.round(dp * mDisplayMetrics.density));
	}
	
	public int dpToPx(int dp) {
	    int px = Math.round(dp * mDisplayMetrics.density);       
	    return px;
	}
	
	public int pxToDp(int px) {
	    int dp = Math.round(px / mDisplayMetrics.density);
	    return dp;
	}
}
