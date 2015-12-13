package org.xzc.bilibili.reg;

import org.junit.Test;

public class TestReg {
	@Test
	public void test1() throws Exception {
		RegService rs = new RegService( "19539389", "590e7bf3%2C1481515346%2Cb6085512" );
		boolean ok = false;
		while (!ok) {
			System.out.println( "开始答题" );
			rs.answer1();
			System.out.println( "通过阶段1" );
			if (rs.answer2()) {
				System.out.println( "通过阶段2" );
				break;
			} else {
				System.out.println( "阶段2失败" );
			}

		}
		System.out.println( "ok!" );
	}
}
