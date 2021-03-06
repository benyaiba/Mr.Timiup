package net.yaiba.timiup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.yaiba.timiup.data.ListViewData;
import net.yaiba.timiup.db.TimiUpDB;
import net.yaiba.timiup.utils.SpecialAdapter;
import net.yaiba.timiup.utils.UpdateTask;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static net.yaiba.timiup.utils.Custom.*;


public class MainActivity extends Activity implements  AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private static final int MENU_ABOUT = 0;
    private static final int MENU_SUPPORT = 1;
    private static final int MENU_WHATUPDATE = 2;
    private static final int MENU_IMPORT_EXPOERT = 3;
    private static final int MENU_CHECK_UPDATE = 5;

    private TimiUpDB TimiUpDB;
    private Cursor mCursor;
    private ListView RecordList;
    private EditText SearchInput;
    private TextView TotalCount;
    private TextView HP;
    private TextView HpTag;

    private Long lCount;
    private UpdateTask updateTask;

    private int RECORD_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 当迁移当前页面时，判断检索框中是否有内容，如果有，恢复检索时下拉列表中的内容，并设置检索框中的文字。
        final ListViewData app = (ListViewData)getApplication();
        if("".equals(app.getQuickSearchText())){
            setUpViews("listInit",app.getMainListSortType());
        } else {
            SearchInput = (EditText)findViewById(R.id.searchInput);
            SearchInput.setText(app.getQuickSearchText());
            setUpViews("search",app.getQuickSearchText());
        }

        //返回前设置前次的位置值
        setRecordListPosition();

        Button bn_go_add = (Button)findViewById(R.id.go_add);
        bn_go_add.setOnClickListener(new View.OnClickListener(){
            public void  onClick(View v)
            {
                Intent mainIntent = new Intent(MainActivity.this,AddActivity.class);
                startActivity(mainIntent);
                setResult(RESULT_OK, mainIntent);
                finish();
            }
        });




        SearchInput = (EditText)findViewById(R.id.searchInput);
        SearchInput.clearFocus();
        SearchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final ListViewData app = (ListViewData)getApplication();
                if(SearchInput.getText().toString().trim().length()!=0){
                    try {
                        setUpViews("search",SearchInput.getText().toString().trim());
                        app.setQuickSearchText(SearchInput.getText().toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    setUpViews("listInit","id desc");
                    app.setQuickSearchText("");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Toast.makeText(LoginActivity.this, "beforeTextChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(LoginActivity.this, "afterTextChanged", Toast.LENGTH_SHORT).show();
            }

        });



        // hp设定监听器
        HP = (TextView)findViewById(R.id.hp);
        HpTag = (TextView)findViewById(R.id.hp_tag);
        HP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if("id desc".equals(app.getMainListSortType())){
                    setUpViews("listInit","hp asc");
                    HpTag.setText("♡");
                    app.setMainListSortType("hp asc");
                } else if("hp asc".equals(app.getMainListSortType())){
                    setUpViews("listInit","hp desc");
                    HpTag.setText("♥");
                    app.setMainListSortType("hp desc");
                } else if("hp desc".equals(app.getMainListSortType())){
                    setUpViews("listInit","id desc");
                    HpTag.setText("");
                    app.setMainListSortType("id desc");
                }

            }
        });

    }




    public void setUpViews(String type, String value){
        TimiUpDB = new TimiUpDB(this);
        if("listInit".equals(type)){
            mCursor = TimiUpDB.getAll(value);
            lCount = TimiUpDB.getAllCount(value);
        } else if("search".equals(type)) {

//            bn_filters = (Button)findViewById(R.id.filters);
//            if(bn_filters.getText().equals("+")){
//                mCursor = TimiUpDB.getForSearch(value,"","");
//            } else{
//                spinner_filter_create_time = (Spinner)findViewById(R.id.filter_create_time);
//                spinner_filter_eat_time = (Spinner)findViewById(R.id.filter_eat_time);
//                mCursor = TimiUpDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),spinner_filter_eat_time.getSelectedItem().toString());
//            }

            mCursor = TimiUpDB.getForSearchName(value);
            lCount = TimiUpDB.getForSearchCount(value);

        } else if("filter".equals(type)){
//            spinner_filter_create_time = (Spinner)findViewById(R.id.filter_create_time);
//            spinner_filter_eat_time = (Spinner)findViewById(R.id.filter_eat_time);
//            if(spinner_filter_eat_time.getSelectedItem().toString().isEmpty()){
//                mCursor = TimiUpDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),"");
//            } else {
//                mCursor = TimiUpDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),spinner_filter_eat_time.getSelectedItem().toString());
//            }
        }

        //将查找到的记录数显示到检索框里面 以hint形式。节省空间
//        TotalCount = (TextView) findViewById(R.id.totalCount);
//        TotalCount.setText("物品：x".replace("x", lCount.toString()));
        SearchInput = (EditText)findViewById(R.id.searchInput);
        SpannableString s = new SpannableString("本仓库总件数：x".replace("x", lCount.toString()));
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(12,true);
        s.setSpan(textSize,0,s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SearchInput.setHint(s);

        RecordList = (ListView)findViewById(R.id.recordslist);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for(mCursor.moveToFirst();!mCursor.isAfterLast();mCursor.moveToNext()) {
            /*String resNo = "["+mCursor.getString(resNoColumn)+"]"; */
            String id = mCursor.getString(mCursor.getColumnIndex("id"));
            String goodName = mCursor.getString(mCursor.getColumnIndex("good_name"));
            String productDate = mCursor.getString(mCursor.getColumnIndex("product_date"));
            String endDate = mCursor.getString(mCursor.getColumnIndex("end_date"));
            String buyDate = mCursor.getString(mCursor.getColumnIndex("buy_date"));
            String status = mCursor.getString(mCursor.getColumnIndex("status"));
            String hp_db = mCursor.getString(mCursor.getColumnIndex("hp"));

            Log.v("v_record"+id,id+"/"+goodName+"/"+productDate+"/"+endDate+"/"+buyDate+"/"+status+"/"+hp_db);

            //若果到日期+6个月，还在当前日期之前，，也就是说 ，到期日在六个月以前，，，并且 status=1，就是说已经使用了，这样的灰色数据不需要显示了，，只针对初期化列表，检索时候这类数据可以呗检索出来
//            if ("listInit".equals(type)){
//                try {
//
//                    if(getStringToDate(getDateAddYearMonthWeekDate(endDate,"6","month")).before(getStringToDate(getNowDate()))  ){
//                        if("1".equals(status)){
//                            continue;
//                        }
//                    }
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }


            String  laveDays = "0";//剩余天数，， _剩余天数=今日-到期日 的天数
            double laveDaysDoub = 0;
            try {
                laveDaysDoub = getDiffDays(getStringToDate(getNowStringDate()),getStringToDate(endDate));
                laveDays = Double.toString(laveDaysDoub).split("\\.")[0];
            } catch (ParseException e) {
                e.printStackTrace();
            }

//            String hp = "";//HP,表示商品的生命周期百分比，， _HP =（剩余天数/（生产日期~到期日的天数））x100----在数据库中设置了hp字段的计算，所以取消了手动计算hp的部分。
//            try {
//                double dhp = 0;
//                if( "0".equals(laveDays)){
//                    hp = "0";
//                } else {
//                    dhp = laveDaysDoub/getDiffDays(getStringToDate(productDate),getStringToDate(endDate))*100;
//                    hp = Double.toString(dhp).split("\\.")[0];
//                }
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("goodName", goodName);
            map.put("laveDays", laveDays+"天");
            map.put("endDate", endDate);
            map.put("HP", hp_db+"%");
            map.put("status", status);
            map.put("productDate", productDate);
            map.put("buyDate", buyDate);



            listItem.add(map);
        }


        //listView 样式设置 重写SimpleAdapter
        SpecialAdapter listItemAdapter = new SpecialAdapter(this,listItem,R.layout.record_items,
                new String[] { "goodName","laveDays","endDate","HP","status"},
                new int[] {R.id.good_name,R.id.lave_days,R.id.end_date,R.id.hp,R.id.status}



        );





        RecordList.setAdapter(listItemAdapter);
        RecordList.setOnItemClickListener(this);



        //TextView ListCount = (TextView)findViewById(R.id.list_counts);
        //ListCount.setText("最近30天的记录/累计30天");



    }






    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //保存当前一览位置
        saveListViewPositionAndTop();
        //迁移到详细页面

        Intent mainIntent = new Intent(MainActivity.this,DetailActivity.class);
        mCursor.moveToPosition(position);
        RECORD_ID = mCursor.getInt(0);

        Log.v("v_debug","RECORD_ID:"+RECORD_ID);
        mainIntent.putExtra("INT", RECORD_ID);
        startActivity(mainIntent);
        setResult(RESULT_OK, mainIntent);
        finish();
    }



    public class RecordListAdapter extends BaseAdapter {
        private Context mContext;
        private Cursor mCursor;
        public RecordListAdapter(Context context,Cursor cursor) {

            mContext = context;
            mCursor = cursor;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView mTextView = new TextView(mContext);
            mCursor.moveToPosition(position);
            mTextView.setText(mCursor.getString(1) + "___" + mCursor.getString(2)+ "___" + mCursor.getString(3)+ "___" + mCursor.getString(4));
            return mTextView;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);
        RECORD_ID = mCursor.getInt(0);
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_IMPORT_EXPOERT, 0, this.getString(R.string.menu_inport_export));//备份与恢复
        //menu.add(Menu.NONE, MENU_CHANGE_LOGIN_PASSWORD, 0, this.getString(R.string.menu_change_login_password));//修改登录密码
        menu.add(Menu.NONE, MENU_WHATUPDATE, 0, this.getString(R.string.menu_whatupdate));//更新信息
        menu.add(Menu.NONE, MENU_CHECK_UPDATE, 0, this.getString(R.string.menu_checkupdate));//检查更新
        menu.add(Menu.NONE, MENU_SUPPORT, 0, this.getString(R.string.menu_support));//技术支持
        menu.add(Menu.NONE, MENU_ABOUT, 0, this.getString(R.string.menu_about));//关于Keep
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        String title = "";
        String msg = "";
        //Context mContext = null;

        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case MENU_ABOUT://关于Eat
                title = this.getString(R.string.menu_about);
                msg = this.getString(R.string.about_app);
                msg = msg + "\n\n";
                msg = msg + "@"+getAppVersion(MainActivity.this);
                showAboutDialog(title,msg);
                break;
            case MENU_SUPPORT://技术支持
                title = this.getString(R.string.menu_support);
                msg = this.getString(R.string.partners);
                showAboutDialog(title,msg);
                break;
            case MENU_WHATUPDATE://更新信息
                title = this.getString(R.string.menu_whatupdate);
                msg = msg + this.getString(R.string.what_updated);
                msg = msg + "\n\n\n";
                showAboutDialog(title,msg);
                break;
            case MENU_CHECK_UPDATE://检查更新
                updateTask = new UpdateTask(MainActivity.this,true);
                updateTask.update();

