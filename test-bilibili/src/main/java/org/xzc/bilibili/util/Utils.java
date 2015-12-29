package org.xzc.bilibili.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class Utils {
	public static final String PASSWORD = "xzchaoo".substring( 0, 3 ) + "@" + ( 1771 * 4 + 2 ) + ( 409 * 5 )
			+ ( 121 / 11 );
	public static final String DATETIME_PATTER = "yyyy年MM月dd日HH时mm分ss秒";
	private static final Logger log = Logger.getLogger( Utils.class );
	private static final File LOG_FILE = new File( "error.log" );
	public static final Charset UTF8 = Charset.forName( "utf8" );

	public static void blockUntil(List<Future<?>> futureList) throws InterruptedException, ExecutionException {
		for (Future<?> f : futureList)
			f.get();
	}

	public static void throwAsRuntimeException(Exception e) {
		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		throw new RuntimeException( e );
	}

	public static void log(String content) {
		try {
			FileUtils.writeStringToFile( LOG_FILE, new Date() + "\r\n" + content + "\r\n", true );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void blockUntil(String tag, DateTime startAt, long sleepTime) {
		PeriodFormatter pf = new PeriodFormatterBuilder().printZeroAlways().appendHours().appendLiteral( "小时" )
				.appendMinutes()
				.appendLiteral( "分" ).appendSeconds().appendLiteral( "秒" ).toFormatter();
		while (true) {
			DateTime now = DateTime.now();
			Period p = new Period( now, startAt );
			if (now.isAfter( startAt )) {
				System.out.println( "时间到了, 启动!" );
				break;
			} else {
				System.out.println( "[" + tag + "]" + " 距离开始还有 " + p.toString( pf ) );
			}
			try {
				Thread.sleep( sleepTime );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println( "执行..." );
	}

	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer( len );
		for (int x = 0; x < len;) {
			aChar = theString.charAt( x++ );
			if (aChar == '\\') {
				aChar = theString.charAt( x++ );
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++ ) {
						aChar = theString.charAt( x++ );
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = ( value << 4 ) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = ( value << 4 ) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = ( value << 4 ) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException( "Malformed   \\uxxxx   encoding." );
						}

					}
					outBuffer.append( (char) value );
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append( aChar );
				}
			} else
				outBuffer.append( aChar );
		}
		return outBuffer.toString();
	}

	public static List<String> uniqueStringList(List<String> list) {
		return new ArrayList<String>( new TreeSet<String>( list ) );
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep( millis );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
