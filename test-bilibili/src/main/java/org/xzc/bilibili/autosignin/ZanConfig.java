package org.xzc.bilibili.autosignin;

public class ZanConfig {
	public final int aid;
	public final int rpid;
	public final int maxCount;
	public final int action;
	public final int batch;

	public ZanConfig(int aid, int rpid, int maxCount, int action) {
		this( aid, rpid, maxCount, action, 1 );
	}

	public ZanConfig(int aid, int rpid, int maxCount, int action, int batch) {
		this.aid = aid;
		this.rpid = rpid;
		this.maxCount = maxCount;
		this.action = action;
		this.batch = batch;
	}

	public static int parseAid(String aids) {
		return aids.startsWith( "av" ) ? Integer.parseInt( aids.substring( 2 ) ) : Integer.parseInt( aids );
	}

	public static int parseRpid(String rpids) {
		return rpids.startsWith( "l_id_" ) ? Integer.parseInt( rpids.substring( 5 ) )
				: Integer.parseInt( rpids );
	}

	public ZanConfig(String aids, String rpids, int maxCount, int action) {
		this( parseAid( aids ), parseRpid( rpids ), maxCount, action );
	}

	public ZanConfig(String aids, String rpids, int maxCount, int action, int batch) {
		this( parseAid( aids ), parseRpid( rpids ), maxCount, action, batch );
	}

	@Override
	public String toString() {
		return "ZanConfig [aid=" + aid + ", rpid=" + rpid + ", maxCount=" + maxCount + ", action=" + action + "]";
	}

}