//                showAboutDialog(title,msg);
                break;
            case MENU_IMPORT_EXPOERT://备份与恢复
                Intent mainIntent = new Intent(MainActivity.this, DataManagementActivity.class);
                mainIntent.putExtra("INT", RECORD_ID);
                startActivity(mainIntent);
                setResult(RESULT_OK, mainIntent);
                finish();
                break;

        }
        return true;
    }

    public void showAboutDialog(String title,String msg){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", null);
        builder.create().show();
    }


    //返回前设置前次的位置值
    public void setRecordListPosition(){
        ListViewData app = (ListViewData)getApplication();
        RecordList.setSelectionFromTop(app.getFirstVisiblePosition(), app.getFirstVisiblePositionTop());
    }

    /**
     * 保存当前页签listView的第一个可见的位置和top
     */
    private void saveListViewPositionAndTop() {

        final ListViewData app = (ListViewData)getApplication();

        app.setFirstVisiblePosition(RecordList.getFirstVisiblePosition());
        View item = RecordList.getChildAt(0);
        app.setFirstVisiblePositionTop((item == null) ? 0 : item.getTop());
    }

    private static Boolean isExit = false;
    private static Boolean hasTask = false;
    Timer tExit = new Timer();
    TimerTask task = new TimerTask(){

        @Override
        public void run() {
            isExit = true;
            hasTask = true;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isExit == false ) {
                isExit = true;
                Toast.makeText(this, "再按一次后退键退出应用程序", Toast.LENGTH_SHORT).show();
                if(!hasTask) {
                    tExit.schedule(task, 2000);
                }
            } else {
                finish();
                System.exit(0);
            }
        }
        return false;
    }

}
