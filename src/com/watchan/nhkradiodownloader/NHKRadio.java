package com.watchan.nhkradiodownloader;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * @author watadashohei
 *
 */
class Kouza implements Serializable {

	private String title;
	private String hdate;
	private String filename;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHdate() {
		return hdate;
	}

	public void setHdate(String hdate) {
		this.hdate = hdate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}


/**
 * @author watadashohei
 *
 */

public class NHKRadio implements Serializable {

	String magicdigit;
	String xmlpath;
	String xml;

	public ArrayList<ArrayList<Kouza>> allkouzalist;
	ArrayList<String> nhkkouzaurl;

	/**
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	NHKRadio() throws IOException, ParserConfigurationException, SAXException {

		this.nhkkouzaurl = new ArrayList<String>();
		this.allkouzalist = new ArrayList<ArrayList<Kouza>>();

		// ダウンロード防止用の週変わりディレクトリ名を取得する
		try {
			this.magicdigit = getDirectoryName();
		} catch (IOException e) {
			Log.d("NHK","週変わりのディレクトリ名が取得できませんでした。");
		}
		
		// ラジオ英会話
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/kaiwa/"
				+ this.magicdigit + "/listdataflv.xml");
		// 基礎英語１
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/basic1/"
				+ this.magicdigit + "/listdataflv.xml");
		// 基礎英語２
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/basic2/"
				+ this.magicdigit + "/listdataflv.xml");
		// 基礎英語３
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/basic3/"
				+ this.magicdigit + "/listdataflv.xml");
		// 英語５分間トレーニング
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/training/"
				+ this.magicdigit + "/listdataflv.xml");
		// 入門ビジネス英語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/business1/"
				+ this.magicdigit + "/listdataflv.xml");
		// 実践ビジネス英語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/english/business2/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにち中国語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/chinese/kouza/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにちフランス語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/french/kouza/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにちイタリア語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/italian/kouza/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにちハングル講座
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/hangeul/kouza/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにちドイツ語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/german/kouza/"
				+ this.magicdigit + "/listdataflv.xml");
		// まいにちスペイン語
		nhkkouzaurl.add("http://www.nhk.or.jp/gogaku/spanish/kouza/"
				+ this.magicdigit + "/listdataflv.xml");

		for (int i = 0; i < nhkkouzaurl.size(); i++) {

			// 講座のlistdataflv.xmlのパスを取得
			xmlpath = nhkkouzaurl.get(i);

			ArrayList<Kouza> kouza = new ArrayList<Kouza>();
			
			// ある講座を１週間分取得
			kouza = this.getKouzaList(xmlpath);
			Log.d("NHK", kouza.get(0).getTitle() + "の１週間分のデータを取得");

			allkouzalist.add(kouza);

		}

	}


	/**
	 * @param 取得対象の語学講座のURL
	 * @return １週間分のKouzaオブジェクトを格納したArrayListを返す
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ArrayList<Kouza> getKouzaList(String xmlurl)
			throws ParserConfigurationException, SAXException, IOException {

		// ドキュメントビルダ
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlurl);

		// 講座の１回放送分のNodeを取得
		NodeList nl = doc.getElementsByTagName("music");

		ArrayList<Kouza> kouzalist = null;
		kouzalist = new ArrayList<Kouza>();

		for (int i = 0; i < nl.getLength(); i++) {

			// 講座の定義を取得
			Kouza kouza = new Kouza();

			NamedNodeMap nm = nl.item(i).getAttributes();
			kouza.setTitle(nm.item(0).getNodeValue());
			kouza.setHdate(nm.item(1).getNodeValue());
			kouza.setFilename(nm.item(3).getNodeValue());

			// 講座リストに追加
			kouzalist.add(kouza);

		}

		return kouzalist;

	}

	/**
	 * @return NHK語学講座の放送回データ"listdataflv.xml"をStringで返す
	 * @throws IOException
	 */
	public String getListDataFlvXml() throws IOException {

		Log.d("NHK", "getListDataFlvXml");
		
		// URLを作成してGET通信を行う
		URL url = new URL(this.xmlpath);
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを標準出力へ出す
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				http.getInputStream()));
		String xml = "", line = "";
		while ((line = reader.readLine()) != null)
			xml += line;
		System.out.println(xml);
		Log.d("NHK", xml);
		reader.close();
		return xml;

	}

	/**
	 * @return ダウンロード防止用マジックワードを返す
	 * @throws IOException
	 */
	public String getDirectoryName() throws IOException {

		// URLを作成してGET通信を行う
		URL url = new URL(
				"http://nhk-rtmp-capture.googlecode.com/svn/trunk/MAGIC-DIGITS.TXT");
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを標準出力へ出す
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				http.getInputStream()));
		String xml = "", line = "";
		while ((line = reader.readLine()) != null)
			xml += line;
		System.out.println(xml);
		Log.d("NHK", xml);
		reader.close();
		return xml;

	}
}
