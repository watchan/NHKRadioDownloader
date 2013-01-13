package com.watchan.nhkradiodownloader;

import com.schriek.rtmpdump.*;
import java.util.ArrayList;

import com.watchan.nhkradiodownloader.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class KouzaList extends Activity implements Runnable {

	NHKRadio radio;
	int kouzanum;
	String command;
	private static ProgressDialog waitDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kouza_list);

		TextView textview = (TextView) findViewById(R.id.textView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		final NHKRadio radio = (NHKRadio) getIntent().getExtras().get("radio");
		String title = (String) getIntent().getExtras().get("title");
		// アイテムを追加します

		for (int i = 0; i < radio.allkouzalist.size(); i++) {
			ArrayList<Kouza> kouzalist = radio.allkouzalist.get(i);

			if (kouzalist.get(0).getTitle().equals(title)) {
				// kouzanum = i;
				textview.setText(title);

				for (int j = 0; j < kouzalist.size(); j++) {
					adapter.add(kouzalist.get(j).getHdate());
				}

				break;
			}
		}

		ListView listView = (ListView) findViewById(R.id.listView1);
		// アダプターを設定します
		listView.setAdapter(adapter);
		// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				// クリックされたアイテムを取得します
				String item = (String) listView.getItemAtPosition(position);

				ArrayList<Kouza> kouzalist = radio.allkouzalist.get(kouzanum);
				for (int i = 0; i < kouzalist.size(); i++) {

					Kouza kouza = kouzalist.get(i);

					if (kouza.getHdate().equals(item)) {

						command = "rtmpdump -r rtmp://flv9.nhk.or.jp/flv9/_definst_/gogaku/streaming/flv/"
								+ radio.magicdigit
								+ "/"
								+ kouza.getFilename()
								+ " -o /sdcard/NHK/"
								+ "["
								+ kouza.getTitle()
								+ "]" + kouza.getHdate() + ".flv";

						Log.d("NHK", item + "=" + kouzalist.get(i).getHdate());
						Log.d("NHK", command);

						// プログレスダイアログの設定
						waitDialog = new ProgressDialog(KouzaList.this);
						// プログレスダイアログのメッセージを設定します
						waitDialog.setMessage("[" + kouza.getTitle() + "]\n"
								+ kouza.getHdate() + "ダウンロード中...");
						// 円スタイル（くるくる回るタイプ）に設定します
						waitDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						// プログレスダイアログを表示
						waitDialog.show();

						Thread thread = new Thread(KouzaList.this);
						/*
						 * show()メソッドでプログレスダイアログを表示しつつ、 別スレッドを使い、裏で重い処理を行う。
						 */
						thread.start();

						break;
					}
				}

			}
		});
		// リストビューのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				// 選択されたアイテムを取得します
				String item = (String) listView.getSelectedItem();
				Toast.makeText(KouzaList.this, item, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	public void run() {
		// ダイアログがしっかり見えるように少しだけスリープ
		// （nnn：任意のスリープ時間・ミリ秒単位）
		Rtmpdump dump = new Rtmpdump();
		dump.parseString(command);
		// run内でUIの操作をしてしまうと、例外が発生する為、
		// Handlerにバトンタッチ
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// HandlerクラスではActivityを継承してないため
			// 別の親クラスのメソッドにて処理を行うようにした。
			// YYY();

			// プログレスダイアログ終了
			waitDialog.dismiss();
			Toast.makeText(KouzaList.this, "ダウンロード完了", Toast.LENGTH_LONG)
					.show();

			waitDialog = null;
		}
	};

}
