# CrashCatch_Android
Android程序Crash时的异常上报

1. 建立异常处理类CrashHandler 实现这个UncaughtExceptionHandler 接口
>      /**
	         * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
        	 * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
        	 */
         	@Override
      	public void uncaughtException(Thread thread, Throwable ex) {
	      	try {
	       		// 导出异常信息到SD卡中
		     	dumpExceptionToSDCard(ex);
		      	// 这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
		    	uploadExceptionToServer();
	    	} catch (IOException e) {
	     		e.printStackTrace();
	    	}
		// 打印出当前调用栈信息
		ex.printStackTrace();
		// 如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
		if (mDefaultCrashHandler != null) {
			mDefaultCrashHandler.uncaughtException(thread, ex);
		} else {
			Process.killProcess(Process.myPid());
		}

	}
2. 把异常信息写入SD卡
>     private void dumpExceptionToSDCard(Throwable ex) throws IOException {
		// 如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (DEBUG) {
				Log.w(TAG, "sdcard unmounted,skip dump exception");
				return;
			}
		}

		File dir = new File(PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		long current = System.currentTimeMillis();
		time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(
				current));
		// 以当前时间创建log文件
		File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);
		if (!file.exists()) {
			try {
				// 在指定的文件夹中创建文件
				file.createNewFile();
			} catch (Exception e) {
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			PrintWriter pw = new PrintWriter(writer);
			// 导出发生异常的时间
			pw.println(time);

			// 导出手机信息
			dumpPhoneInfo(pw);

			pw.println();
			// 导出异常的调用栈信息
			ex.printStackTrace(pw);
			pw.close();
		} catch (Exception e) {
			Log.e(TAG, "dump crash info failed");
		}

	}

3. 将异常文件上传至服务器
>     private void uploadExceptionToServer() {
		// TODO Upload Exception Message To Your Web Server
		// Intent intent = new Intent(mContext, LisenerServers.class);//
		// 开启服务将文件上传
		// intent.putExtra("data", path + File.separator + fileName);
		// mContext.startService(intent);
		new H().readTxtFile(PATH + "/DCIM/TRANS/test.txt");//读取文件内容，这句只是读
		new Thread(runnable).start();

	  }
	  
>   	  Runnable runnable = new Runnable() {
	  	  public void run() {
		    	File file = new File(Environment.getExternalStorageDirectory()
		 			.getPath() + "/DCIM/TRANS/test.txt");
		    	String request = UploadUtil.uploadFile(file,
					"http://190.168.2.120:88/test.php");
		    	Log.i("mylog", "请求结果为--->" + request);
		}
	};
