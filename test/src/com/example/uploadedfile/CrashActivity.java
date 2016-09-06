package com.example.uploadedfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.test.R;

public class CrashActivity extends Activity implements OnClickListener {

	private Button mButton;
	private static final String[] m = { "袋", "箱" };
	private Spinner spinner;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crash);
		initView();
	}

	private void initView() {
		mButton = (Button) findViewById(R.id.button1);
		mButton.setOnClickListener(this);
		spinner = (Spinner) findViewById(R.id.spinner1);

		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, m);

		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
	}

	// 使用数组形式操作
	class SpinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// if (m[position].equals("袋")) {
			// type = "1";
			// } else {
			// type = "2";
			// }
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	@Override
	public void onClick(View v) {
		if (v == mButton) {
			// 在这里默认异常抛出情况，人为抛出一个运行时异常
			throw new RuntimeException("自定义异常：这是自己抛出的异常");
			// FileUploadTask fileuploadtask = new FileUploadTask();
			// fileuploadtask.execute();
		}

	}

	class FileUploadTask extends AsyncTask<Object, Integer, Void> {

		private ProgressDialog dialog = null;
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;
		// the file path to upload
		String pathToOurFile = "/ryg_test/log/crash2016-08-08 11:35:47.trace";
		// the server address to process uploaded file
		String urlServer = "http://190.168.1.17/mobileapp/android/getAppLog.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		File uploadFile = new File(pathToOurFile);
		long totalSize = uploadFile.length(); // Get size of file, bytes

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CrashActivity.this);
			dialog.setMessage("正在上传...");
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Object... arg0) {

			long length = 0;
			int progress;
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 256 * 1024;// 256KB

			try {
				FileInputStream fileInputStream = new FileInputStream(new File(
						pathToOurFile));

				URL url = new URL(urlServer);
				connection = (HttpURLConnection) url.openConnection();

				// Set size of every block for post
				connection.setChunkedStreamingMode(256 * 1024);// 256KB

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				// Enable POST method
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Charset", "UTF-8");
				connection.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				outputStream = new DataOutputStream(
						connection.getOutputStream());
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream
						.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
								+ pathToOurFile + "\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					outputStream.write(buffer, 0, bufferSize);
					length += bufferSize;
					progress = (int) ((length * 100) / totalSize);
					publishProgress(progress);

					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens
						+ lineEnd);
				publishProgress(100);

				// Responses from the server (code and message)
				int serverResponseCode = connection.getResponseCode();
				String serverResponseMessage = connection.getResponseMessage();

				/* 将Response显示于Dialog */
				// Toast toast = Toast.makeText(UploadtestActivity.this, ""
				// + serverResponseMessage.toString().trim(),
				// Toast.LENGTH_LONG);
				// showDialog(serverResponseMessage.toString().trim());
				/* 取得Response内容 */
				// InputStream is = connection.getInputStream();
				// int ch;
				// StringBuffer sbf = new StringBuffer();
				// while ((ch = is.read()) != -1) {
				// sbf.append((char) ch);
				// }
				//
				// showDialog(sbf.toString().trim());

				fileInputStream.close();
				outputStream.flush();
				outputStream.close();

			} catch (Exception ex) {
				// Exception handling
				// showDialog("" + ex);
				// Toast toast = Toast.makeText(UploadtestActivity.this, "" +
				// ex,
				// Toast.LENGTH_LONG);

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			dialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				dialog.dismiss();
				// TODO Auto-generated method stub
			} catch (Exception e) {
			}
		}

		// show Dialog method
		private void showDialog(String mess) {
			new AlertDialog.Builder(CrashActivity.this)
					.setTitle("Message")
					.setMessage(mess)
					.setNegativeButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
	}
}
