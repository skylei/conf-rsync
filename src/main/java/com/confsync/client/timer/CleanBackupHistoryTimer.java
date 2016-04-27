package com.confsync.client.timer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.client.ClientWatcher;
import com.confsync.common.Constant;

public class CleanBackupHistoryTimer extends java.util.TimerTask {

	protected static final Logger logger = LoggerFactory.getLogger(CleanBackupHistoryTimer.class);

	@Override
	public void run() {
		try {
			Calendar baseCalendar = Calendar.getInstance();
			baseCalendar.setTime(new Date());
			baseCalendar.set(Calendar.DATE, baseCalendar.get(Calendar.DATE) - Constant.backupKeepDays);
			// 尝试查找最近半年的历史记录
			for (int i = 0; i < 180; i++) {
				baseCalendar.set(Calendar.DATE, baseCalendar.get(Calendar.DATE) - 1);

				String dateStr = ClientWatcher.getBackupName(baseCalendar.getTime());
				String bkPath = ClientWatcher.getBackupPath(dateStr);
				if (new File(bkPath).exists() && new File(bkPath).isDirectory()) {
					try {
						//删除过期的历史记录
						FileUtils.forceDelete(new File(bkPath));
						logger.info("delete backup history: " + bkPath);
					} catch (Exception e) {
						logger.error("delete backup history error: ", e);
					}
					
				}
			}

		} catch (Exception e) {
			logger.error("timer run error: ", e);
		}
	}

	public static void main(String[] args) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - Constant.backupKeepDays);

			DateFormat df = DateFormat.getDateInstance();
			String s = df.format(calendar.getTime());

			System.out.println(s);
		} catch (Exception e) {
			logger.error("timer run error: ", e);
		}
	}

}
