package com.example.uploadedfile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;

public class UploadfileActivity extends Activity {
	// 要上传的文件路径，理论上可以传输任何文件，实际使用时根据需要处理
	private static final String PATH = Environment
			.getExternalStorageDirectory().getPath();
	// private String uploadFile = PATH + "/changba_log740.txt";
	private String uploadFile = PATH + "/DCIM/TRANS/test.txt";
	private String srcPath = PATH + "/DCIM/TRANS/test.txt";
	// 服务器上接收文件的处理页面，这里根据需要换成自己的
	// private String actionUrl = "http://190.168.2.120:88/test.php";
	private String actionUrl = "http://190.168.1.17/mobileapp/android/getAppLog.php";
	private TextView mText1;
	private TextView mText2;
	private Button mButton;
	File file;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mText1 = (TextView) findViewById(R.id.myText2);
		mText1.setText("文件路径：\n" + uploadFile);
		mText2 = (TextView) findViewById(R.id.myText3);
		mText2.setText("上传网址：\n" + actionUrl);
		/* 设置mButton的onClick事件处理 */
		mButton = (Button) findViewById(R.id.myButton);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// uploadFile(actionUrl);
				file = new File(uploadFile);
				if (file != null) {
					new Thread(networkTask).start();

				}
				Toast.makeText(UploadfileActivity.this, "00000",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * 网络操作相关的子线程
	 */
	Runnable networkTask = new Runnable() {

		@Override
		public void run() {
			// TODO
			String request = UploadUtil.uploadFile(file, actionUrl);
			// uploadFile(actionUrl);
			// 在这里进行 http request.网络请求相关操作
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("value", request);
			msg.setData(data);
			handler.sendMessage(msg);
		}

	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("value");
			Log.i("mylog", "请求结果为-->" + val);
			// TODO
			// UI界面的更新等相关操作
		}
	};

	/* 上传文件至Server，uploadUrl：接收文件的处理页面 */
	private void uploadFile(String uploadUrl) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"attach\"; filename=\""
					+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
					+ "\""
					+ end);
			dos.writeBytes(end);

			FileInputStream fis = new FileInputStream(srcPath);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			// 读取文件
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();

			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();

			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String result = br.readLine();

			Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			dos.close();
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
			setTitle(e.getMessage());
			System.out.println("==========" + e.getMessage());
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}