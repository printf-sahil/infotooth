package app.infotooth;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

	//DECLARATION OF VARIABLES REQUIRED FOR THE ACTIVITY
	protected static final int DISCOVERY_REQUEST=1;
	private Timer mytime;
	private BluetoothAdapter btAdapter;
	public int i=1;
	int x=1,temp=1;
	public String location="";
	public ListView m_listview;
	public TextView statusUpdate;
	public Button connect;
	public String device_name="",device_addr="",device_rssi="";
	public short rssi;
	public Button disconnect;
	public String toastText="";
	ImageView iv1;
	Resources r;
	//STRING STORING THE AVAILABLE DEVICES AFTER DISCOVERY
	String[] items = new String[] {"Available Devices:", "", "", "", "", "", "", "", "","",""};
	int[] strn = new int[10];
	
	 
    //ONCREATE METHOD TO SETUP THE UI USING SETUPUI() FUNCTION 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SET THE MAIN LAYOUT
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        //STATUS UPDATE SHOWS THE ROOM NO. OF OUR LOCATION
        statusUpdate=(TextView)findViewById(R.id.textView1);
        //DISPLAY FLOOR PLAN WITH OUR CURRENT POSITION
        iv1=(ImageView) findViewById(R.id.imageView1);
        
        //GET THE RESOURCE TO IDENTIFY THE IMAGE 
		r=getResources();
        
		//CREATE OBJECT OF TIMER CLASS TO CALL A METHOD "REFRESH" AGAIN AND AGAIN
        mytime=new Timer();
        
        //CLEAR THE THE VALUE OF ARRAY CONTAINING THE STRENGTH OF BLUETOOTH DEVICES
        for(int k=0;k<10;k++)
        	strn[k]=0;
        
        setupUI(); 
    }
     
    //SETUPUI() FUNCTION CALLED FROM ONCREATE() METHOD TO PLACE THE BUTTONS/LIST/TEXT ON THE ACTIVITY
    private void setupUI()
    {
    	//get references
    	final Button connect=(Button)findViewById(R.id.connect);
    	final Button disconnect=(Button)findViewById(R.id.disc);
    	
    	//HIDE THE DISCONNECT BUTTON
    	disconnect.setVisibility(View.GONE);
    	btAdapter=BluetoothAdapter.getDefaultAdapter();
    	
    	//STATEMENT TO CHECK WHETHER THE BLUETOOTH IS ALREADY ON
    	if(btAdapter.isEnabled())
    	{
    		//DISPLAY THE DISCONNECT BUTTON 
			disconnect.setVisibility(View.VISIBLE);
			//HIDE THE CONNECT BUTTON
			connect.setVisibility(View.GONE);
			
			//TO CHECK IF "SETUPUI()" IS CALLED FOR FIRST TIME S
			if(x==1)
			{
				//TO FIND DEVICES WITH BLUETOOTH ON 
//				findDevices();
				// when a device is found we call a broadcast receiver discoveryResult 
    			registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

    			start();
    			
				x++; 
			}
		}
		else
		{
			connect.setVisibility(View.VISIBLE);
			statusUpdate.setText("Bluetooth is Not on.");
		}
    	
    	//METHOD TO PERFORM BLUETOOTH CONNECTION OPERATION ON BUTTON CLICK
    	connect.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			//BLUETOOTH ADAPTER MAKES IT POSSIBLE TO PERFORM DISCOVERY OPERATIONS ON THE DEVICE
    			String beDiscoverable=BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;

    			
    			// use to discover devices and call onActivityResult
    			startActivityForResult(new Intent(beDiscoverable),DISCOVERY_REQUEST);
    			//ONCE THE BT IS SETUP AND DISCOVERY IS ON, WE REGISTER IT ON THE N/W
    			
    			// when a device is found we call a broadcast receiver discoveryResult 
    			registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    			 
    			//THIS WILL START CALLING "REFRESH" FUNCION AFTER FIXED INTERVAL OF TIME
    			start();
    			
    		}
    	});
    	
    	//METHOD TO PERFORM BLUETOOTH DIS-CONNECTION OPERATION ON BUTTON CLICK
    	disconnect.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v)
    		{
    			
    			//CLOSE THE BLUETOOTH CONNECTION
    			btAdapter.disable();
    			
    			disconnect.setVisibility(View.GONE);
    			connect.setVisibility(View.VISIBLE);
    			
    			statusUpdate.setText("Bluetooth Off");
    			
    			//CLEAR THE ARRAY THAT CONTAIN BLUETOOTH DEVICES NAME
    			for(int j=1;j<items.length;j++)
    			{
    				items[j]="";
    			}
    			
    			i=1;
    			
    			setupUI();
    			
    			//STOP CALLING "REFRESH" FUNCTION AGAIN 
    			stop();
    			
    			//GET THE ID OF IMAGE NAMED "BACK"
				int id=r.getIdentifier("back", "drawable", "app.infotooth");
				iv1.setImageResource(id);
    			
    		}
    	});
    
    }

    void stop()
	{
    	//CLOSE THE TIMER
		mytime.cancel();
	}
    
	void start()
	{
		//START THE TIMER
		mytime.schedule(new TimerTask()
		{
    
			//CALL THE "RUN" METHOD AFTER EVERY 9000 MILLISECONDS
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				
				makeuithread();

			}
		}, 0, 9000);
	}
    
	//"RUN" METHOD IS CALLED AFTER EVERY 9000 MILLISECONDS SO IS "MAKEUITHREAD" 
	void makeuithread()
	{
		//MAKES A UI THREAD TO CALL "REFRESH" METHOD
		this.runOnUiThread(on_tick);
	}
	
	Runnable on_tick=new Runnable() 
	{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub

			refresh();
		}
	};
	
	//FINDS THE NEAREST BLUETOOTH MODULE AND DISPLAY THE POSITION ACCORDINGLY
    void refresh()
    {	
    	
    	// FIND THE DEVICE WITH MAX BLUETOOTH STRENGTH
    	int maxD;
		int pos=0;
    			
		//STRN ARRAY STORES THE STRENGTH OF ALL DEVICES
		maxD=strn[0];
		for(int k=0;k<strn.length;k++)
		{
			if(maxD<strn[k]&&strn[k]<0)
			{
				maxD=strn[k];
				pos=k; 
			}
		} 
		
		// SET THE POSTION OF BLACK DOT ACCORDING TO THE MAX TRENGTH OF BLUETOOTH DEVICE
		
		/**
		 *  "A0" , "C8" , "86" , 5E"
		 *  ARE THE NAMES OF BLUETOOTH MODULES 
		 */
		
		// FINDS THE NAME OF THE MODULE OF MAX STRENTH AND ACCORDING DISPLAY THE CURRENT POSTION
		if(items[pos+1]!="")
		{
			if(items[pos+1].substring(15, 17).equals("A0"))
			{
				//GET THE ID OF IMAGE BACK5
				int id=r.getIdentifier("back5", "drawable", "app.infotooth");
				iv1.setImageResource(id);
				location="Room no : 147";
				statusUpdate.setText(location);
			}
			
			else if(items[pos+1].substring(15, 17).equals("C8"))
			{
				int id=r.getIdentifier("back3", "drawable", "app.infotooth");
				iv1.setImageResource(id);
				location="Room no : 158";
				statusUpdate.setText(location);
			}
			
			else if(items[pos+1].substring(15, 17).equals("86"))
			{
				int id=r.getIdentifier("back1", "drawable", "app.infotooth");
				iv1.setImageResource(id);
				location="Room no : 159";
				statusUpdate.setText(location);
			}
			
			else if(items[pos+1].substring(15, 17).equals("5E"))
			{
				int id=r.getIdentifier("back7", "drawable", "app.infotooth");
				iv1.setImageResource(id);
				location="Room no : 151";
				statusUpdate.setText(location);
			}
			
			
			statusUpdate.setText(items[pos+1].toString().substring(15, 17));
			Thread t=new Thread(new Runnable(){
				@Override
				public void run(){
					postData();
				}

				private void postData() {
					String fullUrl="https://docs.google.com/forms/d/15sAJy6mLgp3Mg7t7IxTJuYYz7CVqDiMUq3qPhhDb3hs/formResponse";
					HttpRequest mReq=new HttpRequest();
					String col1=btAdapter.getName().toString()+"//"+btAdapter.getAddress().toString();
					String col2=location;
					String data="entry_323007828="+URLEncoder.encode(col1)+"&"+
								"entry_1806996647="+URLEncoder.encode(col2);
					String response=mReq.sendPost(fullUrl, data);
					Log.i("DocsUpload",response);
				}
			});
			t.start();
		}
	

		 
		//CLEAR THE ARRAY CONTAINING THE BLUETOOTH MODULE NAME AND THIER STRENGTH
    	for(int j=1;j<items.length;j++) 
		{
			items[j]="";
			strn[j-1]=0;
		}
		
		i=1;
		 
		findDevices();
				
    }
    
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
    	if(requestCode==DISCOVERY_REQUEST)
    	{
    		findDevices();
    		setupUI();
    	}
    } 

    
    //THIS METHOD DISCOVERS THE DEVICES IN THE VICINITY USING THE startdiscovery() method
    private void findDevices()
    {
    	// START THE DISCOVERY OF OTHER BLUETOOTH MODULE
    	if(btAdapter.startDiscovery())
    	{  		
    		//REGISTER A RECEIVER NAMED "DISCOVERYRESULT" WHICH IS CALLED EVERY TIME A BLUETOOTH MODULE IS DISCOVERED
    		registerReceiver(discoveryResult,new IntentFilter(BluetoothDevice.ACTION_FOUND));
    	}
    }
    
    //THE DEVICES DISCOVERED ARE SET TO A STRING IN A FORMAT OF NAME,ADDR,RSSI TO BE ADDED TO THE LISTVIEW
    BroadcastReceiver discoveryResult=new BroadcastReceiver()
    {
    	@Override
    	public void onReceive(Context context,Intent intent)
    	{

    		BluetoothDevice remoteDevice;
    		remoteDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		
    		//CHECK IF THE DETECTED BLUETOOTH DEVICE IS OUR BLUETOOTH MODULE OR OTHER DEVICE 
       		if(remoteDevice.getAddress().toString().substring(0,8).equals("00:12:6F"))
    		{
       			rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
    			items[i]=remoteDevice.getAddress()+"/"+String.valueOf(rssi);
    			strn[i-1]=rssi;
    			i++;
    		}
       		
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
    		
    		setupUI();
    	} 
    };
    
    
    //SUB-METHODS OF METHODS FORCED BY ANDROID,NOT OF MUCH USE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
     
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
}
