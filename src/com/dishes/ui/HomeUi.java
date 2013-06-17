/**
 * 
 */
package com.dishes.ui;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dishes.adapter.HomeListViewAdapter;
import com.dishes.common.Constant;
import com.dishes.model.DishInfo;
import com.dishes.model.WSResult;
import com.dishes.util.ImageCallback;
import com.dishes.util.ImageLoader;
import com.dishes.util.ThreadTool;
import com.dishes.webservice.WebServiceAction;
import com.dishes.webservice.WebServiceConstant;

/**
 * @author SenYang
 * 
 */
public class HomeUi extends Activity implements OnClickListener {

	private ListView lv_home;
	private HomeListViewAdapter adapter;
	private Button btn_menu, btn_search;
	private HorizontalScrollView hScrollView;
	private LinearLayout ll_everyday;
	private final int EVERYDAYVIEW = 1;
	private Handler handler = new Handler() {

		@SuppressWarnings( "unchecked" )
		public void handleMessage( android.os.Message msg ) {

			switch( msg.what ) {
			case EVERYDAYVIEW:
				for( Object object : ( List<Object> )msg.obj ) {
					final DishInfo dishInfo = new DishInfo( ( SoapObject )object );
					View view = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.adapter_homeeveryday, null );
					final ImageView imageView = ( ImageView )view.findViewById( R.id.iv_everydish );
					imageView.setOnClickListener( new OnClickListener() {

						@Override
						public void onClick( View v ) {

							Intent intent = new Intent();
							intent.setClass( getApplicationContext(), HowToCook.class );
							intent.putExtra( "dishId", dishInfo.getDishId() );
							startActivity( intent );
							overridePendingTransition( R.anim.slide_right_in, R.anim.slide_left_out );
						}
					} );
					TextView tv_name = ( TextView )view.findViewById( R.id.tv_everydaydishname );
					TextView tv_desc = ( TextView )view.findViewById( R.id.tv_everydaydesc );
					final ProgressBar pr = ( ProgressBar )view.findViewById( R.id.pro );
					tv_name.setText( dishInfo.getDishName() );
					tv_desc.setText( dishInfo.getDishDesc() );
					ll_everyday.addView( view );
					ImageLoader imageLoader = new ImageLoader();
					imageLoader.loadImage( imageView, dishInfo.getDishPic(), dishInfo.getDishName(),Constant.HomeConstant.IMAGE_LENGTH, new ImageCallback() {

						@Override
						public void imageLoading( Bitmap bitmap, float ratio, int width, int height ) {

							imageView.setImageBitmap( bitmap );
						}


						@Override
						public void imageLoadOver() {

							// TODO Auto-generated method stub
							pr.setVisibility( View.GONE );
						}


						@Override
						public void imageLoadFailed() {

						}


						@Override
						public void imageLoadBefore() {

							pr.setVisibility( View.VISIBLE );
						}

					} );
				}
				break;

			default:
				break;
			}

		};
	};


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_home );
		initView();
		System.gc();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();
		getEveryDayDishInfo();

	}


	/**
	 * 
	 */
	private void getEveryDayDishInfo() {

		final HashMap<String, Object> everyDishMap = new HashMap<String, Object>();
		everyDishMap.put( "diseaseStr", "" );
		everyDishMap.put( "num", Constant.HomeConstant.EVERYDAYDISHCOUNTS );
		everyDishMap.put( "wsUser", WebServiceConstant.wsUser );

		// TODO Auto-generated method stub
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				// TODO Auto-generated method stub
				SoapObject soapObject = WebServiceAction.getSoapObject( WebServiceConstant.SERVICE_EVERYDAY_URL, WebServiceConstant.GETPOPULARDISH,
						everyDishMap, WebServiceConstant.SERVICENAMESPACE );
				WSResult wsResult = new WSResult( soapObject );
				switch( Integer.parseInt( wsResult.getState() ) ) {
				case 201:

					break;
				case 202:
					Message msg = new Message();
					msg.obj = wsResult.getResult();
					msg.what = EVERYDAYVIEW;
					handler.sendMessage( msg );

					break;
				default:
					break;
				}
			}
		};
		ThreadTool threadTool = ThreadTool.getInstance();
		threadTool.addTask( runnable );
	}


	/**
	 * 
	 */
	private void initView() {

		// TODO Auto-generated method stub
		btn_menu = ( Button )findViewById( R.id.btn_menu );
		btn_search = ( Button )findViewById( R.id.btn_search );
		btn_search.setOnClickListener( this );
		btn_menu.setOnClickListener( this );
		ll_everyday = ( LinearLayout )findViewById( R.id.ll_everyday );
		hScrollView = ( HorizontalScrollView )findViewById( R.id.hs_everyday );
		lv_home = ( ListView )findViewById( R.id.lv_home );
		adapter = new HomeListViewAdapter( getApplicationContext() );
		lv_home.setAdapter( adapter );
		lv_home.setOnItemClickListener( new HomeListViewClick() );

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick( View v ) {

		// TODO Auto-generated method stub
		switch( v.getId() ) {
		case R.id.btn_menu:

			break;
		case R.id.btn_search:
			Intent intent = new Intent();
			intent.setClass( getApplicationContext(), SearchUi.class );
			startActivity( intent );
			overridePendingTransition( android.R.anim.slide_in_left, android.R.anim.slide_out_right );
			break;

		default:
			break;
		}

	}


	/**
	 * @author SenYang
	 * 
	 */
	public class HomeListViewClick implements OnItemClickListener {

		@Override
		public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {

			Intent intent = new Intent();
			switch( arg2 ) {

			case 0:
				intent.setClass( getApplicationContext(), EachdayMealsUi.class );
				startActivity( intent );
				overridePendingTransition( R.anim.slide_right_in, R.anim.slide_left_out );

				break;
			case 1:

				break;
			case 2:

				break;
			case 3:

				break;

			default:
				break;
			}

		}

	}
}
