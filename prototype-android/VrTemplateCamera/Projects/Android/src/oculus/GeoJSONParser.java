package oculus;

import java.util.ArrayList;
import java.util.LinkedList;

import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.IBufferDownloadListener;
import org.glob3.mobile.generated.IByteBuffer;
import org.glob3.mobile.generated.IDownloader;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoJSONParser {
	
	String jsonData;
	String name;
	ArrayList<Marker> markers;
	
	GeoJSONParser (IDownloader downloader) {
		try
		{			
			downloader.requestBuffer(new URL("file:///markers.json"),
					10000, TimeInterval.fromDays(30), true, new IBufferDownloadListener(){

				@Override
				public void onDownload(URL url, IByteBuffer buffer,
						boolean expired) {
					parseMarkerData(buffer.getAsString());
				}

				@Override
				public void onError(URL url) {}

				@Override
				public void onCancel(URL url) {}

				@Override
				public void onCanceledDownload(URL url,
						IByteBuffer buffer, boolean expired) {}
		
			}, true);
		} 
		catch (Exception e) { e.printStackTrace(); }
	}
	
	void parseMarkerData(String data){
		try
		{
			JSONObject json = new JSONObject(data);
			markers = new ArrayList<Marker>();
			
			JSONArray features = json.getJSONArray("features");
			for (int i=0; i<features.length();i++){
				JSONObject feat = features.getJSONObject(i);
				String ptName = feat.getJSONObject("properties").getString("Name");
				JSONArray wpData = feat.getJSONObject("geometry").getJSONArray("coordinates");
				double lon = wpData.getDouble(0);
				double lat = wpData.getDouble(1);
				markers.add(new Marker(ptName,"",Geodetic2D.fromDegrees(lat,lon)));
			}
			
		} 
		catch (JSONException e) { e.printStackTrace(); }
	}
}
