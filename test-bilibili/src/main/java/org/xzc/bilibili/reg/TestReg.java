package org.xzc.bilibili.reg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestReg {

	@Test
	public void test1() throws Exception {
		List<String[]> list = new ArrayList<String[]>();
		list.add( new String[] { "19752845", "ed4d7491%2C1481871559%2C4915ad08" } );
		for (String[] ss : list) {
			RegService rs = new RegService( ss[0], ss[1] );
			boolean ok = rs.isOK();
			while (!ok) {
				System.out.println( ss[0] + " 开始答题" );
				rs.answer1();
				System.out.println( "通过阶段1" );
				if (rs.answer2()) {
					System.out.println( "通过阶段2" );
					break;
				} else {
					System.out.println( "阶段2失败" );
				}
			}
			System.out.println( ss[0] + " ok!" );
		}
	}
}
