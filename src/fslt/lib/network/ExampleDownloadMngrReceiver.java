package fslt.lib.network;

public class ExampleDownloadMngrReceiver extends DownloadManagerReceiver {

	@Override
	protected void setRootApplicationStorageDirectory() {
		this.mRootAppStorageDir = "StoryScape"; 
	}

}
