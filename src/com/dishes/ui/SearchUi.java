/**
 * @author SenYang
 */
package com.dishes.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.serialization.SoapObject;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dishes.adapter.SearchAdapter;
import com.dishes.common.CommonMethod;
import com.dishes.common.Constant;
import com.dishes.common.ViewHolder;
import com.dishes.model.DishInfo;
import com.dishes.model.WSResult;
import com.dishes.ui.base.BaseActivity;
import com.dishes.util.ImageLoader.ImageLoadTask;
import com.dishes.util.ThreadTool;
import com.dishes.webservice.WebServiceAction;
import com.dishes.webservice.WebServiceConstant;

/**
 * @author SenYang
 * 
 */
public class SearchUi extends BaseActivity implements OnClickListener, OnScrollListener, OnEditorActionListener {

	private Button btn_searching, btn_taste, btn_caixi, btn_process;
	private EditText et_search;
	private ListView lv_searchresult;
	private PopupWindow pop_caixi, pop_taste, pop_process;
	private Map<String, Object> sreachDishMap;
	private final int SHOWSEARCHRESULT = 1;
	private int lastItem = 0;
	private int firstVisibleItem;
	private int visibleItemCount;
	private SearchAdapter adapter;
	private View view;
	private boolean REMOVE = true;
	private List<SoapObject> lists;
	private Handler handler = new Handler() {

		public void handleMessage( Message msg ) {

			switch( msg.what ) {
			case SHOWSEARCHRESULT:
				@SuppressWarnings( "unchecked" )
				List<SoapObject> list = ( List<SoapObject> )msg.obj;
				lists = list;
				adapter = new SearchAdapter( getApplicationContext(), list, lv_searchresult );
				if( lv_searchresult.getFooterViewsCount() != 0 ) {
					lv_searchresult.removeFooterView( view );
				}
				if( lv_searchresult.getFooterViewsCount() == 0 && ( list.size() > Constant.UtilConstant.LISTVIEW_MINCOUNT ) ) {
					lv_searchresult.addFooterView( view );
				}
				lv_searchresult.setAdapter( adapter );

				break;

			default:
				break;
			}
		};
	};
	private InputMethodManager imm;


	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_search );
		initView();
		initSearchMap();
	}


	/**
	 * 
	 */
	private void initView() {

		imm = ( InputMethodManager )getSystemService( Context.INPUT_METHOD_SERVICE );
		btn_caixi = ( Button )findViewById( R.id.btn_caixi );
		btn_caixi.setOnClickListener( this );
		btn_taste = ( Button )findViewById( R.id.btn_taste );
		btn_taste.setOnClickListener( this );
		btn_process = ( Button )findViewById( R.id.btn_process );
		btn_process.setOnClickListener( this );
		btn_searching = ( Button )findViewById( R.id.btn_searching );
		btn_searching.setOnClickListener( this );
		btn_searching.requestFocus();
		et_search = ( EditText )findViewById( R.id.et_search );
		et_search.setOnEditorActionListener( this );
		lv_searchresult = ( ListView )findViewById( R.id.lv_searchresult );
		view = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.tool_refreshlistview, null );
		lv_searchresult.setOnScrollListener( this );
		lv_searchresult.setOnItemClickListener( new ListViewItemClickL() );

	}


	public void initSearchMap() {

		sreachDishMap = new HashMap<String, Object>();
		sreachDishMap.put( "ingredients", null );
		sreachDishMap.put( "process", null );
		sreachDishMap.put( "taste", null );
		sreachDishMap.put( "caixi", null );
		sreachDishMap.put( "searchType", 0 );
		sreachDishMap.put( "wsUser", WebServiceConstant.wsUser );
	}


	/**
	 * 在按钮下面创建popupwindow
	 * 
	 * @param context
	 * @param view
	 * @param strings
	 */
	public PopupWindow createBtnPop( Context context, PopupWindow popupWindow, View view, String[] strings ) {

		ListView listView = new ListView( getApplicationContext() );
		listView.setId( view.getId() );
		listView.setOnItemClickListener( new ListVItemClick() );
		listView.setAdapter( new PopAdapter( context, strings ) );
		listView.setBackgroundColor( Color.DKGRAY );
		popupWindow = new PopupWindow( listView );
		popupWindow.setFocusable( true );
		popupWindow.setOutsideTouchable( true );
		popupWindow.setBackgroundDrawable( getResources().getDrawable( R.drawable.viewflag ) );
		popupWindow.setWidth( CommonMethod.dipTopx( getApplicationContext(), 100 ) );
		popupWindow.setHeight( LayoutParams.WRAP_CONTENT );
		popupWindow.showAsDropDown( view );
		return popupWindow;
	}


	@Override
	public void onClick( View v ) {

		switch( v.getId() ) {
		case R.id.btn_searching:
			imm.hideSoftInputFromWindow( et_search.getApplicationWindowToken(), 0 );
			String string = et_search.getText().toString();
			Intent intent = getIntent();
			if( intent.getStringExtra( "dishName" ) != null ) {
				string = intent.getStringExtra( "dishName" );
			}
			if( string.equals( "" ) || string == null ) {
				Toast.makeText( getApplicationContext(), R.string.searchInfo, Toast.LENGTH_SHORT ).show();
				return;
			}
			sreachDishMap.put( "dishName", string );
			getSreachDishInfo( sreachDishMap );

			break;
		case R.id.btn_caixi:
			pop_caixi = createBtnPop( getApplicationContext(), pop_caixi, btn_caixi, Constant.SearchConstant.CAIXI_LIST );

			break;
		case R.id.btn_taste:
			pop_taste = createBtnPop( getApplicationContext(), pop_taste, btn_taste, Constant.SearchConstant.TASTE_LIST );
			break;
		case R.id.btn_process:
			pop_process = createBtnPop( getApplicationContext(), pop_process, btn_process, Constant.SearchConstant.PROCESS_LIST );
			break;

		default:
			break;
		}

	}


	/**
	 * @param sreachDishMap
	 */
	private void getSreachDishInfo( final Map<String, Object> sreachDishMap ) {

		ThreadTool.getInstance().addTask( new Runnable() {

			@Override
			public void run() {

				SoapObject soapObject = WebServiceAction.getSoapObject( WebServiceConstant.SERVICE_SEARCHDISHES_URL, WebServiceConstant.GETDISHBYCONDITIONS,
						sreachDishMap, WebServiceConstant.SERVICENAMESPACE );
				WSResult wsResult = new WSResult( soapObject );
				Message msg = new Message();
				msg.what = SHOWSEARCHRESULT;
				msg.obj = wsResult.getResult();
				handler.sendMessage( msg );
			}
		} );

	}


	@Override
	public void onScroll( AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount ) {

		this.firstVisibleItem = firstVisibleItem;
		this.visibleItemCount = visibleItemCount;
		lastItem = firstVisibleItem + visibleItemCount;
	}


	/**
	 * @return the visibleItemCount
	 */
	public int getVisibleItemCount() {

		return visibleItemCount;
	}


	/**
	 * @return the lastItem
	 */
	public int getLastItem() {

		return lastItem;
	}


	/**
	 * @return the firstVisibleItem
	 */
	public int getFirstVisibleItem() {

		return firstVisibleItem;
	}


	/**
	 * listview拉到尾部自动刷新
	 * 
	 * @param view
	 * @param scrollState
	 */
	@Override
	public void onScrollStateChanged( AbsListView view, int scrollState ) {

		if( lv_searchresult.getChildCount() == 0 ) {
			return;
		}
		if( scrollState == OnScrollListener.SCROLL_STATE_IDLE ) {
			ArrayList<ImageLoadTask> list = ThreadTool.getImageLoadTasks();
			for( int i = 0; i < list.size(); i++ ) {
				REMOVE = true;
				for( int j = firstVisibleItem; j < lastItem - 1; j++ ) {
					DishInfo dishInfo = new DishInfo( lists.get( j ) );
					if( list.get( i ).getId().equals( dishInfo.getDishPic() ) ) {
						REMOVE = false;
						break;
					}
				}
				if( REMOVE ) {
					list.get( i ).stopTask();
					list.remove( i );
					i--;
				}

			}
			if( lastItem == adapter.getCount() + 1 ) {

				adapter.setCount( adapter.getCount() + Constant.UtilConstant.LISTVIEW_MINCOUNT );
				if( lastItem != adapter.getCount() + 1 ) {

					adapter.notifyDataSetChanged();
				} else {
					lv_searchresult.removeFooterView( this.view );
				}

			}
		}

	}


	@Override
	protected void onPause() {

		super.onPause();
		overridePendingTransition( R.anim.slide_out_donothing, R.anim.slide_top_out );
	}


	public class ListViewItemClickL implements OnItemClickListener {

		@Override
		public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {

			arg0.getChildAt( arg2 );
			DishInfo dishInfo = new DishInfo( ( SoapObject )arg0.getAdapter().getItem( arg2 ) );
			String dishId = dishInfo.getDishId();
			Intent intent = new Intent();
			intent.putExtra( "dishId", dishId );
			intent.setClass( getApplicationContext(), HowToCook.class );
			startActivity( intent );
			overridePendingTransition( R.anim.slide_right_in, R.anim.slide_left_out );
		}
	}

	public class PopAdapter extends BaseAdapter {

		private String[] list;


		/**
		 * @param applicationContext
		 * @param caixiList
		 */
		public PopAdapter( Context applicationContext, String[] caixiList ) {

			this.list = caixiList;

		}


		@Override
		public int getCount() {

			return list.length;
		}


		@Override
		public Object getItem( int position ) {

			return list[ position ];
		}


		@Override
		public long getItemId( int position ) {

			return position;
		}


		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			ViewHolder viewHolder = null;
			if( convertView == null ) {
				viewHolder = new ViewHolder();
				viewHolder.textView1 = new TextView( getApplicationContext() );
				convertView = viewHolder.textView1;
				convertView.setTag( viewHolder );
			} else {
				viewHolder = ( ViewHolder )convertView.getTag();
			}
			viewHolder.textView1.setTextSize( 20 );
			viewHolder.textView1.setTextColor( Color.GREEN );
			viewHolder.textView1.setText( list[ position ] );
			return convertView;
		}

	}

	public class ListVItemClick implements OnItemClickListener {

		@Override
		public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {

			arg0.getAdapter();
			switch( arg0.getId() ) {
			case R.id.btn_caixi:
				pop_caixi.dismiss();
				sreachDishMap.put( "caixi", Constant.SearchConstant.CAIXI_LIST_ID[ arg2 ] );
				btn_caixi.setText( ( ( TextView )arg1 ).getText().toString() );
				getSreachDishInfo( sreachDishMap );
				arg1.getParent();
				break;
			case R.id.btn_taste:
				pop_taste.dismiss();
				sreachDishMap.put( "taste", Constant.SearchConstant.TASTE_LIST_ID[ arg2 ] );
				btn_taste.setText( ( ( TextView )arg1 ).getText().toString() );
				getSreachDishInfo( sreachDishMap );
				break;
			case R.id.btn_process:
				pop_process.dismiss();
				sreachDishMap.put( "process", Constant.SearchConstant.PROCESS_LIST_ID[ arg2 ] );
				btn_process.setText( ( ( TextView )arg1 ).getText().toString() );
				getSreachDishInfo( sreachDishMap );
				break;

			default:
				break;
			}

		}

	}


	@Override
	public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {

		if( actionId == EditorInfo.IME_ACTION_SEARCH ) {
			onClick( btn_searching );
			imm.hideSoftInputFromWindow( et_search.getApplicationWindowToken(), 0 );
			return true;
		}
		return false;
	}
}
