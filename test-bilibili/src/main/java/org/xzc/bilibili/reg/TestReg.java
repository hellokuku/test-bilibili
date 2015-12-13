package org.xzc.bilibili.reg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestReg {

	@Test
	public void test1() throws Exception {
		List<String[]> list = new ArrayList<String[]>();
		list.add( new String[] { "19557513", "315c6283,1450604147,848f2710" } );
		list.add( new String[] { "19557672", "a66f5ff5,1481535408,40539b19" } );
		list.add( new String[] { "19557714", "0f1e34b1,1481535453,720b2233" } );
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
