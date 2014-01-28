package com.microgle.bore;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.R.color;
import android.content.res.Configuration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class ReaderView extends Activity /*implements ScrollViewListener*/{
	
	
	private int fontSizeOff;
	private String assetsDir = "mhbrj1";
	private String articleIndex = "0";
	private int backcolor;
	private int textcolor;
	private String apptitle = "摩诃般若波罗蜜经";
	final String configKey = "brj_conf_v_0_1";
	
	

	private ScrollView mScrollView;
	
	private int[] scrollXY = new int[] {0, 0};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
		mScrollView = (ScrollView) findViewById(R.id.main_scrollview);
		LocaleConfiguration configuration = new LocaleConfiguration();
    	configuration.key = configKey;
    	readConfiguration(this, configuration);
	    if (configuration != null) {
	    	fontSizeOff = configuration.fontOffSize;
	    	assetsDir = configuration.work;
	    	articleIndex = configuration.articleIndex;
	    	backcolor = getConfigColor(configuration.backcolor);
	    	textcolor = getConfigColor(configuration.textcolor);
	    	scrollXY = new int[] {configuration.scrollX, configuration.scrollY};
	    }
	    if (backcolor == textcolor) {
	    	backcolor = R.color.black;
			textcolor = R.color.white;
    	}
		refreshGUI();
		//mScrollView.setScrollViewListener(this);
		mScrollView.scrollTo(scrollXY[0], scrollXY[1]);//TODO 无效果
	}
	
	/*public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
		scrollXY = new int[]{x, y};
    }*/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data);  
		switch(resultCode){  
			case RESULT_OK:{
				String oldAssetsDir = assetsDir;
				String oldArticleIndex = articleIndex;
				try {
					Bundle bunde = data.getExtras();
					if (bunde != null) {
						articleIndex = Integer.toString(bunde.getInt("position"));
						assetsDir = bunde.getString("assetsDir");
					}
					if (articleIndex == null) articleIndex = "0";
				} catch (Exception e) {
					articleIndex = "0";
				}
				if (!oldAssetsDir.equals(assetsDir) || !oldArticleIndex.equals(articleIndex)) {
					scrollXY = new int[] {0, 0};
					mScrollView.scrollTo(0, 0);// 改变滚动条的位置 
					writeLocalConfiguration(); 
				}
				
				try {
					refreshGUI();
				} catch (Exception e) {
				}
		            break;
			}  
		    default:  
		            break;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean isIntent = true;
		Bundle bundle = new Bundle();
		switch (item.getItemId()) {
			case R.id.mhbrjselect1:
				bundle.putString("assetsDir", "mhbrj1");
				bundle.putStringArray("titlelist", mhbrjlist1);
				break;
			case R.id.mhbrjselect2:
				bundle.putString("assetsDir", "mhbrj2");
				bundle.putStringArray("titlelist", mhbrjlist2);
				break;
			case R.id.mhbrjselect3:
				bundle.putString("assetsDir", "mhbrj3");
				bundle.putStringArray("titlelist", mhbrjlist3);
				break;
			case R.id.xpbrjselect:
				bundle.putString("assetsDir", "xpbrj");
				bundle.putStringArray("titlelist", xpbrjList);
				break;
			case R.id.jgbrjselect:
				bundle.putString("assetsDir", "jgbrj");
				bundle.putStringArray("titlelist", jgbrjList);
				break;
			case R.id.brxjselect:
				assetsDir = "brxj";
				apptitle = "般若波罗蜜多心经";
				articleIndex = "0";
				isIntent = false;
				break;
			case R.id.zomeinselect: //缩小字体
				isIntent = false;
				fontSizeOff--;
				break;
			case R.id.zomeoutselect: //增大字体
				isIntent = false;
				fontSizeOff++;
				break;
			case R.id.backgroundselect:
				if (backcolor == R.color.black) {
					backcolor = R.color.white;
					textcolor = R.color.black;
				} else {
					backcolor = R.color.black;
					textcolor = R.color.white;
				}
				isIntent = false;
				break;
			case R.id.about:
				doAbout();
				isIntent = false;
				break;
			default:
				break;
		}
		if (isIntent) {
			Intent intent = new Intent(ReaderView.this, ListTitleActivity.class);
			intent.putExtras(bundle);
	    	startActivityForResult(intent, 0);
		} else if (item.getItemId() != R.id.about) {
			writeLocalConfiguration();
			try {
				refreshGUI();
			} catch (Exception e) {
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void doAbout() {
		Dialog dialog = new AlertDialog.Builder(ReaderView.this).setTitle(
				R.string.aboutTitle).setMessage(R.string.aboutInfo)
				.setPositiveButton(R.string.aboutOK,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int i) {
							}
						}).create();
		dialog.show();
	}

	private void refreshGUI()
	{
		setAppTitle();
		TextView tv = (TextView) findViewById(R.id.view_content);
		
		mScrollView.setBackgroundResource(backcolor);
		
		String fileContent = getStringFromFile();
		tv.setTextColor(getResources().getColor(textcolor));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25 + fontSizeOff);
		tv.setText(fileContent);
		
	}
	
	public String getStringFromFile()
	{
		/*try {
			for (int i = filenameString.length();--i>=0;){   
	    		if (!Character.isDigit(filenameString.charAt(i))){
	    			filenameString = "0";
	    			break;
	    		}
			}
		} catch (Exception e) {
			filenameString = "0";
		}*/
		AssetManager am = getAssets();
    	InputStream inputStream = null;
    	ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
        	byte[] buffer = new byte[1024];
        	String assets = assetsDir + "/" + articleIndex;
            inputStream = am.open(assets);
            int len=-1;
            while ((len = inputStream.read(buffer)) != -1){
            	outSteam.write(buffer, 0, len); 
            }
            outSteam.close();
            inputStream.close();
            return new String(outSteam.toByteArray());
        } catch (IOException e) {   
            e.printStackTrace();   
        }
		return "";
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    writeLocalConfiguration();
	    outState.putIntArray("ARTICLE_SCROLL_POSITION",
	            new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
	    if(position != null)
	        mScrollView.post(new Runnable() {
	            public void run() {
	                mScrollView.scrollTo(position[0], position[1]);
	            }
	        });
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
		scrollXY = new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()};
	    writeLocalConfiguration();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		scrollXY = new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()};
		writeLocalConfiguration();
	}
	
	private void setAppTitle() {
		if (assetsDir.equals("mhbrj1")) {
			apptitle = "摩诃般若波罗蜜经上";
			if (Integer.parseInt(articleIndex) < mhbrjlist1.length) apptitle += mhbrjlist1[Integer.parseInt(articleIndex)];
		} else if (assetsDir.equals("mhbrj2")) {
			apptitle = "摩诃般若波罗蜜经中";
			if (Integer.parseInt(articleIndex) < mhbrjlist2.length) apptitle += mhbrjlist2[Integer.parseInt(articleIndex)];
		} else if (assetsDir.equals("mhbrj3")) {
			apptitle = "摩诃般若波罗蜜经下";
			if (Integer.parseInt(articleIndex) < mhbrjlist3.length) apptitle += mhbrjlist3[Integer.parseInt(articleIndex)];
		} else if (assetsDir.equals("xpbrj")) {
			apptitle = "小品般若波罗蜜经";
			if (Integer.parseInt(articleIndex) < xpbrjList.length) apptitle += xpbrjList[Integer.parseInt(articleIndex)];
		} else if (assetsDir.equals("jgbrj")) {
			apptitle = "金刚般若波罗蜜经";
			if (Integer.parseInt(articleIndex) < jgbrjList.length) apptitle += jgbrjList[Integer.parseInt(articleIndex)];
		}
		TextView barTv = (TextView) findViewById(R.id.app_title);
		barTv.setText(apptitle);
	}
	
	private static final String[] xpbrjList = {
		"卷一序",
		"卷一初品第一",
		"卷一释提桓因品第二",
		"卷二宝塔品第三",
		"卷二大明咒品第四",
		"卷二舍利品第五",
		"卷三佐助品第六",
		"卷三回向品第七",
		"卷三泥犁品第八",
		"卷四叹净品第九",
		"卷四不可思议品第十",
		"卷四魔事品第十一",
		"卷五小如品第十二",
		"卷五相无相品第十三",
		"卷五船喻品第十四",
		"卷五大如品第十五",
		"卷六阿惟越致相品第十六",
		"卷六深功德品第十七",
		"卷七恒伽提婆品第十八",
		"卷七阿毗跋致觉魔品第十九",
		"卷七深心求菩提品第二十",
		"卷八恭敬菩萨品第二十一",
		"卷八无悭烦恼品第二十二",
		"卷八称扬菩萨品第二十三",
		"卷九嘱累品第二十四",
		"卷九见阿閦佛品第二十五",
		"卷九随知品第二十六",
		"卷十萨陀波仑品第二十七",
		"卷十昙无竭品第二十八",
		"卷十嘱累品第二十九",
	};
	private static final String[] jgbrjList = {
		"第一品 法会因由分",
		"第二品 善现启请分",
		"第三品 大乘正宗分",
		"第四品 妙行无住分",
		"第五品 如理实见分",
		"第六品 正信希有分",
		"第七品 无得无说分",
		"第八品 依法出生分",
		"第九品 一相无相分",
		"第十品 庄严净土分",
		"第十一品 无为福胜分",
		"第十二品 尊重正教分",
		"第十三品 如法受持分",
		"第十四品 离相寂灭分",
		"第十五品 持经功德分",
		"第十六品 能净业障分",
		"第十七品 究竟无我分",
		"第十八品 一体同观分",
		"第十九品 法界通分分",
		"第二十品 离色离相分",
		"第二十一品 非说所说分",
		"第二十二品 无法可得分",
		"第二十三品 净心行善分",
		"第二十四品 福智无比分",
		"第二十五品 化无所化分",
		"第二十六品 法身非相分",
		"第二十七品 无断无灭分",
		"第二十八品 不受不贪分",
		"第二十九品 威仪寂净分",
		"第三十品 一合理相分",
		"第三十一品 知见不生分",
		"第三十二品 应化非真分"
	};
	private static final String[] mhbrjlist1 = {
		"卷一序品第一",
		"卷一奉钵品第二",
		"卷一习应品第三",
		"卷二往生品第四",
		"卷二叹度品第五",
		"卷二舌相品第六",
		"卷二三假品第七",
		"卷三劝学品第八",
		"卷三集散品第九",
		"卷三行相品第十",
		"卷四幻学品第十一",
		"卷四句义品第十二",
		"卷四金刚品第十三",
		"卷四断诸见品第十四",
		"卷四富楼那品第十五",
		"卷四乘大乘品第十六",
		"卷五庄严品第十七",
		"卷五问乘品第十八",
		"卷六广乘品第十九",
		"卷六发趣品第二十",
		"卷六出到品第二十一",
		"卷七胜出品第二十二",
		"卷七含受品第二十三（等空品）",
		"卷七会宗品第二十四",
		"卷七十无品第二十五",
		"卷八十无品第二十五之余",
		"卷八无生品第二十六",
		"卷八天王品第二十七（问住品）",
		"卷九幻听品第二十八",
		"卷九散花品第二十九",
		"卷九三叹品第三十",
		"卷九现灭诤品第三十一",
		"卷十宝塔大明品第三十二",
		"卷十述成品第三十三",
		"卷十劝持品第三十四",
		"卷十遣异品第三十五",
		"卷十阿难称誉品第三十六（尊导品）"
	};
	private static final String[] mhbrjlist2 = {
		"卷十一舍利品第三十七（法称品）",
		"卷十一十善品第三十八",
		"卷十二十善品第三十八之余",
		"卷十二随喜品第三十九",
		"卷十三随喜品第三十九之余",
		"卷十三照明品第四十",
		"卷十三信毁品第四十一",
		"卷十四叹净品第四十二",
		"卷十四无作品第四十三",
		"卷十四百波罗蜜遍叹品第四十四",
		"卷十五经耳闻持品第四十五",
		"卷十五魔事品第四十六",
		"卷十六两不和合过品第四十七（两过品）",
		"卷十六佛母品第四十八",
		"卷十六问相品第四十九",
		"卷十七大事起成办品第五十",
		"卷十七譬喻品第五十一",
		"卷十七善知识品第五十二",
		"卷十七趣一切智品第五十三",
		"卷十八大如品第五十四",
		"卷十八阿毗跋致品第五十五（不退品）",
		"卷十九转不转品第五十六（坚固品）",
		"卷十九灯炷深奥品第五十七",
		"卷十九梦行品第五十八",
		"卷二十恒伽提婆品第五十九（河天品）",
		"卷二十学空不证品第六十",
		"卷二十梦中不证品第六十一（梦誓品）"
	};
	private static final String[] mhbrjlist3 = {
		"卷二十一魔愁品第六十二",
		"卷二十一等学品第六十三",
		"卷二十一愿乐随喜品第六十四",
		"卷二十一度空品第六十五",
		"卷二十二嘱累品第六十六（累教品）",
		"卷二十二不可尽品第六十七",
		"卷二十二六度相摄品第六十八（摄五品）",
		"卷二十三大方便品第六十九",
		"卷二十三三慧品第七十",
		"卷二十四道树品第七十一",
		"卷二十四菩萨行品第七十二（道行品）",
		"卷二十四种善根品第七十三（三善品）",
		"卷二十四遍学品第七十四",
		"卷二十五三次第行品第七十五",
		"卷二十五一念品第七十六",
		"卷二十六六喻品第七十七",
		"卷二十六四摄品第七十八",
		"卷二十七四摄品第七十八之余",
		"卷二十七善达品第七十九",
		"卷二十八实际品第八十",
		"卷二十八具足品第八十一",
		"卷二十九净佛国品第八十二",
		"卷二十九毕定品第八十三",
		"卷二十九四谛品第八十四（差别品）",
		"卷二十九七喻品第八十五",
		"卷二十九平等品第八十六",
		"卷二十九如化品第八十七",
		"卷三十萨陀波仑品第八十八（常啼品）",
		"卷三十昙无竭品第八十九（法尚品）",
		"卷三十嘱累品第九十"
	};
	
	private static final String PREFERENCES = "launcher.preferences";
    private static class LocaleConfiguration {  
        public String key;  
        public int fontOffSize = 0;
        public String work = "mhbrj1";  
        public String articleIndex = "0";
        public int backcolor = R.color.black;
        public int textcolor = R.color.white;
        public int scrollX = 0;
        public int scrollY = 0;
    }
    
    private int getConfigColor(int c) {
    	if (c == 1) return R.color.white;
    	if (c == 0) return R.color.black;
    	return R.color.black;
    }
    private int getColorInt(int c) {
    	return c == R.color.black ? 0 : 1;
    }
    
    private void writeLocalConfiguration() {
    	LocaleConfiguration configuration = new LocaleConfiguration();
    	configuration.key = configKey;
    	
    	configuration.fontOffSize = fontSizeOff;
    	configuration.work = assetsDir;
    	configuration.articleIndex = articleIndex;
    	configuration.backcolor = getColorInt(backcolor);
    	configuration.textcolor = getColorInt(textcolor);
    	configuration.scrollX = scrollXY[0];
    	configuration.scrollY = scrollXY[1];
    	writeConfiguration(this, configuration);
    }
    
    private static void writeConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(context.openFileOutput(PREFERENCES,
					MODE_PRIVATE));
			out.writeUTF(configuration.key);
			out.writeInt(configuration.fontOffSize);
			out.writeUTF(configuration.work);
			out.writeUTF(configuration.articleIndex);
			out.writeInt(configuration.backcolor);
			out.writeInt(configuration.textcolor);
			out.writeInt(configuration.scrollX);
			out.writeInt(configuration.scrollY);
			out.flush();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// noinspection ResultOfMethodCallIgnored
			context.getFileStreamPath(PREFERENCES).delete();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}
    
    private static void readConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(context.openFileInput(PREFERENCES));
			configuration.key = in.readUTF();
			configuration.fontOffSize = in.readInt();
			configuration.work = in.readUTF();
			configuration.articleIndex = in.readUTF();
			configuration.backcolor = in.readInt();
			configuration.textcolor = in.readInt();
			configuration.scrollX = in.readInt();
			configuration.scrollY = in.readInt();
		} catch (FileNotFoundException e) {
			// Ignore
			configuration = null;
		} catch (IOException e) {
			// Ignore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}
}
